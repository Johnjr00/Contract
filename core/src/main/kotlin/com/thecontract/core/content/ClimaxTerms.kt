package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.RECEIVER
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.CLIMAX
import com.thecontract.core.model.Category.EAR_PLAY
import com.thecontract.core.model.Category.HANDS
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.LANGUAGE
import com.thecontract.core.model.Category.ORAL
import com.thecontract.core.model.Category.ROUGH
import com.thecontract.core.model.Category.RIMMING
import com.thecontract.core.model.Category.TOYS
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.Term

/**
 * The two mandatory closing terms (sections 36–38).
 *
 * Authoring convention for every climax term: **`{R}` is the player who finishes** and `{G}` is
 * the other player. That holds whether the finisher is the active party (coming inside his
 * partner) or the passive one (being stroked to a finish), which keeps the beneficiary
 * calculation and the guided final execution uniform.
 *
 * Every one of these carries an explicit continuation clause, so the scene cannot stall if the
 * timer runs out or if firmness changes.
 */
internal object ClimaxTerms {

    val terms: List<Term> = listOf(

        // ------------------------------------ finisher is the top, inside the bottom (5)
        t(
            id = "cx_top_inside_on_back", level = 5, cats = setOf(CLIMAX, ANAL), climax = true,
            title = "He finishes inside him, face to face",
            base = "{G} lies on his back with his legs up. {R} [v_enter] him with #lubricant#, moves [adv_thrust_slow] for two minutes, then [adv_thrust_hard] until he [v_come_in] him. He stays inside for a slow count of twenty, then withdraws. If the timer runs out first, he keeps the same hard rhythm without stopping until he finishes.",
            explicit = "{G} lies back with his legs up and takes it. {R} [v_enter] him with #lubricant#, fucks him [adv_thrust_slow] for two minutes, then goes [adv_thrust_hard] until he [v_come_in] him and gives him [n_load]. He stays buried for a slow count of twenty, then pulls out. If the timer ends first he keeps that exact rhythm going until he is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("bottoming"), anal = true,
            rCon = PartyConstraint.TOP, gCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Slow", 120), tm("Hard until he finishes", 180), tm("Staying inside", 20)),
            tail = "He tells him exactly what he is doing while he does it."
        ),
        t(
            id = "cx_top_inside_from_behind", level = 5, cats = setOf(CLIMAX, ANAL), climax = true,
            title = "He finishes inside him from behind",
            base = "{G} gets on his hands and knees. {R} [v_enter] him with #lubricant# and moves up from [adv_thrust_slow] to [adv_thrust_hard] until he [v_come_in] him, then stays inside for a slow count of twenty before withdrawing. If the timer ends first, he holds the hard rhythm until he finishes.",
            explicit = "{G} gets on his hands and knees and stays there. {R} [v_enter] him with #lubricant#, builds from [adv_thrust_slow] to [adv_thrust_hard] and [v_come_in] him, then stays buried for a slow count of twenty before he pulls out. If the timer ends first he keeps going at that pace until he is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("bottoming"), anal = true,
            rCon = PartyConstraint.TOP, gCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Slow", 120), tm("Hard until he finishes", 180), tm("Staying inside", 20))
        ),
        t(
            id = "cx_top_inside_bent_over", level = 5, cats = setOf(CLIMAX, ANAL), climax = true,
            title = "He finishes inside him, bent over",
            base = "{G} bends over #sturdy_chair#. {R} [v_enter] him with #lubricant#, holds him by the hips and fucks him [adv_thrust_hard] until he [v_come_in] him, then stays inside for a slow count of twenty. If the timer ends first, the same rhythm continues until he finishes.",
            explicit = "{G} bends over #sturdy_chair# and holds on. {R} [v_enter] him with #lubricant#, takes both hips and goes [adv_thrust_hard] until he [v_come_in] him, then stays buried for a slow count of twenty. If the timer ends first he does not change a thing until he is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("bottoming"), anal = true,
            rCon = PartyConstraint.TOP, gCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT, Equipment.STURDY_CHAIR),
            timers = listOf(tm("Pushing in", 60), tm("Hard until he finishes", 180), tm("Staying inside", 20))
        ),
        t(
            id = "cx_top_inside_riding", level = 5, cats = setOf(CLIMAX, ANAL), climax = true,
            title = "He finishes inside him while he rides",
            base = "{R} lies on his back and {G} lowers himself onto him with #lubricant# and rides him until {R} [v_come_in] him. {R} holds his hips down for a slow count of twenty afterwards. If the timer ends first, {G} keeps the same rhythm until {R} finishes.",
            explicit = "{R} lies flat and {G} sinks onto him with #lubricant# and rides him until {R} [v_come_in] him. {R} pins his hips down for a slow count of twenty after. If the timer ends first, {G} keeps that exact rhythm until {R} is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("bottoming"), anal = true,
            rCon = PartyConstraint.TOP, gCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Taking him in", 60), tm("Riding until he finishes", 240), tm("Held down", 20))
        ),
        t(
            id = "cx_top_inside_legs_up", level = 5, cats = setOf(CLIMAX, ANAL, KISSING), climax = true,
            title = "He finishes inside him, kissing him through it",
            base = "{G} lies on his back with his knees to his chest. {R} [v_enter] him with #lubricant# and kisses him [adv_kiss_deep] the whole way through until he [v_come_in] him, then stays inside for a slow count of twenty. If the timer ends first, he keeps the rhythm and the kissing going until he finishes.",
            explicit = "{G} lies back with his knees to his chest. {R} [v_enter] him with #lubricant#, kisses him [adv_kiss_deep] the entire time and [v_come_in] him, then stays buried for a slow count of twenty. If the timer ends first, nothing changes until he is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("bottoming", "tongue_kissing"), anal = true,
            rCon = PartyConstraint.TOP, gCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 60), tm("Until he finishes", 240), tm("Staying inside", 20))
        ),

        // --------------------------- finisher is the bottom, helped to finish (12)
        t(
            id = "cx_bottom_oral_and_hand", level = 5, cats = setOf(CLIMAX, ORAL, HANDS), climax = true,
            title = "Mouth and hand until he finishes",
            base = "{G} [v_suck] {R} and [v_stroke] the base with his hand at the same rhythm until {R} finishes. If the timer ends first, he keeps mouth and hand moving at exactly that rhythm until he does.",
            explicit = "{G} [v_suck] {R} and strokes the base with his fist in the same rhythm until {R} [v_come]. If the timer ends first he does not change a thing until he does.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral"),
            timers = listOf(tm("Mouth and hand", 240))
        ),
        t(
            id = "cx_bottom_hand_and_kissing", level = 5, cats = setOf(CLIMAX, HANDS, KISSING), climax = true,
            title = "Hand and kissing until he finishes",
            base = "{G} [v_stroke] {R} with #lubricant# and kisses him [adv_kiss_deep] until he finishes. If the timer ends first, the same grip and pace continue until he does.",
            explicit = "{G} strokes {R+} cock with #lubricant# and kisses him [adv_kiss_deep] until he [v_come]. If the timer ends first, the same grip and pace carry on until he is done.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("tongue_kissing"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Hand and kissing", 240))
        ),
        t(
            id = "cx_bottom_self_while_fingered", level = 5, cats = setOf(CLIMAX, ANAL, HANDS), climax = true,
            title = "His own hand while he is fingered",
            base = "{R} strokes himself with his own hand while {G} [v_finger_deep] him with #lubricant#, keeping steady pressure on his prostate until he finishes. If the timer ends first, {G} holds that pressure until he does.",
            explicit = "{R} strokes his own cock while {G} [v_finger_deep] him with #lubricant#, holding hard on his prostate until he [v_come]. If the timer ends first {G} keeps that pressure exactly where it is until he is done.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("His hand, his fingers", 240))
        ),
        t(
            id = "cx_bottom_self_while_filled", level = 5, cats = setOf(CLIMAX, ANAL, HANDS), climax = true,
            title = "His own hand while he is filled",
            base = "{R} strokes himself with his own hand while {G} takes him with a well-slicked hand, one finger for the first two minutes and two fingers after that. If the timer ends first, {G} holds still and {R} finishes himself.",
            explicit = "{R} strokes his own cock while {G} takes him with a well-slicked hand, one finger for the first two minutes and two fingers after that. If the timer ends first, {G} holds completely still and {R} finishes himself.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering", "anal_toys"), anal = true,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Building up", 120), tm("Until he finishes", 240))
        ),
        t(
            id = "cx_bottom_self_while_dildo", level = 5, cats = setOf(CLIMAX, ANAL, TOYS), climax = true,
            title = "His own hand while he is fucked with a dildo",
            base = "{R} strokes himself with his own hand while {G} uses #dildo# on him with #lubricant# at a steady pace until he finishes. If the timer ends first, {G} keeps the same depth and pace until he does.",
            explicit = "{R} strokes his own cock while {G} fucks him with #dildo# and #lubricant# at a steady pace until he [v_come]. If the timer ends first {G} keeps exactly that depth and pace until he is done.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT),
            timers = listOf(tm("Pushing it in", 60), tm("Until he finishes", 240))
        ),
        t(
            id = "cx_bottom_self_while_rimmed", level = 5, cats = setOf(CLIMAX, RIMMING, ANAL), climax = true,
            title = "His own hand while he is eaten out",
            base = "{R} strokes himself with his own hand while {G} [v_rim] him without stopping until he finishes. If the timer ends first, {G} does not come up until he does.",
            explicit = "{R} strokes his own cock while {G} [v_rim] him and does not stop for anything until he [v_come]. If the timer ends first, {G} still does not come up until he is done.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming"),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_bottom_vibration_and_kissing", level = 5, cats = setOf(CLIMAX, TOYS, KISSING), climax = true,
            title = "Vibration and kissing until he finishes",
            base = "{G} holds #vibrator# against {R} and kisses him [adv_kiss_deep] until he finishes. If the timer ends first, he keeps the vibrator exactly where it is and keeps kissing him until he does.",
            explicit = "{G} pins #vibrator# to {R+} cock and kisses him [adv_kiss_deep] until he [v_come]. If the timer ends first, the vibrator does not move and the kissing does not stop until he is done.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("vibration", "tongue_kissing"),
            equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Vibration and kissing", 240))
        ),
        t(
            id = "cx_bottom_neck_kisses", level = 5, cats = setOf(CLIMAX, KISSING, HANDS), climax = true,
            title = "His neck and a hand until he finishes",
            base = "{G} kisses and sucks {R+} neck and [v_stroke] him with #lubricant# until he finishes. If the timer ends first, he keeps his mouth on his neck and his hand moving until he does.",
            explicit = "{G} kisses and sucks {R+} neck and [v_stroke] him with #lubricant# until he [v_come]. If the timer ends first neither the mouth nor the hand stops until he is done.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("neck_kissing"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Neck and hand", 240))
        ),
        t(
            id = "cx_bottom_ear_kisses", level = 5, cats = setOf(CLIMAX, EAR_PLAY, HANDS), climax = true,
            title = "His ear and a hand until he finishes",
            base = "{G} sucks {R+} ear and [v_stroke] him with #lubricant# until he finishes. If the timer ends first, both continue exactly as they are until he does.",
            explicit = "{G} sucks {R+} ear and [v_stroke] him with #lubricant# until he [v_come]. If the timer ends first, both carry on exactly as they are until he is done.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Ear and hand", 240))
        ),
        t(
            id = "cx_bottom_moaning_in_ear", level = 5, cats = setOf(CLIMAX, EAR_PLAY, LANGUAGE), climax = true,
            title = "Moaning in his ear until he finishes",
            base = "{G} [v_stroke] {R} with #lubricant# and [tone_moan] straight into his ear until he finishes. If the timer ends first, he keeps the same pace and does not go quiet until he does.",
            explicit = "{G} strokes {R} with #lubricant# and [tone_moan] straight into his ear until he [v_come]. If the timer ends first he keeps the pace and does not shut up until he is done.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Moaning and stroking him", 240))
        ),
        t(
            id = "cx_bottom_growling_in_ear", level = 5, cats = setOf(CLIMAX, EAR_PLAY, LANGUAGE), climax = true,
            title = "Growling in his ear until he finishes",
            base = "{G} [v_stroke] {R} and [tone_whisper] explicit instructions straight into his ear until he finishes. If the timer ends first, he keeps talking and keeps the same pace until he does.",
            explicit = "{G} strokes {R} and [tone_whisper] [n_dirty_talk] straight into his ear until he [v_come]. If the timer ends first he keeps talking and does not change the pace until he is done.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "dirty_talk"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Talking and stroking him", 240))
        ),
        t(
            id = "cx_bottom_makeout_and_toy", level = 5, cats = setOf(CLIMAX, KISSING, TOYS), climax = true,
            title = "Making out with a toy in him until he finishes",
            base = "{G} kisses {R} [adv_kiss_deep] while running #TOY# on him and [v_stroke] him until he finishes. If the timer ends first, nothing changes until he does.",
            explicit = "{G} kisses {R} [adv_kiss_deep], runs #TOY# on him and [v_stroke] him at the same time until he [v_come]. If the timer ends first, nothing changes at all until he is done.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("tongue_kissing"), toy = true,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Everything at once", 240))
        ),

        // ------------------------------------------- no-penetration finishes (11)
        t(
            id = "cx_np_oral", level = 5, cats = setOf(CLIMAX, ORAL), climax = true,
            title = "Sucked until he finishes",
            base = "{G} [v_suck] {R} until he finishes and does not come off for anything. If the timer ends first, he keeps the same rhythm until he does.",
            explicit = "{G} [v_suck] {R} until he [v_come] and does not come off for any reason. If the timer ends first, the rhythm does not change until he is done.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral"),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_hand", level = 5, cats = setOf(CLIMAX, HANDS), climax = true,
            title = "Stroked by hand until he finishes",
            base = "{G} [v_stroke] {R} with #lubricant# [adv_grip], thirty seconds slow then thirty seconds fast, repeating that cycle without a break until he finishes. If the timer ends first, he keeps the same cycle going until he does.",
            explicit = "{G} strokes {R+} cock with #lubricant# [adv_grip], thirty seconds slow then thirty seconds fast, repeating that cycle without a break until he [v_come]. If the timer ends first he keeps the same cycle going until he is done.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_sleeve", level = 5, cats = setOf(CLIMAX, TOYS), climax = true,
            title = "The sleeve until he finishes",
            base = "{G} runs #sleeve# on {R} with #lubricant# until he finishes. If the timer ends first, he keeps the same speed and depth until he does.",
            explicit = "{G} slides #sleeve# down {R+} cock with #lubricant# until he [v_come]. If the timer ends first, the speed and depth do not change until he is done.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            equip = setOf(Equipment.SLEEVE, Equipment.LUBRICANT),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_vibration", level = 5, cats = setOf(CLIMAX, TOYS), climax = true,
            title = "Vibration until he finishes",
            base = "{G} holds #vibrator# on {R} where he asks for it, at the setting he asks for, until he finishes. If the timer ends first, it stays exactly there until he does.",
            explicit = "{G} pins #vibrator# against {R+} perineum on the highest setting until he [v_come]. If the timer ends first it does not move until he is done.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("vibration"),
            equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_rimming_self", level = 5, cats = setOf(CLIMAX, RIMMING), climax = true,
            title = "Eaten out while he strokes himself",
            base = "{R} strokes himself with his own hand while {G} [v_rim] him without pausing until he finishes. If the timer ends first, {G} keeps going until he does.",
            explicit = "{R} strokes his own cock while {G} [v_rim] him without a pause until he [v_come]. If the timer ends first {G} keeps going regardless until he is done.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming"),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_fingering_self", level = 5, cats = setOf(CLIMAX, ANAL), climax = true,
            title = "Fingered while he strokes himself",
            base = "{R} strokes himself with his own hand while {G} [v_finger_deep] him with #lubricant# until he finishes. If the timer ends first, {G} holds the same pressure until he does.",
            explicit = "{R} strokes his own cock while {G} [v_finger_deep] him with #lubricant# until he [v_come]. If the timer ends first {G} holds exactly that pressure until he is done.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_dildo_self", level = 5, cats = setOf(CLIMAX, TOYS, ANAL), climax = true,
            title = "Fucked with a dildo while he strokes himself",
            base = "{R} strokes himself with his own hand while {G} uses #dildo# on him with #lubricant# until he finishes. If the timer ends first, {G} keeps the same rhythm until he does.",
            explicit = "{R} strokes his own cock while {G} fucks him with #dildo# and #lubricant# until he [v_come]. If the timer ends first the rhythm does not change until he is done.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_worship_and_kissing", level = 5, cats = setOf(CLIMAX, KISSING), climax = true,
            title = "Worshipped and kissed until he finishes",
            base = "{G} kisses and sucks {R+} chest, neck and stomach while he [v_stroke] him with #lubricant# until he finishes. If the timer ends first, both continue as they are until he does.",
            explicit = "{G} kisses and sucks {R+} chest, neck and stomach while he [v_stroke] him with #lubricant# until he [v_come]. If the timer ends first, both carry on exactly as they are until he is done.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("body_worship", "neck_kissing"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_neck_and_ear", level = 5, cats = setOf(CLIMAX, EAR_PLAY, KISSING), climax = true,
            title = "Neck and ears until he finishes",
            base = "{G} alternates between {R+} neck and both ears with his mouth while he [v_stroke] him until he finishes. If the timer ends first, he keeps moving between them at the same pace until he does.",
            explicit = "{G} moves between {R+} neck and both ears with his mouth while he [v_stroke] him until he [v_come]. If the timer ends first he keeps moving between them at that pace until he is done.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "neck_kissing"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_self_under_direction", level = 5, cats = setOf(CLIMAX, LANGUAGE), climax = true,
            title = "His own hand, his partner's instructions",
            base = "{R} strokes himself while {G} tells him exactly how fast, how tight and when to slow down, right up until he finishes. If the timer ends first, {G} keeps directing him until he does.",
            explicit = "{R} strokes his own cock while {G} tells him exactly how fast, how tight and when to slow down, all the way through until he [v_come]. If the timer ends first {G} keeps directing him until he is done.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("dirty_talk", "commands"),
            timers = listOf(tm("Until he finishes", 240))
        ),
        t(
            id = "cx_np_hand_and_makeout", level = 5, cats = setOf(CLIMAX, HANDS, KISSING), climax = true,
            title = "Kissed through it",
            base = "{G} kisses {R} [adv_kiss_deep] without breaking off and [v_stroke] him with #lubricant# until he finishes. If the timer ends first, neither the kissing nor the hand stops until he does.",
            explicit = "{G} kisses {R} [adv_kiss_deep] without once breaking off and strokes him with #lubricant# until he [v_come]. If the timer ends first, neither stops until he is done.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("long_making_out", "tongue_kissing"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Until he finishes", 240))
        ),

        // --- finishing while his partner's hand is inside him -------------------------
        t(
            id = "cx_bottom_self_while_fisted", level = 5, cats = setOf(CLIMAX, ANAL), climax = true,
            title = "His own hand while he takes his partner's",
            base = "{R} strokes himself with his own hand while {G} pushes as much of his hand into him as he will take, with #lubricant#, and holds it there until he finishes. If the timer ends first, {G} keeps his hand exactly where it is until he does.",
            explicit = "{R} strokes his own cock while {G} pushes as much of his hand into him as he will take, with #lubricant#, and holds it buried until he [v_come]. If the timer ends first {G} keeps his hand exactly where it is until he is done.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fisting"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 150), tm("Until he finishes", 240))
        ),
        t(
            id = "cx_bottom_fisted_and_sucked", level = 5, cats = setOf(CLIMAX, ANAL, ORAL), climax = true,
            title = "Sucked with his partner's hand inside him",
            base = "{G} pushes as much of his hand into {R} as he will take with #lubricant#, keeps it there and [v_suck] him until he finishes. If the timer ends first, nothing changes until he does.",
            explicit = "{G} pushes as much of his hand into {R} as he will take with #lubricant#, keeps it buried and [v_suck] him until he [v_come]. If the timer ends first, nothing changes at all until he is done.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("fisting", "oral"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 150), tm("Until he finishes", 240))
        ),

        // --- finishing while he is fucked hard ---------------------------------------
        t(
            id = "cx_bottom_self_while_pounded", level = 5, cats = setOf(CLIMAX, ANAL, ROUGH), climax = true,
            title = "His own hand while he is pounded",
            base = "{R} strokes himself while {G} fucks him [adv_thrust_hard] with #lubricant# until he finishes. If the timer ends first, {G} keeps the same hard rhythm until he does.",
            explicit = "{R} strokes his own cock while {G} fucks him [adv_thrust_hard] with #lubricant# until he [v_come]. If the timer ends first {G} keeps that exact hard rhythm going until he is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "rough_anal"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.BOTH,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 60), tm("Until he finishes", 240))
        ),
        t(
            id = "cx_bottom_self_while_dildo_hard", level = 5, cats = setOf(CLIMAX, ANAL, ROUGH, TOYS), climax = true,
            title = "His own hand while the dildo goes hard",
            base = "{R} strokes himself while {G} drives #dildo# into him [adv_thrust_hard] with #lubricant# until he finishes. If the timer ends first, {G} keeps the same hard rhythm until he does.",
            explicit = "{R} strokes his own cock while {G} drives #dildo# into him [adv_thrust_hard] with #lubricant# until he [v_come]. If the timer ends first {G} keeps that exact hard rhythm going until he is done.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys", "rough_anal"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT),
            timers = listOf(tm("Pushing it in", 60), tm("Until he finishes", 240))
        ),

        // --- more ways to finish inside him ------------------------------------------
        t(
            id = "cx_top_inside_pinned", level = 5, cats = setOf(CLIMAX, ANAL, ROUGH), climax = true,
            title = "He finishes inside him, pinned flat",
            base = "{G} lies face down and {R} [v_enter] him with #lubricant#, pins his shoulders to the bed and fucks him [adv_thrust_hard] until he [v_come_in] him. He stays inside for a slow count of twenty, then withdraws. If the timer ends first he keeps the same hard rhythm until he finishes.",
            explicit = "{G} lies face down and takes it. {R} [v_enter] him with #lubricant#, pins his shoulders down and fucks him [adv_thrust_hard] until he [v_come_in] him and gives him [n_load]. He stays buried for a slow count of twenty, then pulls out. If the timer ends first he keeps that exact rhythm going until he is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("bottoming", "rough_anal", "pinning"), anal = true,
            rCon = PartyConstraint.TOP, gCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 60), tm("Until he finishes", 240), tm("Staying inside", 20))
        ),
        t(
            id = "cx_top_inside_standing", level = 5, cats = setOf(CLIMAX, ANAL), climax = true,
            title = "He finishes inside him, held by the hips",
            base = "{G} bends over #sturdy_chair# and {R} [v_enter] him with #lubricant#, holds him by the hips and fucks him [adv_thrust_hard] until he [v_come_in] him, then stays inside for a slow count of twenty. If the timer ends first the same rhythm continues until he finishes.",
            explicit = "{G} bends over #sturdy_chair# and holds on. {R} [v_enter] him with #lubricant#, takes both hips and fucks him [adv_thrust_hard] until he [v_come_in] him, then stays buried for a slow count of twenty. If the timer ends first he does not change a thing until he is done.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("bottoming"), anal = true,
            rCon = PartyConstraint.TOP, gCon = PartyConstraint.BOTTOM,
            erection = PartyRef.RECEIVER,
            equip = setOf(Equipment.LUBRICANT, Equipment.STURDY_CHAIR),
            timers = listOf(tm("Pushing in", 60), tm("Until he finishes", 240), tm("Staying inside", 20))
        )
    )
}
