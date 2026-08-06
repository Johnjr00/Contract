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
            BenefitType.RIMMING_RECIPIENT,
            BenefitType.ORGASM_CONTROL,
            BenefitType.OWNERSHIP -> 2
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
        val who = beneficiary?.let { setup.name(it) }
        val other = beneficiary?.let { setup.name(it.other) }
        val reason = when (term.benefitType) {
            BenefitType.MASSAGE_RECIPIENT -> "$receiver is the one being worked on here, and the player on the table gets more out of it than the player doing the work"
            BenefitType.KISS_RECIPIENT -> "$receiver is on the receiving end of the kissing, which is where the pleasure sits in this one"
            BenefitType.EAR_PLAY_RECIPIENT -> "$receiver is the one having his ears worked, and that is the whole point of the term"
            BenefitType.ORAL_RECIPIENT -> "$receiver has someone else's mouth on him for the whole term, and $giver is doing the work"
            BenefitType.RIMMING_RECIPIENT -> "$receiver is the one being eaten out, and $giver is the one putting in the effort"
            BenefitType.HAND_STIMULATION_RECIPIENT -> "$receiver is the one being worked over, and $giver is doing all of it"
            BenefitType.TOY_RECIPIENT -> "$receiver is the one the toy is being used on, and $giver is running it"
            BenefitType.FINGERING_RECIPIENT -> "$receiver is the one being opened up and worked, and $giver is doing it to him"
            BenefitType.PENETRATION_RECIPIENT -> "$receiver is the one physically taken here, which is the larger share of the term"
            BenefitType.COMMAND_AUTHORITY -> "$giver walks away with the authority in this one, and authority is the thing being traded"
            BenefitType.OWNERSHIP -> "$giver gains the ownership this term hands over, which is the whole substance of it"
            BenefitType.PERMISSION_CONTROL -> "$giver ends up deciding what $receiver is allowed to do, which is a real gain for $giver"
            BenefitType.RESTRAINT_CONTROL -> "$giver has control of the restraint and therefore of the whole term"
            BenefitType.ORGASM_CONTROL -> "$giver takes control of when $receiver is allowed to finish, and that control is the benefit"
            BenefitType.SERVICE_RECIPIENT -> "$receiver is served throughout, and $giver does the serving"
            BenefitType.MUTUAL -> "this one runs both ways and neither of them is doing more of the work"
        }
        return if (beneficiary == null || who == null || other == null) {
            "Benefit is balanced: $reason. Consideration for this term is reciprocal — both of you " +
                "perform, and both of you confirm."
        } else {
            "$who benefits more from this term because $reason. $who therefore earns $other's signature."
        }
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
