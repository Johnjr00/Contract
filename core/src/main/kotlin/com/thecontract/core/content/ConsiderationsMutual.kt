package com.thecontract.core.content

import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.BODY_WORSHIP
import com.thecontract.core.model.Category.EAR_PLAY
import com.thecontract.core.model.Category.HANDS
import com.thecontract.core.model.Category.IMPACT
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.MASSAGE
import com.thecontract.core.model.Category.ORAL
import com.thecontract.core.model.Category.RIMMING
import com.thecontract.core.model.Category.SPIT
import com.thecontract.core.model.Category.TOYS
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.ConsiderationFamily.EAR_PLAY as F_EAR
import com.thecontract.core.model.ConsiderationFamily.KISSING as F_KISS
import com.thecontract.core.model.ConsiderationFamily.MASSAGE as F_MASSAGE
import com.thecontract.core.model.ConsiderationFamily.SEXUAL as F_SEX
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyRef

/**
 * Reciprocal consideration actions — the ones offered when a term leaves the benefit balanced.
 *
 * A term that does nothing to either man's body puts neither of them ahead, so consideration for
 * it is reciprocal: both men perform and both confirm. Those offers used to come from a pool of
 * six actions covering three intensities, which meant a balanced term late in a game could reach
 * the phone with one option on it and no real choice at all. This file is that pool, written to
 * carry a full list at every band.
 *
 * Two authoring rules follow from what reciprocal means here. Both men do the same thing, either
 * at the same time or in named turns with a timer each — nothing where one man works and the
 * other only receives, because that is a one-sided action wearing a reciprocal label. And neither
 * man's own cock is the tool, so no action here sets `pCock`.
 *
 * Authoring convention: `{G}` and `{R}` are the two men, and every instruction names both.
 */
internal object ConsiderationsMutual {

