package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.EligibilityEngine
import com.thecontract.core.engine.Renderer
import com.thecontract.core.model.Explicitness
import kotlin.test.Test

class TmpDumpTest {
    @Test fun dump() { Explicitness.entries.forEach { dumpOne(it) } }

    private fun dumpOne(lvl: Explicitness) {
        val ctx = ctxFor(lvl)
        val out = StringBuilder()
        ContentLibrary.regularTerms.forEach { t ->
            val b = EligibilityEngine.candidateBindings(t, ctx).firstOrNull() ?: return@forEach
            val r = Renderer.renderTerm(t, b, ctx)
            out.appendLine("T\t${t.id}\t${r.title}\t${r.instruction}")
        }
        ContentLibrary.climaxTerms.forEach { t ->
            val b = EligibilityEngine.candidateBindings(t, ctx).firstOrNull() ?: return@forEach
            val r = Renderer.renderTerm(t, b, ctx)
            out.appendLine("C\t${t.id}\t${r.title}\t${r.instruction}")
        }
        ContentLibrary.considerations.forEach { a ->
            val r = Renderer.renderConsideration(a, com.thecontract.core.model.Slot.PLAYER_1,
                com.thecontract.core.model.Slot.PLAYER_2, ctx)
            out.appendLine("A\t${a.id}\t${r.title}\t${r.instruction}")
        }
        java.io.File("/tmp/r_${lvl.name}.txt").writeText(out.toString())
    }

    private fun ctxFor(explicitness: Explicitness) = com.thecontract.core.engine.GameContext(
        com.thecontract.core.model.SharedSetup(
            player1 = com.thecontract.core.model.PlayerSetup("Alex", com.thecontract.core.model.Role.DOMINANT),
            player2 = com.thecontract.core.model.PlayerSetup("Chris", com.thecontract.core.model.Role.SUBMISSIVE),
            explicitness = explicitness,
            equipment = com.thecontract.core.model.Equipment.entries.toSet()
        ),
        com.thecontract.core.model.Slot.entries.associateWith { slot ->
            com.thecontract.core.model.PrivateProfile(slot,
                com.thecontract.core.model.PreferenceLibrary.all.associate { it.id to com.thecontract.core.model.Answer.YES },
                complete = true)
        }
    )
}
