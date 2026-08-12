package com.thecontract.core

import com.thecontract.core.content.ContentLibrary
import com.thecontract.core.model.Answer
import com.thecontract.core.model.PreferenceLibrary
import com.thecontract.core.model.PreferenceSection
import com.thecontract.core.model.PrivateProfile
import com.thecontract.core.model.Slot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Questions the profile no longer asks, and takes as a Yes.
 *
 * The distinction that matters here is between removing a *question* and removing a *preference*.
 * Terms are written against activities, and eligibility looks up the giving and receiving side of
 * every one of them by id, so deleting the records would leave a term referring to nothing and
 * filter it out of the game entirely. The records stay; only the questions go.
 */
class AssumedPreferenceTest {

    @Test
    fun `the retired questions are not put to a player`() {
        val asked = PreferenceLibrary.asked.map { it.id }.toSet()
        RETIRED.forEach {
            assertFalse(it in asked, "'$it' is still being asked")
            assertTrue(PreferenceLibrary.byId[it] != null, "'$it' was deleted rather than assumed")
        }
        assertEquals(151, PreferenceLibrary.all.size, "a preference record was lost")
        assertEquals(151 - RETIRED.size, PreferenceLibrary.asked.size)
    }

    /** An empty heading reads as a question that failed to load. */
    @Test
    fun `a section with nothing left to ask is not shown`() {
        val shown = PreferenceLibrary.bySection.keys
        assertFalse(PreferenceSection.LANGUAGE in shown, "the emptied Language section is still shown")
        assertFalse(PreferenceSection.ORGASM in shown, "the emptied Orgasm section is still shown")
        assertEquals(10, shown.size)
        PreferenceLibrary.bySection.forEach { (s, ps) ->
            assertTrue(ps.isNotEmpty(), "${s.title} is shown with nothing in it")
        }
    }

    @Test
    fun `an assumed question answers Yes`() {
        val blank = PrivateProfile(Slot.PLAYER_1)
        RETIRED.forEach { assertEquals(Answer.YES, blank.answer(it), "'$it' did not come back Yes") }
    }

    /**
     * A profile saved while a question was still being asked must not keep a stale No alive: the
     * game no longer offers any way to change it, so it would filter content invisibly and for good.
     */
    @Test
    fun `a stored answer on a retired question is ignored`() {
        val stale = PrivateProfile(
            Slot.PLAYER_1,
            answers = RETIRED.associateWith { Answer.NO } + mapOf("oral_give" to Answer.NO)
        )
        RETIRED.forEach { assertEquals(Answer.YES, stale.answer(it), "a stale No survived on '$it'") }
        assertEquals(Answer.NO, stale.answer("oral_give"), "a live question stopped being honoured")
    }

    @Test
    fun `a question still asked is untouched`() {
        val blank = PrivateProfile(Slot.PLAYER_1)
        assertEquals(Answer.MAYBE, blank.answer("oral_give"), "a live question lost its Maybe default")
        assertEquals(Answer.MAYBE, blank.answer("no_such_preference"), "an unknown id was decided for the player")
    }

    /** The reason the records stay: every activity a term names still has both sides to look up. */
    @Test
    fun `every activity a term names still resolves`() {
        for (term in ContentLibrary.allTerms) {
            for (activity in term.activities) {
                assertTrue(
                    PreferenceLibrary.byId[PreferenceLibrary.give(activity)] != null,
                    "term ${term.id} names '$activity', which has no giving preference"
                )
                assertTrue(
                    PreferenceLibrary.byId[PreferenceLibrary.receive(activity)] != null,
                    "term ${term.id} names '$activity', which has no receiving preference"
                )
            }
        }
    }

    private companion object {
        /** The 56 questions retired, by id. */
        val RETIRED = listOf(
            "body_worship_give", "body_worship_receive",
            "foot_stimulation_give", "foot_stimulation_receive",
            "massage_general_give", "massage_general_receive",
            "massage_light_give", "massage_light_receive",
            "massage_deep_give", "massage_deep_receive",
            "massage_scalp_give", "massage_scalp_receive",
            "massage_jaw_face_give", "massage_jaw_face_receive",
            "massage_neck_shoulders_give", "massage_neck_shoulders_receive",
            "massage_upper_back_give", "massage_upper_back_receive",
            "massage_lower_back_give", "massage_lower_back_receive",
            "massage_chest_give", "massage_chest_receive",
            "massage_arms_hands_give", "massage_arms_hands_receive",
            "massage_buttocks_hips_give", "massage_buttocks_hips_receive",
            "massage_inner_thighs_give", "massage_inner_thighs_receive",
            "massage_calves_feet_give", "massage_calves_feet_receive",
            "massage_groin_give", "massage_groin_receive",
            "massage_oil_give", "massage_oil_receive",
            "ownership_language_give", "ownership_language_receive",
            "permission_control_give",
            "explicit_praise_give", "explicit_praise_receive",
            "degradation_give", "degradation_receive",
            "dirty_talk_give", "dirty_talk_receive",
            "edging_give", "edging_receive",
            "denial_give", "denial_receive",
            "climax_permission_give", "climax_permission_receive",
            "roleplay_lead_give", "roleplay_lead_receive",
            "strangers_fantasy", "authority_roleplay", "capture_roleplay",
            "mirror_play", "watching_porn"
        )
    }
}
