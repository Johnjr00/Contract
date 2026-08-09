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
 *
 * **The slot contract.** All four variants of a key have to read correctly in every slot the key
 * is used in, because the register is chosen at play time and the author does not get to pick.
 * A variant that only works in one slot is a defect: `[v_pull_hair]` used to resolve to "fists",
 * which produced "fists his hair" at the top two registers and "keeps takes a handful of his
 * hair" wherever the slot followed "keeps". Two rules keep that from recurring:
 *
 *  - A verb key takes its object from the template, so it must never imply an object of its own.
 *  - Force lives in the adverb, never in the verb, so a verb can never contradict the pressure
 *    or pace phrase that follows it.
 *
 * `RenderSweepTest` renders every template in every register and enforces both.
 */
object Lexicon {

    private val table: Map<String, List<String>> = buildMap {
        fun k(key: String, erotic: String, direct: String, filthy: String, extreme: String) {
            put(key, listOf(erotic, direct, filthy, extreme))
        }

        // --- Oral -------------------------------------------------------------------------
        k("v_suck", "uses his mouth on", "sucks", "sucks", "swallows")
        k("v_suck_slow", "sucks gently on", "sucks slowly on", "sucks hard and slow on", "sucks deep and slow on")
        k("v_deep", "sinks slowly onto", "works his way down onto", "pushes his throat down onto", "forces himself down onto")
        k("v_lick", "traces his tongue over", "licks", "licks", "laps at")
        k("v_mouth", "attends to", "licks and sucks", "sucks and bites at", "sucks hard on")
        k("v_tongue", "circles his tongue around", "runs his tongue over", "drags his tongue over", "drags his tongue over")
        k("adv_oral_pace", "at a slow, even pace", "at a steady pace", "at a hard pace", "at a hard, fast pace")
        k("n_mouth_use", "attention", "sucking", "mouth", "throat")

        // --- Rimming ----------------------------------------------------------------------
        k("v_rim", "licks slowly at", "rims", "tongues", "eats")
        k("v_rim_hard", "presses his tongue deep into", "rims hard into", "tongue-fucks", "tongue-fucks")
        k("n_rim", "licking", "rimming", "rimming", "rimming")

        // --- Kissing ----------------------------------------------------------------------
        k("adv_kiss", "gently and slowly", "slowly", "hard", "hard and wet")
        k("adv_kiss_deep", "deeply and unhurriedly", "deeply", "deep and wet", "deep, wet and messy")
        k("v_bite", "grazes his teeth over", "nips at", "bites", "bites")
        k("n_kiss_trail", "a trail of kisses", "a trail of kisses", "a wet trail", "a wet, open-mouthed trail")

        // --- Hands and stroking -----------------------------------------------------------
        k("v_stroke", "strokes", "strokes", "jerks", "pumps")
        k("v_grip", "holds", "grips", "grips", "clamps down on")
        k("v_squeeze", "presses", "squeezes", "squeezes", "crushes")
        k("adv_grip", "with a light hold", "with a firm hold", "with a tight grip", "with a punishing grip")

        // --- Massage ----------------------------------------------------------------------
        // Force belongs to the pressure adverb that always follows, not to the verb: a verb that
        // carried its own force produced "grinds his palms into his scalp with light pressure".
        k("v_massage", "rubs", "massages", "kneads", "kneads")
        k("adv_pressure_light", "with light, even pressure", "with light pressure", "with light pressure", "with light pressure")
        k("adv_pressure_firm", "with steady, even pressure", "with firm pressure", "with hard pressure", "with hard, digging pressure")
        k("adv_pressure_deep", "with slow, deep pressure", "with deep pressure", "with deep, grinding pressure", "with deep, grinding pressure")
        k("v_knead", "kneads", "kneads", "digs into", "digs into")

        // --- Anal -------------------------------------------------------------------------
        // "opens" and "presses deeper into" never said what with, which left the softest register
        // as the vaguest one. Every variant names the hand.
        k("v_finger", "slides a finger into", "fingers", "fingers", "finger-fucks")
        k("v_finger_deep", "fingers deep into", "fingers deep into", "finger-fucks deep into", "finger-fucks hard into")
        k("v_open", "opens", "opens up", "opens up", "stretches open")
        k("v_fuck", "takes", "fucks", "fucks", "fucks")
        k("v_fuck_hard", "moves hard into", "fucks hard into", "drives hard into", "pounds")
        k("v_enter", "eases into", "pushes into", "pushes into", "shoves into")
        k("v_bottom_take", "takes", "takes", "takes", "takes every inch of")
        k("adv_thrust_slow", "in long, slow strokes", "in long, slow strokes", "in long, slow strokes", "in long, deep strokes")
        k("adv_thrust_hard", "in hard, steady strokes", "in hard, steady strokes", "in hard, fast strokes", "in brutal, fast strokes")
        k("n_hole", "hole", "hole", "hole", "hole")

        // --- Toys -------------------------------------------------------------------------
        k("v_use_toy", "uses", "uses", "uses", "drives")
        k("v_insert", "eases in", "slides in", "pushes in", "shoves in")
        k("adv_toy_pace", "on a low setting", "on a low setting", "on a high setting", "on the highest setting")

        // --- Rough play -------------------------------------------------------------------
        // Every one of these is followed straight by its object, so no variant may end in an
        // adverb or a particle: "handles roughly" produced "handles roughly Tom into three
        // positions", and "clamps" produced "clamps Dan down".
        k("v_pin", "holds", "pins", "pins", "pins")
        k("v_handle", "moves", "handles", "manhandles", "manhandles")
        k("v_shove", "guides", "pushes", "shoves", "shoves")
        k("v_spank", "swats", "spanks", "spanks", "spanks")
        // "fists" is not something a hand does to hair. Every variant here has to survive being
        // followed directly by "his hair", which is the only slot this key is ever used in.
        k("v_pull_hair", "runs his fingers through", "takes a fistful of", "grips a fistful of", "grips a hard fistful of")
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
        // No duration of its own: the templates supply "for the rest of the night", and the old
        // extreme value made that "his property for the night for the rest of the night".
        k("n_ownership", "belonging to him", "belonging to him", "being his", "being his property")
        k("v_serve", "attends to", "serves", "serves", "serves")

        // --- Language ---------------------------------------------------------------------
        k("v_praise", "tells him how good he is", "tells him how good he is", "tells him what a good boy he is", "tells him what a good boy he is for taking it")
        // No recipient of its own: every template that uses this already names the ear it goes
        // into, and "murmurs to him at Ben's ear" named the man twice.
        k("v_talk_dirty", "murmurs", "talks", "talks dirty", "talks filth")
        k("tone_whisper", "murmurs", "whispers", "whispers", "growls")
        k("tone_moan", "breathes", "breathes hard", "moans", "moans and growls")
        k("n_dirty_talk", "quiet, explicit words", "explicit words", "crude, explicit talk", "the crudest words he knows")

        // --- Orgasm -----------------------------------------------------------------------
        k("v_come", "finishes", "comes", "comes", "unloads")
        k("v_come_in", "finishes inside", "comes inside", "comes inside", "unloads inside")
        k("n_orgasm", "release", "orgasm", "orgasm", "load")
        k("n_load", "what he has", "what he has", "his load", "every drop of his load")
        k("v_edge", "brings him to the edge and stops", "edges him", "edges him", "edges him hard")
        k("v_hold_back", "holds back", "holds back", "holds it", "does not let himself come")

        // --- Pace, tone and general intensity ---------------------------------------------
        k("adv_pace", "slowly", "steadily", "hard", "hard and fast")
        k("adv_slow", "unhurriedly", "slowly", "slowly", "torturously slowly")
        k("adv_hard", "firmly", "hard", "hard", "at full force")
        k("adj_wet", "slick", "wet", "wet", "sloppy")
        k("adj_intense", "intense", "intense", "rough", "brutal")
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
