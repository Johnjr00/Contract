package com.thecontract.core.harness

import com.thecontract.core.model.AnalRole
import com.thecontract.core.model.Answer
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Explicitness
import com.thecontract.core.model.FinaleFormat
import com.thecontract.core.model.MaybeCondition
import com.thecontract.core.model.PlayerSetup
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.Role
import com.thecontract.core.model.SessionLength
import com.thecontract.core.model.SharedSetup

/** Shared setups and profiles for the three specified automated games (section 45). */
object Setups {

    fun allAnswers(answer: Answer): Map<String, Answer> =
        PreferenceLibrary.all.associate { it.id to answer }

    /** Game 1 — broad profile: everything on, all equipment, Vers and Vers, Extremely filthy. */
    fun broad(length: SessionLength = SessionLength.QUICK, finale: FinaleFormat = FinaleFormat.THREE_ORDERS) =
        SharedSetup(
            player1 = PlayerSetup("Marcus", Role.DOMINANT, AnalRole.VERS, false),
            player2 = PlayerSetup("Dan", Role.SUBMISSIVE, AnalRole.VERS, false),
            sessionLength = length,
            explicitness = Explicitness.EXTREME,
            finaleFormat = finale,
            stopWord = "red",
            defaultMaybeCondition = MaybeCondition.GENTLER,
            boundaries = emptySet(),
            equipment = Equipment.entries.toSet()
        )

    /**
     * Game 2 — mixed profile: Yes/Maybe/No, limited equipment, Vers Top and Vers Bottom,
     * one erection-difficulty selection, several boundaries, private Dominant finale.
     */
    fun mixed() = SharedSetup(
        player1 = PlayerSetup("Ravi", Role.DOMINANT, AnalRole.VERS_TOP, erectionDifficulty = false),
        player2 = PlayerSetup("Tom", Role.SUBMISSIVE, AnalRole.VERS_BOTTOM, erectionDifficulty = true),
        sessionLength = SessionLength.QUICK,
        explicitness = Explicitness.FILTHY,
        finaleFormat = FinaleFormat.DOMINANT_PRIVATE,
        stopWord = "amber",
        defaultMaybeCondition = MaybeCondition.GENTLER,
        boundaries = setOf(Boundary.NO_MARKS, Boundary.NO_DEGRADATION, Boundary.NO_FOOT_PLAY),
        equipment = setOf(
            Equipment.LUBRICANT,
            Equipment.MASSAGE_OIL,
            Equipment.BLINDFOLD,
            Equipment.STURDY_CHAIR,
            Equipment.TOWELS,
            Equipment.MUSIC
        )
    )

    /** A mixed profile: Yes on most things, Maybe on a band, No on a band. */
    fun mixedAnswers(seedOffset: Int = 0): Map<String, Answer> =
        PreferenceLibrary.all.mapIndexed { index, pref ->
            val bucket = (index + seedOffset) % 5
            pref.id to when {
                pref.activity in setOf("degradation", "foot_kissing", "foot_stimulation") -> Answer.NO
                bucket == 3 -> Answer.MAYBE
                bucket == 4 -> Answer.NO
                else -> Answer.YES
            }
        }.toMap()

    /** Game 3 answers: yes to whatever the boundaries and roles still allow. */
    fun restrictiveAnswers(): Map<String, Answer> = allAnswers(Answer.YES)

    /**
     * Game 3 — restrictive: both players have erection difficulty, no anal at all, no toys,
     * no pain, no rough sex, no degradation, no foot play, minimal equipment, one
     * uninterrupted finale.
     */
    fun restrictive() = SharedSetup(
        player1 = PlayerSetup("Sam", Role.DOMINANT, AnalRole.NO_ANAL, erectionDifficulty = true),
        player2 = PlayerSetup("Ben", Role.SUBMISSIVE, AnalRole.NO_ANAL, erectionDifficulty = true),
        sessionLength = SessionLength.QUICK,
        explicitness = Explicitness.EROTIC,
        finaleFormat = FinaleFormat.UNINTERRUPTED,
        stopWord = "stop",
        defaultMaybeCondition = MaybeCondition.UNDER_TWO_MINUTES,
        boundaries = setOf(
            Boundary.NO_ANAL_ACTIVITY,
            Boundary.NO_ANAL_PENETRATION,
            Boundary.NO_TOYS,
            Boundary.NO_PAIN,
            Boundary.NO_ROUGH_SEX,
            Boundary.NO_DEGRADATION,
            Boundary.NO_FOOT_PLAY
        ),
        equipment = setOf(Equipment.MASSAGE_OIL, Equipment.LUBRICANT, Equipment.TOWELS)
    )
}
