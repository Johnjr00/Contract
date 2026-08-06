package com.thecontract.core.model

import kotlinx.serialization.Serializable

/** The two — and only two — phone-controller slots. */
@Serializable
enum class Slot {
    PLAYER_1,
    PLAYER_2;

    val other: Slot get() = if (this == PLAYER_1) PLAYER_2 else PLAYER_1
}

/** Fixed for the whole session, chosen by Player 1 during shared setup. */
@Serializable
enum class Role { DOMINANT, SUBMISSIVE }

/**
 * Anal role. Governs eligibility and weighting for every anal activity, in proposals,
 * consideration actions, bundles, closing terms and final instructions.
 */
@Serializable
enum class AnalRole(val canTop: Boolean, val canBottom: Boolean, val topWeight: Double, val bottomWeight: Double) {
    TOP(true, false, 1.0, 0.0),
    VERS_TOP(true, true, 1.6, 0.6),
    VERS(true, true, 1.0, 1.0),
    VERS_BOTTOM(true, true, 0.6, 1.6),
    BOTTOM(false, true, 0.0, 1.0),
    NO_ANAL(false, false, 0.0, 0.0);

    val allowsAnyAnal: Boolean get() = this != NO_ANAL
}

/** Content register. Affects every generated sentence, not just anatomy words. */
@Serializable
enum class Explicitness(val level: Int) {
    EROTIC(1),
    DIRECT(2),
    FILTHY(3),
    EXTREME(4);

    companion object {
        fun ofLevel(level: Int): Explicitness = entries.first { it.level == level }
    }
}

@Serializable
enum class FinaleFormat {
    /** Both players privately vote between three orders; Dominant breaks a tie. */
    THREE_ORDERS,

    /** Only the Dominant's phone sees the three orders. TV shows a neutral wait. */
    DOMINANT_PRIVATE,

    /** No choice; Smooth Escalation is used automatically. */
    UNINTERRUPTED
}

@Serializable
enum class FinaleOrder { SIGNED_ORDER, SMOOTH_ESCALATION, DOMINANTS_CUT }

@Serializable
enum class SessionLength(val regularTerms: Int, val climaxTerms: Int) {
    QUICK(10, 2),
    FULL(15, 2),
    LONG(20, 2),
    EXTENDED(25, 2);

    val totalTerms: Int get() = regularTerms + climaxTerms
}

/** A private-profile answer. Every preference starts at [MAYBE]. */
@Serializable
enum class Answer { YES, MAYBE, NO }

/** Which side of an activity a preference describes. */
@Serializable
enum class Direction { GIVE, RECEIVE, NEUTRAL }

/** The five acts of regular negotiation. Derived from signed regular terms, never from rejections. */
@Serializable
enum class Act(val level: Int, val title: String) {
    CHEMISTRY(1, "Chemistry"),
    ACCESS(2, "Access"),
    PRIVILEGE(3, "Privilege"),
    AUTHORITY(4, "Authority"),
    ENFORCEMENT(5, "Enforcement");

    companion object {
        fun ofLevel(level: Int): Act = entries.first { it.level == level.coerceIn(1, 5) }
    }
}

/** Broad content category of a term or consideration action. */
@Serializable
enum class Category {
    MASSAGE, KISSING, EAR_PLAY, BODY_WORSHIP, ORAL, RIMMING, HANDS, ANAL, TOYS,
    POWER, LANGUAGE, VISUAL, BONDAGE, IMPACT, ORGASM_CONTROL, ROLEPLAY, SENSORY,
    ROUGH, SPIT, CLIMAX
}

/**
 * Which abstract party a term refers to. Terms are authored against these references and
 * bound to concrete [Slot]s at proposal time, honouring role and anal-role constraints.
 */
@Serializable
enum class PartyRef { GIVER, RECEIVER, BOTH, NEITHER }

/** Constraint applied when binding an abstract party to a concrete player. */
@Serializable
enum class PartyConstraint { ANY, DOMINANT, SUBMISSIVE, TOP, BOTTOM }

/**
 * How benefit is produced by a term. Drives who owes consideration (section 27).
 * Deliberately independent of how enthusiastically anyone answered their private profile.
 */
@Serializable
enum class BenefitType(val label: String) {
    MASSAGE_RECIPIENT("receives the massage"),
    KISS_RECIPIENT("receives the kissing"),
    EAR_PLAY_RECIPIENT("receives the ear play"),
    ORAL_RECIPIENT("receives oral"),
    RIMMING_RECIPIENT("receives rimming"),
    HAND_STIMULATION_RECIPIENT("receives the hand stimulation"),
    TOY_RECIPIENT("receives the toy"),
    FINGERING_RECIPIENT("receives the fingering"),
    PENETRATION_RECIPIENT("receives penetration"),
    COMMAND_AUTHORITY("gains command authority"),
    OWNERSHIP("gains ownership"),
    PERMISSION_CONTROL("gains permission control"),
    RESTRAINT_CONTROL("gains restraint control"),
    ORGASM_CONTROL("gains orgasm control"),
    SERVICE_RECIPIENT("receives service"),
    MUTUAL("benefits both equally")
}

/** Which abstract party the benefit accrues to. */
@Serializable
enum class BenefitParty { GIVER, RECEIVER, MUTUAL }

/** Every stage of the server-authoritative state machine (section 14). */
@Serializable
enum class GamePhase {
    NO_SESSION,
    PAIRING,
    PLAYER_1_SETUP,
    WAITING_FOR_PLAYER_2,
    PRIVATE_PROFILES,
    WAITING_FOR_PROFILES,
    PROPOSAL,
    WAITING_FOR_PROPOSAL_RESPONSES,
    COUNTEROFFER_PRIVATE_SELECTION,
    COUNTEROFFER_APPROVAL,
    BUNDLE_PRIVATE_SELECTION,
    BUNDLE_APPROVAL,
    CONSIDERATION_PRIVATE_SELECTION,
    CONSIDERATION_PUBLIC_EXECUTION,
    WAITING_FOR_SIGNATURE_CONFIRMATION,
    TERM_SIGNED,
    CLOSING_TERM_SELECTION,
    CLOSING_TERM_CONSIDERATION,
    FINAL_CONTRACT_REVIEW,
    FINALE_ORDER_SELECTION,
    PRIVATE_DOMINANT_FINALE_SELECTION,
    FINAL_EXECUTION,
    PAUSED,
    COMPLETED;

    /**
     * True when the TV must show a neutral waiting screen because a phone is making a
     * private choice the other player and the room must not see.
     */
    val isPrivateOnTv: Boolean
        get() = this in setOf(
            PRIVATE_PROFILES,
            WAITING_FOR_PROFILES,
            WAITING_FOR_PROPOSAL_RESPONSES,
            COUNTEROFFER_PRIVATE_SELECTION,
            BUNDLE_PRIVATE_SELECTION,
            CONSIDERATION_PRIVATE_SELECTION,
            CLOSING_TERM_SELECTION,
            PRIVATE_DOMINANT_FINALE_SELECTION
        )
}

/** A player's response to a proposed term. */
@Serializable
enum class ProposalResponse { SIGN, COUNTEROFFER, REJECT, BUNDLE }

@Serializable
enum class TimerRunState { IDLE, RUNNING, PAUSED, COMPLETED }

@Serializable
enum class ConnectionState { CONNECTED, DISCONNECTED, NEVER_CONNECTED }
