package com.thecontract.core

import com.thecontract.core.engine.GameEngine
import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Setups
import com.thecontract.core.harness.TestClock
import com.thecontract.core.model.Answer
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.ProposalResponse
import com.thecontract.core.model.Slot
import com.thecontract.core.model.TimerRunState
import com.thecontract.core.persistence.JsonFileStateStore
import com.thecontract.core.protocol.PickConsideration
import com.thecontract.core.protocol.ProposalRespond
import com.thecontract.core.protocol.SaveContract
import com.thecontract.core.protocol.TimerCommand
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Persistence, restart and saved-contract behaviour (sections 11 and 12). */
class PersistenceTest {

    private fun startedGame(store: JsonFileStateStore, clock: TestClock = TestClock()): Pair<Harness, GameDriver> {
        val h = Harness(store, clock).startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2)
        driver.submitSetup(Setups.broad())
        driver.fillProfile(Slot.PLAYER_1, Setups.allAnswers(Answer.YES))
        driver.fillProfile(Slot.PLAYER_2, Setups.allAnswers(Answer.YES))
        return h to driver
    }

    @Test
    fun `every meaningful mutation is written through to storage`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val (h, driver) = startedGame(store)

        val afterSetup = store.loadActiveSession()
        assertNotNull(afterSetup)
        assertTrue(afterSetup.state.setupComplete, "shared setup was not persisted")
        assertTrue(afterSetup.state.profile(Slot.PLAYER_1).complete, "profiles were not persisted")
        assertEquals(h.state().version, afterSetup.state.version)

        driver.p1.act(ProposalRespond(ProposalResponse.SIGN))
        val afterResponse = store.loadActiveSession()!!
        assertEquals(h.state().version, afterResponse.state.version, "a response was not persisted")
        assertEquals(
            ProposalResponse.SIGN,
            afterResponse.state.negotiation.current!!.responses[Slot.PLAYER_1]
        )

        // Reconnect tokens and slot ownership survive too, or a reconnect could not work.
        assertTrue(afterResponse.slot(Slot.PLAYER_1).claimed)
        assertNotNull(afterResponse.slot(Slot.PLAYER_1).reconnectToken)
    }

    @Test
    fun `a running timer is restored paused at the last safely persisted value`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val clock = TestClock()
        val (h, driver) = startedGame(store, clock)

        // Get to a consideration with real timers.
        var guard = 0
        while (h.state().phase != GamePhase.CONSIDERATION_PUBLIC_EXECUTION && guard++ < 200) {
            val s = h.state()
            when (s.phase) {
                GamePhase.PROPOSAL, GamePhase.WAITING_FOR_PROPOSAL_RESPONSES ->
                    Slot.entries.filter { it !in s.negotiation.current!!.responses }.forEach {
                        (if (it == Slot.PLAYER_1) driver.p1 else driver.p2).act(ProposalRespond(ProposalResponse.SIGN))
                    }

                GamePhase.CONSIDERATION_PRIVATE_SELECTION -> {
                    val chooser = s.negotiation.current!!.beneficiary ?: Slot.PLAYER_1
                    val phone = if (chooser == Slot.PLAYER_1) driver.p1 else driver.p2
                    val id = phone.choiceIdsStartingWith("pick_consideration:").first()
                        .removePrefix("pick_consideration:")
                    phone.act(PickConsideration(id))
                }

                else -> driver.runOneStep()
            }
        }
        assertEquals(GamePhase.CONSIDERATION_PUBLIC_EXECUTION, h.state().phase)

        val timer = h.state().timers.timers.first()
        val controller = h.state().timers.controllerSlot ?: Slot.PLAYER_1
        val phone = if (controller == Slot.PLAYER_1) driver.p1 else driver.p2
        phone.act(TimerCommand(timer.id, "start"))
        val startedRemaining = h.state().timers.timer(timer.id)!!.remainingMs

        // Ten seconds pass. The countdown is *not* written to storage every second.
        clock.advance(10_000)
        h.manager.tick()
        val persistedWhileRunning = store.loadActiveSession()!!.state.timers.timer(timer.id)!!
        assertEquals(
            TimerRunState.RUNNING,
            persistedWhileRunning.state,
            "the running state should have been captured at the transition"
        )
        assertEquals(
            startedRemaining,
            store.loadActiveSession()!!.state.timers.timer(timer.id)!!.remainingMs,
            "the per-second countdown must not be written to storage"
        )

        // Process death mid-timer.
        h.manager.persistNow()
        val restarted = Harness(store, clock)
        assertTrue(restarted.manager.resumeSavedSession())
        val restored = restarted.state().timers.timer(timer.id)!!
        assertEquals(
            TimerRunState.PAUSED,
            restored.state,
            "a sexual-action timer must never keep running while the app is unavailable"
        )
        assertTrue(restored.remainingMs > 0)
        assertTrue(
            restored.remainingMs <= startedRemaining,
            "restored remaining time must not exceed what was persisted"
        )
    }

    @Test
    fun `an unfinished session is offered on launch and is never silently destroyed`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val (h, _) = startedGame(store)
        val sessionId = h.state().sessionId
        h.manager.persistNow()

        // Cold start: the TV offers Resume or Start New.
        val fresh = Harness(store, TestClock())
        assertTrue(fresh.manager.hasSavedUnfinishedSession())
        val landing = fresh.tvView()
        assertEquals(GamePhase.NO_SESSION, landing.phase)
        assertTrue(landing.choices.any { it.id == "resume_session" }, "Resume was not offered")
        assertTrue(landing.choices.any { it.id == "new_game" }, "Start New was not offered")

        assertTrue(fresh.manager.resumeSavedSession())
        assertEquals(sessionId, fresh.state().sessionId)

        // Starting a new game replaces the session; the old one is gone only after that choice.
        val replaced = fresh.manager.startNewSession()
        assertTrue(replaced.sessionId != sessionId)
        assertEquals(replaced.sessionId, store.loadActiveSession()!!.sessionId)
    }

    @Test
    fun `a completed contract can be saved, listed, reopened and deleted`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val (h, driver) = startedGame(store)
        val final = driver.run()
        assertEquals(GamePhase.COMPLETED, final.phase)

        assertTrue(driver.p1.act(SaveContract))
        val saved = h.manager.listContracts()
        assertEquals(1, saved.size, "the completed contract was not saved")
        val contract = saved.first()
        assertEquals(12, contract.signedTerms.size, "10 regular slots plus 2 closing terms")
        assertEquals("Marcus", contract.player1Name)
        assertEquals("Dan", contract.player2Name)
        assertNotNull(contract.finaleOrder)
        assertTrue(contract.receipts.isNotEmpty())

        // Reopened from a cold start: it lives on this TV and nowhere else.
        val cold = JsonFileStateStore(dir)
        val reopened = cold.loadContract(contract.id)
        assertNotNull(reopened)
        assertEquals(contract.signedTerms.size, reopened.signedTerms.size)
        assertEquals(contract.signedTerms.first().term.instruction, reopened.signedTerms.first().term.instruction)

        h.manager.deleteContract(contract.id)
        assertTrue(h.manager.listContracts().isEmpty())
        assertNull(cold.loadContract(contract.id))
    }

    @Test
    fun `abandoning a session removes it from storage`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val (h, _) = startedGame(store)
        assertNotNull(store.loadActiveSession())
        h.manager.abandonSession()
        assertNull(store.loadActiveSession())
        assertFalse(h.manager.hasActiveSession())
    }

    @Test
    fun `a half-written session file never corrupts the next launch`(@TempDir dir: File) {
        val store = JsonFileStateStore(dir)
        val (h, _) = startedGame(store)
        h.manager.persistNow()

        File(dir, "active-session.json").writeText("{ this is not json")
        val fresh = Harness(JsonFileStateStore(dir), TestClock())
        assertFalse(fresh.manager.hasSavedUnfinishedSession(), "a corrupt file must not be treated as a session")
        assertFalse(fresh.manager.resumeSavedSession())
        // The TV still starts cleanly.
        assertEquals(GamePhase.NO_SESSION, fresh.tvView().phase)
    }

    @Test
    fun `engine restore pauses timers even when the store was written mid-run`(@TempDir dir: File) {
        val engine = GameEngine()
        val es = engine.newGame("s-test", 1_000L)
        val board = com.thecontract.core.engine.TimerEngine.board(
            "context",
            listOf(com.thecontract.core.model.RenderedTimer("t1", "Shoulders", 60)),
            Slot.PLAYER_1,
            bothMayControl = false,
            completer = Slot.PLAYER_2
        )
        val running = es.copy(
            state = es.state.copy(timers = com.thecontract.core.engine.TimerEngine.start(board, "t1", 1_000L))
        )
        assertEquals(TimerRunState.RUNNING, running.state.timers.timer("t1")!!.state)

        val restored = engine.restore(running, 999_999L)
        assertEquals(TimerRunState.PAUSED, restored.state.timers.timer("t1")!!.state)
        assertEquals(60_000L, restored.state.timers.timer("t1")!!.remainingMs)
    }
}
