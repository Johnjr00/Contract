package com.thecontract.core.content

import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.BODY_WORSHIP
import com.thecontract.core.model.Category.BONDAGE
import com.thecontract.core.model.Category.HANDS
import com.thecontract.core.model.Category.IMPACT
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.LANGUAGE
import com.thecontract.core.model.Category.ORAL
import com.thecontract.core.model.Category.POWER
import com.thecontract.core.model.Category.RIMMING
import com.thecontract.core.model.Category.ROUGH
import com.thecontract.core.model.Category.SENSORY
import com.thecontract.core.model.Category.SPIT
import com.thecontract.core.model.Category.TOYS
import com.thecontract.core.model.Category.VISUAL
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.ConsiderationFamily.SEXUAL as F_SEX
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef

/**
 * Consideration actions — sexual (85 of 124). Intensity 2–5, matched to how far negotiation
 * has already escalated so that consideration gets harder as the contract fills up.
 *
 * Authoring convention: `{G}` performs, `{R}` receives.
 */
internal object ConsiderationsSexual {

    val actions: List<ConsiderationAction> = listOf(

        // ---------------------------------------------------------------- oral (10)
        c(
            id = "cs_oral_slow", intensity = 2, family = F_SEX, cats = setOf(ORAL),
            title = "Slow oral",
            base = "{G} [v_suck_slow] {R+} cock with one hand flat on his stomach, and does not speed up once.",
            explicit = "{G} gets between {R+} legs and [v_suck_slow] his cock, pinning his stomach so he cannot chase it, and does not speed up once.",
            acts = setOf("oral"), timers = listOf(tm("Slow oral", 120))
        ),
        c(
            id = "cs_oral_counted", intensity = 2, family = F_SEX, cats = setOf(ORAL),
            title = "Oral, counted",
            base = "{G} [v_suck] {R} in four sets of thirty seconds with ten seconds off between each.",
            explicit = "{G} [v_suck] {R} in four sets of thirty seconds and pulls off for ten between each, and does not shorten a gap.",
            acts = setOf("oral"),
            timers = listOf(tm("Set 1", 30), tm("Off", 10), tm("Set 2", 30), tm("Off", 10), tm("Set 3", 30), tm("Off", 10), tm("Set 4", 30))
        ),
        c(
            id = "cs_oral_and_hand", intensity = 2, family = F_SEX, cats = setOf(ORAL, HANDS),
            title = "Mouth and hand",
            base = "{G} [v_suck] the head of {R+} cock and [v_stroke] the rest at the same rhythm.",
            explicit = "{G} [v_suck] the head of {R+} cock and strokes the rest with his fist in the same rhythm.",
            acts = setOf("oral"), timers = listOf(tm("Mouth and hand", 150))
        ),
        c(
            id = "cs_oral_deep", intensity = 3, family = F_SEX, cats = setOf(ORAL),
            title = "Deep oral",
            base = "{G} [v_deep] {R+} cock, taking it further on each pass than the last, and comes all the way off between passes rather than during one.",
            explicit = "{G} [v_deep] {R+} cock, further every pass, and comes all the way off between passes and not during one.",
            acts = setOf("oral", "deep_oral"),
            timers = listOf(tm("Deeper", 90), tm("Held deep", 45))
        ),
        c(
            id = "cs_oral_kneeling", intensity = 3, family = F_SEX, cats = setOf(ORAL, POWER),
            title = "On his knees",
            base = "{G} kneels in front of {R} and [v_suck] him with his hands behind his own back.",
            explicit = "{G} goes down on his knees, locks his hands behind his back and [v_suck] {R} for the full timer.",
            acts = setOf("oral", "kneeling"), timers = listOf(tm("On his knees", 120))
        ),
        c(
            id = "cs_oral_eyes_up", intensity = 3, family = F_SEX, cats = setOf(ORAL, POWER),
            title = "Eyes up",
            base = "{G} [v_suck] {R} with his eyes on {R+} face throughout, and starts again if he looks away.",
            explicit = "{G} [v_suck] {R} with his eyes locked on his face. He looks away, it starts again.",
            acts = setOf("oral"), timers = listOf(tm("Eyes up", 120))
        ),
        c(
            id = "cs_oral_ice", intensity = 3, family = F_SEX, cats = setOf(ORAL, SENSORY),
            title = "Cold mouth",
            base = "{G} holds #ice# in his mouth, then [v_suck] {R} cold, warms up for forty-five seconds and does it a second time.",
            explicit = "{G} holds #ice# in his mouth for a slow count of twenty, then [v_suck] {R} cold, warms up for forty-five seconds and does it a second time.",
            acts = setOf("oral", "cold_sensation"), equip = setOf(Equipment.ICE),
            timers = listOf(tm("Cold", 45), tm("Warm", 45), tm("Cold again", 45))
        ),
        c(
            id = "cs_oral_blindfolded", intensity = 3, family = F_SEX, cats = setOf(ORAL, SENSORY),
            title = "Blindfolded oral",
            base = "{R} wears #blindfold# and {G} [v_suck] him, switching between slow and fast every thirty seconds.",
            explicit = "{R} wears #blindfold# and {G} says nothing to him from start to finish. {G} [v_suck] him, switching between slow and fast every thirty seconds.",
            acts = setOf("oral", "blindfold"), equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Blindfolded", 150))
        ),
        c(
            id = "cs_oral_hard_pace", intensity = 4, family = F_SEX, cats = setOf(ORAL, ROUGH),
            title = "Harder oral",
            base = "{R} fucks {G+} mouth hard and {G} keeps his lips sealed around him through all of it.",
            explicit = "{R} fucks {G+} mouth hard and {G} takes all of it for the whole timer, without pulling off once.",
            acts = setOf("oral", "harder_oral"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Hard pace", 90))
        ),
        c(
            id = "cs_oral_wet", intensity = 3, family = F_SEX, cats = setOf(ORAL, SPIT),
            title = "Wet oral",
            base = "{G} keeps {R+} cock [adj_wet] with his own spit and never lets it dry.",
            explicit = "{G} spits on {R+} cock and keeps it [adj_wet] throughout, going back for more every time it dries.",
            acts = setOf("oral", "spit"), blocks = setOf(Boundary.NO_SPIT_PLAY),
            timers = listOf(tm("Wet oral", 120))
        ),

        // -------------------------------------------------------------- rimming (6)
        c(
            id = "cs_rim_basic", intensity = 3, family = F_SEX, cats = setOf(RIMMING, ANAL),
            title = "Rimming",
            base = "{G} spreads {R} open and [v_rim] him [adv_slow], a hand on each cheek.",
            explicit = "{G} spreads {R} wide open and [v_rim] him [adv_slow], keeping a hand on each cheek.",
            acts = setOf("rimming"), timers = listOf(tm("Rimming", 180))
        ),
        c(
            id = "cs_rim_deep", intensity = 4, family = F_SEX, cats = setOf(RIMMING, ANAL),
            title = "Deeper rimming",
            base = "{G} [v_rim_hard] {R}, pushing his tongue in rather than only over the outside.",
            explicit = "{G} [v_rim_hard] {R}, gets his tongue inside him and keeps it there rather than only licking the outside.",
            acts = setOf("rimming"), timers = listOf(tm("Outside", 45), tm("Pushing in", 120))
        ),
        c(
            id = "cs_rim_face_down", intensity = 3, family = F_SEX, cats = setOf(RIMMING, ANAL),
            title = "Face down",
            base = "{R} lies face down with a pillow under his hips and {G} [v_rim] him from there.",
            explicit = "{R} goes face down with his hips up and {G} spreads him and [v_rim] him from there.",
            acts = setOf("rimming"), timers = listOf(tm("Face down", 150))
        ),
        c(
            id = "cs_rim_and_stroke", intensity = 4, family = F_SEX, cats = setOf(RIMMING, HANDS, ANAL),
            title = "Rimming and stroking",
            base = "{G} [v_rim] {R} and, reaching round, [v_stroke] his cock at the same rhythm.",
            explicit = "{G} [v_rim] {R} and reaches round to stroke his cock in the same rhythm so he gets both at once.",
            acts = setOf("rimming"), timers = listOf(tm("Rimming", 60), tm("Both", 120))
        ),
        c(
            id = "cs_rim_over_edge", intensity = 4, family = F_SEX, cats = setOf(RIMMING, ANAL),
            title = "Over the edge",
            base = "{R} bends over #sturdy_chair# and {G} kneels behind him and [v_rim] him from there.",
            explicit = "{R} bends over #sturdy_chair# and holds on while {G} kneels behind him and [v_rim] him for the full timer.",
            acts = setOf("rimming"), equip = setOf(Equipment.STURDY_CHAIR),
            timers = listOf(tm("Over the edge", 150))
        ),
        c(
            id = "cs_rim_reciprocal", intensity = 4, family = F_SEX, cats = setOf(RIMMING, ANAL),
            title = "Rimming each other",
            base = "{G} and {R} get into position and use their tongues on each other at the same time for the full time.",
            explicit = "{G} and {R} get into position and use their tongues on each other at once, and neither one stops before the timer ends.",
            acts = setOf("rimming"), mutual = true, timers = listOf(tm("Both at once", 180))
        ),

        // ------------------------------------------------- cock and hand (8)
        c(
            id = "cs_hand_cock_balls", intensity = 2, family = F_SEX, cats = setOf(HANDS),
            title = "Cock and balls",
            base = "{G} strokes {R+} cock and balls with an open hand and #massage_oil#, slowly, a full minute on each.",
            explicit = "{G} strokes {R+} cock and balls with an open, oiled hand, a full minute on each.",
            acts = setOf("massage_groin"), equip = setOf(Equipment.MASSAGE_OIL),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Cock", 60), tm("Balls", 60))
        ),
        c(
            id = "cs_hand_ball_worship", intensity = 3, family = F_SEX, cats = setOf(HANDS, ORAL),
            title = "Balls, hands and mouth",
            base = "{G} rolls {R+} balls with his hand and then his mouth and does not touch the shaft at all.",
            explicit = "{G} rolls {R+} balls with his hand and then his mouth and never once touches the shaft.",
            acts = setOf("oral"), timers = listOf(tm("Hand", 45), tm("Mouth", 90))
        ),
        c(
            id = "cs_hand_oiled", intensity = 2, family = F_SEX, cats = setOf(HANDS),
            title = "Oiled hand",
            base = "{G} [v_stroke] {R} [adv_grip], using plenty of #lubricant#, thirty seconds slow then thirty seconds fast, on repeat.",
            explicit = "{G} strokes {R+} cock [adv_grip], using plenty of #lubricant#, thirty seconds slow then thirty seconds fast, on repeat, without a break.",
            acts = setOf("massage_groin"), equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Hand only", 150))
        ),
        c(
            id = "cs_hand_two_handed", intensity = 3, family = F_SEX, cats = setOf(HANDS),
            title = "Both hands",
            base = "{G} uses both hands on {R} with #lubricant#, one on the shaft and one on the balls.",
            explicit = "{G} uses both hands on {R} with #lubricant#, one stroking the shaft and one on his balls, and does not stop.",
            acts = setOf("massage_groin"), equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Both hands", 150))
        ),
        c(
            id = "cs_hand_slow_tease", intensity = 3, family = F_SEX, cats = setOf(HANDS),
            title = "Slow tease",
            base = "{G} [v_tease] {R} with his fingertips only and never closes his hand around him.",
            explicit = "{G} [v_tease] {R} with his fingertips only and does not close his hand around him once.",
            acts = setOf("massage_groin"), blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Teasing", 150))
        ),
        c(
            id = "cs_hand_sleeve", intensity = 3, family = F_SEX, cats = setOf(HANDS, TOYS),
            title = "The sleeve",
            base = "{G} uses #sleeve# on {R} with #lubricant#, swapping between slow and fast every thirty seconds.",
            explicit = "{G} slides #sleeve# down {R+} cock with #lubricant#, swapping slow and fast every thirty seconds.",
            equip = setOf(Equipment.SLEEVE, Equipment.LUBRICANT), erection = PartyRef.RECEIVER,
            timers = listOf(tm("Slow", 30), tm("Fast", 30), tm("Slow", 30), tm("Fast", 30))
        ),
        c(
            id = "cs_hand_mutual", intensity = 2, family = F_SEX, cats = setOf(HANDS),
            title = "Stroking each other",
            base = "{G} and {R} lie face to face and stroke each other with #lubricant#, matching pace.",
            explicit = "{G} and {R} lie face to face and stroke each other with #lubricant#, matching pace exactly.",
            acts = setOf("massage_groin"), mutual = true, equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Both hands", 150))
        ),
        c(
            id = "cs_hand_porn", intensity = 2, family = F_SEX, cats = setOf(HANDS, VISUAL),
            title = "Watching and stroking",
            base = "{G} and {R} put on #porn# and stroke each other by hand, both watching the screen.",
            explicit = "{G} and {R} put on #porn# and stroke each other by hand with their eyes on the screen the whole time.",
            acts = setOf("massage_groin"), mutual = true,
            pPrefs = setOf("watching_porn"), rPrefs = setOf("watching_porn"),
            equip = setOf(Equipment.PORN), blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Watching and stroking", 180))
        ),

        // ------------------------------------------------- external anal play (5)
        c(
            id = "cs_ext_tease", intensity = 3, family = F_SEX, cats = setOf(ANAL),
            title = "Outside only",
            base = "{G} rubs {R+} [n_hole] from the outside with a slick thumb and does not go in.",
            explicit = "{G} rubs {R+} [n_hole] from the outside with a slick thumb and does not go in at any point in the timer.",
            acts = setOf("anal_external"), equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Outside only", 120))
        ),
        c(
            id = "cs_ext_spread_tease", intensity = 3, family = F_SEX, cats = setOf(ANAL, VISUAL),
            title = "Held open and teased",
            base = "{G} spreads {R} open with both hands and [v_tease] him with a slick thumb without going in.",
            explicit = "{G} spreads {R} wide open with both hands and [v_tease] him with a slick thumb, and still does not go in.",
            acts = setOf("anal_external"), equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Held open and teased", 120))
        ),
        c(
            id = "cs_ext_oiled_cheeks", intensity = 2, family = F_SEX, cats = setOf(ANAL, BODY_WORSHIP),
            title = "Oiled and stroked",
            base = "{G} oils {R+} ass with #massage_oil# and kneads both cheeks and the crease between them.",
            explicit = "{G} oils {R+} ass with #massage_oil# and kneads both cheeks and the crease between them, a full minute on each.",
            acts = setOf("massage_buttocks_hips", "anal_external"), equip = setOf(Equipment.MASSAGE_OIL),
            timers = listOf(tm("Cheeks", 60), tm("Between them", 60))
        ),
        c(
            id = "cs_ext_grind", intensity = 4, family = F_SEX, cats = setOf(ANAL),
            title = "Right up against him",
            base = "{G} grinds himself against {R+} [n_hole] with #lubricant# and does not go in at all.",
            explicit = "{G} slicks up and grinds himself against {R+} [n_hole] without once pushing in, for the full timer.",
            acts = setOf("anal_external"), equip = setOf(Equipment.LUBRICANT),
            erection = PartyRef.GIVER, pCon = PartyConstraint.TOP,
            timers = listOf(tm("Against him", 150)),
            pCock = true
        ),
        c(
            id = "cs_ext_tongue_outside", intensity = 3, family = F_SEX, cats = setOf(ANAL, RIMMING),
            title = "Tongue on the outside",
            base = "{G} licks {R+} [n_hole] with his tongue over the outside only, and does not go in.",
            explicit = "{G} licks {R+} [n_hole] with his tongue over the outside only and refuses to go in.",
            acts = setOf("rimming", "anal_external"),
            timers = listOf(tm("Outside only", 120))
        ),

        // ------------------------------------------------------------ fisting (4)
        c(
            id = "cs_fist_building", intensity = 5, family = F_SEX, cats = setOf(ANAL),
            title = "Building up to his hand",
            base = "{G} uses plenty of #lubricant# and builds from two fingers to four inside {R}, one more every ninety seconds.",
            explicit = "{G} slicks his whole hand with #lubricant# and builds from two fingers to four inside {R}, adding one more every ninety seconds.",
            acts = setOf("fisting"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Two", 90), tm("Three", 90), tm("Four", 90))
        ),
        c(
            id = "cs_fist_knuckles", intensity = 5, family = F_SEX, cats = setOf(ANAL),
            title = "Past the knuckles",
            base = "{G} slicks his hand with #lubricant# and pushes it into {R} past the knuckles, then holds it still for a slow count of thirty.",
            explicit = "{G} slicks his hand with #lubricant# and pushes it into {R} past the knuckles, then holds completely still for a slow count of thirty.",
            acts = setOf("fisting"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 120), tm("Held still", 30))
        ),
        c(
            id = "cs_fist_whole_hand", intensity = 5, family = F_SEX, cats = setOf(ANAL),
            title = "His whole hand",
            base = "{G} pushes as much of his hand into {R} as it will take with #lubricant# and keeps it there for the full timer.",
            explicit = "{G} pushes as much of his hand into {R} as it will take with #lubricant# and keeps it buried for the full timer.",
            acts = setOf("fisting"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 120), tm("Buried", 120))
        ),
        c(
            id = "cs_fist_slow_turn", intensity = 5, family = F_SEX, cats = setOf(ANAL),
            title = "Turning his hand inside him",
            base = "{G} pushes his hand into {R} with #lubricant# and turns it a quarter turn every thirty seconds without pulling out.",
            explicit = "{G} pushes his hand into {R} with #lubricant# and turns it a quarter turn every thirty seconds, never pulling out once.",
            acts = setOf("fisting"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 90), tm("Turning", 150))
        ),

