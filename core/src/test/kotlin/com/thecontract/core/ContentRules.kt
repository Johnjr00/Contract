package com.thecontract.core

/**
 * What makes a line of this game's content acceptable.
 *
 * The library was originally written as erotica, and erotica earns its effect by evoking rather
 * than by specifying. An instruction earns its effect the opposite way. Three failure modes came
 * out of that mismatch, and all three are checked here against the *rendered* text, because two
 * of them only exist after `{G}`, `{R}` and `[lexicon_key]` have been substituted in:
 *
 *  1. **Lexicon damage.** A key resolves to four different strings and the author only checked
 *     one of them in the slot. `[v_pull_hair] his hair` read "fists his hair" at the top two
 *     registers; `{R} keeps [v_pull_hair] his hair` read "keeps takes a handful of his hair" at
 *     every register.
 *  2. **Judgement calls.** "just long enough for him to feel it", "until the muscle stops
 *     fighting him", "takes his time over it" — grammatical, evocative, and impossible to carry
 *     out, because the endpoint lives in someone's head.
 *  3. **Unnamed targets.** "every knot", "the spot he reacts hardest to", "from the neck down",
 *     "a position of his choosing" — the sentence tells a player to act on something without
 *     saying what it is.
 *
 * An instruction may only end on something both men can point at: a timer, a count, an orgasm,
 * a spoken cue, or a physical fact one of them can see. Anything else is a judgement call
 * wearing an instruction's clothes.
 */
object ContentRules {

    /** id → the phrase to look for, and what is wrong with it. */
    data class Rule(val pattern: Regex, val why: String)

    private fun ci(p: String) = Regex(p, RegexOption.IGNORE_CASE)

    /**
     * Degree and duration that only the other man can measure. Every one of these was written as
     * a substitute for a number, and every one of them has to become a number, a count, or a
     * named physical state.
     */
    val UNMEASURABLE: List<Rule> = listOf(
        Rule(ci("\\blong enough\\b"), "duration nobody can measure — give a count or a timer"),
        Rule(ci("\\bjust enough\\b"), "degree nobody can measure — say how far, or how many"),
        Rule(ci("enough (?:for him|to make him|to feel)"), "degree measured by the other man's reaction"),
        Rule(ci("\\buntil it hurts\\b"), "endpoint measured by the other man's reaction"),
        Rule(ci("takes his time|took his time|taking his time"), "says nothing about what to do"),
        Rule(ci("does not hurry|without hurrying"), "says nothing about what to do"),
        Rule(ci("\\bproperly\\b|\\bthoroughly\\b"), "says nothing about what to do"),
        Rule(ci("\\bas needed\\b|when he actually has to"), "leaves the instruction to the player"),
        Rule(ci("\\bworks him over\\b"), "names no body part and no action"),
        Rule(ci("without ceremony"), "says nothing about what to do")
    )

    /** Endpoints that depend on reading the other man rather than on watching the clock. */
    val JUDGEMENT_ENDPOINT: List<Rule> = listOf(
        Rule(ci("\\b(?:stops?|stopped) (?:early|first)\\b"), "\"early\" and \"first\" are not endpoints"),
        Rule(ci("\\b(?:let go|come off|comes off|let up|come out|comes out|pull off|pulls off) early\\b"),
            "\"early\" is not an endpoint"),
        Rule(ci("for anything\\b|for any reason\\b"), "not an endpoint — say what the endpoint is"),
        Rule(ci("until he is spoken to"), "endpoint with no cue anybody agreed on"),
        Rule(ci("leaves him hanging"), "not an instruction"),
        Rule(ci("\\bease(?:s)? off\\b"), "vague — say what changes, or that nothing does")
    )

    /** A target the sentence never names. */
    val UNNAMED_TARGET: List<Rule> = listOf(
        Rule(ci("\\b(?:each|every|any) (?:knot|tight spot)\\b"), "a knot is not a place either man can point at"),
        Rule(ci("reacts hardest|reacted hardest"), "target chosen by reading the other man"),
        Rule(ci("somewhere new|a new spot|to a new spot"), "says where not to go, not where to go"),
        Rule(ci("\\bsomething new\\b"), "no action named"),
        Rule(ci("from the neck down"), "half a body is not a target — name the parts"),
        Rule(ci("of his choosing"), "leaves the instruction to the player"),
        Rule(ci("where he asks for it|at the setting he asks for|what he asks for(?!\\.)"),
            "leaves the instruction to the player")
    )

    /** Manner clauses that describe an intended effect rather than an action. */
    val EFFECT_NOT_ACTION: List<Rule> = listOf(
        Rule(ci("never settle|can never settle|cannot settle"), "an effect, not an action"),
        Rule(ci("get(?:s)? used to it|cannot get used to"), "an effect, not an action"),
        Rule(ci("brace for it|be braced for|braced for it"), "an effect, not an action"),
        Rule(ci("on purpose|deliberate(?:ly)?"), "an effect, not an action"),
        Rule(ci("\\bgreedy\\b"), "an effect, not an action"),
        Rule(ci("no pattern|uneven timing|uneven gaps|irregular gaps"),
            "say the range of gaps, not that they are uneven"),
        Rule(ci("turns (?:it|the whole thing) sexual"), "names no body part and no action"),
        Rule(ci("using teeth"), "says nothing about where, or how many times")
    )

