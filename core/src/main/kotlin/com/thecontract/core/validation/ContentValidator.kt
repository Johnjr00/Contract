package com.thecontract.core.validation

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.Eligibility
import com.thecontract.core.engine.EligibilityEngine
import com.thecontract.core.engine.GameContext
import com.thecontract.core.engine.Renderer
import com.thecontract.core.model.Answer
import com.thecontract.core.model.BenefitParty
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PlayerSetup
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.PrivateProfile
import com.thecontract.core.model.Role
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import com.thecontract.core.model.Term
import com.thecontract.core.style.Lexicon
import com.thecontract.core.style.StyleEngine

/**
 * Development-time content validation (section 44).
 *
 * Runs against the whole authored library at every explicitness level and every legal binding.
 * The build fails if anything here reports a problem, so a malformed sentence, a dangling
 * preference id or a leaked unavailable item cannot reach a player.
 */
object ContentValidator {

    const val MIN_TERMS = 202
    const val MIN_CONSIDERATIONS = 120
    const val MIN_PREFERENCES = 132

    /** How many lines are suggested for an instruction that tells a man to talk. */
    const val EXAMPLE_COUNT = 5
    val MIN_PER_LEVEL = mapOf(1 to 42, 2 to 40, 3 to 40, 4 to 40, 5 to 40)

    private val PLAYER_A = "Alex"
    private val PLAYER_B = "Chris"

    /** Content the game must never generate (section 43). */
    private val forbidden = listOf(
        "chok", "strangl", "asphyx", "breath play", "breath restriction", "breath control",
        "neck compression", "hands around his throat", "grip his throat", "throat grip",
        "unconscious", "black out", "punch", "headbutt", "slap his face", "hit his head",
        "drunk", "intoxicat", "drugged", "passed out",
        "underage", "minor", "child", "teenager", "schoolboy",
        "leave him tied alone", "leaves him tied alone", "leave him restrained alone",
        "in public", "strangers watch", "in front of strangers"
    )

    /** No female anatomy or gendered assumptions: both players are men. */
    private val femaleTerms = Regex(
        """\b(she|her|hers|herself|vagina|pussy|clitoris|clit|vulva|womb|breasts?|girl|girls|woman|women|wife|girlfriend|lady)\b""",
        RegexOption.IGNORE_CASE
    )

    private val placeholderPattern = Regex("""[\[\]{}#]|\bTODO\b|\bTBD\b|\bXXX\b|%s|<[a-z_]+>""")
    private val doubleWord = Regex("""\b(\w+)\s+\1\b""", RegexOption.IGNORE_CASE)
    private val badSpacing = Regex("""\s[,.;:]|\s\s""")

    data class Problem(val where: String, val what: String)

    fun validate(): List<Problem> {
        val problems = mutableListOf<Problem>()
        problems += validateCounts()
        problems += validateIds()
        problems += validateReferences()
        problems += validateTimers()
        problems += validateBenefits()
        problems += validateClosingTerms()
        problems += validateRoleReferences()
        problems += validateRenderedText()
        problems += validateEquipmentGating()
        problems += validateLexicon()
        problems += validateSpokenExamples()
        problems += validateSuggestionsMatchTheInstruction()
        problems += validateOralNamesTheMouth()
        problems += validatePenetrationNamesTheInstrument()
        return problems
    }

    // ------------------------------------------------------------------ counts

    private fun validateCounts(): List<Problem> = buildList {
        val terms = ContentLibrary.regularTerms.size
        if (terms < MIN_TERMS) add(Problem("library", "only $terms proposed terms, need at least $MIN_TERMS"))
        val cons = ContentLibrary.considerations.size
        if (cons < MIN_CONSIDERATIONS) {
            add(Problem("library", "only $cons consideration actions, need at least $MIN_CONSIDERATIONS"))
        }
        val prefs = ContentLibrary.preferences.size
        if (prefs < MIN_PREFERENCES) {
            add(Problem("library", "only $prefs preferences, need at least $MIN_PREFERENCES"))
        }
        MIN_PER_LEVEL.forEach { (level, min) ->
            val n = ContentLibrary.regularTermsAtLevel(level).size
            if (n < min) add(Problem("level $level", "only $n terms, need at least $min"))
        }
        val levels = ContentLibrary.regularTerms.map { it.level }.distinct().sorted()
        if (levels != listOf(1, 2, 3, 4, 5)) add(Problem("library", "intensity levels are $levels, expected 1..5"))
        if (ContentLibrary.climaxTerms.size < 8) {
            add(Problem("library", "too few closing terms to cover restrictive profiles"))
        }
    }

