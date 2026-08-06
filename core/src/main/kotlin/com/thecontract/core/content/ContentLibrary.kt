package com.thecontract.core.content

import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.Preference
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.Term

/**
 * The complete authored content library.
 *
 * Specification floors: 202 proposed terms, 120 consideration actions, 132 private preferences,
 * five intensity levels with at least 42/40/40/40/40 terms respectively. The
 * [com.thecontract.core.validation.ContentValidator] asserts all of this, and the test suite
 * fails the build if any floor is breached.
 */
object ContentLibrary {

    /** Regular proposed terms, levels 1..5. Never includes the two mandatory closing terms. */
    val regularTerms: List<Term> =
        TermsLevel1.terms + TermsLevel2.terms + TermsLevel3.terms + TermsLevel4.terms + TermsLevel5.terms

    /** Candidate closing (climax) terms. `{R}` is always the player who finishes. */
    val climaxTerms: List<Term> = ClimaxTerms.terms

    val allTerms: List<Term> = regularTerms + climaxTerms

    val considerations: List<ConsiderationAction> =
        ConsiderationsNonSexual.actions + ConsiderationsSexual.actions

    val preferences: List<Preference> = PreferenceLibrary.all

    val termsById: Map<String, Term> = allTerms.associateBy { it.id }
    val considerationsById: Map<String, ConsiderationAction> = considerations.associateBy { it.id }

    fun term(id: String): Term = termsById[id] ?: error("Unknown term id: $id")
    fun consideration(id: String): ConsiderationAction =
        considerationsById[id] ?: error("Unknown consideration id: $id")

    fun regularTermsAtLevel(level: Int): List<Term> = regularTerms.filter { it.level == level }

    /** Counts used by the implementation report and by the validation suite. */
    fun counts(): Map<String, Int> = buildMap {
        put("proposedTerms", regularTerms.size)
        put("climaxTerms", climaxTerms.size)
        put("considerationActions", considerations.size)
        put("privatePreferences", preferences.size)
        put("equipmentOptions", com.thecontract.core.model.Equipment.entries.size)
        put("boundaries", com.thecontract.core.model.Boundary.entries.size)
        for (level in 1..5) put("termsLevel$level", regularTermsAtLevel(level).size)
    }
}