    /**
     * Lexicon substitution damage. These only exist in the rendered string: the template reads
     * `[v_pull_hair] his hair`, and only one of the four registers turns that into English.
     */
    val SUBSTITUTION_DAMAGE: List<Rule> = listOf(
        Rule(ci("\\bfists (?:his|him|it|the)\\b"), "\"fist\" is not something a hand does to hair"),
        Rule(
            ci("\\b(?:keeps|starts|stops|begins) (?:strokes|sucks|kisses|licks|rims|grips|pulls|" +
                "pushes|takes|holds|works|spanks|edges|teases|fucks|kneads|massages|rubs|runs)\\b"),
            "a conjugated verb after keeps/starts/stops needs to be a gerund or a noun"
        ),
        Rule(
            ci("\\bto (?:strokes|sucks|kisses|licks|rims|grips|pulls|pushes|takes|holds|works|" +
                "spanks|edges|teases|fucks|kneads|massages|rubs)\\b"),
            "conjugated verb straight after \"to\""
        ),
        Rule(ci("\\bhas him \\w+s\\b"), "the verb after \"has him\" has to be bare"),
        Rule(ci("\\b\\w+'s the \\b"), "possessive followed by \"the\""),
        Rule(
            ci("(?:grinds his palms into|presses hard into|digs into|kneads) [^.]{0,90}?\\bwith light"),
            "a hard verb contradicting the light-pressure phrase after it"
        ),
        /**
         * Two `with` phrases stacked with nothing between them, which happens whenever a template
         * writes "with #lubricant#" next to an adverb key that is itself a "with …" phrase:
         * "strokes Ben with lube with a light hold".
         */
        Rule(
            // Both phrases have to hang off the same verb, so a conjunction between them means
            // they belong to different clauses and the sentence is fine.
            ci("\\bwith (?:(?!\\b(?:and|then)\\b)[^,.;]){2,30} with (?:a |an |the |plenty|light|firm|deep|hard|steady|slow)"),
            "two \"with\" phrases stacked on one verb — reorder so only one follows it"
        ),
        /**
         * A preposition left in front of an adverbial key that already starts with one:
         * "builds to in hard, fast strokes". "then in hard, fast strokes" is correct and is not
         * caught, because there the phrase heads its own clause.
         */
        Rule(ci("\\b(?:to|into|onto) in (?:long|hard|brutal|short|slow)\\b"),
            "stacked prepositions in front of a stroke-pace phrase"),
        /** A verb followed by an adverb before its object: "handles roughly Tom into three positions". */
        Rule(
            Regex(
                "\\b(?:handles|pins|spanks|holds|grips|strokes|fucks) " +
                    "(?:roughly|hard|slowly|firmly|brutally) (?:Marcus|Dan|[A-Z][a-z]+'s?\\b)"
            ),
            "adverb sitting between the verb and its object"
        )
    )

    /**
     * Plural nouns after "one of"/"both of"/"each of", and singular verbs after two subjects.
     * Both only appear once the names are substituted in.
     */
    val AGREEMENT: List<Rule> = listOf(
        Rule(
            ci(
                "\\b(?:one|both|each) of \\w+(?:'s)? " +
                    "(?:ear|hand|arm|leg|thigh|cheek|nipple|foot|calf|wrist|shoulder|knee)\\b"
            ),
            "singular noun after one of/both of/each of"
        ),
        Rule(
            Regex("\\b(?:Marcus|Dan) and (?:Marcus|Dan)\\b[^.]*?\\b\\w+s each other\\b"),
            "singular verb with two subjects"
        )
    )

    val ALL: List<Rule> =
        UNMEASURABLE + JUDGEMENT_ENDPOINT + UNNAMED_TARGET + EFFECT_NOT_ACTION +
            SUBSTITUTION_DAMAGE + AGREEMENT

    /**
     * Endpoints an instruction is allowed to finish on: the clock, a count, an orgasm, a spoken
     * cue, or a plain physical fact. `until …` anything else is one man guessing how the other
     * one feels.
     */
    private val OBJECTIVE_UNTIL = ci(
        "timer|finish|\\bcomes\\b|unloads|empties|is done|\\bdoes\\b|" +
            "tells him|is told|\\bsays\\b|permission|" +
            "\\bmoved\\b|all the way in|\\bcount\\b|slick from|slick inside|" +
            "\\bhard\\b|the line is gone|he is upright|the count is finished"
    )

    /** The `until …` clause, if it ends on a judgement call rather than on an observable fact. */
    fun vagueUntil(text: String): String? =
        Regex("until ([^.,;]{3,70})").findAll(text)
            .map { it.groupValues[1].trim() }
            .firstOrNull { !OBJECTIVE_UNTIL.containsMatchIn(it) }

    /** A pronoun standing in for someone the sentence has not named yet. */
    fun danglingPronoun(text: String, names: List<String>): Boolean {
        val nameRe = Regex("\\b(${names.joinToString("|")})\\b")
        val firstName = nameRe.find(text)?.range?.first ?: return true
        val firstPronoun = Regex("\\b(he|him|his)\\b", RegexOption.IGNORE_CASE)
            .find(text)?.range?.first ?: return false
        return firstPronoun < firstName
    }

    /**
     * Everything wrong with one rendered instruction. Empty means the line is fit to put in front
     * of two people who have to act on it without asking each other what it meant.
     */
    fun problems(text: String, names: List<String>): List<String> = buildList {
        if (danglingPronoun(text, names)) add("pronoun before any name")
        if (Regex("\\b(\\w+) \\1\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) add("doubled word")
        if (text.contains("  ")) add("double space")
        if (!text.trimEnd().endsWith(".")) add("no full stop")
        vagueUntil(text)?.let { add("judgement-call endpoint: \"until $it\"") }
        ALL.forEach { rule ->
            rule.pattern.find(text)?.let { add("\"${it.value}\" — ${rule.why}") }
        }
    }
}