    private fun validateIds(): List<Problem> = buildList {
        val termDupes = ContentLibrary.allTerms.groupBy { it.id }.filterValues { it.size > 1 }.keys
        termDupes.forEach { add(Problem("term $it", "duplicate term id")) }
        val consDupes = ContentLibrary.considerations.groupBy { it.id }.filterValues { it.size > 1 }.keys
        consDupes.forEach { add(Problem("consideration $it", "duplicate consideration id")) }
        val prefDupes = ContentLibrary.preferences.groupBy { it.id }.filterValues { it.size > 1 }.keys
        prefDupes.forEach { add(Problem("preference $it", "duplicate preference id")) }
    }

    // ------------------------------------------------------------------ references

    private fun validateReferences(): List<Problem> = buildList {
        for (term in ContentLibrary.allTerms) {
            for (activity in term.activities) {
                if (PreferenceLibrary.byId[PreferenceLibrary.give(activity)] == null) {
                    add(Problem("term ${term.id}", "activity '$activity' has no giving preference"))
                }
                if (PreferenceLibrary.byId[PreferenceLibrary.receive(activity)] == null) {
                    add(Problem("term ${term.id}", "activity '$activity' has no receiving preference"))
                }
            }
            (term.extraGiverPrefs + term.extraReceiverPrefs).forEach { pref ->
                if (PreferenceLibrary.byId[pref] == null) {
                    add(Problem("term ${term.id}", "unknown preference id '$pref'"))
                }
            }
            term.blockedBy.forEach { b ->
                if (Boundary.byId(b.id) == null) add(Problem("term ${term.id}", "unknown boundary ${b.id}"))
            }
            term.requiredEquipment.forEach { e ->
                if (Equipment.byId(e.id) == null) add(Problem("term ${term.id}", "unknown equipment ${e.id}"))
            }
            // Equipment named in the text must be declared, or eligibility cannot gate it.
            Regex("""#([A-Za-z0-9_]+)#""").findAll(term.base + " " + term.explicit + " " + (term.extremeTail ?: ""))
                .map { it.groupValues[1] }
                .distinct()
                .forEach { id ->
                    if (id == "TOY") {
                        if (!term.genericToy) add(Problem("term ${term.id}", "uses #TOY# but genericToy is false"))
                    } else {
                        val eq = Equipment.byId(id)
                        when {
                            eq == null -> add(Problem("term ${term.id}", "unknown equipment token #$id#"))
                            eq !in term.requiredEquipment ->
                                add(Problem("term ${term.id}", "text uses #$id# but does not require it"))
                        }
                    }
                }
        }
        for (action in ContentLibrary.considerations) {
            for (activity in action.activities) {
                if (PreferenceLibrary.byId[PreferenceLibrary.give(activity)] == null ||
                    PreferenceLibrary.byId[PreferenceLibrary.receive(activity)] == null
                ) {
                    add(Problem("consideration ${action.id}", "activity '$activity' has no preference pair"))
                }
            }
            (action.extraPerformerPrefs + action.extraRecipientPrefs).forEach { pref ->
                if (PreferenceLibrary.byId[pref] == null) {
                    add(Problem("consideration ${action.id}", "unknown preference id '$pref'"))
                }
            }
            Regex("""#([A-Za-z0-9_]+)#""").findAll(action.base + " " + action.explicit + " " + (action.extremeTail ?: ""))
                .map { it.groupValues[1] }
                .distinct()
                .forEach { id ->
                    if (id == "TOY") {
                        if (!action.genericToy) {
                            add(Problem("consideration ${action.id}", "uses #TOY# but genericToy is false"))
                        }
                    } else {
                        val eq = Equipment.byId(id)
                        when {
                            eq == null -> add(Problem("consideration ${action.id}", "unknown equipment token #$id#"))
                            eq !in action.requiredEquipment ->
                                add(Problem("consideration ${action.id}", "text uses #$id# but does not require it"))
                        }
                    }
                }
        }
    }

