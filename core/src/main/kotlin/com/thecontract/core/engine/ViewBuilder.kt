package com.thecontract.core.engine

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.model.Act
import com.thecontract.core.model.Amendment
import com.thecontract.core.model.AnalRole
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.ConnectionState
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.FinaleFormat
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PartyBinding
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.RenderedConsideration
import com.thecontract.core.model.RenderedTerm
import com.thecontract.core.model.RenderedTimer
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.SignedTerm
import com.thecontract.core.model.Slot
import com.thecontract.core.protocol.Choice
import com.thecontract.core.protocol.ClientView
import com.thecontract.core.protocol.ConsiderationCard
import com.thecontract.core.protocol.DraftView
import com.thecontract.core.protocol.ExecutionView
import com.thecontract.core.protocol.JoinInfo
import com.thecontract.core.protocol.ProfileForm
import com.thecontract.core.protocol.ProfileItem
import com.thecontract.core.protocol.ProfileSection
import com.thecontract.core.protocol.ProgressView
import com.thecontract.core.protocol.SavedContractSummary
import com.thecontract.core.protocol.SetupForm
import com.thecontract.core.protocol.SetupOption
import com.thecontract.core.protocol.SignedTermCard
import com.thecontract.core.protocol.TermCard
import com.thecontract.core.protocol.TimerControl
import com.thecontract.core.protocol.TimerView

/**
 * Projects the authoritative state into per-audience views.
 *
 * This is the single place where privacy is enforced (sections 13, 41, 42). The TV projection
 * simply never contains a profile answer, an unsubmitted private selection or a private vote;
 * a phone projection never contains the other player's. Because clients render only what they
 * are given, there is no client-side path to a leak.
 */
object ViewBuilder {

    private const val WAITING_BODY = "Waiting for both phones. Nothing is shown here until both have answered."

    fun timerView(t: com.thecontract.core.model.GameTimer, nowMs: Long) = TimerView(
        id = t.id,
        label = t.label,
        parentInstruction = t.parentInstruction,
        totalSeconds = t.totalSeconds,
        remainingMs = t.remainingAt(nowMs),
        state = t.state.name
    )

    private fun timerViews(list: List<RenderedTimer>) = list.map {
        TimerView(it.id, it.label, "", it.seconds, it.seconds * 1000L, "IDLE")
    }

    private fun termCard(s: GameState, r: RenderedTerm): TermCard = TermCard(
        termId = r.termId,
        title = r.title,
        instruction = r.instruction,
        level = r.level,
        actTitle = Act.ofLevel(r.level).title,
        giverName = s.setup.name(r.giver),
        receiverName = s.setup.name(r.receiver),
        beneficiaryName = r.beneficiary?.let { s.setup.name(it) },
        benefitExplanation = r.benefitExplanation,
        equipment = r.equipmentUsed,
        conditions = r.conditions,
        amendments = r.amendments,
        timers = timerViews(r.timers),
        climax = r.climax,
        mutual = r.mutual
    )

    private fun considerationCard(s: GameState, c: RenderedConsideration): ConsiderationCard = ConsiderationCard(
        actionId = c.actionId,
        title = c.title,
        instruction = c.instruction,
        performerName = s.setup.name(c.performer),
        recipientName = s.setup.name(c.recipient),
        mutual = c.mutual,
        equipment = c.equipmentUsed,
        timers = timerViews(c.timers)
    )

    private fun progress(s: GameState) = ProgressView(
        signedRegular = s.negotiation.regularSlotsUsed,
        requiredRegular = s.requiredRegularTerms,
        signedClosing = s.negotiation.signedClosing.size,
        receipts = s.negotiation.receipts.size,
        act = ProposalSelector.currentAct(s).level,
        actTitle = ProposalSelector.currentAct(s).title
    )

    private fun draft(s: GameState): DraftView {
        val byAct = s.negotiation.signed.groupBy { signed ->
            signed.closingFor?.let { "Closing terms" } ?: Act.ofLevel(signed.term.level).let { "Act ${it.level} — ${it.title}" }
        }.mapValues { (_, list) -> list.map { signedCard(s, it) } }
        return DraftView(
            signedRegular = s.negotiation.regularSlotsUsed,
            requiredRegular = s.requiredRegularTerms,
            signedClosing = s.negotiation.signedClosing.size,
            receiptsCompleted = s.negotiation.receipts.size,
            byAct = byAct
        )
    }

