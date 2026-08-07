package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.*
import com.thecontract.core.model.*
import kotlin.test.Test

class TmpEscalationTest {
    @Test fun escalation() {
        val out = StringBuilder()
        for (roles in listOf(AnalRole.VERS to AnalRole.VERS, AnalRole.TOP to AnalRole.BOTTOM)) {
            val ctx = GameContext(
                SharedSetup(
                    player1 = PlayerSetup("Alex", Role.DOMINANT, analRole = roles.first),
                    player2 = PlayerSetup("Chris", Role.SUBMISSIVE, analRole = roles.second),
                    explicitness = Explicitness.EXTREME, equipment = Equipment.entries.toSet()
                ),
                Slot.entries.associateWith { s ->
                    PrivateProfile(s, PreferenceLibrary.all.associate { it.id to Answer.YES }, complete = true)
                }
            )
            out.appendLine("== ${roles.first} + ${roles.second}")
            for (lvl in 1..5) {
                val n = ContentLibrary.regularTermsAtLevel(lvl).count {
                    EligibilityEngine.evaluate(it, ctx) is Eligibility.Ok
                }
                val anal = ContentLibrary.regularTermsAtLevel(lvl).count {
                    EligibilityEngine.evaluate(it, ctx) is Eligibility.Ok && it.analPenetration
                }
                out.appendLine("   act $lvl: $n eligible terms (of which $anal penetrative)")
            }
            out.appendLine()
        }
        java.io.File("/tmp/escalation.txt").writeText(out.toString())
    }
}
