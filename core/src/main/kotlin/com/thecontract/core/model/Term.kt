package com.thecontract.core.model

import kotlinx.serialization.Serializable

/**
 * One timed segment. Every distinct timed action inside an instruction gets its own timer —
 * "shoulders for 60 seconds, lower back for 45, each thigh for 30" is four timers, not one.
 */
@Serializable
data class TimerSpec(val label: String, val seconds: Int) {
    init {
        require(seconds > 0) { "Timer '$label' must have a positive duration" }
    }
}

/**
 * A proposed contract term. Describes something that will happen **only during the final
 * scene** — it is never performed during negotiation.
 *
 * Text fields are token templates rendered by [com.thecontract.core.style.StyleEngine]:
 *   `{G}` `{R}` `{DOM}` `{SUB}` `{TOP}` `{BOT}`   party names
 *   `{G+}` …                                      possessives (handles names ending in s)
 *   `[key]`                                       lexicon entry, rewritten per explicitness
 *   `#equipment_id#`                              a specific available item
 *   `#TOY#`                                       generic toy, resolved to a real selected toy
 *
 * [base] is the measured register (used for Erotic/Direct); [explicit] is the coarse register
 * (used for Filthy/Extremely filthy). Within each register the lexicon still differentiates
 * every level, and Extremely filthy additionally appends [extremeTail].
 */
/**
 * A labelled list of suggestions shown under an instruction.
 *
 * Some instructions tell a man to do something without telling him what — "talks filth", "names
 * a position". The instruction stays as written and the suggestions sit under it with their own
 * heading, so nobody mistakes an example for an order.
 */
@Serializable
data class Suggestions(val heading: String, val items: List<String>)

@Serializable
data class Term(
    val id: String,
    val level: Int,
    val categories: Set<Category>,
    val title: String,
    val base: String,
    val explicit: String,
    val benefitParty: BenefitParty,
    val benefitType: BenefitType,
    val giverConstraint: PartyConstraint = PartyConstraint.ANY,
    val receiverConstraint: PartyConstraint = PartyConstraint.ANY,
    /** Activities checked on both sides: giver needs `<a>_give`, receiver needs `<a>_receive`. */
    val activities: Set<String> = emptySet(),
    val extraGiverPrefs: Set<String> = emptySet(),
    val extraReceiverPrefs: Set<String> = emptySet(),
    val requiredEquipment: Set<Equipment> = emptySet(),
    /** True when the text uses `#TOY#` and needs some toy to be available. */
    val genericToy: Boolean = false,
    val blockedBy: Set<Boundary> = emptySet(),
    /** Which party must sustain an erection for this term to work as written. */
    val erectionRequired: PartyRef = PartyRef.NEITHER,
    val analPenetration: Boolean = false,
    val timers: List<TimerSpec> = emptyList(),
    val mutual: Boolean = false,
    val extremeTail: String? = null,
    /**
     * Lines a player could actually say, for an instruction that tells him to talk without
     * telling him what to say — "talks filth", "spells out what he is going to do to him",
     * "says out loud who he belongs to". Suggestions, not part of the instruction: he is free to
     * say something else. Split by register on the same rule as [base] and [explicit].
     */
    val examples: List<String> = emptyList(),
    val examplesExplicit: List<String> = emptyList(),
    /**
     * Positions a player could put the other in, for an instruction that tells him to name or
     * choose one. Register-independent: a position is a physical arrangement of two bodies and
     * reads the same however coarse the rest of the sentence is.
     */
    val positions: List<String> = emptyList(),
    /** Climax terms are negotiated separately at the end and always execute last. */
    val climax: Boolean = false
) {
    val act: Act get() = Act.ofLevel(level)

    val totalSeconds: Int get() = timers.sumOf { it.seconds }
}

/**
 * A consideration action (section 25). Performed **during** negotiation to earn the other
 * player's signature. Never added to the final contract; recorded only as a receipt.
 */