    private fun validateTimers(): List<Problem> = buildList {
        for (term in ContentLibrary.allTerms) {
            if (term.timers.isEmpty()) add(Problem("term ${term.id}", "has no timed segment"))
            term.timers.forEach { if (it.seconds <= 0) add(Problem("term ${term.id}", "timer '${it.label}' is not positive")) }
        }
        for (action in ContentLibrary.considerations) {
            if (action.timers.isEmpty()) add(Problem("consideration ${action.id}", "has no timed segment"))
            action.timers.forEach {
                if (it.seconds <= 0) add(Problem("consideration ${action.id}", "timer '${it.label}' is not positive"))
            }
        }
    }

    private fun validateBenefits(): List<Problem> = buildList {
        for (term in ContentLibrary.allTerms) {
            val mutualParty = term.benefitParty == BenefitParty.MUTUAL
            val mutualType = term.benefitType == BenefitType.MUTUAL
            if (mutualParty != mutualType) {
                add(Problem("term ${term.id}", "benefit party and benefit type disagree about being mutual"))
            }
            // Balanced benefit and "both men perform it" are different claims, and only one
            // implies the other. A term where the submissive holds a position, keeps silent or
            // obeys an order is carried out by one man and benefits neither, because being the
            // one giving the orders is not a benefit.
            if (!mutualParty && term.mutual) {
                add(Problem("term ${term.id}", "marked mutual but names a single beneficiary"))
            }
        }
    }

    private fun validateClosingTerms(): List<Problem> = buildList {
        for (term in ContentLibrary.climaxTerms) {
            if (!term.climax) add(Problem("term ${term.id}", "in the climax library but not marked climax"))
            if (term.benefitParty != BenefitParty.RECEIVER) {
                add(Problem("term ${term.id}", "a closing term must benefit its receiver, who is the finisher"))
            }
            val text = term.base + " " + term.explicit
            val hasFallback = text.contains("if the timer", ignoreCase = true) ||
                text.contains("until he finishes", ignoreCase = true) ||
                text.contains("until he is done", ignoreCase = true)
            if (!hasFallback) {
                add(Problem("term ${term.id}", "closing term has no explicit continuation if the timer runs out"))
            }
        }
        for (term in ContentLibrary.regularTerms) {
            if (term.climax) add(Problem("term ${term.id}", "regular term is marked climax"))
        }
    }

    private fun validateRoleReferences(): List<Problem> = buildList {
        for (term in ContentLibrary.allTerms) {
            val g = term.giverConstraint
            val r = term.receiverConstraint
            if (g != PartyConstraint.ANY && g == r) {
                add(Problem("term ${term.id}", "both parties constrained to $g, which can never bind"))
            }
            if (g == PartyConstraint.DOMINANT && r == PartyConstraint.TOP) {
                add(Problem("term ${term.id}", "mixes role and anal-role constraints in a way that may not bind"))
            }
            val usesDomToken = (term.base + term.explicit).contains("{DOM")
            val usesSubToken = (term.base + term.explicit).contains("{SUB")
            if ((usesDomToken || usesSubToken) &&
                g == PartyConstraint.ANY && r == PartyConstraint.ANY &&
                !usesDomToken.and(usesSubToken)
            ) {
                add(Problem("term ${term.id}", "names a role in the text without constraining the binding"))
            }
        }
    }

    // ------------------------------------------------------------------ rendering

    private fun fullContext(
        p1Anal: com.thecontract.core.model.AnalRole = com.thecontract.core.model.AnalRole.VERS,
        p2Anal: com.thecontract.core.model.AnalRole = com.thecontract.core.model.AnalRole.VERS,
        equipment: Set<Equipment> = Equipment.entries.toSet(),
        explicitness: Explicitness = Explicitness.DIRECT
    ): GameContext {
        val setup = SharedSetup(
            player1 = PlayerSetup(PLAYER_A, Role.DOMINANT, p1Anal, false),
            player2 = PlayerSetup(PLAYER_B, Role.SUBMISSIVE, p2Anal, false),
            explicitness = explicitness,
            boundaries = emptySet(),
            equipment = equipment
        )
        val answers = PreferenceLibrary.all.associate { it.id to Answer.YES }
        return GameContext(
            setup,
            Slot.entries.associateWith { PrivateProfile(it, answers, complete = true) }
        )
    }

