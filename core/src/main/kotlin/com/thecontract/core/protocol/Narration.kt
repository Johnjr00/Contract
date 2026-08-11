package com.thecontract.core.protocol

import com.thecontract.core.model.GamePhase

/**
 * What the television says out loud, and when.
 *
 * **The invariant.** Speech is public by its nature: it reaches whoever is in the room, and the
 * whole design of this game is that the television is shared and the phones are private. So the
 * television may only ever speak text that is *already on the television screen*. This object
 * derives everything from a TV [ClientView] and nothing else — it never sees a phone view, a
 * profile, a private list of consideration options or a bundle candidate. [script] returns null
 * for every phase where a selection is open on somebody's phone, and `NarrationScriptTest` holds
 * that for all of them.
 *
 * **What is spoken**, decided with the players rather than by me: the instruction of a proposed
 * term, the chosen consideration once it is on the timer screen, and each step of the final
 * scene as it comes up. Nothing else — not the benefit explanation, which is a negotiating aid
 * rather than an instruction; not the title, which is a label; and not the suggestion lists,
 * which are there to be glanced at and picked from rather than recited.
 *
 * **When it is spoken.** The state is versioned and republishes on every tick, so "speak when
 * the screen changes" has to mean something sharper than "speak on every view". [Line.key] is
 * the identity of a piece of narration: the television speaks whenever the key differs from the
 * one it spoke last, and stays quiet otherwise. That gives three behaviours for free — an
 * amended term is re-read because its text changed, stepping back to an earlier finale step is
 * re-read because the key differs from the step just spoken, and the "Read it again" button
 * works by raising [ClientView.narrationNonce], which is part of the key.
 *
 * The key is deliberately built from what is being said rather than from the phase saying it.
 * Several phases share a screen — a proposal being read and a proposal waiting for the second
 * answer are the same term on the same television — and keying on the phase made every one of
 * those transitions start the passage again from the top.
 */
object Narration {

    /** One piece of narration: what to say, and the identity that decides when to say it. */
    data class Line(val key: String, val text: String)

    /**
     * Phases whose television screen presents a term for a decision. The proposal itself, the
     * amended version after a counteroffer, and the pair in a trade — each is a new piece of
     * text arriving on the shared screen.
     */
    private val TERM_PHASES = setOf(
        GamePhase.PROPOSAL,
        GamePhase.WAITING_FOR_PROPOSAL_RESPONSES,
        GamePhase.COUNTEROFFER_APPROVAL,
        GamePhase.BUNDLE_APPROVAL
    )

    /** Phases whose television screen carries the chosen consideration and its timers. */
    private val CONSIDERATION_PHASES = setOf(
        GamePhase.CONSIDERATION_PUBLIC_EXECUTION,
        GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION
    )

    /** True on the screens the television reads out, so the phones can offer a replay button. */
    fun narratesNow(phase: GamePhase): Boolean =
        phase in TERM_PHASES || phase in CONSIDERATION_PHASES || phase == GamePhase.FINAL_EXECUTION

    fun script(view: ClientView, enabled: Boolean): Line? {
        if (!enabled) return null
        // Belt and braces on the invariant: nothing derived from a phone view is ever spoken,
        // whatever the caller passes in.
        if (view.audience != "tv") return null

        // Grouped rather than keyed on the phase itself, because a phase can change while the
        // screen does not. Answering a proposal moves PROPOSAL to WAITING_FOR_PROPOSAL_RESPONSES
        // with the same term still on the television, and a finished timer moves
        // CONSIDERATION_PUBLIC_EXECUTION to WAITING_FOR_SIGNATURE_CONFIRMATION with the same
        // consideration still on it. Keyed on the phase, both of those started the passage again
        // from the top — the first one over the second player while he was still deciding.
        val (group, body) = when (view.phase) {
            in TERM_PHASES -> TERM to termScript(view)
            in CONSIDERATION_PHASES -> CONSIDERATION to considerationScript(view)
            GamePhase.FINAL_EXECUTION -> FINALE to finaleScript(view)
            else -> return null
        }
        if (body == null) return null

        val step = view.execution?.stepIndex ?: 0
        return Line(key = "$group|$step|${view.narrationNonce}|${body.hashCode()}", text = body)
    }

    private const val TERM = "term"
    private const val CONSIDERATION = "consideration"
    private const val FINALE = "finale"

    private fun termScript(view: ClientView): String? {
        val parts = listOfNotNull(view.term, view.bundledTerm)
        if (parts.isEmpty()) return null
        return parts.joinToString(" ") { speakable(it.instruction) }.ifBlank { null }
    }

    private fun considerationScript(view: ClientView): String? {
        val c = view.consideration ?: return null
        return speakable(c.instruction).ifBlank { null }
    }

    /**
     * The final scene puts the step's instruction in the view body rather than on the card,
     * because the screen prints the paragraph once above the card and blanks it below.
     */
    private fun finaleScript(view: ClientView): String? =
        speakable(view.body).ifBlank { null }

    /**
     * Text prepared for a speech engine. The library is already unusually well suited to being
     * read — every number is spelled out and there are no abbreviations — so this only has to
     * deal with the punctuation that a phonemiser reads as a word or as silence: the em dash
     * used as an aside, and the middle dot that separates timer labels.
     */
    private fun speakable(text: String): String = text
        .replace("—", ",")
        .replace("·", ",")
        .replace(Regex("\\s+"), " ")
        .trim()
        .let { if (it.isEmpty() || it.last() in ".!?") it else "$it." }
}
