package com.thecontract.core.style

import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot

/**
 * Everything the renderer needs to turn a token template into a finished sentence:
 * who is who, what equipment actually exists, and the session's register.
 */
data class RenderContext(
    val setup: SharedSetup,
    val binding: PartyBinding,
    val explicitness: Explicitness,
    val availableEquipment: Set<Equipment>,
    /**
     * Picks between a lexicon key's alternative wordings. Derived from the id of the item being
     * rendered, so one term always reads the same way while the library as a whole varies.
     */
    val variantSeed: Int = 0
) {
    val giverName: String get() = setup.name(binding.giver)
    val receiverName: String get() = setup.name(binding.receiver)
    val dominantName: String get() = setup.name(setup.dominant)
    val submissiveName: String get() = setup.name(setup.submissive)

    fun name(slot: Slot): String = setup.name(slot)
}

class UnresolvableTemplateException(message: String) : IllegalStateException(message)

/**
 * Renders authored token templates into final display text.
 *
 * Token grammar:
 *   `{G}` `{R}` `{DOM}` `{SUB}` `{P1}` `{P2}`   party names
 *   `{G+}` `{R+}` `{DOM+}` `{SUB+}`             possessive forms
 *   `[lexicon_key]`                             register-dependent word or phrase
 *   `#equipment_id#`                            a specific available item
 *   `#TOY#`                                     generic toy resolved to a real selected toy
 *
 * The engine also runs a punctuation and capitalisation pass so that authored text which
 * loses a clause (for example after a "no toys" amendment) still reads as natural English.
 */
object StyleEngine {

    // Android's ICU-backed regex engine rejects a bare, unescaped `}` or `]` outside a
    // quantifier/character-class context with a PatternSyntaxException, even though the JVM's
    // own regex engine (used by every unit test and the desktop dev server) accepts it — so
    // both must be escaped explicitly to work on-device.
    private val tokenRegex = Regex("""\{([A-Za-z0-9_]+\+?)\}""")
    private val lexiconRegex = Regex("""\[([a-z0-9_]+)\]""")
    private val equipmentRegex = Regex("""#([A-Za-z0-9_]+)#""")

    /** Possessive form. Always `name's`, which is correct for names ending in s as well. */
    fun possessive(name: String): String = if (name.isBlank()) name else "$name's"

    fun render(template: String, ctx: RenderContext): String {
        var text = template

        text = lexiconRegex.replace(text) { m ->
            val key = m.groupValues[1]
            if (!Lexicon.has(key)) {
                throw UnresolvableTemplateException("Unknown lexicon key [$key] in: $template")
            }
            // The key name is mixed into the seed so that two keys in the same sentence choose
            // independently; without it every key in an item would land on the same index and
            // the alternatives would move in lockstep across the library.
            Lexicon.resolve(key, ctx.explicitness, ctx.variantSeed + key.hashCode())
        }

        text = equipmentRegex.replace(text) { m ->
            val id = m.groupValues[1]
            if (id == "TOY") {
                resolveGenericToy(ctx)
                    ?: throw UnresolvableTemplateException(
                        "Generic toy requested but no toy is available: $template"
                    )
            } else {
                val eq = Equipment.byId(id)
                    ?: throw UnresolvableTemplateException("Unknown equipment id #$id# in: $template")
                if (eq !in ctx.availableEquipment) {
                    throw UnresolvableTemplateException(
                        "Template references unavailable equipment ${eq.id}: $template"
                    )
                }
                eq.noun
            }
        }

        text = tokenRegex.replace(text) { m ->
            val raw = m.groupValues[1]
            val possessiveForm = raw.endsWith("+")
            val key = if (possessiveForm) raw.dropLast(1) else raw
            val name = when (key) {
                "G" -> ctx.giverName
                "R" -> ctx.receiverName
                "DOM" -> ctx.dominantName
                "SUB" -> ctx.submissiveName
                "P1" -> ctx.name(Slot.PLAYER_1)
                "P2" -> ctx.name(Slot.PLAYER_2)
                else -> throw UnresolvableTemplateException("Unknown party token {$raw} in: $template")
            }
            if (possessiveForm) possessive(name) else name
        }

        return tidy(text)
    }

    /**
     * Resolves `#TOY#` to a concrete item the couple actually owns. Never invents one: if no
     * toy is available the caller gets null and eligibility filtering has already removed the
     * content, so no fallback vibrator can leak into the game.
     */
    fun resolveGenericToy(ctx: RenderContext): String? =
        Equipment.genericToyPreferenceOrder.firstOrNull { it in ctx.availableEquipment }?.noun

    /** Punctuation, spacing and sentence-capitalisation clean-up. */
    fun tidy(input: String): String {
        var s = input
        s = s.replace(Regex("""\s+"""), " ")
        s = s.replace(Regex("""\s+([,.;:!?])"""), "$1")
        s = s.replace(Regex("""([,;:])(?=\S)"""), "$1 ")
        s = s.replace(Regex(""",\s*,"""), ",")
        s = s.replace(Regex("""\.\s*\."""), ".")
        s = s.replace(Regex("""\(\s+"""), "(")
        s = s.replace(Regex("""\s+\)"""), ")")
        s = s.replace(" ,", ",")
        s = s.trim()
        if (s.isNotEmpty() && s.last() !in ".!?") s = "$s."
        return capitaliseSentences(s)
    }

    private fun capitaliseSentences(input: String): String {
        val out = StringBuilder(input.length)
        var startOfSentence = true
        for (ch in input) {
            if (startOfSentence && ch.isLetter()) {
                out.append(ch.uppercaseChar())
                startOfSentence = false
            } else {
                out.append(ch)
                if (ch == '.' || ch == '!' || ch == '?') startOfSentence = true
            }
        }
        return out.toString()
    }

    /**
     * Picks the authored register for the session's explicitness and applies the
     * Extremely-filthy tail. Erotic and Direct share the measured template; Filthy and
     * Extremely filthy share the coarse one. The lexicon still differentiates all four.
     */
    fun renderInstruction(
        base: String,
        explicit: String,
        extremeTail: String?,
        ctx: RenderContext
    ): String {
        val template = when (ctx.explicitness) {
            Explicitness.EROTIC, Explicitness.DIRECT -> base
            Explicitness.FILTHY, Explicitness.EXTREME -> explicit
        }
        val full = if (ctx.explicitness == Explicitness.EXTREME && !extremeTail.isNullOrBlank()) {
            "${template.trimEnd()} ${extremeTail.trim()}"
        } else {
            template
        }
        return render(full, ctx)
    }
}