    val actions: List<ConsiderationAction> = listOf(

        // ------------------------------------------------------ band 1: above the waist (10)
        c(
            id = "cm_kiss_hands_flat", intensity = 1, family = F_KISS, cats = setOf(KISSING),
            title = "Kissing, hands down",
            base = "{G} and {R} kneel facing each other and kiss deeply and slowly with both hands flat on their own thighs, and neither hand moves.",
            explicit = "{G} and {R} kneel face to face and kiss deep and open-mouthed with both hands flat on their own thighs. Neither one lifts a hand before the timer ends.",
            acts = setOf("long_making_out"), mutual = true,
            timers = listOf(tm("Kissing, hands down", 120))
        ),
        c(
            id = "cm_neck_at_once", intensity = 1, family = F_KISS, cats = setOf(KISSING),
            title = "Necks at the same time",
            base = "{G} and {R} lie face to face on their sides and kiss each other's necks at the same time, from the ear down to the collarbone and back up.",
            explicit = "{G} and {R} lie on their sides face to face and work each other's necks with their mouths at once, ear to collarbone and back up, both of them going the whole time.",
            acts = setOf("neck_kissing"), mutual = true,
            timers = listOf(tm("Both necks", 90))
        ),
        c(
            id = "cm_scalp_both", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Scalps at once",
            base = "{G} and {R} sit face to face, put both hands in each other's hair and work the scalp with firm, even pressure at the same time.",
            explicit = "{G} and {R} sit face to face with both hands in each other's hair and work the scalp with hard, digging pressure at once, and neither pair of hands stops.",
            acts = setOf("massage_scalp"), mutual = true,
            timers = listOf(tm("Both scalps", 90))
        ),
        c(
            id = "cm_shoulders_thumbs", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Thumbs into shoulders",
            base = "{G} and {R} sit face to face and press their thumbs into the tops of each other's shoulders with firm, even pressure, working outwards from the neck.",
            explicit = "{G} and {R} sit face to face and dig their thumbs into the tops of each other's shoulders with hard pressure, working out from the neck to the arm and back in.",
            acts = setOf("massage_neck_shoulders"), mutual = true,
            timers = listOf(tm("Both shoulders", 120))
        ),
        c(
            id = "cm_jaw_and_temples", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Jaw and temples",
            base = "{G} and {R} sit face to face and work each other's jaw and temples [adv_pressure_light], fingertips only, thirty seconds on the jaw and thirty on the temples.",
            explicit = "{G} and {R} sit face to face and work each other's jaw and temples [adv_pressure_light] at once, fingertips only, thirty seconds on the jaw and thirty on the temples.",
            acts = setOf("massage_jaw_face"), mutual = true,
            timers = listOf(tm("Jaw", 30), tm("Temples", 30))
        ),
        c(
            id = "cm_hands_forearms", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Hands and forearms",
            base = "{G} and {R} sit knee to knee and work each other's hands and forearms, one arm at a time, thumb into the palm and then up the inside of the arm.",
            explicit = "{G} and {R} sit knee to knee and work each other's hands and forearms, one arm each at a time, thumb pressed into the palm and then up the inside of the arm.",
            acts = setOf("massage_arms_hands"), mutual = true,
            timers = listOf(tm("Left arms", 45), tm("Right arms", 45))
        ),
        c(
            id = "cm_ears_and_necks", intensity = 1, family = F_EAR, cats = setOf(EAR_PLAY, KISSING),
            title = "Ears, then necks",
            base = "{G} and {R} lie face to face and work each other's ears with their mouths for thirty seconds, then each other's necks for thirty, and swap back twice.",
            explicit = "{G} and {R} lie face to face and suck each other's ears for thirty seconds, then each other's necks for thirty, and go back and forth twice more.",
            acts = setOf("ear_play", "neck_kissing"), mutual = true,
            timers = listOf(tm("Ears", 30), tm("Necks", 30), tm("Ears", 30), tm("Necks", 30))
        ),
        c(
            id = "cm_ear_whisper_both", intensity = 1, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Ears and low words",
            base = "{G} and {R} lie face to face, work each other's ears with their mouths, and each [tone_whisper] into the ear he is working every time he comes off it.",
            explicit = "{G} and {R} lie face to face and suck each other's ears, and each one [tone_whisper] into the ear every time he pulls off it.",
            acts = setOf("ear_play"), mutual = true,
            timers = listOf(tm("Both ears", 120))
        ),
        c(
            id = "cm_chest_nipples_turns", intensity = 1, family = F_KISS, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Chests, in turns",
            base = "{G} and {R} take turns on each other's chest and nipples with mouth and fingers, sixty seconds each, and whoever is receiving keeps his hands flat on the bed.",
            explicit = "{G} and {R} take turns working each other's chest and nipples with mouth and fingers, sixty seconds each, and whoever is receiving keeps both hands flat on the bed.",
            acts = setOf("chest_nipple_kissing", "nipple_stimulation"), mutual = true,
            timers = listOf(tm("{G} works {R}", 60), tm("{R} works {G}", 60))
        ),
        c(
            id = "cm_feet_head_to_toe", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Feet, head to toe",
            base = "{G} and {R} lie head to toe and work each other's feet at the same time, thumb along the arch and then each toe in turn.",
            explicit = "{G} and {R} lie head to toe and work each other's feet at once, thumb hard along the arch and then every toe in turn.",
            acts = setOf("massage_calves_feet"), mutual = true,
            timers = listOf(tm("Left feet", 60), tm("Right feet", 60))
        ),

        // ------------------------------------------------------------- band 2: hands (7)
        c(
            id = "cm_stroke_counted", intensity = 2, family = F_SEX, cats = setOf(HANDS),
            title = "Thirty and thirty",
            base = "{G} and {R} lie face to face and stroke each other with #lubricant#, thirty seconds slow and thirty seconds fast, twice through.",
            explicit = "{G} and {R} lie face to face and work each other's cocks with #lubricant#, thirty seconds slow and thirty seconds fast, twice through, and neither one changes speed early.",
            acts = setOf("massage_groin"), mutual = true, equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Slow", 30), tm("Fast", 30), tm("Slow", 30), tm("Fast", 30))
        ),
        c(
            id = "cm_kiss_and_stroke", intensity = 2, family = F_SEX, cats = setOf(HANDS, KISSING),
            title = "Kissing and stroking",
            base = "{G} and {R} kiss deeply and slowly and stroke each other with #lubricant# at the same time, and neither one breaks the kiss.",
            explicit = "{G} and {R} kiss deep and open-mouthed while they work each other's cocks with #lubricant#, and neither one breaks the kiss before the timer ends.",
            acts = setOf("massage_groin", "long_making_out"), mutual = true, equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Kissing and stroking", 150))
        ),
        c(
            id = "cm_nipples_both_hands", intensity = 2, family = F_SEX, cats = setOf(BODY_WORSHIP),
            title = "Nipples, both at once",
            base = "{G} and {R} sit face to face and work each other's nipples with fingers for sixty seconds, then with their mouths for sixty.",
            explicit = "{G} and {R} sit face to face and work each other's nipples with their fingers for sixty seconds, then with their mouths for sixty, both of them going at once.",
            acts = setOf("nipple_stimulation"), mutual = true,
            timers = listOf(tm("Fingers", 60), tm("Mouths", 60))
        ),
        c(
            id = "cm_inner_thighs_both", intensity = 2, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Inner thighs",
            base = "{G} and {R} lie head to toe, oil their hands with #massage_oil# and work each other's inner thighs with firm, even pressure, from the knee up to the crease and back down.",
            explicit = "{G} and {R} lie head to toe, get #massage_oil# on their hands and work each other's inner thighs with hard pressure, knee to crease and back down, and neither one goes past the crease.",
            acts = setOf("massage_inner_thighs", "massage_oil"), mutual = true,
            equip = setOf(Equipment.MASSAGE_OIL),
            timers = listOf(tm("Left thighs", 60), tm("Right thighs", 60))
        ),
        c(
            id = "cm_ear_and_hand", intensity = 2, family = F_SEX, cats = setOf(EAR_PLAY, HANDS),
            title = "Ear and hand",
            base = "{G} and {R} lie face to face, each with his mouth on the other's ear and his hand on the other's cock, and both keep going at once.",
            explicit = "{G} and {R} lie face to face, each one sucking the other's ear with a hand working his cock, and both keep going at once until the timer ends.",
            acts = setOf("ear_play", "massage_groin"), mutual = true,
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Ear and hand", 150))
        ),
        c(
            id = "cm_body_trail_turns", intensity = 2, family = F_KISS, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Down and back, in turns",
            base = "{G} and {R} take turns putting [n_kiss_trail] from the other's collarbone to his hip bones and back up, ninety seconds each.",
            explicit = "{G} and {R} take turns laying [n_kiss_trail] from the other's collarbone down to his hip bones and back up, ninety seconds each, and neither one skips the stomach.",
            acts = setOf("chest_nipple_kissing", "stomach_hip_kissing"), mutual = true,
            timers = listOf(tm("{G} works down {R}", 90), tm("{R} works down {G}", 90))
        ),
        c(
            id = "cm_grip_and_hold", intensity = 2, family = F_SEX, cats = setOf(HANDS),
            title = "Held, not stroked",
            base = "{G} and {R} lie face to face, each holding the other's cock [adv_grip] without moving his hand, and they kiss [adv_kiss] for the whole time.",
            explicit = "{G} and {R} lie face to face, each with a hand closed round the other's cock [adv_grip] and not moving it, and they kiss [adv_kiss] for the whole time.",
            acts = setOf("massage_groin", "hard_kissing"), mutual = true,
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Held and kissing", 90))
        ),

        // ------------------------------------------------------------- band 3: mouths (7)
        c(
            id = "cm_69_flat", intensity = 3, family = F_SEX, cats = setOf(ORAL),
            title = "Both at once",
            base = "{G} and {R} lie head to toe on their sides and use their mouths on each other at the same time.",
            explicit = "{G} and {R} lie head to toe on their sides and suck each other at once, and neither one pulls off before the timer ends.",
            acts = setOf("oral"), mutual = true, erection = PartyRef.BOTH,
            timers = listOf(tm("Both at once", 180))
        ),
        c(
            id = "cm_69_counted", intensity = 3, family = F_SEX, cats = setOf(ORAL),
            title = "Forty-five on, fifteen off",
            base = "{G} and {R} lie head to toe and use their mouths on each other in three sets of forty-five seconds, both lifting off for fifteen between the sets.",
            explicit = "{G} and {R} lie head to toe and suck each other in three sets of forty-five seconds, both pulling off for fifteen between sets, and neither one cuts a gap short.",
            acts = setOf("oral"), mutual = true, erection = PartyRef.BOTH,
            timers = listOf(tm("Set 1", 45), tm("Off", 15), tm("Set 2", 45), tm("Off", 15), tm("Set 3", 45))
        ),
        c(
            id = "cm_oral_turns", intensity = 3, family = F_SEX, cats = setOf(ORAL),
            title = "Ninety each, turn about",
            base = "{G} and {R} take turns with their mouths, ninety seconds each, and whoever is receiving keeps both hands behind his own head.",
            explicit = "{G} and {R} take turns with their mouths, ninety seconds each, and whoever is receiving keeps both hands behind his own head the whole time.",
            acts = setOf("oral"), mutual = true, erection = PartyRef.BOTH,
            timers = listOf(tm("{G} sucks {R}", 90), tm("{R} sucks {G}", 90))
        ),
        c(
            id = "cm_toy_each_other", intensity = 3, family = F_SEX, cats = setOf(TOYS),
            title = "Toy on each other",
            base = "{G} and {R} lie face to face and take turns running #TOY# over the other's cock and balls [adv_toy_pace], sixty seconds each, twice through.",
            explicit = "{G} and {R} lie face to face and take turns working #TOY# over the other's cock and balls [adv_toy_pace], sixty seconds each, twice through.",
            acts = setOf("vibration"), mutual = true, toy = true,
            timers = listOf(tm("{G} on {R}", 60), tm("{R} on {G}", 60), tm("{G} on {R}", 60), tm("{R} on {G}", 60))
        ),
        c(
            id = "cm_anal_external_both", intensity = 3, family = F_SEX, cats = setOf(ANAL),
            title = "Outside, both at once",
            base = "{G} and {R} lie on their sides head to toe and rub each other's holes with #lubricant# from the outside, and neither one goes in.",
            explicit = "{G} and {R} lie head to toe on their sides and work each other's holes with #lubricant# from the outside at once, and neither finger goes in.",
            acts = setOf("anal_external"), mutual = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Both outside", 120))
        ),
        c(
            id = "cm_spit_and_stroke", intensity = 3, family = F_SEX, cats = setOf(HANDS, SPIT),
            title = "Spit in the hand",
            base = "{G} and {R} kneel facing each other, each spits into his own hand and strokes the other with it, and both go back for more every thirty seconds.",
            explicit = "{G} and {R} kneel face to face, each one spits into his own hand and works the other's cock with it, and both go back for more every thirty seconds.",
            acts = setOf("spit", "massage_groin"), mutual = true,
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Both hands", 120))
        ),
        c(
            id = "cm_mouth_and_hand_swap", intensity = 3, family = F_SEX, cats = setOf(ORAL, HANDS),
            title = "Mouth on one, hand on the other",
            base = "{G} and {R} lie head to toe, one with his mouth on the other and the other with his hand on him, and they swap over every forty-five seconds.",
            explicit = "{G} and {R} lie head to toe, one working with his mouth and the other with his fist, and they swap over every forty-five seconds.",
            acts = setOf("oral", "massage_groin"), mutual = true, erection = PartyRef.BOTH,
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Swap 1", 45), tm("Swap 2", 45), tm("Swap 3", 45), tm("Swap 4", 45))
        ),

        // -------------------------------------------------- band 4: tongues and fingers (7)
        c(
            id = "cm_finger_each_other", intensity = 4, family = F_SEX, cats = setOf(ANAL),
            title = "A finger each",
            base = "{G} and {R} lie face to face on their sides and slide one finger each into the other with #lubricant#, in to the second knuckle and out again.",
            explicit = "{G} and {R} lie face to face on their sides and finger-fuck each other with #lubricant# at once, one finger each, in to the second knuckle and out again.",
            acts = setOf("fingering"), mutual = true, equip = setOf(Equipment.LUBRICANT), anal = true,
            timers = listOf(tm("Both fingers", 150))
        ),
        c(
            id = "cm_rim_turns", intensity = 4, family = F_SEX, cats = setOf(RIMMING, ANAL),
            title = "Two minutes each, face down",
            base = "{G} and {R} take turns face down with the other's tongue on his hole, two minutes each, and whoever is face down keeps his hands under his own chest.",
            explicit = "{G} and {R} take turns face down while the other [v_rim] him, two minutes each, and whoever is face down keeps both hands under his own chest.",
            acts = setOf("rimming"), mutual = true, timers = listOf(tm("{G} on {R}", 120), tm("{R} on {G}", 120))
        ),
        c(
            id = "cm_69_hard", intensity = 4, family = F_SEX, cats = setOf(ORAL),
            title = "Both at once, hard",
            base = "{G} and {R} lie head to toe and use their mouths on each other at a hard, steady pace, and neither one slows down when the other does.",
            explicit = "{G} and {R} lie head to toe and suck each other at a hard, fast pace, and neither one slows down because the other has.",
            acts = setOf("oral", "harder_oral"), mutual = true, erection = PartyRef.BOTH,
            timers = listOf(tm("Both at once", 180))
        ),
        c(
            id = "cm_edge_each_other", intensity = 4, family = F_SEX, cats = setOf(HANDS),
            title = "Three each, hands only",
            base = "{G} and {R} lie face to face and bring each other to the edge with #lubricant# three times each, and each one says out loud when he is there so the other stops.",
            explicit = "{G} and {R} lie face to face and edge each other with #lubricant#, three times each, and each one says out loud the moment he is there so the other takes his hand off.",
            acts = setOf("edging", "massage_groin"), mutual = true, equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Three edges each", 240))
        ),
        c(
            id = "cm_clamps_both", intensity = 4, family = F_SEX, cats = setOf(BODY_WORSHIP, TOYS),
            title = "Clamped, both of them",
            base = "{G} and {R} put #nipple_clamps# on each other, one clamp each, and kiss [adv_kiss] until the timer ends before either one takes them off.",
            explicit = "{G} and {R} clip #nipple_clamps# onto each other, one clamp each, and kiss [adv_kiss] until the timer ends, and neither one takes his off early.",
            acts = setOf("nipple_clamps", "hard_kissing"), mutual = true,
            equip = setOf(Equipment.NIPPLE_CLAMPS),
            timers = listOf(tm("Clamped and kissing", 120))
        ),
        c(
            id = "cm_plug_and_kiss", intensity = 4, family = F_SEX, cats = setOf(ANAL, TOYS),
            title = "Plugged, both of them",
            base = "{G} and {R} take turns easing #anal_plug# into the other with #lubricant#, then kiss deeply and slowly, both plugs still in, until the timer ends.",
            explicit = "{G} and {R} take turns pushing #anal_plug# into the other with #lubricant#, then kiss deep and open-mouthed, both plugs still in, until the timer ends.",
            acts = setOf("plug", "anal_toys", "long_making_out"), mutual = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.LUBRICANT), anal = true,
            timers = listOf(tm("{G} plugs {R}", 45), tm("{R} plugs {G}", 45), tm("Kissing, both plugged", 120))
        ),
        c(
            id = "cm_spank_turns", intensity = 4, family = F_SEX, cats = setOf(IMPACT),
            title = "Twenty each, turn about",
            base = "{G} and {R} take turns over the other's knee for twenty by hand each, alternating cheeks, and whoever is over the knee counts every one out loud.",
            explicit = "{G} and {R} take turns over the other's knee and land twenty by hand each, alternating cheeks, and whoever is over the knee counts every one out loud.",
            acts = setOf("spanking"), mutual = true,
            timers = listOf(tm("{R} takes twenty", 60), tm("{G} takes twenty", 60))
        ),

