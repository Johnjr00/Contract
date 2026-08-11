package com.thecontract.core

import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Answer
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.Narration
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The television reads instructions out loud, so what it may say is a privacy question before it
 * is a feature question.
 *
 * The rule is that speech may only ever carry text that is already on the shared screen. These
 * play whole games and hold [Narration] to it from both directions: everything it decides to
 * speak has to appear verbatim on the television, and nothing a phone is holding privately —
 * a profile, a list of consideration options, a bundle candidate — may ever reach it.
 */
class NarrationScriptTest {

    private fun play(check: (Harness) -> Unit) {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2)
        driver.submitSetup(Setups.broad(SessionLength.QUICK))
        driver.fillProfile(Slot.PLAYER_1, Setups.allAnswers(Answer.YES))
        driver.fillProfile(Slot.PLAYER_2, Setups.allAnswers(Answer.YES))

        var guard = 0
        while (h.state().phase != GamePhase.COMPLETED && guard++ < 4000) {
            check(h)
            driver.runOneStep()
        }
        assertEquals(GamePhase.COMPLETED, h.state().phase, "the game did not finish")
        check(h)
    }

    /**
     * Every word the television speaks is on the television screen. The proposed term, the
     * chosen consideration and the finale step each print their instruction somewhere on the TV
     * view, so the spoken passage has to be reconstructable from that view's own text.
     */
    @Test
    fun `nothing is spoken that is not already on the television`() {
        var spokenLines = 0
        play { h ->
            val tv = h.tvView()
            val line = Narration.script(tv, enabled = true) ?: return@play
            spokenLines++
            val onScreen = buildString {
                append(tv.body).append(' ')
                listOfNotNull(tv.term, tv.bundledTerm).forEach { append(it.instruction).append(' ') }
                tv.consideration?.let { append(it.instruction).append(' ') }
                tv.execution?.term?.let { append(it.instruction).append(' ') }
            }
            // The spoken text is punctuation-normalised, so compare on words rather than bytes.
            val screenWords = onScreen.split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
            val missing = line.text.split(Regex("\\W+")).filter { it.isNotBlank() }.filterNot { it in screenWords }
            assertTrue(
                missing.isEmpty(),
                "the TV would speak words that are not on its own screen: $missing\n  spoken: ${line.text}"
            )
        }
        assertTrue(spokenLines >= 10, "too little narration to judge: $spokenLines")
    }

    /** Nothing is spoken while a private list is open on somebody's phone. */
    @Test
    fun `private phases are silent`() {
        val private = setOf(
            GamePhase.PRIVATE_PROFILES,
            GamePhase.WAITING_FOR_PROFILES,
            GamePhase.COUNTEROFFER_PRIVATE_SELECTION,
            GamePhase.BUNDLE_PRIVATE_SELECTION,
            GamePhase.CONSIDERATION_PRIVATE_SELECTION,
            GamePhase.CLOSING_TERM_SELECTION,
            GamePhase.CLOSING_TERM_CONSIDERATION,
            GamePhase.PRIVATE_DOMINANT_FINALE_SELECTION,
            GamePhase.FINALE_ORDER_SELECTION
        )
        var seen = 0
        play { h ->
            val tv = h.tvView()
            if (tv.phase !in private) return@play
            seen++
            assertEquals(
                null, Narration.script(tv, enabled = true),
                "the TV would speak during ${tv.phase}, when a phone is choosing privately"
            )
        }
        assertTrue(seen > 0, "no private phase was reached")
    }

    /** A phone view is never a narration source, whatever a caller does with it. */
    @Test
    fun `a phone view is never spoken`() {
        play { h ->
            Slot.entries.forEach { slot ->
                assertEquals(
                    null, Narration.script(h.phoneView(slot), enabled = true),
                    "a phone view produced narration in ${h.state().phase}"
                )
            }
        }
    }

    /** The switch in setup silences it completely. */
    @Test
    fun `the setting turns it off`() {
        play { h -> assertEquals(null, Narration.script(h.tvView(), enabled = false)) }
    }

    /**
     * The key is what stops the television repeating itself on every republished view, and what
     * makes "Read it again" work. Same screen, same key; a bumped nonce, a different key.
     */
    @Test
    fun `the key is stable per screen and changes when a replay is asked for`() {
        var checked = 0
        play { h ->
            val tv = h.tvView()
            val a = Narration.script(tv, enabled = true) ?: return@play
            assertEquals(a.key, Narration.script(tv, enabled = true)?.key, "key is not stable")
            val replay = Narration.script(tv.copy(narrationNonce = tv.narrationNonce + 1), enabled = true)
            assertTrue(replay != null && replay.key != a.key, "asking for a replay did not change the key")
            assertEquals(a.text, replay?.text, "a replay said something different")
            checked++
        }
        assertTrue(checked >= 10, "too few screens checked: $checked")
    }
}