    private fun validateRenderedText(): List<Problem> = buildList {
        for (explicitness in Explicitness.entries) {
            val ctx = fullContext(explicitness = explicitness)
            for (term in ContentLibrary.allTerms) {
                for (binding in EligibilityEngine.candidateBindings(term, ctx)) {
                    val rendered = runCatching { Renderer.renderTerm(term, binding, ctx) }.getOrElse { e ->
                        add(Problem("term ${term.id}/$explicitness", "render failed: ${e.message}"))
                        continue
                    }
                    checkTitle(this, "term ${term.id}/$explicitness title", rendered.title)
                    checkText(this, "term ${term.id}/$explicitness", rendered.instruction)
                    checkText(this, "term ${term.id}/$explicitness", rendered.benefitExplanation)
                    rendered.timers.forEach { checkLabel(this, "term ${term.id} timer", it.label) }
                }
            }
            for (action in ContentLibrary.considerations) {
                val rendered = runCatching {
                    Renderer.renderConsideration(action, Slot.PLAYER_1, Slot.PLAYER_2, ctx)
                }.getOrElse { e ->
                    add(Problem("consideration ${action.id}/$explicitness", "render failed: ${e.message}"))
                    continue
                }
                checkTitle(this, "consideration ${action.id}/$explicitness title", rendered.title)
                checkText(this, "consideration ${action.id}/$explicitness", rendered.instruction)
                rendered.timers.forEach { checkLabel(this, "consideration ${action.id} timer", it.label) }
            }
        }
    }

    /** Titles and timer labels are short labels, not sentences. */
    private fun checkTitle(out: MutableList<Problem>, where: String, text: String) {
        if (text.isBlank()) out += Problem(where, "empty title")
        checkLabel(out, where, text)
        forbidden.firstOrNull { text.contains(it, ignoreCase = true) }?.let {
            out += Problem(where, "forbidden content '$it' in title '$text'")
        }
    }

    private fun checkLabel(out: MutableList<Problem>, where: String, text: String) {
        if (placeholderPattern.containsMatchIn(text)) out += Problem(where, "unresolved placeholder in '$text'")
        if (femaleTerms.containsMatchIn(text)) out += Problem(where, "female or gendered wording in '$text'")
    }

    private fun checkText(out: MutableList<Problem>, where: String, text: String) {
        if (text.isBlank()) {
            out += Problem(where, "empty text")
            return
        }
        if (placeholderPattern.containsMatchIn(text)) {
            out += Problem(where, "unresolved placeholder: '${text.take(90)}'")
        }
        forbidden.firstOrNull { text.contains(it, ignoreCase = true) }?.let {
            out += Problem(where, "forbidden content '$it' in '${text.take(90)}'")
        }
        femaleTerms.find(text)?.let {
            out += Problem(where, "female or gendered wording '${it.value}' in '${text.take(90)}'")
        }
        doubleWord.find(text)?.let {
            out += Problem(where, "repeated word '${it.value}' in '${text.take(90)}'")
        }
        badSpacing.find(text)?.let {
            out += Problem(where, "bad spacing before punctuation in '${text.take(90)}'")
        }
        if (text.contains("'s's") || text.contains("s's's")) {
            out += Problem(where, "malformed possessive in '${text.take(90)}'")
        }
        if (text.last() !in ".!?") out += Problem(where, "does not end with sentence punctuation")
        checkSentenceCase(out, where, text)
    }

    /**
     * Mid-sentence capitalisation. Player names are legitimately capitalised anywhere; anything
     * else starting with a capital in the middle of a sentence is a template seam.
     */
    private fun checkSentenceCase(out: MutableList<Problem>, where: String, text: String) {
        val allowed = setOf(PLAYER_A, PLAYER_B, "I", "Yes", "No", "Maybe")
        var newSentence = true
        for (raw in text.split(' ')) {
            val word = raw.trim('(', ')', '"', '\'')
            if (word.isEmpty()) continue
            val core = word.trimEnd('.', ',', ';', ':', '!', '?').removeSuffix("'s").removeSuffix("'")
            if (!newSentence && core.isNotEmpty() && core[0].isUpperCase() && core !in allowed) {
                out += Problem(where, "mid-sentence capital '$core' in '${text.take(90)}'")
            }
            newSentence = word.endsWith('.') || word.endsWith('!') || word.endsWith('?')
        }
    }

    // ------------------------------------------------------------------ gating

