package com.thecontract.core.engine

import com.thecontract.core.model.Answer
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.PrivateProfile
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.model.Term

/** Everything eligibility and rendering need about the current session. */
data class GameContext(
    val setup: SharedSetup,
    val profiles: Map<Slot, PrivateProfile>,
    /**
     * How many times each player has already been the receptive party in anal content this game
     * — counting what is signed, what has been performed as consideration, and what is on the
     * table right now. A vers top gets one for the whole night, so the allowance has to be
     * spent against history rather than judged one term at a time.
     */
    val analReceptionUsed: Map<Slot, Int> = emptyMap()
) {
    fun profile(slot: Slot): PrivateProfile = profiles[slot] ?: PrivateProfile(slot)
    fun answer(slot: Slot, prefId: String): Answer = profile(slot).answer(prefId)

    /** True when this player has no allowance left to be worked on the hole again. */
    fun analReceptionSpent(slot: Slot): Boolean =
        (analReceptionUsed[slot] ?: 0) >= setup.player(slot).analRole.receptiveAnalLimit

    companion object {
        fun of(state: com.thecontract.core.model.GameState): GameContext = GameContext(
            state.setup,
            state.profiles,
            EligibilityEngine.analReceptionCounts(state)
        )
    }
}

sealed interface Eligibility {
    data class Ok(val binding: PartyBinding, val maybeCount: Int) : Eligibility
    /** Filtered because of a shared hard boundary. May be surfaced as a blocked proposal. */
    data class BlockedByBoundary(val boundary: Boundary) : Eligibility
    /** Filtered silently: missing equipment, incompatible roles, or a No answer. */
    data object Unavailable : Eligibility
}

/**
 * Eligibility filtering (sections 16, 17, 21, 22, 23).
 *
 * Order matters. Equipment and profile filtering are *silent* — the players must never see a
 * reference to a toy they do not own or an act someone said No to. Boundary conflicts are
 * reported separately so the TV may show a blocked proposal naming the boundary; replacing a
 * blocked term never consumes progress.
 */
object EligibilityEngine {

    private val ANAL_ACTIVITIES = setOf("anal_external", "fingering", "anal_toys", "topping", "bottoming", "massage_anus", "plug", "dildo", "rough_anal", "fisting")
    private val PENETRATIVE_ACTIVITIES = setOf("fingering", "anal_toys", "topping", "bottoming", "plug", "dildo", "rough_anal", "fisting")
    private val FOOT_ACTIVITIES = setOf("foot_kissing", "foot_stimulation", "massage_calves_feet")
    private val ORGASM_ACTIVITIES = setOf("edging", "denial", "climax_permission")
    private val ROUGH_ACTIVITIES = setOf("pinning", "rough_handling", "harder_oral", "hard_sex", "rough_hair_pulling", "rough_anal")
    private val PAIN_ACTIVITIES = setOf("spanking", "impact_toys", "nipple_clamps", "scratching")
    private val RESTRAINT_ACTIVITIES = setOf("restraint", "collaring")
    private val SEXUAL_MASSAGE_ACTIVITIES = setOf("massage_groin", "massage_anus", "massage_to_sexual")
    private val ROLEPLAY_PREFS = setOf("strangers_fantasy", "authority_roleplay", "capture_roleplay")
    private val RESTRAINT_EQUIPMENT = setOf(Equipment.CUFFS, Equipment.ROPE, Equipment.SPREADER_BAR, Equipment.LEASH)

