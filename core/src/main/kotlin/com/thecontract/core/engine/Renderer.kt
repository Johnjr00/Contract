package com.thecontract.core.engine

import com.thecontract.core.model.Amendment
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.RenderedConsideration
import com.thecontract.core.model.RenderedTerm
import com.thecontract.core.model.RenderedTimer
import com.thecontract.core.model.Slot
import com.thecontract.core.model.Term
import com.thecontract.core.model.TimerSpec
import com.thecontract.core.style.RenderContext
import com.thecontract.core.style.StyleEngine

/**
 * Turns authored content into finished, displayable terms and consideration actions.
 *
 * Maybe conditions and counteroffer amendments do not appear as footnotes contradicting the
 * instruction — they rewrite the instruction and the timers before anything is shown.
 */
object Renderer {

    private const val MASSAGE_FIRST_SECONDS = 300

    fun context(ctx: GameContext, binding: PartyBinding): RenderContext = RenderContext(
        setup = ctx.setup,
        binding = binding,
        explicitness = ctx.setup.explicitness,
        availableEquipment = ctx.setup.equipment
    )

    private fun timerId(ownerId: String, index: Int) = "$ownerId#t$index"

    private fun renderTimers(
        ownerId: String,
        specs: List<TimerSpec>,
        rc: RenderContext
    ): List<RenderedTimer> = specs.mapIndexed { i, spec ->
        RenderedTimer(timerId(ownerId, i), StyleEngine.render(spec.label, rc).removeSuffix("."), spec.seconds)
    }

    private fun equipmentUsed(
        required: Set<Equipment>,
        genericToy: Boolean,
        rc: RenderContext
    ): List<String> = buildList {
        required.sortedBy { it.ordinal }.forEach { add(it.label) }
        if (genericToy) {
            Equipment.genericToyPreferenceOrder.firstOrNull { it in rc.availableEquipment }
                ?.let { if (it.label !in this) add(it.label) }
        }
    }

    fun renderTerm(
        term: Term,
        binding: PartyBinding,
        ctx: GameContext,
        conditions: List<MaybeCondition> = emptyList(),
        amendments: List<Amendment> = emptyList(),
        extraAmendmentLabels: List<String> = emptyList()
    ): RenderedTerm {
        val rc = context(ctx, binding)
        val instruction = StyleEngine.renderInstruction(term.base, term.explicit, term.extremeTail, rc)
        var rendered = RenderedTerm(
            termId = term.id,
            level = term.level,
            title = StyleEngine.render(term.title, rc).removeSuffix("."),
            instruction = instruction,
            timers = renderTimers(term.id, term.timers, rc),
            giver = binding.giver,
            receiver = binding.receiver,
            beneficiary = BenefitAnalysis.beneficiary(term, binding),
            benefitExplanation = BenefitAnalysis.explanation(term, binding, ctx.setup),
            equipmentUsed = equipmentUsed(term.requiredEquipment, term.genericToy, rc),
            categories = term.categories,
            analPenetration = term.analPenetration,
            climax = term.climax,
            mutual = term.mutual,
            amendments = extraAmendmentLabels
        )
        for (condition in conditions) rendered = applyCondition(condition, rendered, rc)
        for (amendment in amendments) rendered = applyAmendment(amendment, rendered, rc)
        return rendered
    }

    fun renderConsideration(
        action: ConsiderationAction,
        performer: Slot,
        recipient: Slot,
        ctx: GameContext
    ): RenderedConsideration {
        val binding = PartyBinding(performer, recipient)
        val rc = context(ctx, binding)
        return RenderedConsideration(
            actionId = action.id,
            title = StyleEngine.render(action.title, rc).removeSuffix("."),
            instruction = StyleEngine.renderInstruction(action.base, action.explicit, action.extremeTail, rc),
            timers = renderTimers(action.id, action.timers, rc),
            performer = performer,
            recipient = recipient,
            mutual = action.mutual,
            intensity = action.intensity,
            equipmentUsed = equipmentUsed(action.requiredEquipment, action.genericToy, rc)
        )
    }

    // ------------------------------------------------------------------ modifications

    private fun scaleTimers(timers: List<RenderedTimer>, factor: Double, floor: Int): List<RenderedTimer> =
        timers.map { it.copy(seconds = (it.seconds * factor).toInt().coerceAtLeast(floor)) }

    private fun capTotal(timers: List<RenderedTimer>, capSeconds: Int): List<RenderedTimer> {
        val total = timers.sumOf { it.seconds }
        if (total <= capSeconds || total == 0) return timers
        return scaleTimers(timers, capSeconds.toDouble() / total, 10)
    }

    private fun dropEquipment(
        rendered: RenderedTerm,
        predicate: (Equipment) -> Boolean
    ): List<String> {
        val dropped = Equipment.entries.filter(predicate).map { it.label }.toSet()
        return rendered.equipmentUsed.filterNot { it in dropped }
    }

    fun applyCondition(condition: MaybeCondition, r: RenderedTerm, rc: RenderContext): RenderedTerm {
        val label = condition.label
        val g = rc.giverName
        val recv = rc.receiverName
        return when (condition) {
            // Written as an override, because it lands on instructions that have already named a
            // pressure: "with hard pressure … lighter pressure" reads as a contradiction unless
            // the second sentence says plainly that it replaces the first.
            MaybeCondition.GENTLER -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Everything above is done at the gentler end instead: lighter pressure " +
                        "than written, a slower pace, and nothing taken as far as the term says."
                ),
                conditions = r.conditions + label
            )