    private fun signedCard(s: GameState, signed: SignedTerm): SignedTermCard {
        val receipt = s.negotiation.receipts.firstOrNull { it.id == signed.considerationReceiptId }
        return SignedTermCard(
            index = signed.index + 1,
            actTitle = signed.closingFor?.let { "Closing term for ${s.setup.name(it)}" }
                ?: "Act ${signed.act} — ${Act.ofLevel(signed.act).title}",
            title = signed.term.title,
            instruction = signed.term.instruction,
            bundledTitle = signed.bundledTerm?.title,
            bundledInstruction = signed.bundledTerm?.instruction,
            conditions = signed.term.conditions,
            amendments = signed.term.amendments,
            beneficiaryName = signed.term.beneficiary?.let { s.setup.name(it) },
            considerationTitle = receipt?.title,
            considerationInstruction = receipt?.instruction,
            signedBy = signed.signedBySlots.map { s.setup.name(it) },
            closingForName = signed.closingFor?.let { s.setup.name(it) },
            timers = timerViews(signed.term.timers)
        )
    }

    private fun setupForm(s: GameState): SetupForm {
        val setup = s.setup
        return SetupForm(
            player1Name = setup.player1.name,
            player2Name = setup.player2.name,
            dominantSlot = setup.dominant.name,
            analRoles = mapOf(
                Slot.PLAYER_1.name to setup.player1.analRole.name,
                Slot.PLAYER_2.name to setup.player2.analRole.name
            ),
            erectionDifficulty = mapOf(
                Slot.PLAYER_1.name to setup.player1.erectionDifficulty,
                Slot.PLAYER_2.name to setup.player2.erectionDifficulty
            ),
            sessionLength = setup.sessionLength.name,
            explicitness = setup.explicitness.name,
            finaleFormat = setup.finaleFormat.name,
            stopWord = setup.stopWord,
            defaultMaybeCondition = setup.defaultMaybeCondition.name,
            boundaries = setup.boundaries.map { it.name },
            equipment = setup.equipment.map { it.name },
            options = mapOf(
                "sessionLength" to SessionLength.entries.map {
                    SetupOption(it.name, it.name.lowercase().replaceFirstChar(Char::uppercase), "${it.regularTerms} regular terms plus 2 climax terms")
                },
                "analRole" to AnalRole.entries.map { SetupOption(it.name, analRoleLabel(it)) },
                "explicitness" to Explicitness.entries.map { SetupOption(it.name, explicitnessLabel(it)) },
                "finaleFormat" to FinaleFormat.entries.map { SetupOption(it.name, finaleFormatLabel(it)) },
                "maybeCondition" to MaybeCondition.entries.map { SetupOption(it.name, it.label) },
                "boundaries" to Boundary.entries.map { SetupOption(it.name, it.label) },
                "equipment" to Equipment.entries.map { SetupOption(it.name, it.label) }
            ),
            errors = setup.validationErrors()
        )
    }

    private fun analRoleLabel(r: AnalRole) = when (r) {
        AnalRole.TOP -> "Top"
        AnalRole.VERS_TOP -> "Vers Top"
        AnalRole.VERS -> "Vers"
        AnalRole.VERS_BOTTOM -> "Vers Bottom"
        AnalRole.BOTTOM -> "Bottom"
        AnalRole.NO_ANAL -> "No anal"
    }

    private fun explicitnessLabel(e: Explicitness) = when (e) {
        Explicitness.EROTIC -> "Erotic and polished"
        Explicitness.DIRECT -> "Direct"
        Explicitness.FILTHY -> "Filthy"
        Explicitness.EXTREME -> "Extremely filthy"
    }

    private fun finaleFormatLabel(f: FinaleFormat) = when (f) {
        FinaleFormat.THREE_ORDERS -> "Choose between three orders"
        FinaleFormat.DOMINANT_PRIVATE -> "Dominant chooses privately"
        FinaleFormat.UNINTERRUPTED -> "One uninterrupted escalating contract"
    }

    private fun finaleOrderLabel(o: FinaleOrder) = when (o) {
        FinaleOrder.SIGNED_ORDER -> "Signed Order"
        FinaleOrder.SMOOTH_ESCALATION -> "Smooth Escalation"
        FinaleOrder.DOMINANTS_CUT -> "Dominant's Cut"
    }