    /**
     * Nothing that names an item may survive eligibility when the couple does not own it, and
     * nothing that needs a toy may survive the No-toys boundary. This is what stops fallback
     * code from ever inventing a vibrator.
     */
    private fun validateEquipmentGating(): List<Problem> = buildList {
        for (term in ContentLibrary.allTerms) {
            for (required in term.requiredEquipment) {
                val ctx = fullContext(equipment = Equipment.entries.toSet() - required)
                if (EligibilityEngine.evaluate(term, ctx) is Eligibility.Ok) {
                    add(Problem("term ${term.id}", "still eligible without ${required.id}"))
                }
            }
            if (term.genericToy) {
                val ctx = fullContext(equipment = Equipment.entries.toSet() - Equipment.toys.toSet())
                if (EligibilityEngine.evaluate(term, ctx) is Eligibility.Ok) {
                    add(Problem("term ${term.id}", "needs a toy but is eligible with no toys available"))
                }
            }
            if (term.requiredEquipment.any { it.toyLike } || term.genericToy) {
                val ctx = fullContext().let { c ->
                    c.copy(setup = c.setup.copy(boundaries = setOf(Boundary.NO_TOYS)))
                }
                if (EligibilityEngine.evaluate(term, ctx) !is Eligibility.BlockedByBoundary) {
                    add(Problem("term ${term.id}", "uses a toy but is not blocked by the No-toys boundary"))
                }
            }
        }
        for (action in ContentLibrary.considerations) {
            for (required in action.requiredEquipment) {
                val ctx = fullContext(equipment = Equipment.entries.toSet() - required)
                if (EligibilityEngine.evaluateConsideration(action, Slot.PLAYER_1, Slot.PLAYER_2, ctx) is Eligibility.Ok) {
                    add(Problem("consideration ${action.id}", "still eligible without ${required.id}"))
                }
            }
            if (action.genericToy) {
                val ctx = fullContext(equipment = Equipment.entries.toSet() - Equipment.toys.toSet())
                if (EligibilityEngine.evaluateConsideration(action, Slot.PLAYER_1, Slot.PLAYER_2, ctx) is Eligibility.Ok) {
                    add(Problem("consideration ${action.id}", "needs a toy but is eligible with no toys available"))
                }
            }
        }
    }

    /**
     * Suggested lines for an instruction that tells a man to talk.
     *
     * Five is the count, and both registers carry their own five, because a measured line in an
     * Extremely-filthy game reads as somebody else's script. They are suggestions, so nothing
     * here checks that a player uses them — only that what he is shown is complete, is spoken
     * English rather than a stage direction, and does not name a player the other man is not.
     */
    private fun validateSpokenExamples(): List<Problem> = buildList {
        fun check(where: String, base: List<String>, explicit: List<String>) {
            if (base.isEmpty() && explicit.isEmpty()) return
            if (base.size != EXAMPLE_COUNT) add(Problem(where, "has ${base.size} suggested lines, needs $EXAMPLE_COUNT"))
            if (explicit.size != EXAMPLE_COUNT) {
                add(Problem(where, "has ${explicit.size} coarse suggested lines, needs $EXAMPLE_COUNT"))
            }
            (base + explicit).forEach { line ->
                if (line.isBlank()) add(Problem(where, "blank suggested line"))
                if (line.trim() != line) add(Problem(where, "suggested line has stray whitespace: '$line'"))
                if (line.isNotEmpty() && line.last() !in ".!?") {
                    add(Problem(where, "suggested line does not end with sentence punctuation: '$line'"))
                }
                if (femaleTerms.containsMatchIn(line)) add(Problem(where, "gendered wording in '$line'"))
                if (placeholderPattern.containsMatchIn(line.replace(Regex("""\{[A-Z+]+\}"""), ""))) {
                    add(Problem(where, "unresolved placeholder in '$line'"))
                }
            }
            if (base.distinct().size != base.size) add(Problem(where, "repeats a suggested line"))
            if (explicit.distinct().size != explicit.size) add(Problem(where, "repeats a coarse suggested line"))
        }
        fun checkPositions(where: String, positions: List<String>) {
            if (positions.isEmpty()) return
            if (positions.size != EXAMPLE_COUNT) {
                add(Problem(where, "suggests ${positions.size} positions, needs $EXAMPLE_COUNT"))
            }
            if (positions.distinct().size != positions.size) add(Problem(where, "repeats a suggested position"))
            positions.forEach { line ->
                if (line.isBlank()) add(Problem(where, "blank suggested position"))
                if (line.trim() != line) add(Problem(where, "suggested position has stray whitespace: '$line'"))
                if (line.isNotEmpty() && line.last() !in ".!?") {
                    add(Problem(where, "suggested position does not end with sentence punctuation: '$line'"))
                }
                if (femaleTerms.containsMatchIn(line)) add(Problem(where, "gendered wording in '$line'"))
            }
        }
        ContentLibrary.allTerms.forEach {
            check("term ${it.id}", it.examples, it.examplesExplicit)
            checkPositions("term ${it.id}", it.positions)
        }
        ContentLibrary.considerations.forEach {
            check("consideration ${it.id}", it.examples, it.examplesExplicit)
            checkPositions("consideration ${it.id}", it.positions)
        }
    }