    /**
     * Boundaries that block a piece of content, derived from its structure rather than from
     * per-item tagging, so a new term cannot accidentally slip past a boundary.
     */
    fun derivedBoundaries(
        categories: Set<Category>,
        activities: Set<String>,
        extraPrefs: Set<String>,
        equipment: Set<Equipment>,
        genericToy: Boolean,
        analPenetration: Boolean
    ): Set<Boundary> = buildSet {
        if (equipment.any { it.toyLike } || genericToy) add(Boundary.NO_TOYS)
        if (equipment.any { it in Boundary.markingEquipment }) add(Boundary.NO_MARKS)
        if (equipment.any { it in RESTRAINT_EQUIPMENT }) add(Boundary.NO_RESTRAINTS)
        if (Category.ANAL in categories || Category.RIMMING in categories ||
            activities.any { it in ANAL_ACTIVITIES }
        ) {
            add(Boundary.NO_ANAL_ACTIVITY)
        }
        if (analPenetration || activities.any { it in PENETRATIVE_ACTIVITIES }) {
            add(Boundary.NO_ANAL_PENETRATION)
        }
        if ("spit" in activities || Category.SPIT in categories) add(Boundary.NO_SPIT_PLAY)
        if (activities.any { it in FOOT_ACTIVITIES }) add(Boundary.NO_FOOT_PLAY)
        if ("degradation" in activities) add(Boundary.NO_DEGRADATION)
        if (activities.any { it in ORGASM_ACTIVITIES }) add(Boundary.NO_ORGASM_CONTROL)
        if (Category.ROLEPLAY in categories || "roleplay_lead" in activities ||
            extraPrefs.any { it in ROLEPLAY_PREFS }
        ) {
            add(Boundary.NO_ROLEPLAY)
        }
        if (Category.ROUGH in categories || activities.any { it in ROUGH_ACTIVITIES }) {
            add(Boundary.NO_ROUGH_SEX)
        }
        if (Category.IMPACT in categories || activities.any { it in PAIN_ACTIVITIES }) {
            add(Boundary.NO_PAIN)
        }
        if (activities.any { it in RESTRAINT_ACTIVITIES }) add(Boundary.NO_RESTRAINTS)
        if (activities.any { it in SEXUAL_MASSAGE_ACTIVITIES }) add(Boundary.MASSAGE_NONSEXUAL)
    }

    fun blockingBoundaries(term: Term): Set<Boundary> =
        term.blockedBy + derivedBoundaries(
            term.categories,
            term.activities,
            term.extraGiverPrefs + term.extraReceiverPrefs,
            term.requiredEquipment,
            term.genericToy,
            term.analPenetration
        )

    fun blockingBoundaries(action: ConsiderationAction): Set<Boundary> =
        action.blockedBy + derivedBoundaries(
            action.categories,
            action.activities,
            action.extraPerformerPrefs + action.extraRecipientPrefs,
            action.requiredEquipment,
            action.genericToy,
            action.analPenetration
        )

    private fun satisfies(constraint: PartyConstraint, slot: Slot, ctx: GameContext): Boolean {
        val p = ctx.setup.player(slot)
        return when (constraint) {
            PartyConstraint.ANY -> true
            PartyConstraint.DOMINANT -> p.role == com.thecontract.core.model.Role.DOMINANT
            PartyConstraint.SUBMISSIVE -> p.role == com.thecontract.core.model.Role.SUBMISSIVE
            PartyConstraint.TOP -> p.analRole.canTop
            PartyConstraint.BOTTOM -> p.analRole.canBottom
        }
    }

    private fun equipmentOk(
        required: Set<Equipment>,
        genericToy: Boolean,
        ctx: GameContext
    ): Boolean {
        if (!ctx.setup.equipment.containsAll(required)) return false
        if (genericToy && Equipment.genericToyPreferenceOrder.none { it in ctx.setup.equipment }) return false
        return true
    }

    /**
     * Preference check. A No from either side removes the content silently; a Maybe is allowed
     * and is counted so the caller can attach the appropriate Maybe condition.
     */
    private fun preferenceCheck(
        activities: Set<String>,
        extraGiver: Set<String>,
        extraReceiver: Set<String>,
        giver: Slot,
        receiver: Slot,
        ctx: GameContext
    ): Int? {
        var maybes = 0
        for (activity in activities) {
            val g = ctx.answer(giver, PreferenceLibrary.give(activity))
            val r = ctx.answer(receiver, PreferenceLibrary.receive(activity))
            if (g == Answer.NO || r == Answer.NO) return null
            if (g == Answer.MAYBE) maybes++
            if (r == Answer.MAYBE) maybes++
        }
        for (pref in extraGiver) {
            when (ctx.answer(giver, pref)) {
                Answer.NO -> return null
                Answer.MAYBE -> maybes++
                Answer.YES -> Unit
            }
        }
        for (pref in extraReceiver) {
            when (ctx.answer(receiver, pref)) {
                Answer.NO -> return null
                Answer.MAYBE -> maybes++
                Answer.YES -> Unit
            }
        }
        return maybes
    }

    /**
     * Erection filtering (section 17). Silent: the players never see an explanation, and the
     * only place erection-dependent content returns is that player's own closing term.
     */
    private fun erectionOk(term: Term, binding: PartyBinding, ctx: GameContext): Boolean {
        val parties = when (term.erectionRequired) {
            PartyRef.NEITHER -> return true
            PartyRef.GIVER -> listOf(binding.giver)
            PartyRef.RECEIVER -> listOf(binding.receiver)
            PartyRef.BOTH -> listOf(binding.giver, binding.receiver)
        }
        return parties.none { slot ->
            val hasDifficulty = ctx.setup.player(slot).erectionDifficulty
            // A climax term may reinstate erection-dependent content for the player who is
            // finishing in it — every climax term carries an explicit fallback continuation.
            hasDifficulty && !(term.climax && slot == binding.receiver)
        }
    }

