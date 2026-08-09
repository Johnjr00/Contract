package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.ConsiderationFamily
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.Term
import com.thecontract.core.model.TimerSpec

/** Short constructors so the content files stay readable. */

internal fun tm(label: String, seconds: Int): TimerSpec = TimerSpec(label, seconds)

@Suppress("LongParameterList")
internal fun t(
    id: String,
    level: Int,
    cats: Set<Category>,
    title: String,
    base: String,
    explicit: String,
    benefit: BenefitParty,
    type: BenefitType,
    acts: Set<String> = emptySet(),
    gPrefs: Set<String> = emptySet(),
    rPrefs: Set<String> = emptySet(),
    equip: Set<Equipment> = emptySet(),
    toy: Boolean = false,
    blocks: Set<Boundary> = emptySet(),
    erection: PartyRef = PartyRef.NEITHER,
    anal: Boolean = false,
    timers: List<TimerSpec> = emptyList(),
    gCon: PartyConstraint = PartyConstraint.ANY,
    rCon: PartyConstraint = PartyConstraint.ANY,
    mutual: Boolean = false,
    tail: String? = null,
    climax: Boolean = false
): Term = Term(
    id = id,
    level = level,
    categories = cats,
    title = title,
    base = base,
    explicit = explicit,
    benefitParty = benefit,
    benefitType = type,
    giverConstraint = gCon,
    receiverConstraint = rCon,
    activities = acts,
    extraGiverPrefs = gPrefs,
    extraReceiverPrefs = rPrefs,
    requiredEquipment = equip,
    genericToy = toy,
    blockedBy = blocks,
    erectionRequired = erection,
    analPenetration = anal,
    timers = timers,
    mutual = mutual,
    extremeTail = tail,
    climax = climax
)

@Suppress("LongParameterList")
internal fun c(
    id: String,
    intensity: Int,
    family: ConsiderationFamily,
    cats: Set<Category>,
    title: String,
    base: String,
    explicit: String,
    acts: Set<String> = emptySet(),
    pPrefs: Set<String> = emptySet(),
    rPrefs: Set<String> = emptySet(),
    equip: Set<Equipment> = emptySet(),
    toy: Boolean = false,
    blocks: Set<Boundary> = emptySet(),
    erection: PartyRef = PartyRef.NEITHER,
    anal: Boolean = false,
    pCon: PartyConstraint = PartyConstraint.ANY,
    timers: List<TimerSpec> = emptyList(),
    mutual: Boolean = false,
    pCock: Boolean = false,
    tail: String? = null
): ConsiderationAction = ConsiderationAction(
    id = id,
    intensity = intensity,
    family = family,
    categories = cats,
    title = title,
    base = base,
    explicit = explicit,
    activities = acts,
    extraPerformerPrefs = pPrefs,
    extraRecipientPrefs = rPrefs,
    requiredEquipment = equip,
    genericToy = toy,
    blockedBy = blocks,
    erectionRequired = erection,
    analPenetration = anal,
    performerConstraint = pCon,
    timers = timers,
    mutual = mutual,
    usesPerformersCock = pCock,
    extremeTail = tail
)
