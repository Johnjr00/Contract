package com.thecontract.core.style

import com.thecontract.core.model.Explicitness

/**
 * The explicitness lexicon.
 *
 * Section 18 requires that the four registers differ by more than anatomy words, so the
 * rewriting happens in two independent layers:
 *
 *  1. **Register templates.** Every term and consideration action is authored twice — a
 *     measured sentence used for Erotic/Direct and a coarse sentence, structured differently,
 *     used for Filthy/Extremely filthy. Sentence shape, clause order and command framing
 *     change between the two.
 *
 *  2. **This lexicon.** Verbs, adverbs, commands, dominance/submission phrasing, oral, anal,
 *     rough-sex, orgasm and ownership language each resolve to a different string per level,
 *     so Erotic differs from Direct and Filthy differs from Extremely filthy even where the
 *     underlying template is shared.
 *
 * Anatomy is written literally in the templates; it is deliberately *not* the mechanism of
 * differentiation.
 *
 * Every value is a drop-in substitution: transitive verbs are third-person singular present and
 * are immediately followed by their object in the template, adverbial keys are complete
 * adverbial phrases, and no value begins or ends with whitespace or punctuation.
 */
object Lexicon {

    private val table: Map<String, List<String>> = buildMap {
        fun k(key: String, erotic: String, direct: String, filthy: String, extreme: String) {
            put(key, listOf(erotic, direct, filthy, extreme))
        }

        // --- Oral -------------------------------------------------------------------------
        k("v_suck", "worships", "sucks", "sucks", "swallows")
        k("v_suck_slow", "takes his time over", "sucks slowly on", "works over", "works over")
        k("v_deep", "takes deep", "takes deep into his throat", "takes down his throat", "buries in his throat")
        k("v_lick", "traces his tongue over", "licks", "licks", "laps at")
        k("v_mouth", "attends to", "works", "works over", "uses")
        k("v_tongue", "circles his tongue around", "runs his tongue over", "drags his tongue over", "drags his tongue over")
        k("adv_oral_pace", "at an unhurried pace", "at a steady pace", "at a hard pace", "at a hard, greedy pace")
        k("n_mouth_use", "attention", "mouth work", "mouth", "throat")

        // --- Rimming ----------------------------------------------------------------------
        k("v_rim", "opens with his tongue", "rims", "eats out", "eats out")
        k("v_rim_hard", "works open with his tongue", "rims hard", "tongue-fucks", "tongue-fucks")
        k("n_rim", "tongue work", "rimming", "rimming", "rimming")

        // --- Kissing ----------------------------------------------------------------------
        k("adv_kiss", "slowly and thoroughly", "slowly", "hard", "hard and filthy")
        k("adv_kiss_deep", "deeply and unhurriedly", "deeply", "deep and wet", "deep, wet and filthy")
        k("v_bite", "grazes his teeth over", "nips at", "bites", "bites")
        k("n_kiss_trail", "a trail of kisses", "a trail of kisses", "a wet trail", "a wet, open-mouthed trail")

        // --- Hands and stroking -----------------------------------------------------------
        k("v_stroke", "strokes", "strokes", "works", "pumps")
        k("v_grip", "holds", "grips", "grips", "clamps down on")
        k("v_squeeze", "presses", "squeezes", "squeezes", "crushes")
        k("adv_grip", "with a light hold", "with a firm hold", "with a tight grip", "with a punishing grip")

        // --- Massage ----------------------------------------------------------------------
        k("v_massage", "eases out", "massages", "works over", "works over")
        k("adv_pressure_light", "with light, even pressure", "with light pressure", "with light pressure", "with light pressure")
        k("adv_pressure_firm", "with steady, even pressure", "with firm pressure", "with hard pressure", "with hard, digging pressure")
        k("adv_pressure_deep", "with slow, deep pressure", "with deep pressure", "with deep, grinding pressure", "with deep, grinding pressure")
        k("v_knead", "kneads", "kneads", "digs into", "digs into")

        // --- Anal -------------------------------------------------------------------------
        k("v_finger", "opens with one finger", "fingers", "fingers", "works open with his fingers")
        k("v_finger_deep", "works deeper with two fingers", "fingers deep with two fingers", "fucks with two fingers", "fucks open with two fingers")
        k("v_open", "opens", "opens up", "opens up", "works open")
        k("v_fuck", "takes", "fucks", "fucks", "fucks")
        k("v_fuck_hard", "moves hard into", "fucks hard", "fucks hard", "pounds")
        k("v_enter", "eases into", "pushes into", "pushes into", "shoves into")
        k("v_bottom_take", "takes", "takes", "takes", "takes every inch of")
        k("adv_thrust_slow", "in long, slow strokes", "in long, slow strokes", "in long, slow strokes", "in long, deep strokes")
        k("adv_thrust_hard", "in hard, steady strokes", "in hard, steady strokes", "in hard, fast strokes", "in brutal, fast strokes")
        k("n_hole", "hole", "hole", "hole", "hole")

        // --- Toys -------------------------------------------------------------------------
        k("v_use_toy", "uses", "uses", "uses", "works")
        k("v_insert", "eases in", "works in", "pushes in", "shoves in")
        k("adv_toy_pace", "on a low setting", "on a low setting", "on a high setting", "on the highest setting he can take")

        // --- Rough play -------------------------------------------------------------------
        k("v_pin", "holds down", "pins", "pins", "pins down hard")
        k("v_handle", "moves", "handles", "handles roughly", "manhandles")
        k("v_shove", "guides", "pushes", "shoves", "shoves")
        k("v_spank", "swats", "spanks", "spanks", "cracks his hand across")
        k("v_pull_hair", "runs his hand through", "takes a handful of", "fists", "fists")
        k("adv_rough", "firmly", "roughly", "roughly", "brutally")

        // --- Power, command and ownership -------------------------------------------------
        k("v_order", "asks", "tells", "orders", "orders")
        k("v_instruct", "guides", "directs", "commands", "commands")
        k("v_permit", "lets", "allows", "permits", "permits")
        k("v_deny", "asks him to wait", "makes him wait", "denies him", "denies him")
        k("v_kneel_cmd", "asks him to kneel", "tells him to kneel", "orders him onto his knees", "puts him on his knees")
        k("n_dom_title", "the one in charge", "the one in charge", "the one who owns the room", "the one who owns him")
        k("n_sub_title", "the one following", "the one taking orders", "the one who obeys", "the one who obeys")
        k("v_own", "calls him his", "calls him his", "tells him he belongs to him", "tells him he is his tonight")
        k("n_ownership", "belonging", "belonging to him", "being his", "being his property for the night")
        k("v_serve", "attends to", "serves", "serves", "serves")

        // --- Language ---------------------------------------------------------------------
        k("v_praise", "tells him how good he is", "tells him how good he is", "tells him what a good boy he is", "tells him what a filthy good boy he is")
        k("v_talk_dirty", "murmurs to him", "talks to him", "talks dirty to him", "talks filthy to him")
        k("tone_whisper", "murmurs", "whispers", "whispers", "growls")
        k("tone_moan", "breathes", "breathes hard", "moans", "moans and growls")
        k("n_dirty_talk", "quiet, explicit words", "explicit words", "filthy talk", "the filthiest talk he has")

        // --- Orgasm -----------------------------------------------------------------------
        k("v_come", "finishes", "comes", "comes", "unloads")
        k("v_come_in", "finishes inside", "comes inside", "comes inside", "unloads inside")
        k("n_orgasm", "release", "orgasm", "orgasm", "load")
        k("n_load", "what he has", "what he has", "his load", "every drop of his load")
        k("v_edge", "brings him close and eases off", "edges him", "edges him", "edges him until he is begging")
        k("v_hold_back", "asks him to hold back", "makes him hold back", "makes him hold it", "makes him hold it until he is shaking")

        // --- Pace, tone and general intensity ---------------------------------------------
        k("adv_pace", "slowly", "steadily", "hard", "hard and filthy")
        k("adv_slow", "unhurriedly", "slowly", "slowly", "torturously slowly")
        k("adv_hard", "firmly", "hard", "hard", "as hard as he can take")
        k("adj_wet", "slick", "wet", "wet", "sloppy")
        k("adj_intense", "intense", "intense", "filthy", "filthy")
        k("v_tease", "teases", "teases", "teases", "torments")
        k("v_worship", "takes his time over", "worships", "worships", "worships every inch of")
        k("v_hold_pos", "stays where he is", "holds the position", "holds the position", "holds the position and does not move")
        k("n_endpoint", "until the timer ends", "until the timer ends", "until the timer ends", "until the timer ends")
    }

    val keys: Set<String> get() = table.keys

    fun has(key: String): Boolean = table.containsKey(key)

    fun resolve(key: String, explicitness: Explicitness): String =
        table[key]?.get(explicitness.level - 1)
            ?: throw IllegalArgumentException("Unknown lexicon key: [$key]")

    /** All four renderings of a key, for validation and for the developer content report. */
    fun variants(key: String): List<String> = table.getValue(key)
}