        // ------------------------------------------------------- rough anal (5)
        c(
            id = "cs_ranal_hard_behind", intensity = 5, family = F_SEX, cats = setOf(ANAL, ROUGH),
            title = "Hard from behind",
            base = "{G} takes {R} from behind with #lubricant# [adv_thrust_hard] for the whole timer and does not slow down once.",
            explicit = "{G} takes {R} from behind with #lubricant# [adv_thrust_hard] for the whole timer and does not slow down once.",
            acts = setOf("topping", "rough_anal"), anal = true, erection = PartyRef.GIVER,
            pCon = PartyConstraint.TOP, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 150)),
            pCock = true
        ),
        c(
            id = "cs_ranal_pinned_hard", intensity = 5, family = F_SEX, cats = setOf(ANAL, ROUGH),
            title = "Pinned down and taken hard",
            base = "{G} pins {R+} shoulders to the bed with #lubricant# and fucks him [adv_thrust_hard], keeping him flat the whole time.",
            explicit = "{G} pins {R+} shoulders down with #lubricant# and fucks him [adv_thrust_hard], keeping him flat and taking it the whole time.",
            acts = setOf("topping", "rough_anal", "pinning"), anal = true, erection = PartyRef.GIVER,
            pCon = PartyConstraint.TOP, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Pinned and fucked", 150)),
            pCock = true
        ),
        c(
            id = "cs_ranal_hair_hard", intensity = 5, family = F_SEX, cats = setOf(ANAL, ROUGH),
            title = "Hard, with a fist in his hair",
            base = "{G} takes {R} from behind with #lubricant#, keeps a fist in his hair and fucks him [adv_thrust_hard] for the whole timer.",
            explicit = "{G} takes {R} from behind with #lubricant#, keeps a fist closed in his hair to hold his head up, and fucks him [adv_thrust_hard] the whole way through.",
            acts = setOf("topping", "rough_anal", "rough_hair_pulling"), anal = true, erection = PartyRef.GIVER,
            pCon = PartyConstraint.TOP, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 150)),
            pCock = true
        ),
        c(
            id = "cs_ranal_dildo_hard", intensity = 5, family = F_SEX, cats = setOf(ANAL, ROUGH, TOYS),
            title = "The dildo, hard",
            base = "{G} pushes #dildo# into {R} with #lubricant# and drives it [adv_thrust_hard] for the whole timer.",
            explicit = "{G} pushes #dildo# into {R} with #lubricant# and drives it [adv_thrust_hard] for the whole timer without slowing.",
            acts = setOf("dildo", "anal_toys", "rough_anal"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT),
            timers = listOf(tm("Pushing it in", 45), tm("Hard", 150))
        ),
        c(
            id = "cs_ranal_fingers_hard", intensity = 5, family = F_SEX, cats = setOf(ANAL, ROUGH),
            title = "Three fingers, hard",
            base = "{G} builds to three fingers in {R} with #lubricant# and finger-fucks him hard for the whole timer.",
            explicit = "{G} builds to three fingers in {R} with #lubricant# and finger-fucks him hard for the whole timer, without easing off.",
            acts = setOf("fingering", "rough_anal"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Building up", 60), tm("Hard", 150))
        ),

        // ------------------------------------------------------------ fingering (6)
        c(
            id = "cs_fing_one", intensity = 3, family = F_SEX, cats = setOf(ANAL),
            title = "One finger",
            base = "{G} slicks up with #lubricant# and [v_finger] {R}, using one finger only, for the whole time.",
            explicit = "{G} slicks up with #lubricant# and [v_finger] {R} with one finger only, and does not add a second at any point.",
            acts = setOf("fingering"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("One finger", 120))
        ),
        c(
            id = "cs_fing_two", intensity = 4, family = F_SEX, cats = setOf(ANAL),
            title = "Two fingers",
            base = "{G} builds up to two fingers with #lubricant# and [v_finger_deep] {R} for the full timer.",
            explicit = "{G} builds up to two fingers with #lubricant# and [v_finger_deep] {R} for the full timer.",
            acts = setOf("fingering"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("One finger", 60), tm("Two fingers", 120))
        ),
        c(
            id = "cs_fing_prostate", intensity = 4, family = F_SEX, cats = setOf(ANAL),
            title = "Steady pressure",
            base = "{G} presses two slick fingers into {R} and holds steady pressure on his prostate.",
            explicit = "{G} gets two slick fingers into {R}, finds his prostate and holds hard on it while he tries to stay still.",
            acts = setOf("fingering"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Finding it", 45), tm("Holding", 90))
        ),
        c(
            id = "cs_fing_and_kiss", intensity = 4, family = F_SEX, cats = setOf(ANAL, KISSING),
            title = "Fingered and kissed",
            base = "{G} [v_finger] {R} with #lubricant# and kisses him [adv_kiss_deep] throughout.",
            explicit = "{G} [v_finger] {R} with #lubricant# and kisses him [adv_kiss_deep] the entire time without breaking off.",
            acts = setOf("fingering", "tongue_kissing"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Fingers and kissing", 150))
        ),
        c(
            id = "cs_fing_and_oral", intensity = 4, family = F_SEX, cats = setOf(ANAL, ORAL),
            title = "Fingers and mouth",
            base = "{G} [v_suck] {R} and [v_finger] him at the same time, both at the same rhythm.",
            explicit = "{G} [v_suck] {R} and [v_finger] him at once, both in the same rhythm, and does not stop either one before the timer ends.",
            acts = setOf("oral", "fingering"), anal = true, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Mouth only", 45), tm("Mouth and fingers", 150))
        ),
        c(
            id = "cs_fing_gloved", intensity = 3, family = F_SEX, cats = setOf(ANAL, SENSORY),
            title = "Gloved fingers",
            base = "{G} puts on #gloves#, slicks up and [v_finger] {R} so the texture is different.",
            explicit = "{G} pulls on #gloves#, slicks up and [v_finger] {R} so he feels the change on every stroke.",
            acts = setOf("fingering"), anal = true, equip = setOf(Equipment.GLOVES, Equipment.LUBRICANT),
            timers = listOf(tm("Gloved fingering", 120))
        ),

        // ----------------------------------------------------------------- toys (10)
        c(
            id = "cs_toy_vibe_cock", intensity = 3, family = F_SEX, cats = setOf(TOYS),
            title = "Vibrator on his cock",
            base = "{G} holds #vibrator# against {R+} cock [adv_toy_pace], moving it every thirty seconds.",
            explicit = "{G} pins #vibrator# to {R+} cock [adv_toy_pace] and moves it every thirty seconds — underside, head, base, then balls.",
            acts = setOf("vibration"), equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Underside", 30), tm("Head", 30), tm("Base", 30), tm("Balls", 30))
        ),
        c(
            id = "cs_toy_vibe_hole", intensity = 4, family = F_SEX, cats = setOf(TOYS, ANAL),
            title = "Vibrator on his hole",
            base = "{G} holds #vibrator# against {R+} [n_hole] [adv_toy_pace] while pushing a slick finger into him.",
            explicit = "{G} holds #vibrator# hard against {R+} [n_hole] [adv_toy_pace] and presses a slick finger in at the same time.",
            acts = setOf("vibration", "anal_external", "fingering"), anal = true,
            equip = setOf(Equipment.VIBRATOR, Equipment.LUBRICANT),
            timers = listOf(tm("Outside", 60), tm("With a finger", 120))
        ),
        c(
            id = "cs_toy_vibe_nipples", intensity = 3, family = F_SEX, cats = setOf(TOYS),
            title = "Vibrator on his nipples",
            base = "{G} runs #vibrator# over each of {R+} nipples in turn and then down his stomach.",
            explicit = "{G} runs #vibrator# over each of {R+} nipples in turn and drags it down his stomach after.",
            acts = setOf("vibration", "nipple_stimulation"), equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Left nipple", 45), tm("Right nipple", 45), tm("Stomach", 45))
        ),
        c(
            id = "cs_toy_wand_body", intensity = 3, family = F_SEX, cats = setOf(TOYS),
            title = "Wand over his body",
            base = "{G} runs #massage_wand# over {R+} cock, balls and inner thighs, never more than fifteen seconds in one place.",
            explicit = "{G} runs #massage_wand# over {R+} cock, balls and inner thighs and never leaves it anywhere longer than fifteen seconds.",
            acts = setOf("massage_wand", "vibration"), equip = setOf(Equipment.MASSAGE_WAND),
            timers = listOf(tm("Cock", 45), tm("Balls", 45), tm("Inner thighs", 45))
        ),
        c(
            id = "cs_toy_plug", intensity = 4, family = F_SEX, cats = setOf(TOYS, ANAL),
            title = "Plugged",
            base = "{G} opens {R} up with #lubricant# and [v_insert] #anal_plug#, and it stays in.",
            explicit = "{G} opens {R} up with #lubricant# and [v_insert] #anal_plug#, and it stays exactly where it is.",
            acts = setOf("plug", "anal_toys"), anal = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.LUBRICANT),
            timers = listOf(tm("Opening him", 60), tm("Pushing it in", 45), tm("Wearing it", 120))
        ),
        c(
            id = "cs_toy_dildo_tease", intensity = 4, family = F_SEX, cats = setOf(TOYS, ANAL),
            title = "Teased with the dildo",
            base = "{G} pushes #dildo# into {R} with #lubricant# [adv_thrust_slow] and refuses to go faster.",
            explicit = "{G} pushes #dildo# into {R} with #lubricant# and fucks him with it [adv_thrust_slow], refusing to speed up.",
            acts = setOf("dildo", "anal_toys"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT),
            timers = listOf(tm("Pushing it in", 60), tm("Slow", 150))
        ),
        c(
            id = "cs_toy_prostate", intensity = 4, family = F_SEX, cats = setOf(TOYS, ANAL),
            title = "Prostate toy",
            base = "{G} [v_insert] #prostate_toy# with #lubricant# and leaves it running while he kisses {R}.",
            explicit = "{G} [v_insert] #prostate_toy# with #lubricant#, leaves it running inside him and kisses him through it.",
            acts = setOf("anal_toys", "tongue_kissing"), anal = true,
            equip = setOf(Equipment.PROSTATE_TOY, Equipment.LUBRICANT),
            timers = listOf(tm("Pushing it in", 45), tm("Left in", 150))
        ),
        c(
            id = "cs_toy_clamps", intensity = 4, family = F_SEX, cats = setOf(TOYS, SENSORY),
            title = "Clamps",
            base = "{G} sucks and pinches {R+} nipples with his mouth, then puts #nipple_clamps# on and kisses him through it.",
            explicit = "{G} sucks and pinches {R+} nipples hard, puts #nipple_clamps# on and kisses him through the first minute of it.",
            acts = setOf("nipple_clamps", "nipple_stimulation"), equip = setOf(Equipment.NIPPLE_CLAMPS),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Mouth first", 60), tm("Clamps on", 90))
        ),
        c(
            id = "cs_toy_cock_ring", intensity = 3, family = F_SEX, cats = setOf(TOYS),
            title = "Cock ring",
            base = "{G} puts #cock_ring# on {R} and strokes and sucks him while it stays on.",
            explicit = "{G} puts #cock_ring# on {R} and strokes and sucks him while it stays exactly where it is.",
            acts = setOf("cock_ring", "oral"), equip = setOf(Equipment.COCK_RING),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Hand", 90), tm("Mouth", 90))
        ),
        c(
            id = "cs_toy_his_choice", intensity = 4, family = F_SEX, cats = setOf(TOYS),
            title = "A toy over three places",
            base = "{G} uses #TOY# on {R+} nipples, cock and inner thighs in that order for the full time.",
            explicit = "{G} runs #TOY# over {R+} nipples, cock and inner thighs in that order, for the whole timer.",
            toy = true, timers = listOf(tm("Toy", 150))
        ),

        // ------------------------------------------------------ kneeling service (5)
        c(
            id = "cs_serv_kneel_wait", intensity = 2, family = F_SEX, cats = setOf(POWER),
            title = "Kneel and wait",
            base = "{G} kneels in front of {R} with his hands on his own thighs and waits without speaking.",
            explicit = "{G} kneels in front of {R}, hands flat on his own thighs, and waits in silence until the timer ends.",
            acts = setOf("kneeling", "sexual_service"), timers = listOf(tm("Kneeling", 120))
        ),
        c(
            id = "cs_serv_worship_thighs", intensity = 3, family = F_SEX, cats = setOf(POWER, BODY_WORSHIP),
            title = "Worship from the floor",
            base = "{G} kneels and kisses {R+} thighs, stomach and hips, avoiding his cock entirely.",
            explicit = "{G} kneels and kisses {R+} thighs, stomach and hips and stays well away from his cock.",
            acts = setOf("kneeling", "body_worship", "stomach_hip_kissing"),
            timers = listOf(tm("Thighs", 60), tm("Stomach", 45), tm("Hips", 45))
        ),
        c(
            id = "cs_serv_undress", intensity = 2, family = F_SEX, cats = setOf(POWER),
            title = "Undressing him on his knees",
            base = "{G} kneels and undresses {R} one item at a time, kissing whatever he uncovers.",
            explicit = "{G} kneels and strips {R} one item at a time, putting his mouth on everything he uncovers.",
            acts = setOf("kneeling", "sexual_service", "body_worship"),
            timers = listOf(tm("Undressing him", 120))
        ),
        c(
            id = "cs_serv_inspected", intensity = 4, family = F_SEX, cats = setOf(POWER),
            title = "Presented and inspected",
            base = "{G} presents himself on his knees with his hands behind his back and holds it while {R} looks him over and touches him.",
            explicit = "{G} presents himself on his knees with his hands behind his back and holds it while {R} looks him over and puts his hands on him.",
            acts = setOf("commands", "sexual_service"),
            timers = listOf(tm("Presented", 120))
        ),
        c(
            id = "cs_serv_suck_on_command", intensity = 4, family = F_SEX, cats = setOf(POWER, ORAL),
            title = "Mouth on command",
            base = "{G} [v_suck] {R} and stops and starts exactly when he is told, not a second before or after.",
            explicit = "{G} [v_suck] {R} and stops and starts on the word, not a second before or after.",
            acts = setOf("oral", "kneeling", "commands"),
            timers = listOf(tm("On", 45), tm("Off", 20), tm("On", 45), tm("Off", 20), tm("On", 60))
        ),

        // ------------------------------------------------------- blindfold play (4)
        c(
            id = "cs_blind_hands", intensity = 3, family = F_SEX, cats = setOf(SENSORY),
            title = "Blindfolded, hands only",
            base = "{R} wears #blindfold# and {G} kneads his chest, then his stomach, then his thighs with his hands, moving on only when the timer says so.",
            explicit = "{R} wears #blindfold# and {G} kneads his chest, then his stomach, then his thighs with his hands, and says nothing before he moves from one to the next.",
            acts = setOf("blindfold", "body_worship"), equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Chest", 45), tm("Stomach", 45), tm("Thighs", 45))
        ),
        c(
            id = "cs_blind_mouth", intensity = 3, family = F_SEX, cats = setOf(SENSORY, ORAL),
            title = "Blindfolded, mouth only",
            base = "{R} wears #blindfold# and {G} uses only his mouth, moving from his neck to his chest to his stomach to his cock and saying nothing before each move.",
            explicit = "{R} wears #blindfold# and {G} uses only his mouth, moving from his neck to his chest to his stomach to his cock without a word.",
            acts = setOf("blindfold", "oral"), equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Blindfolded", 150))
        ),
        c(
            id = "cs_blind_toy", intensity = 4, family = F_SEX, cats = setOf(SENSORY, TOYS),
            title = "Blindfolded with a toy",
            base = "{R} wears #blindfold# and {G} uses #TOY# on him without saying what it is.",
            explicit = "{R} wears #blindfold# and {G} runs #TOY# on him and tells him nothing about it.",
            acts = setOf("blindfold"), toy = true, equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Pass 1", 60), tm("Pass 2", 60))
        ),
        c(
            id = "cs_blind_ice", intensity = 3, family = F_SEX, cats = setOf(SENSORY),
            title = "Blindfolded with ice",
            base = "{R} wears #blindfold# and {G} alternates #ice# and his mouth over the same ground.",
            explicit = "{R} wears #blindfold# and {G} alternates #ice# and his hot mouth over exactly the same ground.",
            acts = setOf("blindfold", "cold_sensation"), equip = setOf(Equipment.BLINDFOLD, Equipment.ICE),
            timers = listOf(tm("Ice", 45), tm("Mouth", 45), tm("Ice again", 45))
        ),

        // ------------------------------------------------------- short restraint (5)
        c(
            id = "cs_rest_cuffs_tease", intensity = 4, family = F_SEX, cats = setOf(BONDAGE),
            title = "Cuffed and teased",
            base = "{R} is restrained with #cuffs# and {G} [v_tease] him with hands and mouth without finishing anything.",
            explicit = "{R} is locked in #cuffs# and {G} [v_tease] him with hands and mouth and finishes nothing he starts.",
            acts = setOf("restraint"), equip = setOf(Equipment.CUFFS),
            blocks = setOf(Boundary.NO_RESTRAINTS), timers = listOf(tm("Cuffed and teased", 150))
        ),
        c(
            id = "cs_rest_rope_mouth", intensity = 4, family = F_SEX, cats = setOf(BONDAGE, ORAL),
            title = "Tied and sucked",
            base = "{R+} wrists are bound with #rope# and {G} [v_suck] him, thirty seconds slow then thirty seconds hard, on repeat.",
            explicit = "{R+} wrists are tied with #rope# and {G} [v_suck] him, thirty seconds slow then thirty seconds hard, on repeat, ignoring what he asks for.",
            acts = setOf("restraint", "oral"), equip = setOf(Equipment.ROPE),
            blocks = setOf(Boundary.NO_RESTRAINTS), timers = listOf(tm("Tied and sucked", 150))
        ),
        c(
            id = "cs_rest_held_wrists", intensity = 3, family = F_SEX, cats = setOf(BONDAGE, ROUGH),
            title = "Wrists held",
            base = "{G} holds {R+} wrists above his head with one hand and strokes him with the other.",
            explicit = "{G} pins {R+} wrists above his head with one hand and uses his hands on him with the other.",
            acts = setOf("pinning"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Held", 120))
        ),
        c(
            id = "cs_rest_spreader", intensity = 4, family = F_SEX, cats = setOf(BONDAGE, ANAL),
            title = "Held open",
            base = "{R} is put on #spreader_bar# so his legs stay apart and {G} strokes him with hands and mouth.",
            explicit = "{R} is fixed onto #spreader_bar# so his legs cannot close and {G} uses his hands and mouth on him.",
            acts = setOf("restraint", "anal_external"), equip = setOf(Equipment.SPREADER_BAR),
            blocks = setOf(Boundary.NO_RESTRAINTS), timers = listOf(tm("Set up", 45), tm("Stroked", 150))
        ),
        c(
            id = "cs_rest_collar_service", intensity = 4, family = F_SEX, cats = setOf(BONDAGE, POWER),
            title = "Collared and serving",
            base = "{G} wears #collar# and serves {R} with his mouth, moving from his cock to his balls and back on command.",
            explicit = "{G} wears #collar# and serves {R} with his mouth, moving from his cock to his balls and back on command, without being asked twice.",
            acts = setOf("collaring", "sexual_service", "oral"), equip = setOf(Equipment.COLLAR),
            timers = listOf(tm("Serving", 150))
        ),

        // ------------------------------------------------------------- impact (6)
        c(
            id = "cs_imp_hand", intensity = 3, family = F_SEX, cats = setOf(IMPACT),
            title = "Spanked by hand",
            base = "{G} [v_spank] {R} by hand, starting light and building over the timer.",
            explicit = "{G} [v_spank] {R+} ass by hand, starting light and building over the full timer.",
            acts = setOf("spanking"), blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Warm-up", 60), tm("Building", 60))
        ),
        c(
            id = "cs_imp_counted", intensity = 4, family = F_SEX, cats = setOf(IMPACT, POWER),
            title = "Ten, counted",
            base = "{G} [v_spank] {R} ten times and {R} counts each one out loud.",
            explicit = "{G} [v_spank] {R} ten times and {R} counts every one out loud, or it starts again.",
            acts = setOf("spanking", "commands"), blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Ten counted", 90))
        ),
        c(
            id = "cs_imp_paddle", intensity = 4, family = F_SEX, cats = setOf(IMPACT),
            title = "Paddle",
            base = "{G} warms {R} up by hand and then uses #paddle# in two sets of five.",
            explicit = "{G} warms {R+} ass up by hand and then swings #paddle# across it in two sets of five.",
            acts = setOf("impact_toys", "spanking"), equip = setOf(Equipment.PADDLE),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Warm-up", 60), tm("Set 1", 30), tm("Set 2", 30))
        ),
        c(
            id = "cs_imp_flogger", intensity = 4, family = F_SEX, cats = setOf(IMPACT),
            title = "Flogger",
            base = "{G} runs #flogger# over {R+} back and ass in two building rounds.",
            explicit = "{G} swings #flogger# across {R+} back and ass through two rounds, harder on the second.",
            acts = setOf("impact_toys"), equip = setOf(Equipment.FLOGGER),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Round 1", 60), tm("Round 2", 60))
        ),
        c(
            id = "cs_imp_crop", intensity = 5, family = F_SEX, cats = setOf(IMPACT),
            title = "Crop",
            base = "{G} uses #crop# on {R+} thighs and ass one stroke at a time, naming each spot first.",
            explicit = "{G} runs #crop# over {R+} thighs and ass one stroke at a time, naming exactly where each lands before it does.",
            acts = setOf("impact_toys"), equip = setOf(Equipment.CROP),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Thighs", 60), tm("Ass", 60))
        ),
        c(
            id = "cs_imp_thighs", intensity = 3, family = F_SEX, cats = setOf(IMPACT),
            title = "Inner thighs",
            base = "{G} [v_spank] the inside of each of {R+} thighs by hand, alternating sides.",
            explicit = "{G} [v_spank] the inside of each of {R+} thighs by hand, swapping sides and keeping him open.",
            acts = setOf("spanking"), blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Left thigh", 30), tm("Right thigh", 30), tm("Left again", 30), tm("Right again", 30))
        ),

        // --------------------------------------------------------- rough play (8)
        c(
            id = "cs_rough_pin_kiss", intensity = 4, family = F_SEX, cats = setOf(ROUGH, KISSING),
            title = "Pinned and kissed",
            base = "{G} [v_pin] {R} by the wrists and kisses him [adv_kiss] while he stays pinned.",
            explicit = "{G} [v_pin] {R} by the wrists, puts his weight on him and kisses him [adv_kiss] while he tries to get loose.",
            acts = setOf("pinning", "hard_kissing"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pinned", 120))
        ),
        c(
            id = "cs_rough_manhandle", intensity = 4, family = F_SEX, cats = setOf(ROUGH, POWER),
            title = "Moved by the hips",
            base = "{G} [v_handle] {R} into three positions without asking, and {R} lets himself be moved.",
            explicit = "{G} [v_handle] {R} into three positions without warning, and {R} goes exactly where he is put.",
            acts = setOf("rough_handling"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Position 1", 45), tm("Position 2", 45), tm("Position 3", 45))
        ),
        c(
            id = "cs_rough_hair_kiss", intensity = 4, family = F_SEX, cats = setOf(ROUGH, KISSING),
            title = "By the hair",
            base = "{G} [v_pull_hair] {R+} hair, tips his head back and kisses him [adv_kiss] on his own terms.",
            explicit = "{G} [v_pull_hair] {R+} hair, drags his head back and kisses him [adv_kiss] entirely on his own terms.",
            acts = setOf("rough_hair_pulling", "hard_kissing"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("By the hair", 90))
        ),
        c(
            id = "cs_rough_oral", intensity = 5, family = F_SEX, cats = setOf(ROUGH, ORAL),
            title = "Rough oral",
            base = "{R} keeps both hands in {G+} hair and pulls him all the way down in a steady rhythm, and {G} takes it.",
            explicit = "{R} keeps both hands in {G+} hair and pulls him all the way down in a steady rhythm, and {G} takes all of it.",
            acts = setOf("oral", "harder_oral", "rough_hair_pulling"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Steady rhythm", 120))
        ),
        c(
            id = "cs_rough_firm_handling", intensity = 4, family = F_SEX, cats = setOf(ROUGH),
            title = "Firm handling",
            base = "{G} handles {R} [adv_rough] with his hands, gripping him by the arms, hips and jaw and moving him into three different positions.",
            explicit = "{G} handles {R} [adv_rough] with his hands, gripping him by the arms, hips and jaw and shoving him into three different positions.",
            acts = setOf("rough_handling"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Handled", 120))
        ),
        c(
            id = "cs_rough_pin_rim", intensity = 5, family = F_SEX, cats = setOf(ROUGH, RIMMING, ANAL),
            title = "Pinned and eaten out",
            base = "{G} holds {R} face down, spreads him and [v_rim_hard] him while he stays pinned.",
            explicit = "{G} pins {R} face down, spreads him wide and [v_rim_hard] him while he cannot get up.",
            acts = setOf("pinning", "rimming"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pinned and eaten out", 150))
        ),
        c(
            id = "cs_rough_shoved_knees", intensity = 5, family = F_SEX, cats = setOf(ROUGH, ORAL, POWER),
            title = "Put on his knees",
            base = "{R} [v_shove] {G} down onto his knees and {G} [v_suck] him from there.",
            explicit = "{R} [v_shove] {G} onto his knees and puts his own cock in {G+} mouth without asking.",
            acts = setOf("oral", "kneeling"),
            pPrefs = setOf("rough_handling_receive"), rPrefs = setOf("rough_handling_give"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("On his knees", 150))
        ),
        c(
            id = "cs_rough_held_worked", intensity = 4, family = F_SEX, cats = setOf(ROUGH),
            title = "Held down and stroked",
            base = "{G} holds {R} face down with one hand between his shoulders and strokes him with the other.",
            explicit = "{G} holds {R} face down with a hand between his shoulder blades and strokes him with the other until the timer ends.",
            acts = setOf("pinning", "rough_handling"), blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Held down", 150))
        ),

        // --------------------------------------------------- short hard sex (4)
        c(
            id = "cs_sex_short_behind", intensity = 5, family = F_SEX, cats = setOf(ANAL, ROUGH),
            title = "Hard, from behind",
            base = "{G} [v_fuck_hard] {R} from behind with #lubricant# [adv_thrust_hard] for the length of the timer.",
            explicit = "{G} [v_fuck_hard] {R} from behind with #lubricant# [adv_thrust_hard] and does not slow down for the whole timer.",
            acts = setOf("topping", "hard_sex"), anal = true, erection = PartyRef.GIVER,
            pCon = PartyConstraint.TOP, equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 120)),
            pCock = true
        ),
        c(
            id = "cs_sex_short_back", intensity = 5, family = F_SEX, cats = setOf(ANAL),
            title = "Face to face",
            base = "{G} [v_enter] {R} with #lubricant# and moves [adv_thrust_slow], staying face to face.",
            explicit = "{G} [v_enter] {R} with #lubricant# and [v_fuck] him [adv_thrust_slow] without looking away once.",
            acts = setOf("topping"), anal = true, erection = PartyRef.GIVER,
            pCon = PartyConstraint.TOP, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Slow", 120)),
            pCock = true
        ),
        c(
            id = "cs_sex_short_bent", intensity = 5, family = F_SEX, cats = setOf(ANAL, ROUGH),
            title = "Bent over",
            base = "{R} bends over #sturdy_chair# and {G} [v_fuck_hard] him there for the length of the timer.",
            explicit = "{R} bends over #sturdy_chair# and {G} [v_fuck_hard] him without letting him stand up once.",
            acts = setOf("topping", "hard_sex"), anal = true, erection = PartyRef.GIVER,
            pCon = PartyConstraint.TOP, equip = setOf(Equipment.LUBRICANT, Equipment.STURDY_CHAIR),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 120)),
            pCock = true
        ),
        c(
            id = "cs_sex_short_ride", intensity = 5, family = F_SEX, cats = setOf(ANAL),
            title = "He rides him",
            base = "{G} lies on his back and {R} lowers himself onto him with #lubricant#, taking him all the way in over the first minute, then riding him hard.",
            explicit = "{G} lies flat and {R} sinks onto him with #lubricant#, all the way down inside the first minute, then rides him hard for the rest.",
            acts = setOf("topping"), anal = true, erection = PartyRef.GIVER,
            pCon = PartyConstraint.TOP, equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Taking him in", 60), tm("Riding him hard", 120)),
            pCock = true
        ),

        // ------------------------------------------------------------- other (8)
        c(
            id = "cs_other_body_trail", intensity = 2, family = F_SEX, cats = setOf(BODY_WORSHIP),
            title = "Worshipped head to hip",
            base = "{G} moves down {R+} body with hands and mouth and names what he likes about each part.",
            explicit = "{G} moves down {R+} body with hands and mouth and says out loud exactly what he likes about every part.",
            acts = setOf("body_worship", "explicit_praise"),
            timers = listOf(tm("Chest and arms", 60), tm("Stomach", 45), tm("Thighs", 60))
        ),
        c(
            id = "cs_other_nipples", intensity = 2, family = F_SEX, cats = setOf(BODY_WORSHIP),
            title = "Nipples, sucked and pinched",
            base = "{G} sucks and pinches {R+} nipples, one at a time and then both.",
            explicit = "{G} sucks and pinches {R+} nipples [adv_pace] with fingers and mouth, one at a time and then both together.",
            acts = setOf("nipple_stimulation"),
            timers = listOf(tm("Left", 45), tm("Right", 45), tm("Both", 45))
        ),
        c(
            id = "cs_other_scratching", intensity = 3, family = F_SEX, cats = setOf(SENSORY),
            title = "Nails down his back",
            base = "{G} drags his nails down {R+} back and chest with the flat of the nail rather than the point, light enough to leave no mark at all.",
            explicit = "{G} drags his nails down {R+} back and across his chest hard enough to raise a white line and light enough that the line is gone inside a minute.",
            acts = setOf("scratching"), blocks = setOf(Boundary.NO_MARKS),
            timers = listOf(tm("Back", 45), tm("Chest", 45))
        ),
        c(
            id = "cs_other_ice", intensity = 2, family = F_SEX, cats = setOf(SENSORY),
            title = "Ice and mouth",
            base = "{G} runs #ice# over {R+} chest and stomach, then follows the same path with his mouth.",
            explicit = "{G} runs #ice# over {R+} chest and stomach and then goes back over the same path with his hot mouth.",
            acts = setOf("cold_sensation"), equip = setOf(Equipment.ICE),
            timers = listOf(tm("Ice", 45), tm("Mouth", 45))
        ),
        c(
            id = "cs_other_wax", intensity = 3, family = F_SEX, cats = setOf(SENSORY),
            title = "Warm wax",
            base = "{G} drips warm wax from #massage_candle# over {R+} chest and stomach and presses it in.",
            explicit = "{G} drips warm wax from #massage_candle# over {R+} chest and stomach, waits a slow count of five, then presses every bit of it in.",
            acts = setOf("warm_wax", "massage_candle"), equip = setOf(Equipment.MASSAGE_CANDLE),
            timers = listOf(tm("Wax", 45), tm("Pushing it in", 90))
        ),
        c(
            id = "cs_other_mirror", intensity = 3, family = F_SEX, cats = setOf(VISUAL),
            title = "In front of the mirror",
            base = "{G} strokes {R} with both hands in front of #mirror# and makes him watch the reflection.",
            explicit = "{G} strokes {R} with both hands in front of #mirror# and does not let him look anywhere but the reflection.",
            pPrefs = setOf("mirror_play"), rPrefs = setOf("mirror_play"), equip = setOf(Equipment.MIRROR),
            timers = listOf(tm("Watching", 120))
        ),
        c(
            id = "cs_other_dirty_talk", intensity = 3, family = F_SEX, cats = setOf(LANGUAGE),
            title = "Talked through it",
            base = "{G} [v_talk_dirty] at {R+} ear, describing what he intends to do later, hands still.",
            explicit = "{G} [v_talk_dirty] straight into {R+} ear, spelling out what he is going to do later, and keeps his hands completely still.",
            acts = setOf("dirty_talk", "ear_play"),
            timers = listOf(tm("Talking", 120))
        ),
        c(
            id = "cs_other_music_full_body", intensity = 2, family = F_SEX, cats = setOf(BODY_WORSHIP),
            title = "One track, hands only",
            base = "{G} puts on #music# and kneads {R+} whole body with his hands for one track without speaking.",
            explicit = "{G} puts on #music# and kneads {R+} whole body with his hands for a full track, and neither of them says a word.",
            acts = setOf("body_worship", "massage_general"), equip = setOf(Equipment.MUSIC),
            timers = listOf(tm("One track", 180))
        )
    )
}
