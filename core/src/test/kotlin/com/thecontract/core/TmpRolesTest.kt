package com.thecontract.core

import com.thecontract.core.engine.*
import com.thecontract.core.model.*
import kotlin.test.Test

class TmpRolesTest {
    @Test fun roles() {
        val out = StringBuilder()
        for (role in listOf(AnalRole.TOP, AnalRole.VERS_TOP, AnalRole.VERS, AnalRole.VERS_BOTTOM, AnalRole.BOTTOM)) {
            val ctx = GameContext(
                SharedSetup(
                    player1 = PlayerSetup("Alex", Role.DOMINANT, analRole = role),
                    player2 = PlayerSetup("Chris", Role.SUBMISSIVE, analRole = AnalRole.VERS),
                    explicitness = Explicitness.EXTREME, equipment = Equipment.entries.toSet()
                ),
                Slot.entries.associateWith { s ->
                    PrivateProfile(s, PreferenceLibrary.all.associate { it.id to Answer.YES }, complete = true)
                }
            )
            val st = com.thecontract.core.engine.GameEngine().newGame("s-x", 0L).state
            val picks = ProposalSelector.closingCandidates(st, ctx, Slot.PLAYER_1)
            val inside = picks.count { it.term.analPenetration && "bottoming" in it.term.activities }
            val takes  = picks.count { it.term.analPenetration && "bottoming" !in it.term.activities }
            out.appendLine("$role: ${picks.size} offered | comes-inside-partner=$inside | penetrated-himself=$takes | neither=${picks.size-inside-takes}")
            picks.forEach { out.appendLine("      ${it.term.id}") }
            out.appendLine()
        }
        java.io.File("/tmp/roles.txt").writeText(out.toString())
    }
}
