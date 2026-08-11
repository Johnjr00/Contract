package com.thecontract.core.engine

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.model.Act
import com.thecontract.core.model.AnalRole
import com.thecontract.core.model.Answer
import com.thecontract.core.model.BenefitParty
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

    /**
     * A term never runs ahead of the act it is in.
     *
     * The game is meant to climb from the first term to the last, so the act sets a ceiling and
     * not merely a centre: act one draws on level one only, act two on levels one and two while
     * favouring two, and so on. Allowing the level above, as this once did, let the opening
     * proposal of a game be something two people had not worked up to yet.
     */
    private fun levelWeight(termLevel: Int, actLevel: Int, floor: Int = 1): Double = when {
        termLevel < floor -> 0.0
        termLevel == actLevel -> 4.0
        termLevel == actLevel - 1 -> 1.0
        else -> 0.0
    }

    /**
     * The contract may not walk back down.
     *
     * The act sets the ceiling, but the level below it stays in the pool so that an act has more
     * than one band to draw on. Without a floor as well, that produced signed contracts running
     * 1, 1, 2, 1, 3, 2, 4, 4, 5, 4 — an overall climb with visible steps backwards inside it,
     * where a term softer than one already signed turns up later in the night. The floor is the
     * highest level signed so far, and it is dropped only if nothing at or above it is eligible,
     * so a heavily bounded profile still gets a game rather than a stall.
     */
    private fun signedFloor(state: GameState): Int =
        state.negotiation.signed.filterNot { it.term.climax }
            .flatMap { it.allTerms }.maxOfOrNull { it.level } ?: 1

    /**
     * Which man the contract last put ahead.
     *
     * Read from what is *signed* rather than from what has been proposed, so declining a term
     * never lets one man collect a run of terms in his favour. Balanced terms name nobody and
     * are skipped rather than treated as a turn: they leave the debt exactly where it was. A
     * bundle counts once, on its combined beneficiary, because the pair is signed and paid for
     * as a single step.
     */
    private fun lastBeneficiary(state: GameState): Slot? {
        for (signed in state.negotiation.signedRegular.asReversed()) {
            val parts = signed.allTerms.mapNotNull { r ->
                ContentLibrary.termsById[r.termId]?.let { it to PartyBinding(r.giver, r.receiver) }
            }
            val who = if (parts.isEmpty()) signed.term.beneficiary else BenefitAnalysis.combinedBeneficiary(parts)
            if (who != null) return who
        }
        return null
    }

    /** Whose turn it is to come out ahead, or null before anything one-sided has been signed. */
    private fun owedBeneficiary(state: GameState): Slot? = lastBeneficiary(state)?.other

    /**
     * The binding a term should be rendered with to keep the benefit alternating, or null when
     * nothing is owed yet or the term has no side that can benefit.
     *
     * The engine re-evaluates a bundled second term when the initiator picks it, and without this
     * it re-evaluated with no preference at all — so the binding the candidate list was filtered
     * on was not the binding the term was signed under, and a trade could hand the same man two
     * turns running.
     */
    fun preferredBinding(state: GameState, term: Term): PartyBinding? =
        owedBeneficiary(state)?.let { bindingFavouring(term, it) }

    /**
     * The binding that puts [owed] on the winning side of this term, or null if the term has no
     * winning side. Which slot benefits is fixed by the term's own structure, so the only lever
     * is which man is cast as the giver and which as the receiver.
     */
    private fun bindingFavouring(term: Term, owed: Slot): PartyBinding? = when (term.benefitParty) {
        BenefitParty.GIVER -> PartyBinding(owed, owed.other)
        BenefitParty.RECEIVER -> PartyBinding(owed.other, owed)
        BenefitParty.MUTUAL -> null
    }

    /** True when signing this selection would not hand the same man two terms in a row. */
    private fun alternates(selection: Selection, owed: Slot?): Boolean {
        if (owed == null) return true
        val benefits = BenefitAnalysis.beneficiary(selection.term, selection.binding)
        return benefits == null || benefits == owed
    }

    fun nextProposal(state: GameState, ctx: GameContext): Selection? {
        val act = currentAct(state)
        val excluded = state.negotiation.seenTermIds + state.negotiation.declinedTermIds +
            state.negotiation.signed.flatMap { s -> s.allTerms.map { it.termId } }
        val owed = owedBeneficiary(state)

        var blockedNotice: Boundary? = null
        var weighted = mutableListOf<Pair<Selection, Double>>()

        // Two rules shape the pool, and each is dropped only when nothing survives with it in
        // place. The floor stops the contract walking back down to something softer than it has
        // already signed; alternation stops one man collecting four terms in his favour in a
        // row. The floor is the outer rule because a visible step backwards in intensity reads
        // worse than a repeated beneficiary, but in practice almost every term can be bound
        // either way round, so the first attempt is the one that lands.
        val attempts = listOf(signedFloor(state), 1).distinct()
            .flatMap { floor -> listOf(floor to true, floor to false) }

        for ((floor, alternate) in attempts) {
            weighted = mutableListOf()
            for (term in ContentLibrary.regularTerms) {
                if (term.id in excluded) continue
                val lw = levelWeight(term.level, act.level, floor)
                if (lw == 0.0) continue
                val preferred = if (alternate && owed != null) bindingFavouring(term, owed) else null
                when (val e = EligibilityEngine.evaluate(term, ctx, preferred)) {
                    is Eligibility.BlockedByBoundary -> {
                        if (blockedNotice == null && term.level == act.level) blockedNotice = e.boundary
                    }
                    Eligibility.Unavailable -> Unit
                    is Eligibility.Ok -> {
                        val selection = Selection(
                            term = term,
                            binding = e.binding,
                            conditions = EligibilityEngine.maybeConditions(term, e.binding, ctx)
                        )
                        if (!alternate || alternates(selection, owed)) {
                            val weight = lw * analWeight(term, e.binding, ctx) * enthusiasmWeight(term, e.binding, ctx)
                            if (weight > 0.0) weighted += selection to weight
                        }
                    }
                }
            }
            if (weighted.isNotEmpty()) break
        }
        if (weighted.isEmpty()) {
            // Fall back to any eligible unseen term at any level rather than stalling, still
            // preferring one that keeps the benefit alternating.
            for (alternate in listOf(true, false)) {
                for (term in ContentLibrary.regularTerms) {
                    if (term.id in excluded) continue
                    val preferred = if (alternate && owed != null) bindingFavouring(term, owed) else null
                    val e = EligibilityEngine.evaluate(term, ctx, preferred)
                    if (e is Eligibility.Ok) {
                        val selection = Selection(
                            term, e.binding, EligibilityEngine.maybeConditions(term, e.binding, ctx), blockedNotice
                        )
                        if (!alternate || alternates(selection, owed)) return selection
                    }
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
        // A bundled second term is signed alongside the first, so it obeys the same floor.
        val floor = signedFloor(state)
        val owed = owedBeneficiary(state)
        // The pair is signed and paid for as one step, so it is the *combined* benefit that has
        // to land on the man whose turn it is. A second term in the other man's favour can
        // cancel the first one out, and then the trade quietly skips somebody's turn.
        val primary = state.negotiation.current?.term
            ?.takeIf { it.termId == primaryTermId }
            ?.let { r -> ContentLibrary.termsById[r.termId]?.let { it to PartyBinding(r.giver, r.receiver) } }

        val eligible = ContentLibrary.regularTerms
            .asSequence()
            .filter { it.id !in excluded && levelWeight(it.level, act.level, floor) > 0.0 }
            .mapNotNull { term ->
                val preferred = owed?.let { bindingFavouring(term, it) }
                val e = EligibilityEngine.evaluate(term, ctx, preferred)
                if (e is Eligibility.Ok) {
                    Selection(term, e.binding, EligibilityEngine.maybeConditions(term, e.binding, ctx))
                } else {
                    null
                }
            }
            .toList()

        val alternating = if (owed == null || primary == null) {
            eligible
        } else {
            eligible.filter {
                BenefitAnalysis.combinedBeneficiary(listOf(primary, it.term to it.binding)) != owed.other
            }
        }
        // Offering nothing turns a bundle request into a plain signature, so an empty filtered
        // list falls back to the unfiltered one rather than cancelling the trade.
        return alternating.ifEmpty { eligible }.shuffled(rng).take(limit)
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
 * Only one phone ever sees this list: the player who gained less from the term, since he is the
 * one being paid and so the one who says what the payment is. Intensity tracks negotiation
 * progress, and recently used actions are pushed down so consideration stays varied.
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
        limit: Int = 10
    ): List<ConsiderationAction> {
        // The act is a ceiling, not a centre. Consideration climbs with the game, so the opening
        // of a session offers hands, mouths and ears and nothing below the waist, and the last
        // act is the only place the hardest actions can appear at all.
        val ceiling = band(state)
        // A moving window rather than everything up to the ceiling: by the last act, offering a
        // jaw massage next to a fist reads as a step backwards, and the game is supposed to be
        // climbing. A bundle of two terms is paid for from the top of the window only.
        val floor = if (stronger) ceiling else (ceiling - 1).coerceAtLeast(1)
        val recent = state.negotiation.recentConsiderationIds.takeLast(8).toSet()

        fun offerable(action: ConsiderationAction): Boolean {
            // Consideration is owed by whoever gained more from the term. An act that gets the
            // performer off is not a payment, so it is never on the list.
            if (action.usesPerformersCock) return false
            if (mutualRequired && !action.mutual) return false
            if (!mutualRequired && action.mutual) return false
            return EligibilityEngine.evaluateConsideration(action, performer, recipient, ctx) is Eligibility.Ok
        }

        // The window is where the offer should come from; everything below it up to the ceiling
        // is what the offer is topped up from when the window cannot fill a full list on its own.
        // Widening beats showing him three options and calling it a choice, and the scoring below
        // keeps the window's own actions at the head of the list either way.
        val inWindow = ContentLibrary.considerations.filter { it.intensity in floor..ceiling && offerable(it) }
        val eligible = if (inWindow.size >= limit) {
            inWindow
        } else {
            ContentLibrary.considerations.filter { it.intensity <= ceiling && offerable(it) }
        }
        if (eligible.isEmpty()) {
            // Never leave him with nothing: relax reciprocity, but never the ceiling, and never
            // offer the performer something he gets off on.
            return ContentLibrary.considerations
                .filter {
                    !it.usesPerformersCock && it.intensity <= ceiling &&
                        EligibilityEngine.evaluateConsideration(it, performer, recipient, ctx) is Eligibility.Ok
                }
                .sortedByDescending { it.intensity }
                .take(limit)
        }
        val target = ceiling

        val rng = Random(state.sessionId.hashCode().toLong() * 15_485_863L + state.negotiation.proposalCounter)
        val scored = eligible.map { action ->
            var score = 10.0 - abs(action.intensity - target) * 3.0
            if (action.id in recent) score -= 20.0
            // Enough jitter that the lower half of the window can outrank the top of it. With
            // less, the same two or three actions headed the list for several terms running and
            // the offer stopped feeling like a choice.
            score += rng.nextDouble() * 4.0
            action to score
        }.sortedByDescending { it.second }

        // Spread the offer across families so he always has a real choice. Two from any one
        // family, not three: the ear-play list has four late-game variants of itself, and three
        // of them side by side made every late offer read as the same thing worded differently.
        val maxPerFamily = (limit / 3).coerceAtLeast(2)
        val chosen = LinkedHashMap<String, ConsiderationAction>()
        val familiesSeen = mutableMapOf<ConsiderationFamily, Int>()

        // Eligibility already stops a man being offered anything on his hole once his allowance
        // is gone, but a vers top with his one still unspent could otherwise see four of them
        // side by side in the same list. He is allowed one all night, so he is shown one.
        val receptiveHeadroom = ctx.setup.player(recipient).analRole.receptiveAnalLimit -
            (ctx.analReceptionUsed[recipient] ?: 0)
        var receptiveOffered = 0
        fun mayOffer(action: ConsiderationAction): Boolean {
            if (recipient !in EligibilityEngine.receptiveParties(action, performer, recipient)) return true
            if (receptiveHeadroom > 1) return true
            return receptiveOffered == 0
        }

        for ((action, _) in scored) {
            val seen = familiesSeen.getOrDefault(action.family, 0)
            if (seen >= maxPerFamily) continue
            if (!mayOffer(action)) continue
            chosen[action.id] = action
            if (recipient in EligibilityEngine.receptiveParties(action, performer, recipient)) receptiveOffered++
            familiesSeen[action.family] = seen + 1
            if (chosen.size >= limit) break
        }
        for ((action, _) in scored) {
            if (chosen.size >= limit) break
            if (action.id in chosen || !mayOffer(action)) continue
            chosen[action.id] = action
            if (recipient in EligibilityEngine.receptiveParties(action, performer, recipient)) receptiveOffered++
        }
        return chosen.values.toList()
    }
}
