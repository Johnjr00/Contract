package com.thecontract.core

import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.SimulatedPhone
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Answer
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.Slot
import com.thecontract.core.persistence.JsonFileStateStore
import com.thecontract.core.persistence.StateStore
import com.thecontract.core.protocol.DeleteProfilePreset
import com.thecontract.core.protocol.LoadProfilePreset
import com.thecontract.core.protocol.SaveProfilePreset
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Named profile answers survive the session they were saved in, and come back unaltered.
 *
 * "Unaltered" is the rule with teeth: a load that quietly softened a Yes, dropped a Maybe's
 * condition or marked the profile finished would be wrong in a way nobody would notice until the
 * game proposed something. The rest guards the two deliberate decisions — that these are shared
 * between the players, and that sharing stops at the phones.
 */
class ProfilePresetTest {

    /** A spread of all three answers, so a load that flattened them would show up. */
    private val answers: Map<String, Answer> = PreferenceLibrary.asked.mapIndexed { i, p ->
        p.id to when (i % 3) {
            0 -> Answer.YES
            1 -> Answer.NO
            else -> Answer.MAYBE
        }
    }.toMap()

    private val conditionPrefId = PreferenceLibrary.asked[2].id
    private val conditions = mapOf(conditionPrefId to MaybeCondition.ROLES_REVERSED.name)

    private fun profileScreen(store: StateStore): Triple<Harness, SimulatedPhone, SimulatedPhone> {
        val h = Harness(store).startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        GameDriver(h, p1, p2).submitSetup(Setups.mixed())
        assertEquals(GamePhase.PRIVATE_PROFILES, h.state().phase)
        return Triple(h, p1, p2)
    }

    @Test
    fun `a saved profile comes back exactly as it was saved, and does not finish it`(@TempDir dir: File) {
        val (_, p1, _) = profileScreen(JsonFileStateStore(dir))
        assertTrue(p1.act(SaveProfilePreset("Marcus normal", answers, conditions)))

        // A later evening: new television process, new session, same box.
        val (later, laterP1, _) = profileScreen(JsonFileStateStore(dir))
        val listed = laterP1.view?.profile?.presets.orEmpty()
        assertEquals(listOf("Marcus normal"), listed.map { it.name })

        assertTrue(laterP1.act(LoadProfilePreset(listed.single().id)))
        val profile = later.state().profile(Slot.PLAYER_1)
        assertEquals(answers, profile.answers.filterKeys { it in answers.keys })
        assertEquals(MaybeCondition.ROLES_REVERSED, profile.maybeConditions[conditionPrefId])
        // Loading fills the form in and stops there; he still submits it himself.
        assertFalse(profile.complete)
        assertEquals(GamePhase.PRIVATE_PROFILES, later.state().phase)
    }

    @Test
    fun `saved profiles are shared between the players but never reach the TV`(@TempDir dir: File) {
        val (h, p1, p2) = profileScreen(JsonFileStateStore(dir))
        assertTrue(p1.act(SaveProfilePreset("Marcus normal", answers, conditions)))

        // Deliberately shared: the other phone is offered the same list and may load from it.
        val listed = p2.view?.profile?.presets.orEmpty()
        assertEquals(listOf("Marcus normal"), listed.map { it.name })
        assertTrue(p2.act(LoadProfilePreset(listed.single().id)))
        assertEquals(answers, h.state().profile(Slot.PLAYER_2).answers.filterKeys { it in answers.keys })

        // Sharing stops at the phones. The television is never handed a profile form at all, so
        // neither the answers nor the names the men chose for them can appear on the shared screen.
        assertNull(h.tvView().profile)
        assertTrue(h.tvViews.all { it.profile == null })
    }

    @Test
    fun `a saved profile outlives the session and is only removed by deleting it`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val (h, p1, _) = profileScreen(store)
        assertTrue(p1.act(SaveProfilePreset("Marcus normal", answers, conditions)))
        assertTrue(p1.act(SaveProfilePreset("  MARCUS normal  ", answers)))
        assertEquals(1, store.listProfilePresets().size, "a second save under the same name is an overwrite")

        h.manager.abandonSession()
        assertEquals(listOf("MARCUS normal"), store.listProfilePresets().map { it.name })

        val (_, laterP1, _) = profileScreen(store)
        assertTrue(laterP1.act(DeleteProfilePreset(store.listProfilePresets().single().id)))
        assertEquals(emptyList(), store.listProfilePresets())
    }
}