    private fun profileForm(s: GameState, slot: Slot): ProfileForm {
        val profile = s.profile(slot)
        return ProfileForm(
            sections = PreferenceLibrary.bySection.map { (section, prefs) ->
                ProfileSection(
                    id = section.id,
                    title = section.title,
                    items = prefs.map { p ->
                        ProfileItem(
                            id = p.id,
                            label = p.label,
                            answer = profile.answer(p.id).name,
                            condition = profile.maybeConditions[p.id]?.name
                        )
                    }
                )
            },
            complete = profile.complete,
            defaultConditionId = s.setup.defaultMaybeCondition.name,
            conditionOptions = MaybeCondition.entries.map { Choice(it.name, it.label, kind = "option") }
        )
    }

    // ------------------------------------------------------------------ phone view

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun phoneView(
        s: GameState,
        slot: Slot,
        connections: Map<Slot, ConnectionState>,
        nowMs: Long,
        canGoBack: Boolean
    ): ClientView {
        val base = baseView("phone", s, slot, connections, nowMs, canGoBack)
        val n = s.negotiation
        val current = n.current

        if (s.pause.paused) {
            return base.copy(
                heading = "Paused",
                body = "Everything is stopped. Both of you need to confirm before it starts again.",
                choices = listOf(
                    Choice("resume", "I'm ready to carry on"),
                    Choice("end", "End the session", danger = true)
                )
            )
        }
        if (s.draftReviewOpen) {
            return base.copy(
                heading = "Draft contract",
                body = "Everything signed so far. Private profile answers are not shown.",
                draft = draft(s),
                choices = listOf(Choice("close_draft", "Back to the game", kind = "nav"))
            )
        }

        return when (s.phase) {
            GamePhase.NO_SESSION, GamePhase.PAIRING -> base.copy(
                heading = "Connected",
                body = "Waiting for the other phone to join.",
                waiting = true
            )

            GamePhase.PLAYER_1_SETUP ->
                if (slot == Slot.PLAYER_1) {
                    base.copy(
                        heading = "Set the game up",
                        body = "You are Player 1, so you configure the shared setup. The other phone waits.",
                        setup = setupForm(s)
                    )
                } else {
                    base.copy(
                        heading = "Waiting",
                        body = "Player 1 is setting the game up. Nothing for you to do yet.",
                        waiting = true
                    )
                }

            GamePhase.WAITING_FOR_PLAYER_2 -> base.copy(
                heading = "Waiting for the second phone",
                body = "Setup is saved. The private profiles open as soon as the second phone joins.",
                waiting = true
            )

            GamePhase.PRIVATE_PROFILES, GamePhase.WAITING_FOR_PROFILES -> {
                val mine = s.profile(slot)
                if (mine.complete) {
                    base.copy(
                        heading = "Profile saved",
                        body = "Only you have seen your answers. Waiting for the other profile.",
                        waiting = true,
                        profile = profileForm(s, slot),
                        choices = listOf(Choice("reopen_profile", "Change my answers", kind = "nav"))
                    )
                } else {
                    base.copy(
                        heading = "Your private profile",
                        body = "Everything starts at Maybe. Nobody else — not the TV, not the other phone — " +
                            "ever sees these answers.",
                        profile = profileForm(s, slot)
                    )
                }
            }

            GamePhase.PROPOSAL, GamePhase.WAITING_FOR_PROPOSAL_RESPONSES -> {
                val responded = current?.responses?.containsKey(slot) == true
                if (current == null) {
                    base.copy(heading = "Preparing the next term", waiting = true)
                } else if (responded) {
                    base.copy(
                        heading = "Answer sent",
                        body = "Waiting for the other phone. Neither answer is shown until both are in.",
                        waiting = true,
                        term = termCard(s, current.term),
                        canOpenDraft = true
                    )
                } else {
                    base.copy(
                        heading = "Proposed term",
                        body = "Read it, then choose. Your answer is private until both of you have sent one.",
                        term = termCard(s, current.term),
                        choices = buildList {
                            add(Choice("sign", "Sign it"))
                            add(Choice("counteroffer", "Counteroffer"))
                            if (s.regularTermsRemaining >= 2) add(Choice("bundle", "Trade — bundle it with another term"))
                            add(Choice("reject", "Reject", danger = true))
                            add(Choice("open_draft", "Review the draft", kind = "nav"))
                        },
                        canOpenDraft = true,
                        blockedNotice = current.blockedBoundary?.let {
                            "A proposal was blocked by your shared boundary: $it. It was replaced and nothing was used up."
                        }
                    )
                }
            }

            GamePhase.COUNTEROFFER_PRIVATE_SELECTION -> {
                if (current == null) return base.copy(heading = "Preparing", waiting = true)
                val mayPick = current.responses[slot] == com.thecontract.core.model.ProposalResponse.COUNTEROFFER
                when {
                    current.amendmentBallot.isNotEmpty() && slot !in current.amendmentVotes -> base.copy(
                        heading = "Two amendments on the table",
                        body = "You each asked for something different. Pick the one you can both live with.",
                        term = termCard(s, current.term),
                        choices = current.amendmentBallot.map { Choice("vote_amendment:${it.name}", it.label) }
                    )

                    current.amendmentBallot.isNotEmpty() -> base.copy(
                        heading = "Vote sent", body = WAITING_BODY, waiting = true,
                        term = termCard(s, current.term)
                    )

                    mayPick && slot !in current.amendmentPicks -> base.copy(
                        heading = "Choose your amendment",
                        body = "This is private until it is applied.",
                        term = termCard(s, current.term),
                        choices = relevantAmendmentChoices(s, current.term)
                    )

                    else -> base.copy(
                        heading = "Waiting", body = WAITING_BODY, waiting = true,
                        term = termCard(s, current.term)
                    )
                }
            }

            GamePhase.COUNTEROFFER_APPROVAL -> {
                if (current == null) return base.copy(heading = "Preparing", waiting = true)
                if (slot in current.amendmentApproval) {
                    base.copy(heading = "Vote sent", body = WAITING_BODY, waiting = true, term = termCard(s, current.term))
                } else {
                    base.copy(
                        heading = "Amended term",
                        body = "This is the amended version. Sign it or reject it.",
                        term = termCard(s, current.term),
                        choices = listOf(
                            Choice("approve_amended:true", "Sign the amended term"),
                            Choice("approve_amended:false", "Reject", danger = true)
                        )
                    )
                }
            }

            GamePhase.BUNDLE_PRIVATE_SELECTION -> {
                if (current == null) return base.copy(heading = "Preparing", waiting = true)
                if (slot == current.bundleInitiator) {
                    base.copy(
                        heading = "Pick the second term",
                        body = "A trade uses two of your regular term slots and needs a stronger consideration.",
                        term = termCard(s, current.term),
                        termOptions = current.bundleCandidateIds.mapNotNull { id -> bundleOptionCard(s, id) },
                        choices = current.bundleCandidateIds.mapNotNull { id ->
                            ContentLibrary.termsById[id]?.let { Choice("pick_bundle:$id", it.title) }
                        }
                    )
                } else {
                    base.copy(heading = "Waiting", body = WAITING_BODY, waiting = true, term = termCard(s, current.term))
                }
            }

            GamePhase.BUNDLE_APPROVAL -> {
                if (current == null) return base.copy(heading = "Preparing", waiting = true)
                if (slot in current.bundleApproval) {
                    base.copy(heading = "Vote sent", body = WAITING_BODY, waiting = true, term = termCard(s, current.term))
                } else {
                    base.copy(
                        heading = "The trade",
                        body = "Both terms go in together, or neither does.",
                        term = termCard(s, current.term),
                        bundledTerm = current.bundledTerm?.let { termCard(s, it) },
                        choices = listOf(
                            Choice("vote_bundle:true", "Sign both"),
                            Choice("vote_bundle:false", "Reject the trade", danger = true)
                        )
                    )
                }
            }

            GamePhase.CONSIDERATION_PRIVATE_SELECTION, GamePhase.CLOSING_TERM_CONSIDERATION -> {
                if (current == null) return base.copy(heading = "Preparing", waiting = true)
                val mutual = current.beneficiary == null
                val mine = mutual || current.beneficiary == slot
                if (mine) {
                    base.copy(
                        heading = "Choose your consideration",
                        body = "You benefit more from this term, so you earn the signature. " +
                            "This list is private — the TV shows nothing while you choose.",
                        term = termCard(s, current.term),
                        bundledTerm = current.bundledTerm?.let { termCard(s, it) },
                        considerationOptions = current.considerationOptions.map { considerationCard(s, it) },
                        choices = current.considerationOptions.map { Choice("pick_consideration:${it.actionId}", it.title) }
                    )
                } else {
                    base.copy(
                        heading = "Waiting",
                        body = "The other player is choosing what he will do to earn your signature.",
                        waiting = true,
                        term = termCard(s, current.term)
                    )
                }
            }

            GamePhase.CONSIDERATION_PUBLIC_EXECUTION -> {
                val c = current?.consideration ?: return base.copy(heading = "Preparing", waiting = true)
                val isPerformer = c.mutual || c.performer == slot
                base.copy(
                    heading = "Consideration",
                    body = if (isPerformer) {
                        "You are doing this now. You control the timers."
                    } else {
                        "He is doing this for you now. You confirm when the signature is earned."
                    },
                    term = termCard(s, current.term),
                    consideration = considerationCard(s, c),
                    choices = buildList {
                        if (isPerformer && slot !in current.considerationConfirmed) {
                            add(Choice("consideration_performed", "Done"))
                        }
                        add(Choice("open_draft", "Review the draft", kind = "nav"))
                        if (canGoBack) add(Choice("back", "Back", kind = "nav"))
                    }
                )
            }

            GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION -> {
                val c = current?.consideration ?: return base.copy(heading = "Preparing", waiting = true)
                val confirmers = if (c.mutual) setOf(c.performer, c.recipient) else setOf(c.recipient)
                if (slot in confirmers && slot !in current.signatures) {
                    base.copy(
                        heading = "Did he earn it?",
                        body = "Confirm the signature, or ask for it again.",
                        term = termCard(s, current.term),
                        consideration = considerationCard(s, c),
                        choices = listOf(
                            Choice("confirm_signature:true", "Signature earned — sign the term"),
                            Choice("confirm_signature:false", "Repeat or choose another consideration")
                        )
                    )
                } else {
                    base.copy(
                        heading = "Waiting for his confirmation",
                        body = "He decides whether the signature is earned.",
                        waiting = true,
                        term = termCard(s, current.term),
                        consideration = considerationCard(s, c)
                    )
                }
            }

            GamePhase.TERM_SIGNED -> {
                val last = n.signed.lastOrNull()
                base.copy(
                    heading = "Signed",
                    body = last?.let { "\"${it.term.title}\" is in the contract." } ?: "Term signed.",
                    term = last?.let { termCard(s, it.term) },
                    choices = listOf(
                        Choice("continue", "Next"),
                        Choice("open_draft", "Review the draft", kind = "nav")
                    ),
                    canOpenDraft = true
                )
            }

            GamePhase.CLOSING_TERM_SELECTION -> {
                val turn = n.closingTurn
                if (current != null) {
                    if (slot in current.signatures) {
                        base.copy(heading = "Vote sent", body = WAITING_BODY, waiting = true, term = termCard(s, current.term))
                    } else {
                        base.copy(
                            heading = "Closing term",
                            body = "This is the term that guarantees ${s.setup.name(current.term.receiver)} finishes.",
                            term = termCard(s, current.term),
                            choices = listOf(
                                Choice("vote_closing:true", "Sign it"),
                                Choice("vote_closing:false", "Choose a different one", danger = true)
                            )
                        )
                    }
                } else if (turn == slot) {
                    base.copy(
                        heading = "Your closing term",
                        body = "Pick how you finish. Only you can see this list.",
                        termOptions = n.closingOptionIds.mapNotNull { closingOptionCard(s, it, slot) },
                        choices = n.closingOptionIds.mapNotNull { id ->
                            ContentLibrary.termsById[id]?.let { Choice("pick_closing:$id", it.title) }
                        }
                    )
                } else {
                    base.copy(
                        heading = "Waiting",
                        body = "He is choosing his own closing term.",
                        waiting = true
                    )
                }
            }

            GamePhase.FINAL_CONTRACT_REVIEW -> base.copy(
                heading = "The contract is complete",
                body = "Every term is signed. Read it through, then move to the finale.",
                draft = draft(s),
                choices = listOf(Choice("continue", "Go to the finale"))
            )

            GamePhase.FINALE_ORDER_SELECTION -> {
                val finale = s.finale
                when {
                    finale.revealedBallot.isNotEmpty() && slot == s.setup.dominant -> base.copy(
                        heading = "You decide",
                        body = "You picked different orders. As Dominant, you choose between them. " +
                            "The signed content does not change either way.",
                        choices = finale.revealedBallot.map { Choice("pick_finale_order:${it.name}", finaleOrderLabel(it)) }
                    )

                    finale.revealedBallot.isNotEmpty() -> base.copy(
                        heading = "Waiting",
                        body = "You picked different orders, so the Dominant decides between them.",
                        waiting = true
                    )

                    slot in finale.orderVotes -> base.copy(
                        heading = "Vote sent", body = WAITING_BODY, waiting = true
                    )

                    else -> base.copy(
                        heading = "How should the scene run?",
                        body = "Your vote is private. The signed content is identical in all three.",
                        choices = FinaleOrder.entries.map {
                            Choice("pick_finale_order:${it.name}", finaleOrderLabel(it), finaleOrderDetail(it))
                        }
                    )
                }
            }

            GamePhase.PRIVATE_DOMINANT_FINALE_SELECTION ->
                if (slot == s.setup.dominant) {
                    base.copy(
                        heading = "Choose the order",
                        body = "Only you see this. Nothing is revealed until you submit it.",
                        choices = FinaleOrder.entries.map {
                            Choice("pick_finale_order:${it.name}", finaleOrderLabel(it), finaleOrderDetail(it))
                        }
                    )
                } else {
                    base.copy(heading = "Waiting", body = "The Dominant is choosing the order.", waiting = true)
                }

            GamePhase.FINAL_EXECUTION -> executionView(s, slot, base, nowMs)

            GamePhase.COMPLETED -> base.copy(
                heading = "Done",
                body = "The contract has been carried out. It can be saved on the TV.",
                draft = draft(s),
                choices = listOf(Choice("save_contract", "Save this contract"))
            )

            GamePhase.PAUSED -> base.copy(heading = "Paused", body = "Everything is stopped.", waiting = true)
        }
    }