    /**
     * Penetration has to name what does the penetrating.
     *
     * Same failure as the oral one: "eases into him with lube" reads as any of a cock, a finger
     * or a toy, and a player is entitled to know which before he acts on it.
     */
    private fun validatePenetrationNamesTheInstrument(): List<Problem> = buildList {
        val namesInstrument = Regex(
            "\\b(?:cock|fingers?|finger-fuck\\w*|hand|fist|dildo|plug|toy|vibrator|wand|sleeve|" +
                "tongue|fucks?|fucking|fucked|pounds?|rides?|riding|inch)",
            RegexOption.IGNORE_CASE
        )
        fun check(where: String, text: String) {
            if (!namesInstrument.containsMatchIn(text)) {
                add(Problem(where, "penetration that never names what goes in: '${text.take(110)}'"))
            }
        }
        for (explicitness in Explicitness.entries) {
            val ctx = fullContext(explicitness = explicitness)
            val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
            ContentLibrary.allTerms.filter { it.analPenetration }
                .forEach { check("term ${it.id} [$explicitness]", Renderer.renderTerm(it, binding, ctx).instruction) }
            ContentLibrary.considerations.filter { it.analPenetration }
                .forEach {
                    check(
                        "consideration ${it.id} [$explicitness]",
                        Renderer.renderConsideration(it, Slot.PLAYER_1, Slot.PLAYER_2, ctx).instruction
                    )
                }
        }
    }

    /**
     * A suggestion list only belongs on an item whose instruction asks for it, in every register.
     *
     * Two items once carried suggested lines in registers that never told anybody to talk — the
     * collar term only said so in its coarse template, and the closing term only in its
     * Extremely-filthy tail — so a player saw five things to say and no instruction to say them.
     */
    private fun validateSuggestionsMatchTheInstruction(): List<Problem> = buildList {
        val asksForSpeech = Regex(
            "\\[v_talk_dirty\\]|\\[v_praise\\]|\\[v_own\\]|\\[n_dirty_talk\\]|\\[tone_whisper\\]|" +
                "\\bout loud\\b|\\btells him\\b|\\btalks\\b|\\bspelling out\\b|\\bdescrib\\w+|" +
                "\\bnames (?:one thing|what|each place|a part)|\\ba direct question\\b",
            RegexOption.IGNORE_CASE
        )
        val asksForPosition = Regex("\\bposition", RegexOption.IGNORE_CASE)
        fun check(where: String, base: String, explicit: String, speech: Boolean, positions: Boolean) {
            if (speech) {
                listOf("measured" to base, "coarse" to explicit).forEach { (which, text) ->
                    if (!asksForSpeech.containsMatchIn(text)) {
                        add(Problem(where, "suggests lines to say, but the $which template never asks him to talk"))
                    }
                }
            }
            if (positions) {
                listOf("measured" to base, "coarse" to explicit).forEach { (which, text) ->
                    if (!asksForPosition.containsMatchIn(text)) {
                        add(Problem(where, "suggests positions, but the $which template never names one"))
                    }
                }
            }
        }
        ContentLibrary.allTerms.forEach {
            check(
                "term ${it.id}", it.base, it.explicit + " " + (it.extremeTail ?: ""),
                it.examples.isNotEmpty(), it.positions.isNotEmpty()
            )
        }
        ContentLibrary.considerations.forEach {
            check(
                "consideration ${it.id}", it.base, it.explicit + " " + (it.extremeTail ?: ""),
                it.examples.isNotEmpty(), it.positions.isNotEmpty()
            )
        }
    }

