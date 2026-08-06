package com.thecontract.core.model

import kotlinx.serialization.Serializable

/**
 * Available equipment (section 22). A term or consideration action that requires an item the
 * couple does not own is filtered out *silently*, before it is ever proposed — the players
 * never see a reference to something they do not have.
 */
@Serializable
enum class Equipment(
    val id: String,
    val label: String,
    /** Lower-case noun phrase, already carrying its article, for use inside a sentence. */
    val noun: String,
    val toyLike: Boolean = false
) {
    LUBRICANT("lubricant", "Lubricant", "lube"),
    MASSAGE_OIL("massage_oil", "Massage oil or lotion", "massage oil"),
    MASSAGE_CANDLE("massage_candle", "Low-temperature massage candle", "the massage candle"),
    MASSAGE_WAND("massage_wand", "Massage wand", "the massage wand", toyLike = true),
    BLINDFOLD("blindfold", "Blindfold", "the blindfold"),
    CUFFS("cuffs", "Quick-release cuffs", "the cuffs"),
    ROPE("rope", "Bondage rope", "the rope"),
    COLLAR("collar", "Collar", "the collar"),
    LEASH("leash", "Leash", "the leash"),
    PADDLE("paddle", "Paddle", "the paddle"),
    FLOGGER("flogger", "Flogger", "the flogger"),
    CROP("crop", "Crop", "the crop"),
    VIBRATOR("vibrator", "Vibrator", "the vibrator", toyLike = true),
    ANAL_PLUG("anal_plug", "Anal plug", "the plug", toyLike = true),
    DILDO("dildo", "Dildo", "the dildo", toyLike = true),
    PROSTATE_TOY("prostate_toy", "Prostate toy", "the prostate toy", toyLike = true),
    COCK_RING("cock_ring", "Cock ring", "the cock ring", toyLike = true),
    SLEEVE("sleeve", "Masturbation sleeve", "the sleeve", toyLike = true),
    NIPPLE_CLAMPS("nipple_clamps", "Nipple clamps", "the nipple clamps", toyLike = true),
    SPREADER_BAR("spreader_bar", "Spreader bar", "the spreader bar"),
    ICE("ice", "Ice", "ice"),
    FEATHER("feather", "Feather or soft brush", "the feather"),
    GLOVES("gloves", "Gloves", "gloves"),
    MIRROR("mirror", "Large mirror", "the mirror"),
    STURDY_CHAIR("sturdy_chair", "Sturdy chair or furniture edge", "the chair"),
    PORN("porn", "Porn or erotic video", "porn"),
    MUSIC("music", "Music or speaker", "music"),
    TOWELS("towels", "Towels or wipes", "a towel");

    companion object {
        fun byId(id: String): Equipment? = entries.firstOrNull { it.id == id }

        /** Items that count as "a toy" for the No-toys boundary and for generic toy resolution. */
        val toys: List<Equipment> = entries.filter { it.toyLike }

        /** Preference order used when a term asks for a generic toy and several are available. */
        val genericToyPreferenceOrder: List<Equipment> =
            listOf(VIBRATOR, PROSTATE_TOY, ANAL_PLUG, DILDO, MASSAGE_WAND, SLEEVE, COCK_RING, NIPPLE_CLAMPS)
    }
}

/**
 * Shared hard boundaries (section 21). Unlike equipment, a boundary conflict may be shown as a
 * blocked proposal naming the boundary — but replacing a blocked term never consumes progress.
 */
@Serializable
enum class Boundary(val id: String, val label: String) {
    NO_ANAL_ACTIVITY("no_anal_activity", "No anal activity"),
    NO_ANAL_PENETRATION("no_anal_penetration", "No anal penetration"),
    NO_PAIN("no_pain", "No pain"),
    NO_MARKS("no_marks", "No visible marks"),
    NO_RESTRAINTS("no_restraints", "No restraints"),
    NO_ROUGH_SEX("no_rough_sex", "No rough sex"),
    NO_DEGRADATION("no_degradation", "No degradation"),
    NO_ORGASM_CONTROL("no_orgasm_control", "No orgasm control"),
    NO_ROLEPLAY("no_roleplay", "No roleplay"),
    NO_TOYS("no_toys", "No toys"),
    NO_FOOT_PLAY("no_foot_play", "No foot play"),
    NO_SPIT_PLAY("no_spit_play", "No spit play"),
    MASSAGE_NONSEXUAL("massage_nonsexual", "Massage must remain nonsexual");

    companion object {
        fun byId(id: String): Boundary? = entries.firstOrNull { it.id == id }

        /**
         * Equipment that inherently produces marks. "No visible marks" filters these
         * comprehensively rather than relying on per-term tagging alone.
         */
        val markingEquipment: Set<Equipment> =
            setOf(Equipment.PADDLE, Equipment.CROP, Equipment.FLOGGER, Equipment.NIPPLE_CLAMPS)
    }
}
