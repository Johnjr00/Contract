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
        Rule(ci("without ceremony"), "says nothing about what to do"),
        /**
         * "and takes it" is the whole family: enduring something is not an instruction, because
         * it tells a man nothing to do with his hands, his mouth or his weight. Every one of
         * these is now written as the thing he actually holds still, seals or does not move.
         */
        Rule(ci("\\btakes? (?:it|all of it)\\b"), "enduring is not an action — say what he does"),
        Rule(ci("takes what he is given"), "enduring is not an action — say what he does"),
        Rule(ci("cannot do a thing about it"), "an effect, not an action"),
        Rule(ci("no cheating|no arguing|no exceptions"), "an assertion, not an instruction"),
        Rule(ci("leaving him there"), "says nothing about what to do"),
        Rule(ci("cannot separate them"), "an effect, not an action"),
        Rule(ci("soften(?:s|ing)? it"), "vague — say what he keeps doing instead"),
        Rule(ci("easing off"), "vague — say what changes, or that nothing does"),
        Rule(ci("does not skip anything"), "vague — name what may not be skipped"),
        Rule(ci("does not assume"), "vague — say what he waits for")
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
        Rule(ci("never (?:gets? to )?settle|cannot settle"), "an effect, not an action"),
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
        /**
         * The hand named twice in one clause. A lexicon verb may not carry an instrument any
         * more than an object: `[v_stroke]` resolving to "runs his hand over" produced "while
         * his hand runs his hand over the rest" and "runs his hand over the base with his hand".
         */
        Rule(
            ci("\\b(?:his |one |the other )?hands?\\b[^.]{0,45}?\\bwith (?:his|the other) hand\\b"),
            "the hand is named twice in one clause"
        ),
        Rule(ci("\\bhis hand\\b[^.,;]{0,40}\\bhis hand\\b"), "the hand is named twice in one clause"),
        /** Both hands used by the hand that is not busy: "uses his hands on him with the other". */
        Rule(ci("\\bhis hands\\b[^.]{0,40}\\bwith the other\\b"), "one hand is already busy, so it cannot be both"),
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
        /**
         * Lexicon verbs are third-person singular, so dropping one after "{G} and {R}" produces
         * "Marcus and Dan lie head to toe and sucks each other". Matching any `\w+s` here caught
         * "kiss each other's necks" as well, which is correct English — the verb list is explicit
         * so that a plural whose base form happens to end in s is left alone.
         */
        Rule(
            Regex(
                "\\b(?:Marcus|Dan) and (?:Marcus|Dan)\\b[^.]*?\\b(?:sucks|kisses|licks|rims|" +
                    "strokes|jerks|works|holds|grips|fingers|finger-fucks|tongues|eats|edges|" +
                    "teases|fucks|spanks|smacks|kneads|massages|rubs|pins|takes|pushes|pulls) " +
                    "each other\\b"
            ),
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
     * "him" with nobody for it to be.
     *
     * `{G} [v_edge] three times with his hand` rendered "Jem edges him three times with his hand":
     * grammatical, and the only man named is the one doing it, so the man it is being done to is
     * never identified. It slips past [danglingPronoun] because the name does come first. An
     * instruction that acts on "him" has to name both men.
     */
    fun objectWithNoOwner(text: String, names: List<String>): Boolean {
        if (!Regex("\\bhim\\b|\\bhimself\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) return false
        if (Regex("\\bhimself\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) return false
        val named = names.count { Regex("\\b$it\\b").containsMatchIn(text) }
        return named < 2
    }

    /**
     * Words that narrow a plural subject back down to one man, so that "he" and "his" after them
     * are correct. "each other" is deliberately not one of them — it is what two men do to each
     * other, and treating it as singular is what let the defect below through.
     */
    private val SINGULARISER =
        ci("\\beach\\b(?!\\s+other)|\\bone\\b|\\bthe other\\b|\\bwhoever\\b|\\bneither\\b|\\beither\\b")

    /**
     * "he" or "his" in a clause whose subject is both men.
     *
     * Reciprocal content is the only place this can happen, and it is invisible in the source:
     * the templates say `{G} and {R}`, and the singular pronoun arrives from a lexicon wording
     * that is perfectly correct after one man — "suck each other as fast and as hard as *he* can
     * keep up", "work each other's inner thighs with *his* full weight behind it". A clause that
     * has already named one of them ("each one", "the other", "whoever is face down") is fine.
     */
    fun pluralSubjectPronoun(text: String, names: List<String>): String? {
        val bothMen = Regex("\\b(${names.joinToString("|")}) and (${names.joinToString("|")})\\b")
        // What makes a later "he" legitimate: one of the two named again as a new subject, or a
        // word that picks one of them out.
        val establishesOne = Regex(
            "\\b(?:${names.joinToString("|")})\\b|" + SINGULARISER.pattern,
            RegexOption.IGNORE_CASE
        )
        val singular = Regex("\\b(?:he|his)\\b", RegexOption.IGNORE_CASE)
        for (sentence in text.split(Regex("(?<=[.!?])\\s+"))) {
            val pair = bothMen.find(sentence) ?: continue
            val rest = sentence.substring(pair.range.last + 1)
            val pronoun = singular.find(rest) ?: continue
            val established = establishesOne.find(rest)?.range?.first ?: Int.MAX_VALUE
            if (pronoun.range.first < established) {
                return rest.substring(pronoun.range.first).trim().take(60)
            }
        }
        return null
    }

    /**
     * Everything wrong with one rendered instruction. Empty means the line is fit to put in front
     * of two people who have to act on it without asking each other what it meant.
     */
    fun problems(text: String, names: List<String>): List<String> = buildList {
        if (danglingPronoun(text, names)) add("pronoun before any name")
        if (objectWithNoOwner(text, names)) add("\"him\" with only one man named")
        pluralSubjectPronoun(text, names)?.let { add("singular pronoun with two subjects: \"$it\"") }
        if (Regex("\\b(\\w+) \\1\\b", RegexOption.IGNORE_CASE).containsMatchIn(text)) add("doubled word")
        if (text.contains("  ")) add("double space")
        if (!text.trimEnd().endsWith(".")) add("no full stop")
        vagueUntil(text)?.let { add("judgement-call endpoint: \"until $it\"") }
        ALL.forEach { rule ->
            rule.pattern.find(text)?.let { add("\"${it.value}\" — ${rule.why}") }
        }
    }
}
