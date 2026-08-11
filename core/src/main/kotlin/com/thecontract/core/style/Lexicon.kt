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
 * **Alternatives.** A register may offer several wordings rather than one. A single string per
 * register meant that one phrase reached the screen once for every item that used the key — 24
 * different kissing terms all said "deep, wet and messy", and the wording stopped registering as
 * anything at all. [kv] gives a register a list, and the rendering picks from it using a seed
 * taken from the item's own id, so any one term always reads the same way while the library as a
 * whole varies. Alternatives obey the slot contract exactly as the primary does, and a verb's
 * alternatives must stay force-neutral: `[v_suck]` is followed by `[adv_oral_pace]` in several
 * templates, so a "sucks hard on" alternative would produce "sucks hard on his cock at a slow,
 * even pace".
 *
 * `RenderSweepTest` renders every template in every register, across enough seeds to reach every
 * alternative, and enforces all of it.
 */
object Lexicon {

    /** key -> four registers, each holding one or more interchangeable wordings. */
    private val table: Map<String, List<List<String>>> = buildMap {
        fun k(key: String, erotic: String, direct: String, filthy: String, extreme: String) {
            put(key, listOf(listOf(erotic), listOf(direct), listOf(filthy), listOf(extreme)))
        }
        /** Same, but each register offers a list of wordings. The first is the primary. */
        fun kv(
            key: String,
            erotic: List<String>,
            direct: List<String>,
            filthy: List<String>,
            extreme: List<String>
        ) {
            put(key, listOf(erotic, direct, filthy, extreme))
        }

        // --- Oral -------------------------------------------------------------------------
        // "swallows" described an outcome rather than the act, and read as the wrong thing
        // entirely wherever the template went on to name a pace. Every register sucks; they
        // differ in how the mouth is described, not in whether it is a mouth.
        kv(
            "v_suck",
            listOf("uses his mouth on", "puts his mouth on", "works his mouth over"),
            listOf("sucks", "sucks on", "puts his mouth on"),
            listOf("sucks", "sucks on", "gets his mouth around"),
            listOf("sucks", "gets his mouth around", "gets his mouth all the way down on")
        )
        k("v_suck_slow", "sucks gently on", "sucks slowly on", "sucks hard and slow on", "sucks deep and slow on")
        k("v_deep", "sinks slowly onto", "works his way down onto", "pushes his throat down onto", "forces himself down onto")
        k("v_lick", "traces his tongue over", "licks", "licks", "laps at")
        k("v_mouth", "attends to", "licks and sucks", "sucks and bites at", "sucks hard on")
        k("v_tongue", "circles his tongue around", "runs his tongue over", "drags his tongue over", "drags his tongue over")
        kv(
            "adv_oral_pace",
            listOf("at a slow, even pace", "at an unhurried pace", "slowly, without changing speed"),
            listOf("at a steady pace", "at an even pace", "steadily, without changing speed"),
            listOf("at a hard pace", "fast and hard", "at a hard pace the whole way through"),
            listOf("at a hard, fast pace", "as fast and as hard as he can keep up", "flat out for the whole term")
        )
        k("n_mouth_use", "attention", "sucking", "mouth", "throat")

        // --- Rimming ----------------------------------------------------------------------
        // "licks slowly at him" never said where, which made the softest register the vaguest.
        // Not "eats out": the object of a phrasal verb goes inside it, so "[v_rim] him" came
        // out as "eats out him". Every wording here has to survive a bare pronoun object.
        kv(
            "v_rim",
            listOf("rims", "runs his tongue over"),
            listOf("rims", "tongues"),
            listOf("tongues", "eats"),
            listOf("eats", "tongues deep into")
        )
        k("v_rim_hard", "presses his tongue deep into", "rims hard into", "tongue-fucks", "tongue-fucks")
        k("n_rim", "licking", "rimming", "rimming", "rimming")

        // --- Kissing ----------------------------------------------------------------------
        // The kissing keys carry the highest usage counts in the library, so a single wording
        // per register was the most visible repetition in the game.
        kv(
            "adv_kiss",
            listOf("gently and slowly", "softly and slowly", "slowly and lightly"),
            listOf("slowly", "steadily", "firmly and slowly"),
            listOf("hard", "hard and open-mouthed", "hard, with tongue"),
            listOf("hard and wet", "hard and open-mouthed", "hard, with tongue the whole time")
        )
        kv(
            "adv_kiss_deep",
            listOf("deeply and unhurriedly", "slowly and deeply", "deeply, with his mouth open"),
            listOf("deeply", "deeply and slowly", "deeply, with his mouth open"),
            listOf("deep and wet", "deep, with tongue", "open-mouthed and deep"),
            listOf(
                "deep, with his tongue in his mouth",
                "open-mouthed and deep, tongue included",
                "deep, and does not break it to breathe"
            )
        )
        k("v_bite", "grazes his teeth over", "nips at", "bites", "bites")
        kv(
            "n_kiss_trail",
            listOf("a trail of kisses", "an unbroken line of kisses"),
            listOf("a trail of kisses", "an unbroken line of kisses"),
            listOf("a wet trail", "a line of open-mouthed kisses"),
            listOf("a wet, open-mouthed trail", "an unbroken line of open-mouthed kisses")
        )

        // --- Hands and stroking -----------------------------------------------------------
        kv(
            "v_stroke",
            listOf("strokes", "runs his hand over"),
            listOf("strokes", "works"),
            listOf("jerks", "works"),
            listOf("jerks", "works")
        )
        k("v_grip", "holds", "grips", "grips", "clamps down on")
        k("v_squeeze", "presses", "squeezes", "squeezes", "crushes")
        kv(
            "adv_grip",
            listOf("with a light hold", "with a loose hold"),
            listOf("with a firm hold", "with a closed fist"),
            listOf("with a tight grip", "with a tight fist"),
            listOf("with a punishing grip", "with a fist as tight as he can close it")
        )

        // --- Massage ----------------------------------------------------------------------
        // Force belongs to the pressure adverb that always follows, not to the verb: a verb that
        // carried its own force produced "grinds his palms into his scalp with light pressure".
        kv(
            "v_massage",
            listOf("rubs", "works"),
            listOf("massages", "works"),
            listOf("kneads", "works over"),
            listOf("kneads", "works over")
        )
        kv(
            "adv_pressure_light",
            listOf("with light, even pressure", "with barely any weight behind it"),
            listOf("with light pressure", "with barely any weight behind it"),
            listOf("with light pressure", "with barely any weight behind it"),
            listOf("with light pressure", "with barely any weight behind it")
        )
        kv(
            "adv_pressure_firm",
            listOf("with steady, even pressure", "with his weight behind it"),
            listOf("with firm pressure", "with his weight behind it"),
            listOf("with hard pressure", "with his full weight behind it"),
            listOf("with hard, digging pressure", "with his full weight behind every pass")
        )
        kv(
            "adv_pressure_deep",
            listOf("with slow, deep pressure", "slowly, pressing in as he goes"),
            listOf("with deep pressure", "slowly, pressing in as he goes"),
            listOf("with deep, grinding pressure", "pressing in hard with each pass"),
            listOf("with deep, grinding pressure", "pressing in hard with each pass")
        )
        k("v_knead", "kneads", "kneads", "digs into", "digs into")

        // --- Anal -------------------------------------------------------------------------
        // "opens" and "presses deeper into" never said what with, which left the softest register
        // as the vaguest one. Every variant names the hand.
        kv(
            "v_finger",
            listOf("slides a finger into", "works a finger into"),
            listOf("fingers", "works a finger into"),
            listOf("fingers", "finger-fucks"),
            listOf("finger-fucks", "fingers")
        )
        k("v_finger_deep", "fingers deep into", "fingers deep into", "finger-fucks deep into", "finger-fucks hard into")
        k("v_open", "opens", "opens up", "opens up", "stretches open")
        k("v_fuck", "takes", "fucks", "fucks", "fucks")
        k("v_fuck_hard", "moves hard into", "fucks hard into", "drives hard into", "pounds")
        kv(
            "v_enter",
            listOf("eases into", "slides slowly into"),
            listOf("pushes into", "slides into"),
            listOf("pushes into", "works his way into"),
            listOf("shoves into", "drives into")
        )
        k("v_bottom_take", "takes", "takes", "takes", "takes every inch of")
        kv(
            "adv_thrust_slow",
            listOf("in long, slow strokes", "slowly, all the way in and all the way out"),
            listOf("in long, slow strokes", "slowly, all the way in and all the way out"),
            listOf("in long, slow strokes", "slowly, all the way in and all the way back out"),
            listOf("in long, deep strokes", "slowly, all the way in and all the way back out")
        )
        kv(
            "adv_thrust_hard",
            listOf("in hard, steady strokes", "in firm, even strokes", "in long, hard strokes"),
            listOf("in hard, steady strokes", "in firm, even strokes", "in long, hard strokes"),
            listOf("in hard, fast strokes", "in short, hard strokes", "fast, and all the way in each time"),
            listOf("in brutal, fast strokes", "in short, brutal strokes", "at full force, all the way in each time")
        )
        k("n_hole", "hole", "hole", "hole", "hole")

        // --- Toys -------------------------------------------------------------------------
        k("v_use_toy", "uses", "uses", "uses", "drives")
        k("v_insert", "eases in", "slides in", "pushes in", "shoves in")
        kv(
            "adv_toy_pace",
            listOf("on a low setting", "on the lowest setting it has"),
            listOf("on a low setting", "on the lowest setting it has"),
            listOf("on a high setting", "two settings short of the top"),
            listOf("on the highest setting", "on the highest setting it has")
        )

        // --- Rough play -------------------------------------------------------------------
        // Every one of these is followed straight by its object, so no variant may end in an
        // adverb or a particle: "handles roughly" produced "handles roughly Tom into three
        // positions", and "clamps" produced "clamps Dan down".
        k("v_pin", "holds", "pins", "pins", "pins")
        // "handles Beau into three positions" is not English; every variant has to survive both
        // "[v_handle] {R} into three positions" and a bare "[v_handle] him".
        k("v_handle", "moves", "moves", "manhandles", "manhandles")
        k("v_shove", "guides", "pushes", "shoves", "shoves")
        kv(
            "v_spank",
            listOf("swats", "smacks"),
            listOf("spanks", "smacks"),
            listOf("spanks", "smacks"),
            listOf("spanks", "smacks")
        )
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
        k("v_talk_dirty", "murmurs", "talks dirty", "talks dirty", "talks filth")
        // Both a transitive and an intransitive slot use this key — "[tone_whisper]
        // [n_dirty_talk] into his ear" and "[tone_whisper] into it" — so it stays a bare verb.
        k("tone_whisper", "murmurs", "whispers", "whispers", "growls")
        k("tone_moan", "breathes", "breathes hard", "moans", "moans and growls")
        k("n_dirty_talk", "quiet, explicit words", "explicit words", "crude, explicit talk", "the crudest words he knows")

        // --- Orgasm -----------------------------------------------------------------------
        // Sits inside "until he [v_come]", and an endpoint has to be a word both men can
        // point at, so this key keeps one wording per register.
        k("v_come", "finishes", "comes", "comes", "unloads")
        k("v_come_in", "finishes inside", "comes inside", "comes inside", "unloads inside")
        k("n_orgasm", "release", "orgasm", "orgasm", "load")
        k("n_load", "what he has", "what he has", "his load", "every drop of his load")
        k("v_edge", "brings him to the edge and stops", "edges him", "edges him", "edges him hard")
        k("v_hold_back", "holds back", "holds back", "holds it", "does not let himself come")

        // --- Pace, tone and general intensity ---------------------------------------------
        kv(
            "adv_pace",
            listOf("slowly", "at a slow, even speed"),
            listOf("steadily", "at one steady speed"),
            listOf("hard", "hard, at one speed"),
            listOf("hard and fast", "hard and fast, at one speed")
        )
        k("adv_slow", "unhurriedly", "slowly", "slowly", "torturously slowly")
        k("adv_hard", "firmly", "hard", "hard", "at full force")
        k("adj_wet", "slick", "wet", "wet", "sloppy")
        k("adj_intense", "intense", "intense", "rough", "brutal")
        // "torments him with a slick thumb" names an effect on the other man, not an action.
        k("v_tease", "teases", "teases", "teases", "keeps teasing")
        k("v_worship", "takes his time over", "worships", "worships", "worships every inch of")
        k("v_hold_pos", "stays where he is", "holds the position", "holds the position", "holds the position and does not move")
        k("n_endpoint", "until the timer ends", "until the timer ends", "until the timer ends", "until the timer ends")
    }

