package com.thecontract.core.engine

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.model.Act
import com.thecontract.core.model.AnalRole
import com.thecontract.core.model.Answer
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.ConsiderationFamily
import com.thecontract.core.model.GameState
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.Slot
import com.thecontract.core.model.Term
import kotlin.math.abs
import kotlin.random.Random

/**
 * Chooses the next proposed term (sections 33, 34).
 *
 * Escalation is driven by *signed* regular terms, never by rejected proposals, so rejecting
 * something never pushes the game forwards. Anal-role weighting biases which side of a
 * penetrative term gets proposed without ever proposing something a player cannot do.
 */
object ProposalSelector {

    data class Selection(
        val term: Term,
        val binding: PartyBinding,
        val conditions: List<MaybeCondition>,
        /** A higher-ranked candidate that a shared hard boundary blocked, if any. */
        val blockedBoundary: Boundary? = null
    )

    /** Act I..V, derived from how much of the contract is already signed. */
    fun currentAct(state: GameState): Act {
        val required = state.requiredRegularTerms.coerceAtLeast(1)
        val used = state.negotiation.regularSlotsUsed
        return Act.ofLevel((used * 5 / required) + 1)
    }

    private fun analWeight(term: Term, binding: PartyBinding, ctx: GameContext): Double {
        var w = 1.0
        if ("topping" in term.activities || term.analPenetration) {
            w *= ctx.setup.player(binding.giver).analRole.topWeight.coerceAtLeast(0.01)
            w *= ctx.setup.player(binding.receiver).analRole.bottomWeight.coerceAtLeast(0.01)
        }
        if ("bottoming" in term.activities) {
            w *= ctx.setup.player(binding.giver).analRole.bottomWeight.coerceAtLeast(0.01)
            w *= ctx.setup.player(binding.receiver).analRole.topWeight.coerceAtLeast(0.01)
        }
        return w
    }

    private fun enthusiasmWeight(term: Term, binding: PartyBinding, ctx: GameContext): Double {
        if (term.activities.isEmpty()) return 1.0
        var yes = 0
        var total = 0
        for (activity in term.activities) {
            total += 2
            if (ctx.answer(binding.giver, PreferenceLibrary.give(activity)) == Answer.YES) yes++
            if (ctx.answer(binding.receiver, PreferenceLibrary.receive(activity)) == Answer.YES) yes++
        }
        return 1.0 + (yes.toDouble() / total)
    }

    private fun levelWeight(termLevel: Int, actLevel: Int): Double = when (abs(termLevel - actLevel)) {
        0 -> 4.0
        1 -> 1.0
        else -> 0.0
    }

    fun nextProposal(state: GameState, ctx: GameContext): Selection? {
        val act = currentAct(state)
        val excluded = state.negotiation.seenTermIds + state.negotiation.declinedTermIds +
            state.negotiation.signed.flatMap { s -> s.allTerms.map { it.termId } }

        var blockedNotice: Boundary? = null
        val weighted = mutableListOf<Pair<Selection, Double>>()

        for (term in ContentLibrary.regularTerms) {
            if (term.id in excluded) continue
            val lw = levelWeight(term.level, act.level)
            if (lw == 0.0) continue
            when (val e = EligibilityEngine.evaluate(term, ctx)) {
                is Eligibility.BlockedByBoundary -> {
                    if (blockedNotice == null && term.level == act.level) blockedNotice = e.boundary
                }
                Eligibility.Unavailable -> Unit
                is Eligibility.Ok -> {
                    val weight = lw * analWeight(term, e.binding, ctx) * enthusiasmWeight(term, e.binding, ctx)
                    if (weight > 0.0) {
                        weighted += Selection(
                            term = term,
                            binding = e.binding,
                            conditions = EligibilityEngine.maybeConditions(term, e.binding, ctx)
                        ) to weight
                    }
                }
            }
        }
        if (weighted.isEmpty()) {
            // Fall back to any eligible unseen term at any level rather than stalling.
            for (term in ContentLibrary.regularTerms) {
                if (term.id in excluded) continue
                val e = EligibilityEngine.evaluate(term, ctx)
                if (e is Eligibility.Ok) {
                    return Selection(term, e.binding, EligibilityEngine.maybeConditions(term, e.binding, ctx), blockedNotice)
                }
            }
            return null
        }

        val rng = Random(state.sessionId.hashCode().toLong() * 1_000_003L + state.negotiation.proposalCounter)
        val total = weighted.sumOf { it.second }
        var roll = rng.nextDouble() * total
        for ((selection, weight) in weighted) {
            roll -= weight
            if (roll <= 0.0) return selection.copy(blockedBoundary = blockedNotice)
        }
        return weighted.last().first.copy(blockedBoundary = blockedNotice)
    }

    /** Compatible second terms for a bundle trade, excluding anything already in play. */
    fun bundleCandidates(
        state: GameState,
        ctx: GameContext,
        primaryTermId: String,
        limit: Int = 5
    ): List<Selection> {
        if (state.regularTermsRemaining < 2) return emptyList()
        val act = currentAct(state)
        val excluded = state.negotiation.seenTermIds + state.negotiation.declinedTermIds +
            state.negotiation.signed.flatMap { s -> s.allTerms.map { it.termId } } + primaryTermId
        val rng = Random(state.sessionId.hashCode().toLong() * 7_919L + state.negotiation.proposalCounter)
        return ContentLibrary.regularTerms
            .asSequence()
            .filter { it.id !in excluded && levelWeight(it.level, act.level) > 0.0 }
            .mapNotNull { term ->
                val e = EligibilityEngine.evaluate(term, ctx)
                if (e is Eligibility.Ok) {
                    Selection(term, e.binding, EligibilityEngine.maybeConditions(term, e.binding, ctx))
                } else {
                    null
                }
            }
            .toList()
            .shuffled(rng)
            .take(limit)
    }