    private fun finaleOrderDetail(o: FinaleOrder) = when (o) {
        FinaleOrder.SIGNED_ORDER -> "Exactly the order you signed them in."
        FinaleOrder.SMOOTH_ESCALATION -> "Gentlest first, building to the hardest."
        FinaleOrder.DOMINANTS_CUT -> "A shuffled running order."
    }

    private fun relevantAmendmentChoices(s: GameState, r: RenderedTerm): List<Choice> {
        val term = ContentLibrary.termsById[r.termId] ?: return emptyList()
        val ctx = GameContext(s.setup, s.profiles)
        return Renderer.relevantAmendments(term, r)
            .filter { amendment ->
                if (amendment != Amendment.ROLES_REVERSED) return@filter true
                val reversed = PartyBinding(r.giver, r.receiver).reversed()
                val e = EligibilityEngine.evaluate(term, ctx, preferredBinding = reversed)
                e is Eligibility.Ok && e.binding == reversed
            }
            .map { Choice("pick_amendment:${it.name}", it.label) }
    }

    private fun bundleOptionCard(s: GameState, termId: String): TermCard? {
        val term = ContentLibrary.termsById[termId] ?: return null
        val ctx = GameContext(s.setup, s.profiles)
        val e = EligibilityEngine.evaluate(term, ctx)
        if (e !is Eligibility.Ok) return null
        return termCard(s, Renderer.renderTerm(term, e.binding, ctx, EligibilityEngine.maybeConditions(term, e.binding, ctx)))
    }