    /**
     * Whether a piece of content goes anywhere near anybody's hole.
     */
    private fun touchesAnal(categories: Set<Category>, activities: Set<String>, analPenetration: Boolean): Boolean =
        Category.ANAL in categories || Category.RIMMING in categories ||
            activities.any { it in ANAL_ACTIVITIES } || analPenetration

    /**
     * The men whose hole this term involves — penetrated, rimmed, fingered, plugged, fisted or
     * rubbed from the outside, it makes no difference which.
     *
     * Activity names are written from the giver's side, so "bottoming" means the *giver* is the
     * one taking it and the receiver is the one doing the penetrating. Everything else in the
     * library is done to the receiver. A mutual term is done to both.
     */
    fun receptiveParties(term: Term, binding: PartyBinding): Set<Slot> {
        if (!touchesAnal(term.categories, term.activities, term.analPenetration)) return emptySet()
        if (term.mutual) return setOf(binding.giver, binding.receiver)
        return setOf(if ("bottoming" in term.activities) binding.giver else binding.receiver)
    }

    /** The same question for a consideration action, where the recipient is the one worked on. */
    fun receptiveParties(action: ConsiderationAction, performer: Slot, recipient: Slot): Set<Slot> {
        if (!touchesAnal(action.categories, action.activities, action.analPenetration)) return emptySet()
        if (action.mutual) return setOf(performer, recipient)
        return setOf(recipient)
    }

    /**
     * How many times each player has already been on the receiving end of anal content: signed
     * terms, consideration actions actually performed, and whatever is on the table right now,
     * so a proposal in flight already counts against a vers top's single allowance.
     */
    fun analReceptionCounts(state: com.thecontract.core.model.GameState): Map<Slot, Int> {
        val counts = mutableMapOf<Slot, Int>()
        fun bump(slots: Set<Slot>) = slots.forEach { counts[it] = (counts[it] ?: 0) + 1 }

        fun countTerm(termId: String, giver: Slot, receiver: Slot) {
            ContentLibrary.termsById[termId]?.let { bump(receptiveParties(it, PartyBinding(giver, receiver))) }
        }
        state.negotiation.signed.forEach { signed ->
            signed.allTerms.forEach { countTerm(it.termId, it.giver, it.receiver) }
        }
        state.negotiation.receipts.forEach { receipt ->
            ContentLibrary.considerationsById[receipt.actionId]
                ?.let { bump(receptiveParties(it, receipt.performer, receipt.recipient)) }
        }
        state.negotiation.current?.let { current ->
            countTerm(current.term.termId, current.term.giver, current.term.receiver)
            current.bundledTerm?.let { countTerm(it.termId, it.giver, it.receiver) }
        }
        return counts
    }

    private fun analRolesOk(term: Term, binding: PartyBinding, ctx: GameContext): Boolean {
        val touchesAnal = touchesAnal(term.categories, term.activities, term.analPenetration)
        if (!touchesAnal) return true
        if (!ctx.setup.player(binding.giver).analRole.allowsAnyAnal) return false
        if (!ctx.setup.player(binding.receiver).analRole.allowsAnyAnal) return false
        // Nothing goes near a top's hole, ever, and near a vers top's only once a night. This
        // covers every kind of attention, not only penetration.
        if (receptiveParties(term, binding).any { ctx.analReceptionSpent(it) }) return false
        if (term.analPenetration || term.activities.any { it in PENETRATIVE_ACTIVITIES }) {
            // Which party is penetrated depends on the term, and is not always the receiver.
            // The activity names are written from the giver's side: "topping" means the giver
            // penetrates the receiver, "bottoming" means the giver is the one taking it, so the
            // receiver is the penetrating party. Assuming the receiver is always the penetrated
            // one excluded a strict top from every "he finishes inside him" closing term — the
            // player those terms exist for.
            val receiverPenetrates = "bottoming" in term.activities
            val penetrated = if (receiverPenetrates) binding.giver else binding.receiver
            if (!ctx.setup.player(penetrated).analRole.canBottom) return false
            if (("topping" in term.activities) && !ctx.setup.player(binding.giver).analRole.canTop) return false
            if (receiverPenetrates && !ctx.setup.player(binding.receiver).analRole.canTop) return false
        }
        return true
    }

