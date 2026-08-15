package com.thecontract.core

import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.Slot
import com.thecontract.core.persistence.JsonFileStateStore
import com.thecontract.core.protocol.DeleteSetupPreset
import com.thecontract.core.protocol.LoadSetupPreset
import com.thecontract.core.protocol.SaveSetupPreset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Saved settings survive the session they were saved in.
 *
 * That is the whole point of the feature and the one thing no other test would catch: a preset
 * lives on the television beside the saved contracts, so ending a game, abandoning it or closing
 * the app must leave it alone. The rest of the behaviour here — a load replacing the setup whole,
 * and the revision that tells Player 1's phone to rebuild its form — exists because a load that
 * restored only some of the fields, or that left the old ones on screen, would look like it had
 * worked.
 */
class SetupPresetTest {

    private fun setupScreen(store: JsonFileStateStore): Pair<Harness, com.thecontract.core.harness.SimulatedPhone> {
        val h = Harness(store).startGame()
        val p1 = h.newPhone("one").join()
        assertEquals(GamePhase.PLAYER_1_SETUP, h.state().phase)
        return h to p1
    }

    @Test
    fun `a saved setup comes back whole in a later game`(@TempDir dir: File) {
        val saved = Setups.mixed().copy(sessionLength = SessionLength.EXTENDED)

        // Thursday: Player 1 configures the game and saves it under a name.
        val (first, p1) = setupScreen(JsonFileStateStore(dir))
        assertTrue(p1.act(SaveSetupPreset("Thursday", saved)))
        assertEquals(listOf("Thursday"), p1.view?.setup?.presets?.map { it.name })

        // A different evening: new television process, new session, same box.
        val (second, laterP1) = setupScreen(JsonFileStateStore(dir))
        val listed = laterP1.view?.setup?.presets.orEmpty()
        assertEquals(listOf("Thursday"), listed.map { it.name })

        assertTrue(laterP1.act(LoadSetupPreset(listed.single().id)))
        // Every field, not merely the ones that are easy to carry: names, roles, anal roles,
        // length, register, finale format, stop word, boundaries and equipment.
        assertEquals(saved, second.state().setup)
        val form = laterP1.view?.setup
        assertEquals(saved.boundaries.map { it.name }.sorted(), form?.boundaries?.sorted())
        assertEquals(saved.equipment.map { it.name }.sorted(), form?.equipment?.sorted())
        // The phone edits a local draft and ignores the incoming form while it has one; the
        // revision is the only signal it has that the setup underneath was replaced.
        assertTrue((form?.revision ?: 0) > 0)
    }

    @Test
    fun `saving under a name already in the list replaces it`(@TempDir dir: File) {
        val (h, p1) = setupScreen(JsonFileStateStore(dir))

        assertTrue(p1.act(SaveSetupPreset("Thursday", Setups.mixed())))
        assertTrue(p1.act(SaveSetupPreset("  thursday  ", Setups.broad())))

        val presets = p1.view?.setup?.presets.orEmpty()
        assertEquals(1, presets.size, "a second save under the same name is an overwrite")
        assertTrue(p1.act(LoadSetupPreset(presets.single().id)))
        assertEquals(Setups.broad(), h.state().setup)
    }

    @Test
    fun `a preset is not part of the session and only Player 1 sees the list`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val (h, p1) = setupScreen(store)
        assertTrue(p1.act(SaveSetupPreset("Thursday", Setups.mixed())))

        val p2 = h.newPhone("two").join()
        assertEquals(emptyList(), p2.view?.setup?.presets.orEmpty())

        // Abandoning takes the game and leaves the saved settings, which is what makes them
        // usable at all: they are keyed to the television, not to a session.
        h.manager.abandonSession()
        assertEquals(listOf("Thursday"), store.listSetupPresets().map { it.name })

        // Deleting is the only thing that removes one, and it is Player 1's to do.
        val (later, laterP1) = setupScreen(store)
        val id = laterP1.view?.setup?.presets.orEmpty().single().id
        assertTrue(laterP1.act(DeleteSetupPreset(id)))
        assertEquals(emptyList(), store.listSetupPresets())
        assertFalse(later.state().setupComplete)
    }
}