    private fun closingOptionCard(s: GameState, termId: String, finisher: Slot): TermCard? {
        val term = ContentLibrary.termsById[termId] ?: return null
        val ctx = GameContext(s.setup, s.profiles)
        val binding = PartyBinding(finisher.other, finisher)
        val e = EligibilityEngine.evaluate(term, ctx, preferredBinding = binding)
        if (e !is Eligibility.Ok || e.binding != binding) return null
        return termCard(s, Renderer.renderTerm(term, binding, ctx, EligibilityEngine.maybeConditions(term, binding, ctx)))
    }

    private fun executionView(s: GameState, slot: Slot?, base: ClientView, nowMs: Long): ClientView {
        val idx = s.finale.executionOrder.getOrNull(s.finale.stepIndex)
        val signed = idx?.let { s.negotiation.signed.getOrNull(it) }
        val card = signed?.let { termCard(s, it.term) }
        val started = s.finale.stepStarted
        val mayControl = TimerEngine.mayControl(s.timers, slot)
        val mayComplete = TimerEngine.mayComplete(s.timers, slot)
        return base.copy(
            heading = signed?.term?.title ?: "Final scene",
            body = signed?.term?.instruction ?: "",
            term = card,
            execution = ExecutionView(
                stepIndex = s.finale.stepIndex + 1,
                stepCount = s.finale.executionOrder.size,
                started = started,
                canPrevious = s.finale.stepIndex > 0,
                canNext = s.finale.stepIndex in s.finale.stepsCompleted ||
                    s.finale.stepIndex < s.finale.executionOrder.size,
                term = card,
                stopWord = s.setup.stopWord
            ),
            choices = buildList {
                if (!started) add(Choice("exec:begin", "Begin this term"))
                if (started && mayComplete) add(Choice("exec:complete", "Mark it complete"))
                if (started) add(Choice("exec:next", "Next term", kind = "nav"))
                if (s.finale.stepIndex > 0) add(Choice("exec:prev", "Previous term", kind = "nav"))
                add(Choice("global_pause", "Pause everything", danger = true))
            },
            timerControl = TimerControl(
                mayControl = mayControl,
                mayComplete = mayComplete,
                controllerName = s.timers.controllerSlot?.let { s.setup.name(it) },
                showStopAll = s.timers.timers.size > 1
            ),
            stopWord = s.setup.stopWord
        )
    }

