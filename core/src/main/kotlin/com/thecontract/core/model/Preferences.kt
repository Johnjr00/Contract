package com.thecontract.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class PreferenceSection(val id: String, val title: String) {
    KISSING_EAR("kissing_ear", "Kissing and ear play"),
    TOUCH("touch", "Touch and body worship"),
    MASSAGE("massage", "Massage"),
    ORAL("oral", "Oral and rimming"),
    ANAL("anal", "Anal"),
    TOYS("toys", "Toys"),
    POWER("power", "Power"),
    LANGUAGE("language", "Language"),
    BONDAGE("bondage", "Bondage and impact"),
    ORGASM("orgasm", "Orgasm control"),
    ROUGH("rough", "Rough play"),
    FANTASY("fantasy", "Fantasy and sensory")
}

/**
 * One private-profile question. Every preference defaults to [Answer.MAYBE].
 *
 * [activity] is the shared key that both the giving and the receiving side of the same act
 * hang off, so a term can say "this needs `oral` given by X and received by Y" and the
 * eligibility engine will look up `oral_give` on X and `oral_receive` on Y.
 */
@Serializable
data class Preference(
    val id: String,
    val section: PreferenceSection,
    val activity: String,
    val direction: Direction,
    val label: String
)

/**
 * The private preference library (section 24). 147 preferences; the specification floor is 132.
 *
 * No answer in a player's profile is ever sent to the TV or to the other player's phone.
 */
object PreferenceLibrary {

    private val builder = mutableListOf<Preference>()

    private fun pair(section: PreferenceSection, activity: String, giving: String, receiving: String) {
        builder += Preference("${activity}_give", section, activity, Direction.GIVE, giving)
        builder += Preference("${activity}_receive", section, activity, Direction.RECEIVE, receiving)
    }

    private fun neutral(section: PreferenceSection, activity: String, label: String) {
        builder += Preference(activity, section, activity, Direction.NEUTRAL, label)
    }

