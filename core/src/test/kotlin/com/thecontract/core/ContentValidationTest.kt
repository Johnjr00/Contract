package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.server.WebAssets
import com.thecontract.core.style.Lexicon
import com.thecontract.core.validation.ContentValidator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Section 44 — automated content validation. */
class ContentValidationTest {

    @Test
    fun `content library passes every validation rule`() {
        val problems = ContentValidator.validate()
        if (problems.isNotEmpty()) {
            val report = problems.joinToString("\n") { " - ${it.where}: ${it.what}" }
            throw AssertionError("${problems.size} content problems:\n$report")
        }
    }

    @Test
    fun `library meets the specified minimum sizes`() {
        assertTrue(ContentLibrary.regularTerms.size >= 202, "proposed terms")
        assertTrue(ContentLibrary.considerations.size >= 120, "consideration actions")
        assertTrue(PreferenceLibrary.all.size >= 132, "private preferences")
        assertEquals(28, com.thecontract.core.model.Equipment.entries.size, "equipment options")
        assertEquals(13, com.thecontract.core.model.Boundary.entries.size, "shared boundaries")
        listOf(1 to 42, 2 to 40, 3 to 40, 4 to 40, 5 to 40).forEach { (level, min) ->
            assertTrue(
                ContentLibrary.regularTermsAtLevel(level).size >= min,
                "level $level has ${ContentLibrary.regularTermsAtLevel(level).size}, needs $min"
            )
        }
    }

    @Test
    fun `every explicitness register produces different text`() {
        val samples = ContentLibrary.regularTerms.take(40)
        var differingPairs = 0
        for (term in samples) {
            val texts = Explicitness.entries.map { level ->
                val ctx = testContext(level)
                val binding = com.thecontract.core.engine.EligibilityEngine.candidateBindings(term, ctx).first()
                com.thecontract.core.engine.Renderer.renderTerm(term, binding, ctx).instruction
            }
            assertTrue(texts.distinct().size >= 2, "term ${term.id} reads identically at every level")
            if (texts[0] != texts[1]) differingPairs++
            assertTrue(texts[1] != texts[3], "term ${term.id}: Direct and Extremely filthy are identical")
        }
        assertTrue(differingPairs > samples.size / 2, "Erotic and Direct rarely differ")
    }

    @Test
    fun `lexicon differentiates all four registers`() {
        var differing = 0
        for (key in Lexicon.keys) {
            val v = Lexicon.variants(key)
            assertEquals(4, v.size)
            if (v.distinct().size > 1) differing++
        }
        assertTrue(differing.toDouble() / Lexicon.keys.size > 0.9, "most lexicon keys must vary by register")
    }

    @Test
    fun `phone controller assets are bundled in the artifact`() {
        assertEquals(emptyList(), WebAssets.verifyBundled(), "missing bundled web assets")
        val js = WebAssets.read("controller.js") ?: error("controller.js missing")
        val html = WebAssets.read("controller.html") ?: error("controller.html missing")
        val css = WebAssets.read("controller.css") ?: error("controller.css missing")

        // Offline-only: no CDN, no remote font, no remote script, no outbound calls.
        listOf(js, html, css).forEach { asset ->
            listOf("http://", "https://", "//cdn", "fonts.googleapis", "unpkg", "jsdelivr").forEach { needle ->
                assertTrue(
                    !asset.contains(needle, ignoreCase = true),
                    "bundled asset references an external resource: $needle"
                )
            }
        }
        // Ordinary browser page: no PWA, no service worker, no install banner.
        listOf("serviceWorker", "manifest.json", "beforeinstallprompt", "standalone").forEach { needle ->
            assertTrue(!js.contains(needle) && !html.contains(needle), "controller must not use $needle")
        }
    }

    @Test
    fun `validation summary is printable for the implementation report`() {
        val summary = ContentValidator.summary()
        assertTrue(summary.contains("Proposed terms"))
        println(summary)
    }

    private fun testContext(explicitness: Explicitness) = com.thecontract.core.engine.GameContext(
        com.thecontract.core.model.SharedSetup(
            player1 = com.thecontract.core.model.PlayerSetup("Alex", com.thecontract.core.model.Role.DOMINANT),
            player2 = com.thecontract.core.model.PlayerSetup("Chris", com.thecontract.core.model.Role.SUBMISSIVE),
            explicitness = explicitness,
            equipment = com.thecontract.core.model.Equipment.entries.toSet()
        ),
        com.thecontract.core.model.Slot.entries.associateWith { slot ->
            com.thecontract.core.model.PrivateProfile(
                slot,
                PreferenceLibrary.all.associate { it.id to com.thecontract.core.model.Answer.YES },
                complete = true
            )
        }
    )
}
