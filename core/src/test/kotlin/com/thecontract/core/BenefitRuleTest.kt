package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.engine.BenefitAnalysis
import com.thecontract.core.engine.EligibilityEngine
import com.thecontract.core.model.BenefitParty
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.Slot
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Who owes consideration is decided by whose body the term is done to — never by who is in
 * charge of it.
 *
 * Command, ownership, restraint, permission and orgasm control used to be benefit types in their
 * own right, which handed the benefit to whoever held the whip and left the man being edged,
 * spanked or tied owing nothing. Those types are gone, so the rule is now structural; these
 * tests hold the remaining edges, including the four rough-anal terms that named the man doing
 * the fucking as the one who "receives penetration".
 */
class BenefitRuleTest {

    private val binding = PartyBinding(Slot.PLAYER_1, Slot.PLAYER_2)

    /**
     * Only three terms in the library benefit the man the template calls the giver, and all
     * three are ones where he is the man being served with a mouth. Anything else appearing here
     * is a term crediting somebody for being in control.
     */
    @Test
    fun `only the man being served benefits as the giver`() {
        val served = setOf(
            "l4_service_worship_on_command",
            "l4_own_collar_service",
            "l5_bdsm_collar_leash_service"
        )
        val actual = ContentLibrary.allTerms
            .filter { it.benefitParty == BenefitParty.GIVER }
            .map { it.id }
            .toSet()
        assertEquals(served, actual, "a term is crediting the wrong man, or a new served term needs listing")
    }

    /**
     * In a penetration term the benefit belongs to the man who is penetrated.
     *
     * Closing terms are the exception and are excluded: there `{R}` is by convention the man the
     * term guarantees will finish, and the guaranteed orgasm is the benefit he pays for. That is
     * a real gain, not authority over anyone.
     */
    @Test
    fun `penetration benefits the man taking it`() {
        val failures = ContentLibrary.allTerms
            .filter { it.analPenetration && !it.climax }
            .mapNotNull { term ->
                val beneficiary = BenefitAnalysis.beneficiary(term, binding) ?: return@mapNotNull null
                val taken = EligibilityEngine.receptiveParties(term, binding)
                if (beneficiary in taken) null
                else "${term.id}: benefit goes to $beneficiary but $taken is the one taken"
            }
        failures.forEach(::println)
        assertEquals(0, failures.size, "penetration terms above credit the wrong man")
    }

    /** Nothing in the library may still be typed as a benefit for being in charge. */
    @Test
    fun `no benefit type rewards authority`() {
        val physical = BenefitType.entries.map { it.name }.toSet()
        listOf("COMMAND_AUTHORITY", "OWNERSHIP", "PERMISSION_CONTROL", "RESTRAINT_CONTROL", "ORGASM_CONTROL")
            .forEach { assertTrue(it !in physical, "$it is back on BenefitType") }
    }

    /**
     * A term that does nothing to either man's body — an order, a rule, a spoken declaration —
     * leaves neither of them ahead, so consideration for it is reciprocal.
     */
    @Test
    fun `protocol terms leave the benefit balanced`() {
        listOf(
            "l2_power_commands", "l3_orgasm_permission", "l4_service_ask_to_speak",
            "l4_own_rules_for_the_night", "l4_bond_collar", "l5_rule_no_hands",
            "l5_rule_stay_in_position", "l5_rule_obeys_first_time"
        ).forEach { id ->
            val term = ContentLibrary.termsById.getValue(id)
            assertEquals(
                null, BenefitAnalysis.beneficiary(term, binding),
                "$id names a beneficiary, but nobody is on the receiving end of it"
            )
        }
    }
}