    init {
        // --- Kissing and ear play -------------------------------------------------------
        val k = PreferenceSection.KISSING_EAR
        pair(k, "long_making_out", "Making out with him for a long time", "Being made out with for a long time")
        pair(k, "tongue_kissing", "Kissing him with a lot of tongue", "Being kissed with a lot of tongue")
        pair(k, "hard_kissing", "Kissing him hard", "Being kissed hard")
        pair(k, "ear_play", "Working his ears with my mouth", "Having my ears worked with his mouth")
        pair(k, "neck_kissing", "Kissing his neck", "Having my neck kissed")
        pair(k, "chest_nipple_kissing", "Kissing his chest and nipples", "Having my chest and nipples kissed")
        pair(k, "stomach_hip_kissing", "Kissing his stomach and hips", "Having my stomach and hips kissed")
        pair(k, "inner_thigh_kissing", "Kissing his inner thighs", "Having my inner thighs kissed")
        pair(k, "foot_kissing", "Kissing his feet", "Having my feet kissed")

        // --- Touch and body worship -----------------------------------------------------
        val t = PreferenceSection.TOUCH
        pair(t, "body_worship", "Worshipping his body with my hands and mouth", "Having my body worshipped")
        pair(t, "nipple_stimulation", "Working his nipples", "Having my nipples worked")
        pair(t, "scratching", "Scratching him", "Being scratched")
        pair(t, "hair_pulling", "Pulling his hair", "Having my hair pulled")
        pair(t, "foot_stimulation", "Working his feet", "Having my feet worked")

        // --- Massage --------------------------------------------------------------------
        val m = PreferenceSection.MASSAGE
        pair(m, "massage_general", "Giving him a massage", "Getting a massage from him")
        pair(m, "massage_light", "Working him with light pressure", "Being worked with light pressure")
        pair(m, "massage_deep", "Working him with deep pressure", "Being worked with deep pressure")
        pair(m, "massage_scalp", "Massaging his scalp and hair", "Having my scalp and hair massaged")
        pair(m, "massage_jaw_face", "Massaging his jaw and face", "Having my jaw and face massaged")
        pair(m, "massage_neck_shoulders", "Massaging his neck and shoulders", "Having my neck and shoulders massaged")
        pair(m, "massage_upper_back", "Massaging his upper back", "Having my upper back massaged")
        pair(m, "massage_lower_back", "Massaging his lower back", "Having my lower back massaged")
        pair(m, "massage_chest", "Massaging his chest and pecs", "Having my chest and pecs massaged")
        pair(m, "massage_arms_hands", "Massaging his arms and hands", "Having my arms and hands massaged")
        pair(m, "massage_buttocks_hips", "Massaging his buttocks and hips", "Having my buttocks and hips massaged")
        pair(m, "massage_anus", "Massaging his anus", "Having my anus massaged")
        pair(m, "massage_inner_thighs", "Massaging his inner thighs", "Having my inner thighs massaged")
        pair(m, "massage_calves_feet", "Massaging his calves and feet", "Having my calves and feet massaged")
        pair(m, "massage_groin", "Massaging his groin and the area around his cock", "Having my groin and the area around my cock massaged")
        pair(m, "massage_oil", "Using massage oil on him", "Having massage oil used on me")
        pair(m, "massage_candle", "Using a massage candle on him", "Having a massage candle used on me")
        pair(m, "massage_wand", "Using a massage wand on him", "Having a massage wand used on me")
        pair(m, "massage_to_sexual", "Turning a massage into sexual touch", "Having a massage turn into sexual touch")

        // --- Oral and rimming -----------------------------------------------------------
        val o = PreferenceSection.ORAL
        pair(o, "oral", "Sucking his cock", "Having my cock sucked")
        pair(o, "deep_oral", "Taking him deep in my throat", "Being taken deep in his throat")
        pair(o, "rimming", "Eating his ass", "Having my ass eaten")
        pair(o, "spit", "Using spit on him", "Having his spit used on me")

        // --- Anal -----------------------------------------------------------------------
        val a = PreferenceSection.ANAL
        pair(a, "anal_external", "Touching and teasing his hole from the outside", "Having my hole touched and teased from the outside")
        pair(a, "fingering", "Fingering his hole", "Having my hole fingered")
        pair(a, "anal_toys", "Using anal toys on him", "Having anal toys used on me")
        pair(a, "topping", "Fucking his ass", "Having my ass fucked by him")
        pair(a, "bottoming", "Bottoming for him", "Having him bottom for me")

        // --- Toys -----------------------------------------------------------------------
        val y = PreferenceSection.TOYS
        pair(y, "vibration", "Using a vibrator on him", "Having a vibrator used on me")
        pair(y, "plug", "Putting a plug in him", "Wearing a plug")
        pair(y, "dildo", "Using a dildo on him", "Having a dildo used on me")
        pair(y, "cock_ring", "Putting a cock ring on him", "Wearing a cock ring")
        pair(y, "nipple_clamps", "Putting nipple clamps on him", "Wearing nipple clamps")

        // --- Power ----------------------------------------------------------------------
        val p = PreferenceSection.POWER
        pair(p, "commands", "Giving him orders", "Being given orders")
        pair(p, "kneeling", "Kneeling for him", "Having him kneel for me")
        pair(p, "sexual_service", "Serving him sexually", "Being served sexually")
        pair(p, "permission_control", "Controlling what he is allowed to do", "Having to ask permission")
        pair(p, "ownership_language", "Talking about him as mine", "Being talked about as his")

        // --- Language -------------------------------------------------------------------
        val l = PreferenceSection.LANGUAGE
        pair(l, "explicit_praise", "Telling him explicitly how good he is", "Being told explicitly how good I am")
        pair(l, "degradation", "Degrading him verbally", "Being degraded verbally")
        pair(l, "dirty_talk", "Talking very dirty to him", "Being talked to very dirty")

        // --- Bondage and impact ---------------------------------------------------------
        val b = PreferenceSection.BONDAGE
        pair(b, "blindfold", "Blindfolding him", "Being blindfolded")
        pair(b, "restraint", "Restraining him", "Being restrained")
        pair(b, "collaring", "Collaring him", "Being collared")
        pair(b, "spanking", "Spanking him", "Being spanked")
        pair(b, "impact_toys", "Using impact toys on him", "Having impact toys used on me")

        // --- Orgasm control -------------------------------------------------------------
        val g = PreferenceSection.ORGASM
        pair(g, "edging", "Edging him", "Being edged")
        pair(g, "denial", "Denying him an orgasm", "Being denied an orgasm")
        pair(g, "climax_permission", "Deciding when he is allowed to come", "Having to be allowed to come")

        // --- Rough play -----------------------------------------------------------------
        val r = PreferenceSection.ROUGH
        pair(r, "pinning", "Pinning him down", "Being pinned down")
        pair(r, "rough_handling", "Handling him roughly", "Being handled roughly")
        pair(r, "harder_oral", "Using his mouth at a harder pace", "Having my mouth used at a harder pace")
        pair(r, "hard_sex", "Fucking him hard", "Being fucked hard")
        pair(r, "rough_hair_pulling", "Pulling his hair during rough play", "Having my hair pulled during rough play")

        // --- Fantasy and sensory --------------------------------------------------------
        val f = PreferenceSection.FANTASY
        pair(f, "roleplay_lead", "Leading a roleplay", "Following his lead in a roleplay")
        pair(f, "cold_sensation", "Using cold on him", "Having cold used on me")
        pair(f, "warm_wax", "Dripping warm massage wax on him", "Having warm massage wax dripped on me")
        neutral(f, "strangers_fantasy", "Playing it like we just met")
        neutral(f, "authority_roleplay", "Authority or service roleplay")
        neutral(f, "capture_roleplay", "Consensual capture roleplay")
        neutral(f, "mirror_play", "Watching ourselves in a mirror")
        neutral(f, "watching_porn", "Watching porn together")
    }