@Serializable
data class ConsiderationAction(
    val id: String,
    /** Intensity band 1..5; matched against negotiation progress so consideration escalates. */
    val intensity: Int,
    val family: ConsiderationFamily,
    val categories: Set<Category>,
    val title: String,
    val base: String,
    val explicit: String,
    val activities: Set<String> = emptySet(),
    val extraPerformerPrefs: Set<String> = emptySet(),
    val extraRecipientPrefs: Set<String> = emptySet(),
    val requiredEquipment: Set<Equipment> = emptySet(),
    val genericToy: Boolean = false,
    val blockedBy: Set<Boundary> = emptySet(),
    val erectionRequired: PartyRef = PartyRef.NEITHER,
    val analPenetration: Boolean = false,
    val performerConstraint: PartyConstraint = PartyConstraint.ANY,
    val timers: List<TimerSpec> = emptyList(),
    /** Reciprocal action: both players control timers and both confirm completion. */
    val mutual: Boolean = false,
    /**
     * The performer's own cock is what the act is done with.
     *
     * Two things follow. He has to be able to top, which a toy or a hand would not require of
     * him. And he is being stimulated himself, so the act is not pure service — consideration is
     * owed by the player who gained more from the term, and an option that gets him off as well
     * is not a payment. The selector leaves these out of what it offers.
     */
    val usesPerformersCock: Boolean = false,
    val extremeTail: String? = null,
    /** Lines he could say, for an action that tells him to talk. See [Term.examples]. */
    val examples: List<String> = emptyList(),
    val examplesExplicit: List<String> = emptyList(),
    /** Positions he could use, for an action that tells him to choose one. See [Term.positions]. */
    val positions: List<String> = emptyList()
)

@Serializable
enum class ConsiderationFamily { MASSAGE, KISSING, EAR_PLAY, SEXUAL }

/** Binding of a term's abstract giver/receiver to concrete player slots. */
@Serializable
data class PartyBinding(val giver: Slot, val receiver: Slot) {
    fun reversed(): PartyBinding = PartyBinding(receiver, giver)
}

/** A fully rendered, name-substituted, style-applied term ready for display. */
@Serializable
data class RenderedTerm(
    val termId: String,
    val level: Int,
    val title: String,
    val instruction: String,
    val timers: List<RenderedTimer>,
    val giver: Slot,
    val receiver: Slot,
    /** null when benefit is balanced and consideration is reciprocal. */
    val beneficiary: Slot?,
    val benefitExplanation: String,
    val equipmentUsed: List<String>,
    val conditions: List<String> = emptyList(),
    val amendments: List<String> = emptyList(),
    val categories: Set<Category> = emptySet(),
    val analPenetration: Boolean = false,
    val climax: Boolean = false,
    val mutual: Boolean = false,
    /** Set by the "Save for finale" amendment or Maybe condition. */
    val deferToEnd: Boolean = false,
    /** Set by the "Trade only" amendment or the "Only as part of a trade" Maybe condition. */
    val requiresTrade: Boolean = false,
    /** Labelled suggestion lists shown under the instruction. Never mandatory. */
    val suggestions: List<Suggestions> = emptyList()
) {
    val totalSeconds: Int get() = timers.sumOf { it.seconds }
}

@Serializable
data class RenderedTimer(val id: String, val label: String, val seconds: Int)

/** A rendered consideration action offered to, or performed by, the beneficiary. */
@Serializable
data class RenderedConsideration(
    val actionId: String,
    val title: String,
    val instruction: String,
    val timers: List<RenderedTimer>,
    val performer: Slot,
    val recipient: Slot,
    val mutual: Boolean,
    val intensity: Int,
    val equipmentUsed: List<String>,
    /** Labelled suggestion lists shown under the instruction. Never mandatory. */
    val suggestions: List<Suggestions> = emptyList()
)
