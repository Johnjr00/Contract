package com.thecontract.core

import com.thecontract.core.harness.GameDriver
import com.thecontract.core.harness.Harness
import com.thecontract.core.harness.Setups
import com.thecontract.core.model.Answer
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.ProposalResponse
import com.thecontract.core.model.Slot
import com.thecontract.core.model.TimerRunState
import com.thecontract.core.net.InterfaceKind
import com.thecontract.core.net.LocalInterface
import com.thecontract.core.net.NetworkScanner
import com.thecontract.core.net.QrCode
import com.thecontract.core.protocol.CloseDraft
import com.thecontract.core.protocol.GlobalPause
import com.thecontract.core.protocol.OpenDraft
import com.thecontract.core.protocol.ProposalRespond
import com.thecontract.core.protocol.ResumeVote
import com.thecontract.core.protocol.TimerCommand
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Protocol, slot and pairing behaviour (sections 7–10, 20, 41, 42) driven through the wire
 * protocol with simulated phones.
 */
class ProtocolTest {

    private fun startedGame(): Triple<Harness, GameDriver, GameDriver> {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2)
        driver.submitSetup(Setups.broad())
        driver.fillProfile(Slot.PLAYER_1, Setups.allAnswers(Answer.YES))
        driver.fillProfile(Slot.PLAYER_2, Setups.allAnswers(Answer.YES))
        return Triple(h, driver, driver)
    }

    // ---------------------------------------------------------------- pairing

    @Test
    fun `first phone becomes Player 1, second becomes Player 2, third is refused`() {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val p3 = h.newPhone("three").join()

        assertEquals(Slot.PLAYER_1, p1.slot)
        assertEquals(Slot.PLAYER_2, p2.slot)
        assertNull(p3.slot, "a third device must never receive a slot")
        assertNotNull(p3.sessionFull, "a third device must get a clear session-full response")
        assertNull(p3.view, "a third device must not receive any game state")
        assertTrue(p3.received.none { it is com.thecontract.core.protocol.StateSnapshot })
    }

    @Test
    fun `only Player 1 can run the shared setup`() {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()

        assertFalse(p2.act(com.thecontract.core.protocol.SubmitSetup(Setups.broad())))
        assertEquals("NOT_ALLOWED", p2.rejections.last().first)
        assertEquals(GamePhase.PLAYER_1_SETUP, h.state().phase)

        // Player 2 sees a waiting state with no setup form at all.
        assertNull(p2.view?.setup)
        assertTrue(p2.view?.waiting == true)

        assertTrue(p1.act(com.thecontract.core.protocol.SubmitSetup(Setups.broad())))
        assertEquals(GamePhase.PRIVATE_PROFILES, h.state().phase)
    }

    @Test
    fun `the join code disappears from the TV once both phones are connected`() {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        assertNotNull(h.tvView().join, "the QR code must be shown while a slot is free")
        assertTrue(h.tvView().join!!.qrMatrix.isNotEmpty())

        h.newPhone("two").join()
        assertNull(h.tvView().join, "the QR code must disappear once both phones are in")
        assertEquals("CONNECTED", h.tvView().connections["PLAYER_1"])
        assertEquals("CONNECTED", h.tvView().connections["PLAYER_2"])
        assertEquals(Slot.PLAYER_1, p1.slot)
    }

    // ---------------------------------------------------------------- reconnection

    @Test
    fun `a refreshed phone returns to its own slot and a stranger cannot take it`() {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val token2 = p2.resumeToken

        p2.disconnect()
        assertEquals("DISCONNECTED", h.tvView().connections["PLAYER_2"])

        // The same browser comes back with its stored token.
        val back = h.newPhone("two", token2).join()
        assertEquals(Slot.PLAYER_2, back.slot)
        assertNotEquals(Slot.PLAYER_1, back.slot, "a reconnecting phone must not become the other player")
        assertEquals("CONNECTED", h.tvView().connections["PLAYER_2"])
        assertEquals(Slot.PLAYER_1, p1.slot)
    }

    @Test
    fun `an unknown browser needs a TV confirmation before taking an occupied slot`() {
        val h = Harness().startGame()
        h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        p2.disconnect()

        val stranger = h.newPhone("stranger").join()
        assertNull(stranger.slot, "an unknown browser must not be given the slot automatically")
        assertNotNull(stranger.reclaimRequestId, "a reclaim request must be raised for the TV")
        assertNotNull(h.manager.pendingReclaim)

        // The TV remote confirms it. No PIN anywhere in this flow.
        assertTrue(h.manager.confirmReclaim(stranger.reclaimRequestId!!, approve = true))
        assertEquals(Slot.PLAYER_2, stranger.slot)
        assertNull(h.manager.pendingReclaim)
    }

    @Test
    fun `the TV remote can release a disconnected slot and restart pairing`() {
        val h = Harness().startGame()
        val p1 = h.newPhone("one").join()
        val p2 = h.newPhone("two").join()
        val driver = GameDriver(h, p1, p2)
        driver.submitSetup(Setups.broad())
        val setupBefore = h.state().setup

        p2.disconnect()
        h.manager.releaseSlot(Slot.PLAYER_2)
        assertEquals("NEVER_CONNECTED", h.tvView().connections["PLAYER_2"])

        val replacement = h.newPhone("replacement").join()
        assertEquals(Slot.PLAYER_2, replacement.slot, "a released slot is available again")

        val tokenBefore = h.manager.joinToken
        h.manager.restartPairing()
        assertNotEquals(tokenBefore, h.manager.joinToken, "restarting pairing must mint a new join token")
        assertEquals(setupBefore, h.state().setup, "restarting pairing must not delete the contract setup")
    }

    // ---------------------------------------------------------------- actions

    @Test
    fun `a duplicate client action is applied exactly once`() {
        val (h, driver, _) = startedGame()
        val versionBefore = h.state().version
        val actionId = "duplicate-1"

        driver.p1.actWithId(actionId, ProposalRespond(ProposalResponse.SIGN))
        val versionAfter = h.state().version
        assertNotEquals(versionBefore, versionAfter)

        // The same id replayed after a dropped socket must not register a second response.
        driver.p1.actWithId(actionId, ProposalRespond(ProposalResponse.REJECT), expectedVersion = versionBefore)
        assertEquals(versionAfter, h.state().version, "a replayed action changed the state")
        assertEquals(
            ProposalResponse.SIGN,
            h.state().negotiation.current!!.responses[Slot.PLAYER_1],
            "the replay overwrote the original response"
        )
    }

    @Test
    fun `a stale state version is rejected`() {
        val (h, driver, _) = startedGame()
        val staleVersion = h.state().version
        driver.p2.act(ProposalRespond(ProposalResponse.SIGN))
        assertNotEquals(staleVersion, h.state().version)

        val accepted = driver.p1.act(ProposalRespond(ProposalResponse.SIGN), expectedVersion = staleVersion)
        assertFalse(accepted, "a stale client must not be able to change the current state")
        assertEquals("STALE_VERSION", driver.p1.rejections.last().first)
    }

    @Test
    fun `a second response from the same phone is ignored`() {
        val (h, driver, _) = startedGame()
        driver.p1.act(ProposalRespond(ProposalResponse.SIGN))
        val version = h.state().version
        driver.p1.act(ProposalRespond(ProposalResponse.REJECT))
        assertEquals(version, h.state().version, "a double tap produced a second response")
    }

    // ---------------------------------------------------------------- timers

    @Test
    fun `only the performer controls the consideration timers, with the TV remote as backup`() {
        val (h, driver, _) = startedGame()

        // Find a proposal whose benefit is not balanced: a reciprocal consideration is
        // deliberately controllable by both phones, which is a different rule.
        var guard = 0
        while (h.state().negotiation.current?.beneficiary == null && guard++ < 40) {
            driver.p1.act(ProposalRespond(ProposalResponse.SIGN))
            driver.p2.act(ProposalRespond(ProposalResponse.SIGN))
            if (h.state().negotiation.current?.beneficiary != null) break
            // Balanced benefit: back out and let the next proposal come round.
            driver.p1.act(com.thecontract.core.protocol.Back)
            driver.p1.act(ProposalRespond(ProposalResponse.REJECT))
            driver.p2.act(ProposalRespond(ProposalResponse.REJECT))
        }
        val current = h.state().negotiation.current!!
        val beneficiary = assertNotNull(current.beneficiary, "no unbalanced proposal appeared")
        // The player who gained less names the payment; the beneficiary performs it.
        val chooser = if (beneficiary == Slot.PLAYER_1) driver.p2 else driver.p1
        val actionId = chooser.choiceIdsStartingWith("pick_consideration:").first()
            .removePrefix("pick_consideration:")
        chooser.act(com.thecontract.core.protocol.PickConsideration(actionId))

        val board = h.state().timers
        assertTrue(board.timers.isNotEmpty())
        val timerId = board.timers.first().id
        val controller = assertNotNull(board.controllerSlot, "a non-mutual action must name a controller")
        assertFalse(board.bothMayControl)
        val other = if (controller == Slot.PLAYER_1) driver.p2 else driver.p1
        val owner = if (controller == Slot.PLAYER_1) driver.p1 else driver.p2

        // Both phones try to start the same timer at once: only the controller may.
        assertFalse(other.act(TimerCommand(timerId, "start")), "the wrong phone started a timer")
        assertEquals("NOT_ALLOWED", other.rejections.last().first)
        assertTrue(owner.act(TimerCommand(timerId, "start")))
        assertEquals(TimerRunState.RUNNING, h.state().timers.timer(timerId)!!.state)

        // A second start from the controller is a no-op, not a restart.
        val remaining = h.state().timers.timer(timerId)!!.remainingMs
        h.advance(1_000)
        owner.act(TimerCommand(timerId, "start"))
        assertEquals(remaining, h.state().timers.timer(timerId)!!.remainingMs, "the timer restarted")

        // The TV remote is always allowed as backup.
        assertTrue(h.manager.applyAction(null, TimerCommand(timerId, "pause")))
        assertEquals(TimerRunState.PAUSED, h.state().timers.timer(timerId)!!.state)

        // Timers run down on the server clock, not on any phone's clock.
        h.manager.applyAction(null, TimerCommand(timerId, "start"))
        h.advance(h.state().timers.timer(timerId)!!.remainingMs + 500)
        assertEquals(TimerRunState.COMPLETED, h.state().timers.timer(timerId)!!.state)
    }

    // ---------------------------------------------------------------- pause

    @Test
    fun `either phone can pause immediately and resuming needs both`() {
        val (h, driver, _) = startedGame()
        assertTrue(driver.p2.act(GlobalPause))
        assertEquals(GamePhase.PAUSED, h.state().phase)
        assertTrue(h.tvView().paused)

        // The paused screen names nobody.
        val tv = h.tvView()
        assertTrue(!tv.body.contains("Marcus") && !tv.body.contains("Dan"), "the paused screen blamed a player")

        // Ordinary actions are refused while paused.
        assertFalse(driver.p1.act(ProposalRespond(ProposalResponse.SIGN)))

        driver.p1.act(ResumeVote(true))
        assertEquals(GamePhase.PAUSED, h.state().phase, "one confirmation must not resume the session")
        driver.p2.act(ResumeVote(true))
        assertNotEquals(GamePhase.PAUSED, h.state().phase)
    }

    @Test
    fun `pause works from a stale phone and the TV remote can resume alone`() {
        val (h, driver, _) = startedGame()
        val staleVersion = h.state().version - 5
        assertTrue(driver.p1.act(GlobalPause, expectedVersion = staleVersion), "the stop control must always work")
        assertEquals(GamePhase.PAUSED, h.state().phase)

        assertTrue(h.manager.applyAction(null, ResumeVote(true)))
        assertNotEquals(GamePhase.PAUSED, h.state().phase, "the TV remote must be able to resume alone")
    }

    // ---------------------------------------------------------------- draft & back

    @Test
    fun `the draft opens and closes back into the exact prior state`() {
        val (h, driver, _) = startedGame()
        val before = h.state().copy(version = 0, updatedAtMs = 0, draftReviewOpen = false, processedActionIds = emptyList())

        driver.p1.act(OpenDraft)
        assertTrue(h.state().draftReviewOpen)
        assertNotNull(driver.p1.view?.draft)
        assertNull(driver.p1.view?.profile, "the draft must not expose profile answers")

        driver.p1.act(CloseDraft)
        val after = h.state().copy(version = 0, updatedAtMs = 0, draftReviewOpen = false, processedActionIds = emptyList())
        assertEquals(before, after, "closing the draft did not restore the exact prior state")
    }

    @Test
    fun `back never duplicates a vote, a term or a receipt`() {
        val (h, driver, _) = startedGame()
        val termId = h.state().negotiation.current!!.term.termId
        driver.p1.act(ProposalRespond(ProposalResponse.SIGN))
        assertEquals(1, h.state().negotiation.current!!.responses.size)

        assertTrue(driver.p1.act(com.thecontract.core.protocol.Back))
        assertEquals(0, h.state().negotiation.current!!.responses.size, "the response survived Back")
        assertEquals(termId, h.state().negotiation.current!!.term.termId, "Back replaced the proposal")
        assertEquals(0, h.state().negotiation.signed.size)
        assertEquals(0, h.state().negotiation.receipts.size)
        assertEquals(0, h.state().negotiation.regularSlotsUsed, "Back consumed contract progress")
    }

    @Test
    fun `back cannot reopen a signed term`() {
        val (h, driver, _) = startedGame()
        var guard = 0
        while (h.state().negotiation.signed.isEmpty() && guard++ < 500) driver.runOneStep()
        assertTrue(h.state().negotiation.signed.isNotEmpty())

        val signedCount = h.state().negotiation.signed.size
        driver.p1.act(com.thecontract.core.protocol.Back)
        assertEquals(signedCount, h.state().negotiation.signed.size, "Back reopened a signed term")
    }

    // ---------------------------------------------------------------- network

    @Test
    fun `interface selection, address changes and QR regeneration`() {
        val h = Harness().startGame()
        val first = h.manager.joinUrl()
        assertNotNull(first)
        assertTrue(first.startsWith("http://192.168.1.50:"), "the best private interface must be selected: $first")
        val qrBefore = h.tvView().join!!.qrMatrix

        // The TV user picks the other interface.
        assertTrue(h.manager.selectInterface("wlan0@192.168.1.51"))
        val second = h.manager.joinUrl()!!
        assertTrue(second.contains("192.168.1.51"))
        assertNotEquals(qrBefore, h.tvView().join!!.qrMatrix, "the QR code did not regenerate")

        // DHCP hands out a new address: the advertised URL follows, the game state does not move.
        val versionBefore = h.state().version
        h.manager.refreshInterfaces(
            listOf(
                LocalInterface("eth0@192.168.1.77", "Ethernet (eth0)", "192.168.1.77", InterfaceKind.ETHERNET, true, false, 100)
            )
        )
        assertTrue(h.manager.joinUrl()!!.contains("192.168.1.77"))
        assertEquals(versionBefore, h.state().version, "a network change must not disturb the game state")
    }

    @Test
    fun `interface ranking ignores loopback, link-local and VPN`() {
        val candidates = listOf(
            LocalInterface("tun0@10.8.0.2", "VPN (tun0)", "10.8.0.2", InterfaceKind.VPN, true, false, 1),
            LocalInterface("wlan0@169.254.4.5", "Wi-Fi (wlan0)", "169.254.4.5", InterfaceKind.WIFI, false, true, 10),
            LocalInterface("wlan0@192.168.0.9", "Wi-Fi (wlan0)", "192.168.0.9", InterfaceKind.WIFI, true, false, 90),
            LocalInterface("eth0@10.0.0.5", "Ethernet (eth0)", "10.0.0.5", InterfaceKind.ETHERNET, true, false, 100)
        )
        assertEquals("eth0@10.0.0.5", NetworkScanner.best(candidates)!!.id)
        assertEquals(InterfaceKind.VPN, NetworkScanner.classify("tun0"))
        assertEquals(InterfaceKind.WIFI, NetworkScanner.classify("wlan0"))
        assertEquals(InterfaceKind.ETHERNET, NetworkScanner.classify("eth0"))
        assertTrue(NetworkScanner.isPrivateV4("192.168.1.1"))
        assertTrue(NetworkScanner.isPrivateV4("172.20.3.4"))
        assertFalse(NetworkScanner.isPrivateV4("8.8.8.8"))
        assertTrue(NetworkScanner.isLinkLocalV4("169.254.1.2"))

        // A real scan of this machine must never offer loopback.
        assertTrue(NetworkScanner.scan().none { it.address.startsWith("127.") })
    }

    @Test
    fun `an invalid join token is refused and repeated attempts are rate limited`() {
        val h = Harness().startGame()
        val good = h.manager.joinToken!!
        assertTrue(h.manager.authorizeJoin(good, "192.168.1.99"))
        assertFalse(h.manager.authorizeJoin("not-the-token", "192.168.1.99"))

        repeat(12) { h.manager.authorizeJoin("wrong-$it", "192.168.1.200") }
        assertTrue(h.manager.isRateLimited("192.168.1.200"), "repeated invalid joins must be rate limited")
        assertFalse(h.manager.authorizeJoin(good, "192.168.1.200"), "a rate-limited address is refused outright")
        assertFalse(h.manager.isRateLimited("192.168.1.201"), "rate limiting must be per address")
    }

    @Test
    fun `the join token is long, random and unguessable`() {
        val tokens = (1..25).map { Harness().startGame().manager.joinToken!! }
        assertEquals(tokens.size, tokens.distinct().size, "join tokens repeated")
        tokens.forEach {
            assertTrue(it.length >= 40, "join token is too short: ${it.length}")
            assertTrue(it.none { c -> c == '+' || c == '/' || c == '=' }, "token is not URL-safe")
        }
    }

    @Test
    fun `the QR matrix encodes the join URL offline`() {
        val matrix = QrCode.matrix("http://192.168.1.50:8765/join/abc123")
        assertTrue(matrix.isNotEmpty())
        assertTrue(matrix.all { it.length == matrix.size }, "the QR matrix must be square")
        assertTrue(matrix.any { row -> row.contains('1') })
    }
}
