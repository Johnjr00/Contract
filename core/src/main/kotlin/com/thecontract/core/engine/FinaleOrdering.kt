package com.thecontract.core.engine

import com.thecontract.core.model.ExecutionStep
import com.thecontract.core.model.FinaleOrder
import com.thecontract.core.model.GamePhase
import com.thecontract.core.model.GameState
import com.thecontract.core.model.SignedTerm
import kotlin.random.Random

/**
 * The running order of the final scene (section 39).
 *
 * Held here rather than inside the engine because the view builder needs the same answer: how many
 * steps there are and which one is current decides what the screen says as much as what the
 * commands do, and the two reading different lists is how a step count can disagree with the
 * thing being performed.
 *
 * The order is computed once when the finale starts and then stored, not recomputed on demand.
 * "Dominant's cut" shuffles against a generator seeded from the session id, which is reproducible
 * within a build but not a promise across them — deriving it on every read would let an upgrade
 * mid-scene reorder the terms nobody has performed yet.
 */
object FinaleOrdering {

    /**
     * The stored order, or a rebuilt one if there is none.
     *
     * The fallback covers a session saved before the finale was stepped by half rather than by
     * contract item. Such a save has no stored steps, and returning nothing would leave the
     * television on an empty final scene with no way forward.
     */
    fun resolve(state: GameState): List<ExecutionStep> {
        state.finale.executionSteps.takeIf { it.isNotEmpty() }?.let { return it }
        if (state.phase != GamePhase.FINAL_EXECUTION) return emptyList()
        return build(state, state.finale.chosenOrder ?: FinaleOrder.SIGNED_ORDER)
    }

    /**
     * Builds the order from the signed contract.
     *
     * Terms held back by a "Save for finale" amendment move to the end of the regular block, and
     * the two climax terms are always last — ordinary reordering can never move them earlier.
     * With anal penetration in the contract the top's climax term comes first (section 37).
     */
    fun build(state: GameState, order: FinaleOrder): List<ExecutionStep> {
        val signed = state.negotiation.signed
        val regularIdx = signed.indices.filter { signed[it].closingFor == null }
        val closingIdx = signed.indices.filter { signed[it].closingFor != null }

        val (deferred, normal) = regularIdx.partition { signed[it].term.deferToEnd }
        val orderedNormal = when (order) {
            FinaleOrder.SIGNED_ORDER -> normal
            FinaleOrder.SMOOTH_ESCALATION -> normal.sortedBy { signed[it].term.level }
            FinaleOrder.DOMINANTS_CUT ->
                normal.shuffled(Random(state.sessionId.hashCode().toLong() * 31 + 7))
        }
        val orderedDeferred = deferred.sortedBy { signed[it].term.level }
        val orderedClosing = closingIdx.sortedByDescending { signed[it].term.analPenetration }

        return (orderedNormal + orderedDeferred + orderedClosing).flatMap { steps(signed, it) }
    }

    /**
     * One contract item as steps: one for an ordinary term, two for a trade.
     *
     * The halves stay adjacent. They were proposed together, approved together and paid for with
     * a single consideration, so separating them in performance would present the second as an
     * unrelated act somebody had already agreed to without saying when.
     */
    private fun steps(signed: List<SignedTerm>, index: Int): List<ExecutionStep> = buildList {
        add(ExecutionStep(index, bundled = false))
        if (signed[index].bundledTerm != null) add(ExecutionStep(index, bundled = true))
    }
}