    val keys: Set<String> get() = table.keys

    fun has(key: String): Boolean = table.containsKey(key)

    /**
     * The wording for a key at a register.
     *
     * [seed] selects between a register's alternatives and is derived from the id of the item
     * being rendered, so the same term always reads the same way while different terms sharing a
     * key do not all say the same thing. Passing 0 gives the primary wording, which is what the
     * validator and the content report use.
     */
    fun resolve(key: String, explicitness: Explicitness, seed: Int = 0): String {
        val options = table[key]?.get(explicitness.level - 1)
            ?: throw IllegalArgumentException("Unknown lexicon key: [$key]")
        return options[Math.floorMod(seed, options.size)]
    }

    /** The primary rendering of each register, for validation and for the developer report. */
    fun variants(key: String): List<String> = table.getValue(key).map { it.first() }

    /** Every alternative wording of a key at one register. */
    fun alternatives(key: String, explicitness: Explicitness): List<String> =
        table.getValue(key)[explicitness.level - 1]

    /**
     * How many seeds a sweep has to try to reach every alternative of every key. Alternative
     * counts are capped at [MAX_ALTERNATIVES] and this is a multiple of every count up to it, so
     * seeding 0 until this value walks each key through all of its wordings.
     */
    const val SEED_CYCLE: Int = 12
    const val MAX_ALTERNATIVES: Int = 4
}