    /** Candidate bindings of a term's abstract giver/receiver onto the two players. */
    fun candidateBindings(term: Term, ctx: GameContext): List<PartyBinding> =
        listOf(
            PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2),
            PartyBinding(Slot.PLAYER_2, Slot.PLAYER_1)
        ).filter {
            satisfies(term.giverConstraint, it.giver, ctx) && satisfies(term.receiverConstraint, it.receiver, ctx)
        }

    fun evaluate(term: Term, ctx: GameContext, preferredBinding: PartyBinding? = null): Eligibility {
        val blocked = blockingBoundaries(term).firstOrNull { it in ctx.setup.boundaries }
        if (blocked != null) return Eligibility.BlockedByBoundary(blocked)
        if (!equipmentOk(term.requiredEquipment, term.genericToy, ctx)) return Eligibility.Unavailable

        val bindings = candidateBindings(term, ctx).let { list ->
            if (preferredBinding != null && preferredBinding in list) {
                listOf(preferredBinding) + list.filter { it != preferredBinding }
            } else {
                list
            }
        }
        for (binding in bindings) {
            if (!analRolesOk(term, binding, ctx)) continue
            if (!erectionOk(term, binding, ctx)) continue
            val maybes = preferenceCheck(
                term.activities, term.extraGiverPrefs, term.extraReceiverPrefs,
                binding.giver, binding.receiver, ctx
            ) ?: continue
            return Eligibility.Ok(binding, maybes)
        }
        return Eligibility.Unavailable
    }

    fun evaluateConsideration(
        action: ConsiderationAction,
        performer: Slot,
        recipient: Slot,
        ctx: GameContext
    ): Eligibility {
        val blocked = blockingBoundaries(action).firstOrNull { it in ctx.setup.boundaries }
        if (blocked != null) return Eligibility.BlockedByBoundary(blocked)
        if (!equipmentOk(action.requiredEquipment, action.genericToy, ctx)) return Eligibility.Unavailable
        if (!satisfies(action.performerConstraint, performer, ctx)) return Eligibility.Unavailable

        val touchesAnal = touchesAnal(action.categories, action.activities, action.analPenetration)
        if (touchesAnal) {
            if (!ctx.setup.player(performer).analRole.allowsAnyAnal) return Eligibility.Unavailable
            if (!ctx.setup.player(recipient).analRole.allowsAnyAnal) return Eligibility.Unavailable
            // Same rule as for terms: never for a top, once a night for a vers top.
            if (receptiveParties(action, performer, recipient).any { ctx.analReceptionSpent(it) }) {
                return Eligibility.Unavailable
            }
            if (action.analPenetration && !ctx.setup.player(recipient).analRole.canBottom) {
                return Eligibility.Unavailable
            }
            // Doing it with his own cock is the one case that asks the performer to top; a hand,
            // a finger or a toy asks nothing of his role.
            if (action.usesPerformersCock && !ctx.setup.player(performer).analRole.canTop) {
                return Eligibility.Unavailable
            }
        }

        val erectionParties = when (action.erectionRequired) {
            PartyRef.NEITHER -> emptyList()
            PartyRef.GIVER -> listOf(performer)
            PartyRef.RECEIVER -> listOf(recipient)
            PartyRef.BOTH -> listOf(performer, recipient)
        }
        if (erectionParties.any { ctx.setup.player(it).erectionDifficulty }) return Eligibility.Unavailable

        val maybes = preferenceCheck(
            action.activities, action.extraPerformerPrefs, action.extraRecipientPrefs,
            performer, recipient, ctx
        ) ?: return Eligibility.Unavailable

        return Eligibility.Ok(PartyBinding(performer, recipient), maybes)
    }

    /**
     * The Maybe conditions that apply to a term for a given binding: one per preference the
     * players answered Maybe on, using their own per-preference override when they set one and
     * the session default otherwise. Capped at two so the rewritten instruction stays readable.
     */
    fun maybeConditions(term: Term, binding: PartyBinding, ctx: GameContext): List<com.thecontract.core.model.MaybeCondition> {
        val result = LinkedHashSet<com.thecontract.core.model.MaybeCondition>()
        fun consider(slot: Slot, prefId: String) {
            if (ctx.answer(slot, prefId) == Answer.MAYBE) {
                result += ctx.profile(slot).maybeConditions[prefId] ?: ctx.setup.defaultMaybeCondition
            }
        }
        for (activity in term.activities) {
            consider(binding.giver, PreferenceLibrary.give(activity))
            consider(binding.receiver, PreferenceLibrary.receive(activity))
        }
        term.extraGiverPrefs.forEach { consider(binding.giver, it) }
        term.extraReceiverPrefs.forEach { consider(binding.receiver, it) }
        return result.take(2)
    }
}