    /** Compatible closing (climax) terms for one player, who is the one guaranteed to finish. */
    fun closingCandidates(
        state: GameState,
        ctx: GameContext,
        finisher: Slot,
        limit: Int = 8
    ): List<Selection> {
        val alreadyUsed = state.negotiation.signed.flatMap { s -> s.allTerms.map { it.termId } }.toSet()
        val anyPenetration = state.negotiation.signed.any { s -> s.allTerms.any { it.analPenetration } }
        val rng = Random(state.sessionId.hashCode().toLong() * 104_729L + finisher.ordinal)

        val candidates = ContentLibrary.climaxTerms.mapNotNull { term ->
            if (term.id in alreadyUsed) return@mapNotNull null
            // {R} is the finisher by authoring convention, so force that binding.
            val binding = PartyBinding(finisher.other, finisher)
            val e = EligibilityEngine.evaluate(term, ctx, preferredBinding = binding)
            if (e is Eligibility.Ok && e.binding == binding) {
                Selection(term, binding, EligibilityEngine.maybeConditions(term, binding, ctx))
            } else {
                null
            }
        }

        // The mix is chosen from the finisher's own anal role, not just from whether the
        // contract happens to contain penetration. "bottoming" is written from the giver's
        // side, so a closing term carrying it is one where the finisher penetrates his partner.
        val (finisherPenetrates, rest) = candidates.partition {
            it.term.analPenetration && "bottoming" in it.term.activities
        }
        val (finisherTakes, noAnal) = rest.partition { it.term.analPenetration }
        val role = ctx.setup.player(finisher).analRole

        // How many of the offered options should have something going into the finisher.
        // A strict top never sees one; a strict bottom sees as many as exist.
        val takeQuota = when (role) {
            AnalRole.TOP, AnalRole.NO_ANAL -> 0
            AnalRole.VERS_TOP -> 1
            AnalRole.VERS -> limit / 2
            AnalRole.VERS_BOTTOM -> limit - 2
            AnalRole.BOTTOM -> limit
        }

        val picked = LinkedHashSet<Selection>()
        // Section 37 and the standing rule that finishing inside his partner is always on the
        // table for whoever can top: reserve a slot for it before anything else competes.
        if (finisherPenetrates.isNotEmpty()) picked += finisherPenetrates.shuffled(rng).first()
        picked += finisherTakes.shuffled(rng).take(takeQuota)
        // Fill the rest, preferring more penetrative finishes when the contract already has
        // penetration in it, and keeping at least a couple of non-anal options otherwise.
        val filler = if (anyPenetration && role.canTop) {
            finisherPenetrates.shuffled(rng) + noAnal.shuffled(rng) + finisherTakes.shuffled(rng)
        } else {
            noAnal.shuffled(rng) + finisherPenetrates.shuffled(rng) + finisherTakes.shuffled(rng)
        }
        picked += filler
        return picked.take(limit)
    }
}

/**
 * Chooses consideration actions (sections 28, 29).
 *
 * Only the beneficiary's phone ever sees this list. Intensity tracks negotiation progress, and
 * recently used actions are pushed down so consideration stays varied across a whole game.
 */
object ConsiderationSelector {

    /** Intensity band for the current point in the game, 1..5. */
    fun band(state: GameState): Int = ProposalSelector.currentAct(state).level

    fun options(
        state: GameState,
        ctx: GameContext,
        performer: Slot,
        recipient: Slot,
        mutualRequired: Boolean,
        stronger: Boolean = false,
        limit: Int = 6
    ): List<ConsiderationAction> {
        val target = (band(state) + if (stronger) 1 else 0).coerceIn(1, 5)
        val recent = state.negotiation.recentConsiderationIds.takeLast(8).toSet()

        val eligible = ContentLibrary.considerations.filter { action ->
            if (mutualRequired && !action.mutual) return@filter false
            if (!mutualRequired && action.mutual) return@filter false
            EligibilityEngine.evaluateConsideration(action, performer, recipient, ctx) is Eligibility.Ok
        }
        if (eligible.isEmpty()) {
            // Never leave the beneficiary with nothing: relax the reciprocity requirement.
            return ContentLibrary.considerations
                .filter { EligibilityEngine.evaluateConsideration(it, performer, recipient, ctx) is Eligibility.Ok }
                .sortedBy { abs(it.intensity - target) }
                .take(limit)
        }

        val rng = Random(state.sessionId.hashCode().toLong() * 15_485_863L + state.negotiation.proposalCounter)
        val scored = eligible.map { action ->
            var score = 10.0 - abs(action.intensity - target) * 3.0
            if (action.id in recent) score -= 20.0
            score += rng.nextDouble() * 2.0
            action to score
        }.sortedByDescending { it.second }

        // Spread the offer across families so the beneficiary always has a real choice.
        val chosen = LinkedHashMap<String, ConsiderationAction>()
        val familiesSeen = mutableMapOf<ConsiderationFamily, Int>()
        for ((action, _) in scored) {
            val seen = familiesSeen.getOrDefault(action.family, 0)
            if (seen >= 3) continue
            chosen[action.id] = action
            familiesSeen[action.family] = seen + 1
            if (chosen.size >= limit) break
        }
        for ((action, _) in scored) {
            if (chosen.size >= limit) break
            chosen[action.id] = action
        }
        return chosen.values.toList()
    }
}