            MaybeCondition.UNDER_TWO_MINUTES -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " The whole thing is kept under two minutes and stops there."
                ),
                timers = capTotal(r.timers, 120),
                conditions = r.conditions + label
            )

            MaybeCondition.ROLES_REVERSED -> r.copy(conditions = r.conditions + label)

            MaybeCondition.ONLY_AS_TRADE -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " This one only stands as half of a trade — it goes into the contract " +
                        "paired with a second term or not at all."
                ),
                requiresTrade = true,
                conditions = r.conditions + label
            )

            MaybeCondition.WITHOUT_TOY -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " No toy is used for this one: $g does with his hands and mouth " +
                        "everything a toy would have done."
                ),
                equipmentUsed = dropEquipment(r) { it.toyLike },
                conditions = r.conditions + label
            )

            MaybeCondition.WITHOUT_RESTRAINT -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Nothing is used to hold $recv down — he holds the position himself instead."
                ),
                equipmentUsed = dropEquipment(r) {
                    it in setOf(Equipment.CUFFS, Equipment.ROPE, Equipment.SPREADER_BAR, Equipment.LEASH)
                },
                conditions = r.conditions + label
            )

            MaybeCondition.SAVE_FOR_FINALE -> r.copy(
                deferToEnd = true,
                conditions = r.conditions + label
            )

            MaybeCondition.STOP_BEFORE_ORGASM -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " It stops before either of them finishes; neither comes during this term."
                ),
                conditions = r.conditions + label
            )

            MaybeCondition.MASSAGE_FIRST -> r.copy(
                instruction = StyleEngine.tidy(
                    "$g starts with five full minutes of massage before anything else happens. " + r.instruction
                ),
                timers = listOf(RenderedTimer("${r.termId}#massagefirst", "Massage first", MASSAGE_FIRST_SECONDS)) + r.timers,
                conditions = r.conditions + label
            )
        }
    }

    fun applyAmendment(amendment: Amendment, r: RenderedTerm, rc: RenderContext): RenderedTerm {
        val label = amendment.label
        val g = rc.giverName
        val recv = rc.receiverName
        return when (amendment) {
            Amendment.GENTLER -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Amended: everything above is done at lighter pressure and a slower " +
                        "pace than written."
                ),
                amendments = r.amendments + label
            )

            Amendment.SHORTER -> r.copy(
                instruction = StyleEngine.tidy(r.instruction + " Amended to run shorter than written."),
                timers = scaleTimers(r.timers, 0.6, 15),
                amendments = r.amendments + label
            )

            Amendment.ROLES_REVERSED -> r.copy(amendments = r.amendments + label)

            Amendment.MASSAGE_FIRST -> r.copy(
                instruction = StyleEngine.tidy(
                    "$g gives $recv five full minutes of massage before this term starts. " + r.instruction
                ),
                timers = listOf(RenderedTimer("${r.termId}#massagefirst", "Massage first", MASSAGE_FIRST_SECONDS)) + r.timers,
                amendments = r.amendments + label
            )

            Amendment.NO_TOYS -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Amended: no toy is used. $g does with his hands and mouth everything " +
                        "the toy would have done."
                ),
                equipmentUsed = dropEquipment(r) { it.toyLike },
                amendments = r.amendments + label
            )

            Amendment.NO_RESTRAINT -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Amended: nothing is used to restrain $recv. He holds the position himself."
                ),
                equipmentUsed = dropEquipment(r) {
                    it in setOf(Equipment.CUFFS, Equipment.ROPE, Equipment.SPREADER_BAR, Equipment.LEASH)
                },
                amendments = r.amendments + label
            )

            Amendment.STOP_BEFORE_ORGASM -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Amended: this term stops before either of them finishes."
                ),
                amendments = r.amendments + label
            )

            Amendment.SAVE_FOR_FINALE -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Amended: this term is held back to the end of the scene."
                ),
                deferToEnd = true,
                amendments = r.amendments + label
            )

            Amendment.TRADE_ONLY -> r.copy(
                instruction = StyleEngine.tidy(
                    r.instruction + " Amended: this term only goes into the contract as half of a trade."
                ),
                requiresTrade = true,
                amendments = r.amendments + label
            )
        }
    }

    /**
     * Only amendments that mean something for this particular term are offered (section 30).
     */
    fun relevantAmendments(term: Term, rendered: RenderedTerm): List<Amendment> = buildList {
        add(Amendment.GENTLER)
        if (rendered.timers.isNotEmpty()) add(Amendment.SHORTER)
        if (!term.mutual && !term.climax && term.giverConstraint == com.thecontract.core.model.PartyConstraint.ANY &&
            term.receiverConstraint == com.thecontract.core.model.PartyConstraint.ANY
        ) {
            add(Amendment.ROLES_REVERSED)
        }
        if (com.thecontract.core.model.Category.MASSAGE !in term.categories) add(Amendment.MASSAGE_FIRST)
        if (term.requiredEquipment.any { it.toyLike } || term.genericToy) add(Amendment.NO_TOYS)
        if (term.requiredEquipment.any {
                it in setOf(Equipment.CUFFS, Equipment.ROPE, Equipment.SPREADER_BAR, Equipment.LEASH)
            } || "restraint" in term.activities
        ) {
            add(Amendment.NO_RESTRAINT)
        }
        if (!term.climax) add(Amendment.STOP_BEFORE_ORGASM)
        if (!term.climax) {
            add(Amendment.SAVE_FOR_FINALE)
            add(Amendment.TRADE_ONLY)
        }
    }
}