        // ------------------------------------------------------------ band 5: hardest (6)
        c(
            id = "cm_edge_three_hard", intensity = 5, family = F_SEX, cats = setOf(HANDS),
            title = "Three hard edges each",
            base = "{G} and {R} kneel facing each other, put #lubricant# on their hands and bring each other to the edge with a tight fist three times each, and each one says out loud when he is there so the other takes his hand away.",
            explicit = "{G} and {R} kneel face to face, get #lubricant# on their hands and edge each other with a fist closed as tight as it will go, three times each, and each one says out loud the second he is there so the other's hand comes off.",
            acts = setOf("edging", "denial", "massage_groin"), mutual = true,
            equip = setOf(Equipment.LUBRICANT), blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Three edges each", 300))
        ),
        c(
            id = "cm_rim_then_finger", intensity = 5, family = F_SEX, cats = setOf(RIMMING, ANAL),
            title = "Tongue then finger, each way",
            base = "{G} and {R} take turns face down: two minutes of the other's tongue on his hole, then two minutes of one finger with #lubricant#.",
            explicit = "{G} and {R} take turns face down: two minutes of the other's tongue on his hole, then two minutes of one finger with #lubricant#, and then they swap.",
            acts = setOf("rimming", "fingering"), mutual = true,
            equip = setOf(Equipment.LUBRICANT), anal = true,
            timers = listOf(
                tm("{G} on {R}: tongue", 120), tm("{G} on {R}: finger", 120),
                tm("{R} on {G}: tongue", 120), tm("{R} on {G}: finger", 120)
            )
        ),
        c(
            id = "cm_69_deep_hard", intensity = 5, family = F_SEX, cats = setOf(ORAL),
            title = "Both at once, all the way down",
            base = "{G} and {R} lie head to toe and take each other into their throats at a hard, steady pace, both of them all the way down every time.",
            explicit = "{G} and {R} lie head to toe and push their throats down onto each other at a hard, fast pace, both of them all the way down on every stroke.",
            acts = setOf("oral", "deep_oral", "harder_oral"), mutual = true, erection = PartyRef.BOTH,
            timers = listOf(tm("Both at once", 180))
        ),
        c(
            id = "cm_toy_and_hand_both", intensity = 5, family = F_SEX, cats = setOf(TOYS, ANAL),
            title = "Toy in one, hand on the other",
            base = "{G} and {R} take turns: one holds #TOY# against the other and [v_stroke] him with #lubricant# for two minutes, then they swap.",
            explicit = "{G} and {R} take turns: one holds #TOY# on the other and works his cock with #lubricant# for two minutes, then they swap over.",
            acts = setOf("vibration", "massage_groin"), mutual = true, toy = true,
            equip = setOf(Equipment.LUBRICANT), blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("{G} on {R}", 120), tm("{R} on {G}", 120))
        ),
        c(
            id = "cm_clamps_and_edge", intensity = 5, family = F_SEX, cats = setOf(BODY_WORSHIP, HANDS, TOYS),
            title = "Clamped and edged",
            base = "{G} and {R} put #nipple_clamps# on each other, one clamp each, then coat their hands in #lubricant# and bring each other to the edge twice each, clamps still on.",
            explicit = "{G} and {R} clip #nipple_clamps# onto each other, one clamp each, then get #lubricant# on their hands and edge each other twice each, clamps still on.",
            acts = setOf("nipple_clamps", "edging", "massage_groin"), mutual = true,
            equip = setOf(Equipment.NIPPLE_CLAMPS, Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Two edges each", 240))
        ),
        c(
            id = "cm_spank_and_kiss", intensity = 5, family = F_SEX, cats = setOf(IMPACT, KISSING),
            title = "Thirty each, then the kiss",
            base = "{G} and {R} take turns face down while the other lands thirty with #paddle#, alternating cheeks, then kiss deeply and slowly until the timer ends.",
            explicit = "{G} and {R} take turns face down while the other lands thirty with #paddle#, alternating cheeks, and then they kiss deep and open-mouthed until the timer ends.",
            acts = setOf("spanking", "impact_toys", "hard_kissing"), mutual = true,
            equip = setOf(Equipment.PADDLE),
            timers = listOf(tm("{R} takes thirty", 75), tm("{G} takes thirty", 75), tm("Kissing", 90))
        )
    )
}