    /** All preferences, in phone-display order. */
    val all: List<Preference> = builder.toList()

    val byId: Map<String, Preference> = all.associateBy { it.id }

    val bySection: Map<PreferenceSection, List<Preference>> =
        PreferenceSection.entries.associateWith { s -> all.filter { it.section == s } }

    fun require(id: String): Preference =
        byId[id] ?: error("Unknown preference id: $id")

    /** Convenience: the id of the giving side of an activity. */
    fun give(activity: String): String = "${activity}_give"

    /** Convenience: the id of the receiving side of an activity. */
    fun receive(activity: String): String = "${activity}_receive"

    /** A fresh profile: every preference at Maybe. */
    fun blankAnswers(): Map<String, Answer> = all.associate { it.id to Answer.MAYBE }
}

/**
 * A "Maybe" condition (section 23). When a required preference is answered [Answer.MAYBE],
 * the condition does not appear as a contradictory footnote — it rewrites the actual
 * instruction text before the term is ever displayed.
 */
@Serializable
enum class MaybeCondition(val id: String, val label: String) {
    ASK_AGAIN("ask_again", "Ask again immediately before starting"),
    GENTLER("gentler", "Gentler version"),
    UNDER_TWO_MINUTES("under_two_minutes", "Keep it under two minutes"),
    ROLES_REVERSED("roles_reversed", "Roles reversed"),
    ONLY_AS_TRADE("only_as_trade", "Only as part of a trade"),
    WITHOUT_TOY("without_toy", "Without a toy"),
    WITHOUT_RESTRAINT("without_restraint", "Without restraint"),
    SAVE_FOR_FINALE("save_for_finale", "Save it for the finale"),
    PAUSE_MIDWAY("pause_midway", "Pause midway for a direct check-in"),
    STOP_BEFORE_ORGASM("stop_before_orgasm", "Stop before orgasm"),
    MASSAGE_FIRST("massage_first", "Begin with at least five minutes of massage");

    companion object {
        /** Accepts either the wire id ("ask_again") or the enum name ("ASK_AGAIN"). */
        fun byId(id: String): MaybeCondition? =
            entries.firstOrNull { it.id == id || it.name == id }
    }
}

/** Counteroffer amendments (section 30). */
@Serializable
enum class Amendment(val id: String, val label: String) {
    GENTLER("gentler", "Gentler version"),
    SHORTER("shorter", "Shorter duration"),
    ROLES_REVERSED("roles_reversed", "Roles reversed"),
    MASSAGE_FIRST("massage_first", "Massage first"),
    NO_TOYS("no_toys", "No toys"),
    NO_RESTRAINT("no_restraint", "No restraint"),
    STOP_BEFORE_ORGASM("stop_before_orgasm", "Stop before orgasm"),
    MIDPOINT_CHECK("midpoint_check", "Midpoint check"),
    ASK_AGAIN("ask_again", "Ask again immediately before execution"),
    SAVE_FOR_FINALE("save_for_finale", "Save for finale"),
    TRADE_ONLY("trade_only", "Trade only");

    companion object {
        fun byId(id: String): Amendment? =
            entries.firstOrNull { it.id == id || it.name == id }
    }
}