    // ------------------------------------------------------------------ TV view

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun tvView(
        s: GameState,
        connections: Map<Slot, ConnectionState>,
        join: JoinInfo?,
        nowMs: Long,
        savedContracts: List<SavedContractSummary> = emptyList()
    ): ClientView {
        val base = baseView("tv", s, null, connections, nowMs, canGoBack = false)
            .copy(join = join, savedContracts = savedContracts)
        val n = s.negotiation
        val current = n.current

        if (s.pause.paused) {
            return base.copy(
                heading = "Paused",
                body = "The session is paused. It restarts when both of you confirm on your phones, " +
                    "or from this remote if a phone is offline.",
                choices = listOf(
                    Choice("tv_resume", "Resume from the remote"),
                    Choice("end", "End the session", danger = true)
                ),
                join = null
            )
        }
        if (s.draftReviewOpen) {
            return base.copy(
                heading = "Draft contract",
                body = "Signed terms only. Private profile answers are never shown here.",
                draft = draft(s)
            )
        }

        val bothConnected = connections.values.count { it == ConnectionState.CONNECTED } == 2
        return when (s.phase) {
            GamePhase.NO_SESSION -> base.copy(heading = "The Contract", body = "No session is running.")

            GamePhase.PAIRING, GamePhase.PLAYER_1_SETUP, GamePhase.WAITING_FOR_PLAYER_2 -> base.copy(
                heading = if (bothConnected) "Both phones connected" else "Scan to join",
                body = when {
                    !bothConnected -> "Open the camera on your phone and scan the code. The first phone " +
                        "becomes Player 1 and runs the setup."
                    s.phase == GamePhase.PLAYER_1_SETUP -> "Player 1 is configuring the game on his phone."
                    else -> "Setup saved. Waiting for the second phone."
                },
                // Section 7: the code disappears from the normal screen once both phones are in.
                join = if (bothConnected) null else join,
                waiting = s.phase == GamePhase.PLAYER_1_SETUP
            )

            GamePhase.PRIVATE_PROFILES, GamePhase.WAITING_FOR_PROFILES -> base.copy(
                heading = "Private profiles",
                body = "Both of you are filling these in on your own phone. Nothing you answer appears here.",
                waiting = true,
                choices = Slot.entries.map {
                    Choice(
                        "profile_status_${it.name}",
                        "${s.setup.name(it)}: ${if (s.profile(it).complete) "complete" else "in progress"}",
                        kind = "status"
                    )
                }
            )

            GamePhase.PROPOSAL, GamePhase.WAITING_FOR_PROPOSAL_RESPONSES -> base.copy(
                heading = "Proposed term",
                body = if (current == null) "Preparing the next term." else "Waiting for both players' responses.",
                term = current?.let { termCard(s, it.term) },
                waiting = true,
                blockedNotice = current?.blockedBoundary?.let {
                    "A proposal was blocked by a shared boundary: $it. It was replaced; nothing was used up."
                }
            )

            GamePhase.COUNTEROFFER_PRIVATE_SELECTION, GamePhase.BUNDLE_PRIVATE_SELECTION -> base.copy(
                heading = "Private choice in progress",
                body = "A private selection is being made on a phone. Nothing is shown here until it is submitted.",
                waiting = true
            )

            GamePhase.COUNTEROFFER_APPROVAL -> base.copy(
                heading = "Amended term",
                body = "Waiting for both players' votes.",
                term = current?.let { termCard(s, it.term) },
                waiting = true
            )

            GamePhase.BUNDLE_APPROVAL -> base.copy(
                heading = "Trade proposed",
                body = "Two terms, signed together. Waiting for both players' votes.",
                term = current?.let { termCard(s, it.term) },
                bundledTerm = current?.bundledTerm?.let { termCard(s, it) },
                waiting = true
            )

            GamePhase.CONSIDERATION_PRIVATE_SELECTION, GamePhase.CLOSING_TERM_CONSIDERATION -> base.copy(
                heading = "Choosing consideration",
                body = "A private list is open on one phone. Nothing is shown here until it is chosen.",
                waiting = true
            )

            GamePhase.CONSIDERATION_PUBLIC_EXECUTION, GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION -> base.copy(
                heading = "Consideration",
                body = current?.consideration?.instruction ?: "",
                term = current?.let { termCard(s, it.term) },
                consideration = current?.consideration?.let { considerationCard(s, it) },
                waiting = s.phase == GamePhase.WAITING_FOR_SIGNATURE_CONFIRMATION,
                timerControl = TimerControl(
                    mayControl = true,
                    mayComplete = true,
                    controllerName = s.timers.controllerSlot?.let { s.setup.name(it) },
                    showStopAll = s.timers.timers.size > 1
                )
            )

            GamePhase.TERM_SIGNED -> base.copy(
                heading = "Signed",
                body = n.signed.lastOrNull()?.let { "\"${it.term.title}\" is in the contract." } ?: "",
                term = n.signed.lastOrNull()?.let { termCard(s, it.term) }
            )

            GamePhase.CLOSING_TERM_SELECTION ->
                if (current == null) {
                    base.copy(
                        heading = "Closing terms",
                        body = "A closing term is being chosen privately on a phone.",
                        waiting = true
                    )
                } else {
                    base.copy(
                        heading = "Closing term",
                        body = "Waiting for both players' votes.",
                        term = termCard(s, current.term),
                        waiting = true
                    )
                }

            GamePhase.FINAL_CONTRACT_REVIEW -> base.copy(
                heading = "Signed contract",
                body = "Every term is signed.",
                draft = draft(s)
            )

            GamePhase.FINALE_ORDER_SELECTION ->
                if (s.finale.revealedBallot.isNotEmpty()) {
                    base.copy(
                        heading = "Two orders on the table",
                        body = s.finale.revealedBallot.joinToString(" or ") { finaleOrderLabel(it) } +
                            ". The Dominant decides.",
                        waiting = true
                    )
                } else {
                    base.copy(heading = "Choosing the running order", body = WAITING_BODY, waiting = true)
                }

            GamePhase.PRIVATE_DOMINANT_FINALE_SELECTION -> base.copy(
                heading = "Choosing the running order",
                body = "The order is being chosen privately. It is revealed when it is submitted.",
                waiting = true
            )

            GamePhase.FINAL_EXECUTION -> executionView(s, null, base, nowMs)

            GamePhase.COMPLETED -> base.copy(
                heading = "Complete",
                body = "The contract has been carried out.",
                draft = draft(s),
                choices = listOf(
                    Choice("save_contract", "Save this contract"),
                    Choice("new_game", "Start a new game")
                )
            )

            GamePhase.PAUSED -> base.copy(heading = "Paused", body = "The session is paused.")
        }
    }

    private fun baseView(
        audience: String,
        s: GameState,
        slot: Slot?,
        connections: Map<Slot, ConnectionState>,
        nowMs: Long,
        canGoBack: Boolean
    ) = ClientView(
        audience = audience,
        sessionId = s.sessionId,
        version = s.version,
        phase = s.phase,
        slot = slot,
        names = Slot.entries.associate { it.name to s.setup.name(it) },
        roles = Slot.entries.associate { it.name to s.setup.player(it).role.name },
        connections = connections.entries.associate { it.key.name to it.value.name },
        paused = s.pause.paused,
        timers = s.timers.timers.map { timerView(it, nowMs) },
        timerControl = if (s.timers.timers.isEmpty()) {
            null
        } else {
            TimerControl(
                mayControl = TimerEngine.mayControl(s.timers, slot),
                mayComplete = TimerEngine.mayComplete(s.timers, slot),
                controllerName = s.timers.controllerSlot?.let { s.setup.name(it) },
                showStopAll = s.timers.timers.size > 1
            )
        },
        progress = if (s.setupComplete) progress(s) else null,
        canGoBack = canGoBack,
        stopWord = if (s.setupComplete) s.setup.stopWord else null
    )
}