    /**
     * Oral content has to name the mouth.
     *
     * `[v_deep]` resolving to "forces himself down onto" never said what he was going down on it
     * with, and reads as lowering himself onto it rather than taking it in his throat. Category
     * is the check rather than wording, so a new oral item cannot slip past by paraphrasing.
     */
    private fun validateOralNamesTheMouth(): List<Problem> = buildList {
        val namesMouth = Regex(
            "\\b(?:mouth|throat|lips?|tongue|sucks?|sucking|teeth|swallow|spits?|spit\\b)",
            RegexOption.IGNORE_CASE
        )
        fun check(where: String, text: String) {
            if (!namesMouth.containsMatchIn(text)) {
                add(Problem(where, "oral content that never names the mouth: '${text.take(110)}'"))
            }
        }
        for (explicitness in Explicitness.entries) {
            val ctx = fullContext(explicitness = explicitness)
            val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)
            ContentLibrary.allTerms.filter { com.thecontract.core.model.Category.ORAL in it.categories }
                .forEach { check("term ${it.id} [$explicitness]", Renderer.renderTerm(it, binding, ctx).instruction) }
            ContentLibrary.considerations.filter { com.thecontract.core.model.Category.ORAL in it.categories }
                .forEach {
                    check(
                        "consideration ${it.id} [$explicitness]",
                        Renderer.renderConsideration(it, Slot.PLAYER_1, Slot.PLAYER_2, ctx).instruction
                    )
                }
        }
    }

    private fun validateLexicon(): List<Problem> = buildList {
        val used = mutableSetOf<String>()
        fun scan(where: String, text: String) {
            Regex("""\[([a-z0-9_]+)\]""").findAll(text).forEach { m ->
                val key = m.groupValues[1]
                used += key
                if (!Lexicon.has(key)) add(Problem(where, "unknown lexicon key [$key]"))
            }
        }
        ContentLibrary.allTerms.forEach {
            scan("term ${it.id}", it.base)
            scan("term ${it.id}", it.explicit)
            it.extremeTail?.let { t -> scan("term ${it.id}", t) }
            scan("term ${it.id}", it.title)
        }
        ContentLibrary.considerations.forEach {
            scan("consideration ${it.id}", it.base)
            scan("consideration ${it.id}", it.explicit)
            it.extremeTail?.let { t -> scan("consideration ${it.id}", t) }
        }
        Lexicon.keys.forEach { key ->
            val variants = Lexicon.variants(key)
            if (variants.size != 4) add(Problem("lexicon $key", "must have four registers"))
            // Every alternative reaches the screen, so every alternative is held to the same
            // rules as the primary — not just the one the report happens to print.
            Explicitness.entries.forEach { register ->
                val options = Lexicon.alternatives(key, register)
                if (options.isEmpty()) add(Problem("lexicon $key", "$register has no wording"))
                if (options.size > Lexicon.MAX_ALTERNATIVES) {
                    add(Problem("lexicon $key", "$register has ${options.size} alternatives, more than the sweep covers"))
                }
                if (options.distinct().size != options.size) {
                    add(Problem("lexicon $key", "$register repeats one of its alternatives"))
                }
                options.forEach { v ->
                    if (v.isBlank()) add(Problem("lexicon $key", "$register has a blank wording"))
                    if (v != v.trim()) add(Problem("lexicon $key", "$register wording '$v' has stray whitespace"))
                    if (femaleTerms.containsMatchIn(v)) add(Problem("lexicon $key", "gendered wording in '$v'"))
                }
            }
            // Erotic and Extremely filthy must actually differ, or the register does nothing.
            if (variants[0] == variants[3] && key.startsWith("v_")) {
                add(Problem("lexicon $key", "identical in Erotic and Extremely filthy"))
            }
        }
    }

    /** Convenience for the developer report. */
    fun summary(): String = buildString {
        val c = ContentLibrary.counts()
        appendLine("Proposed terms:        ${c["proposedTerms"]}")
        appendLine("  level 1..5:          " + (1..5).joinToString(" / ") { "${c["termsLevel$it"]}" })
        appendLine("Closing term options:  ${c["climaxTerms"]}")
        appendLine("Consideration actions: ${c["considerationActions"]}")
        appendLine("Private preferences:   ${c["privatePreferences"]}")
        appendLine("Equipment options:     ${c["equipmentOptions"]}")
        appendLine("Shared boundaries:     ${c["boundaries"]}")
        appendLine("Lexicon entries:       ${Lexicon.keys.size} × 4 registers")
    }
}
