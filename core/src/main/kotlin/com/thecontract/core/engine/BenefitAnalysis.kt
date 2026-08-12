package com.thecontract.core.engine

import com.thecontract.core.model.BenefitParty
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.SharedSetup
import com.thecontract.core.model.Slot
import com.thecontract.core.model.Term

/**
 * Who owes consideration (section 27).
 *
 * Benefit is read off the *structure of the proposed term* — who receives the massage, whose
 * cock is in someone's mouth, who gains authority — and deliberately **not** off how
 * enthusiastically either player answered his private profile. A player who said Yes to
 * everything does not thereby owe more.
 *
 * Scores exist only so that bundles can be combined; they are never displayed.
 */
object BenefitAnalysis {

    private fun scoreFor(term: Term, binding: PartyBinding): Map<Slot, Int> {
        val weight = when (term.benefitType) {
            BenefitType.PENETRATION_RECIPIENT,
            BenefitType.ORAL_RECIPIENT,
            BenefitType.RIMMING_RECIPIENT -> 2
            else -> 1
        }
        return when (term.benefitParty) {
            BenefitParty.GIVER -> mapOf(binding.giver to weight, binding.receiver to 0)
            BenefitParty.RECEIVER -> mapOf(binding.receiver to weight, binding.giver to 0)
            BenefitParty.MUTUAL -> mapOf(binding.giver to weight, binding.receiver to weight)
        }
    }

    /** null means balanced: consideration is reciprocal and both players confirm. */
    fun beneficiary(term: Term, binding: PartyBinding): Slot? = when (term.benefitParty) {
        BenefitParty.GIVER -> binding.giver
        BenefitParty.RECEIVER -> binding.receiver
        BenefitParty.MUTUAL -> null
    }

    /** For a bundle trade, the two terms' scores are combined (section 31). */
    fun combinedBeneficiary(parts: List<Pair<Term, PartyBinding>>): Slot? {
        val totals = mutableMapOf(Slot.PLAYER_1 to 0, Slot.PLAYER_2 to 0)
        for ((term, binding) in parts) {
            scoreFor(term, binding).forEach { (slot, v) -> totals[slot] = (totals[slot] ?: 0) + v }
        }
        val p1 = totals.getValue(Slot.PLAYER_1)
        val p2 = totals.getValue(Slot.PLAYER_2)
        return when {
            p1 > p2 -> Slot.PLAYER_1
            p2 > p1 -> Slot.PLAYER_2
            else -> null
        }
    }

    /**
     * A natural-language explanation, shown identically on the TV and on both phones.
     * No numbers, no profile information.
     */
    fun explanation(term: Term, binding: PartyBinding, setup: SharedSetup): String {
        val giver = setup.name(binding.giver)
        val receiver = setup.name(binding.receiver)
        if (term.climax) {
            return "This is the closing term that guarantees $receiver finishes, so $receiver is the one " +
                "who has to earn $giver's signature for it."
        }
        val beneficiary = beneficiary(term, binding)
        if (beneficiary == null) {
            return "Benefit is balanced: nobody is on the receiving end of this one, so being the man " +
                "in charge of it counts for nothing. Consideration is reciprocal — both of you perform, " +
                "and both of you confirm."
        }
        val who = setup.name(beneficiary)
        val other = setup.name(beneficiary.other)
        // Written from the beneficiary's side rather than the receiver's, because the man who is
        // worked on is not always the one the term calls its receiver — a term where the
        // submissive serves with his mouth is received by the dominant.
        val reason = when (term.benefitType) {
            BenefitType.MASSAGE_RECIPIENT -> "$who is the one being worked on, and $other is doing the work"
            BenefitType.KISS_RECIPIENT -> "$who is on the receiving end of the kissing"
            BenefitType.EAR_PLAY_RECIPIENT -> "$who is the one having his ears worked, and that is the whole point of it"
            BenefitType.ORAL_RECIPIENT -> "$who has $other's mouth on him for the whole term"
            BenefitType.RIMMING_RECIPIENT -> "$who is the one being eaten out, and $other is putting in the effort"
            BenefitType.HAND_STIMULATION_RECIPIENT -> "$who is the one being worked over, and $other is doing all of it"
            BenefitType.TOY_RECIPIENT -> "$who is the one the toy is used on, and $other is running it"
            BenefitType.FINGERING_RECIPIENT -> "$who is the one being opened up and worked"
            BenefitType.PENETRATION_RECIPIENT -> "$who is the one physically taken here"
            BenefitType.IMPACT_RECIPIENT -> "$who is the one taking every stroke, and $other is only landing them"
            BenefitType.HANDLING_RECIPIENT -> "$who is the one being moved and held, and $other is doing it to him"
            BenefitType.SERVICE_RECIPIENT -> "$who is served throughout, and $other does the serving"
            BenefitType.MUTUAL -> "neither of them is doing more of the work"
        }
        return "$who benefits more from this term because $reason. $who therefore earns $other's signature."
    }

    fun bundleExplanation(parts: List<Pair<Term, PartyBinding>>, setup: SharedSetup): String {
        val beneficiary = combinedBeneficiary(parts)
        return if (beneficiary == null) {
            "Taken together the two terms balance out, so consideration for this trade is reciprocal."
        } else {
            val who = setup.name(beneficiary)
            val other = setup.name(beneficiary.other)
            "Across both terms in this trade $who comes out ahead, so $who owes a stronger " +
                "consideration to earn $other's signature on the pair."
        }
    }
}
