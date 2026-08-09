package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.GIVER
import com.thecontract.core.model.BenefitParty.MUTUAL
import com.thecontract.core.model.BenefitParty.RECEIVER
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.HANDS
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.ORAL
import com.thecontract.core.model.Category.ORGASM_CONTROL
import com.thecontract.core.model.Category.POWER
import com.thecontract.core.model.Category.RIMMING
import com.thecontract.core.model.Category.SENSORY
import com.thecontract.core.model.Category.TOYS
import com.thecontract.core.model.Category.VISUAL
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.Term

/**
 * Act III — Privilege. Anal preparation, toys, penetration and orgasm terms.
 * 40 terms (specification floor for level 3: 40).
 */
internal object TermsLevel3 {

    val terms: List<Term> = listOf(

        // ------------------------------------------------------- anal preparation (8)
        t(
            id = "l3_anal_finger_one", level = 3, cats = setOf(ANAL),
            title = "One finger",
            base = "{G} slicks up with #lubricant# and [v_finger] {R} slowly, and stays with that one finger for the whole time.",
            explicit = "{G} slicks up with #lubricant# and [v_finger] {R}, one finger only, [adv_slow], and does not add a second at any point in the term.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("One finger", 120))
        ),
        t(
            id = "l3_anal_finger_two", level = 3, cats = setOf(ANAL),
            title = "Two fingers",
            base = "{G} moves up from one finger to two with #lubricant# and [v_finger_deep] {R} for the full timer.",
            explicit = "{G} builds up to two fingers with #lubricant# and [v_finger_deep] {R} for the full timer.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("One finger", 60), tm("Two fingers", 120))
        ),
        t(
            id = "l3_anal_prostate_fingers", level = 3, cats = setOf(ANAL),
            title = "Finding the spot",
            base = "{G} presses two slick fingers inside {R} and holds steady pressure on his prostate without moving anywhere else.",
            explicit = "{G} gets two slick fingers inside {R}, finds his prostate and holds hard, steady pressure right on it while {R} tries to hold still.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Finding it", 45), tm("Steady pressure", 90))
        ),
        t(
            id = "l3_anal_lube_ritual", level = 3, cats = setOf(ANAL, POWER),
            title = "Slicking him up",
            base = "{G} warms #lubricant# in his hands and works it into {R} with two fingers, slowly, telling him out loud what he is being opened up for.",
            explicit = "{G} warms #lubricant# in his hands, works it into {R} with two fingers until he is slick inside and out, and tells him out loud exactly what he is being opened up for.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering", "dirty_talk"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Slicking him up", 90))
        ),
        t(
            id = "l3_anal_gloves", level = 3, cats = setOf(ANAL, SENSORY),
            title = "Gloved",
            base = "{G} puts on #gloves#, slicks up with #lubricant# and [v_finger] {R} so the texture is different from bare skin.",
            explicit = "{G} pulls on #gloves#, slicks up with #lubricant# and [v_finger] {R} so he feels the change in texture on every stroke.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.GLOVES, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Gloved fingering", 120))
        ),
        t(
            id = "l3_anal_rim_to_finger", level = 3, cats = setOf(ANAL, RIMMING),
            title = "Tongue then fingers",
            base = "{G} [v_rim] {R} for two minutes, then adds #lubricant# and [v_finger] him without stopping the rhythm.",
            explicit = "{G} [v_rim] {R} for two solid minutes, then slicks up with #lubricant# and [v_finger] him while his mouth keeps going.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming", "fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Tongue only", 120), tm("Tongue and fingers", 120))
        ),
        t(
            id = "l3_anal_spread_open", level = 3, cats = setOf(ANAL, VISUAL),
            title = "Held open",
            base = "{R} holds himself open on his knees while {G} strokes him with #lubricant# and tells him what he sees.",
            explicit = "{R} gets on his knees and holds himself open with his own hands while {G} strokes him with #lubricant# and describes exactly what he is looking at.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering", "anal_external", "dirty_talk"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Held open", 90), tm("Opened up", 90))
        ),
        t(
            id = "l3_anal_finger_and_suck", level = 3, cats = setOf(ANAL, ORAL),
            title = "Fingers and mouth",
            base = "{G} [v_suck] {R+} cock and [v_finger] him at the same time, keeping both at the same rhythm.",
            explicit = "{G} [v_suck] {R+} cock and [v_finger] him at once, both in the same rhythm, and does not let either one drop.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Mouth only", 45), tm("Mouth and fingers", 150))
        ),

        // ------------------------------------------------------------------- toys (12)
        t(
            id = "l3_toy_plug_insert", level = 3, cats = setOf(TOYS, ANAL),
            title = "Plugged",
            base = "{G} opens {R} up with #lubricant#, then [v_insert] #anal_plug# and leaves it there.",
            explicit = "{G} opens {R} up with #lubricant#, then [v_insert] #anal_plug# and leaves it in him while he carries on with something else.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("plug", "anal_toys"), anal = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Opening him up", 60), tm("Pushing it in", 45), tm("Wearing it", 120))
        ),
        t(
            id = "l3_toy_plug_during", level = 3, cats = setOf(TOYS, ANAL, ORAL),
            title = "Plugged and sucked",
            base = "{R} wears #anal_plug# while {G} [v_suck] him, and it stays in for the whole term.",
            explicit = "{R} keeps #anal_plug# in him the entire time {G} [v_suck] him, and it does not come out before the timer ends.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("plug", "anal_toys", "oral"), anal = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Plugged and sucked", 180))
        ),
        t(
            id = "l3_toy_dildo_slow", level = 3, cats = setOf(TOYS, ANAL),
            title = "Slow with the dildo",
            base = "{G} uses #dildo# on {R} with plenty of #lubricant#, [adv_thrust_slow], and does not speed up at all.",
            explicit = "{G} pushes #dildo# into {R} with plenty of #lubricant# and fucks him with it [adv_thrust_slow], and does not go faster at any point in the term.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Pushing it in", 60), tm("Slow strokes", 150))
        ),
        t(
            id = "l3_toy_dildo_deep", level = 3, cats = setOf(TOYS, ANAL),
            title = "All the way",
            base = "{G} pushes #dildo# into {R} until it is all the way in, holds it there, then pulls it out slowly and starts again.",
            explicit = "{G} pushes #dildo# all the way into {R}, holds it buried while he counts, then drags it out slowly and starts over.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("All the way in", 45), tm("Held there", 30), tm("Out and in again", 120))
        ),
        t(
            id = "l3_toy_prostate", level = 3, cats = setOf(TOYS, ANAL),
            title = "Prostate toy",
            base = "{G} [v_insert] #prostate_toy# with #lubricant# and leaves it running while he kisses {R} and keeps his hands off his cock.",
            explicit = "{G} [v_insert] #prostate_toy# with #lubricant#, leaves it running inside him and kisses him through it with his hands nowhere near his cock.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("anal_toys", "tongue_kissing"), anal = true,
            equip = setOf(Equipment.PROSTATE_TOY, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Pushing it in", 45), tm("Left in and kissing", 180))
        ),
        t(
            id = "l3_toy_vibe_cock", level = 3, cats = setOf(TOYS),
            title = "Vibrator on his cock",
            base = "{G} holds #vibrator# against the underside of {R+} cock [adv_toy_pace], moving it every thirty seconds.",
            explicit = "{G} presses #vibrator# to the underside of {R+} cock [adv_toy_pace] and moves it every thirty seconds — underside, head, base, then balls.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("vibration"),
            equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Underside", 30), tm("Head", 30), tm("Base", 30), tm("Balls", 30))
        ),
        t(
            id = "l3_toy_vibe_hole", level = 3, cats = setOf(TOYS, ANAL),
            title = "Vibrator on his hole",
            base = "{G} holds #vibrator# against {R+} [n_hole] [adv_toy_pace] while pushing a slick finger into him.",
            explicit = "{G} holds #vibrator# hard against {R+} [n_hole] [adv_toy_pace] and presses a slick finger into him at the same time.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("vibration", "anal_external", "fingering"), anal = true,
            equip = setOf(Equipment.VIBRATOR, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Vibrator outside", 60), tm("Vibrator and finger", 120))
        ),
        t(
            id = "l3_toy_vibe_oral", level = 3, cats = setOf(TOYS, ORAL),
            title = "Vibrator and mouth",
            base = "{G} [v_suck] {R} while holding #vibrator# against his balls and the skin behind them.",
            explicit = "{G} [v_suck] {R} and keeps #vibrator# pressed to his balls and the skin behind them the entire time.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "vibration"),
            equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Mouth only", 45), tm("Mouth and vibrator", 150))
        ),
        t(
            id = "l3_toy_clamps", level = 3, cats = setOf(TOYS, SENSORY),
            title = "Clamps",
            base = "{G} sucks and pinches {R+} nipples with his mouth first, then puts #nipple_clamps# on and leaves them while he kisses him.",
            explicit = "{G} sucks and pinches {R+} nipples until they are hard, then puts #nipple_clamps# on and kisses him through the first minute of it.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("nipple_clamps", "nipple_stimulation"),
            equip = setOf(Equipment.NIPPLE_CLAMPS),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Mouth first", 60), tm("Clamps on", 90))
        ),
        t(
            id = "l3_toy_cock_ring", level = 3, cats = setOf(TOYS),
            title = "Cock ring",
            base = "{G} puts #cock_ring# on {R} and then strokes him with his hand and mouth while it stays on.",
            explicit = "{G} puts #cock_ring# on {R} and strokes and sucks him while it stays exactly where it is.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("cock_ring", "oral"),
            equip = setOf(Equipment.COCK_RING),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Ring on and hand", 90), tm("Mouth", 90))
        ),
        t(
            id = "l3_toy_wand_cock", level = 3, cats = setOf(TOYS),
            title = "Wand",
            base = "{G} runs #massage_wand# over {R+} cock, balls and inner thighs, holding it in one place for no more than fifteen seconds.",
            explicit = "{G} runs #massage_wand# over {R+} cock, balls and inner thighs and never leaves it in one place longer than fifteen seconds.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("massage_wand", "vibration"),
            equip = setOf(Equipment.MASSAGE_WAND),
            timers = listOf(tm("Cock", 45), tm("Balls", 45), tm("Inner thighs", 45))
        ),
        t(
            id = "l3_toy_blindfold_tease", level = 3, cats = setOf(TOYS, SENSORY),
            title = "Blindfolded with a toy",
            base = "{R} wears #blindfold#. {G} uses #TOY# on him and does not say aloud what it is or where he is putting it next.",
            explicit = "{R} wears #blindfold#. {G} uses #TOY# on him, moves it to a different part of his body every thirty seconds, and names none of them out loud.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("blindfold"), toy = true,
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("First pass", 60), tm("Second pass", 60), tm("Third pass", 60))
        ),

        // ------------------------------------------------------------ penetration (10)
        t(
            id = "l3_pen_on_back", level = 3, cats = setOf(ANAL),
            title = "On his back",
            base = "{R} lies on his back. {G} [v_enter] him with #lubricant# and moves [adv_thrust_slow], staying face to face throughout.",
            explicit = "{R} lies on his back and takes it face to face. {G} [v_enter] him with #lubricant# and [v_fuck] him [adv_thrust_slow], not looking away once.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Slow strokes", 180))
        ),
        t(
            id = "l3_pen_from_behind", level = 3, cats = setOf(ANAL),
            title = "From behind",
            base = "{R} gets on his hands and knees. {G} [v_enter] him from behind with #lubricant# and keeps a hand on the small of his back.",
            explicit = "{R} gets on his hands and knees and stays there. {G} [v_enter] him from behind with #lubricant#, one hand pinning the small of his back down, and [v_fuck] him.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("From behind", 180))
        ),
        t(
            id = "l3_pen_riding", level = 3, cats = setOf(ANAL),
            title = "All the way down, then hard",
            base = "{G} lies on his back and {R} lowers himself onto him with #lubricant#, taking him all the way in over the first minute, then riding him hard for the rest of the term.",
            explicit = "{G} lies flat and {R} sinks down onto him with #lubricant#, all the way down inside the first minute, then rides him hard for the rest, with {G} keeping his hands off his hips.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Taking him in", 60), tm("Riding him hard", 180))
        ),
        t(
            id = "l3_pen_over_furniture", level = 3, cats = setOf(ANAL),
            title = "Bent over",
            base = "{R} bends over #sturdy_chair#. {G} [v_enter] him from behind with #lubricant# and holds him in place by the hips.",
            explicit = "{R} bends over #sturdy_chair# and holds on. {G} [v_enter] him with #lubricant#, takes both hips in his hands and [v_fuck] him where he stands.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT, Equipment.STURDY_CHAIR),
            timers = listOf(tm("Pushing in", 45), tm("Bent over", 180))
        ),
        t(
            id = "l3_pen_legs_up", level = 3, cats = setOf(ANAL),
            title = "Legs up",
            base = "{R} lies on his back and pulls both knees to his chest. {G} [v_enter] him from there [adv_thrust_slow].",
            explicit = "{R} lies back and pulls both knees to his chest, wide open. {G} [v_enter] him from there and [v_fuck] him [adv_thrust_slow] with his weight on him.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Legs up", 180))
        ),
        t(
            id = "l3_pen_side_by_side", level = 3, cats = setOf(ANAL, KISSING),
            title = "On their sides",
            base = "{G} and {R} lie on their sides. {G} [v_enter] him from behind with #lubricant# and kisses his neck and ear throughout.",
            explicit = "{G} and {R} lie on their sides and {G} [v_enter] him from behind with #lubricant#, kissing his neck and ear the whole time he [v_fuck] him.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "neck_kissing", "ear_play"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("On their sides", 180))
        ),
        t(
            id = "l3_pen_standing", level = 3, cats = setOf(ANAL),
            title = "Standing",
            base = "{R} stands and braces against a wall. {G} [v_enter] him from behind with #lubricant# and holds him upright.",
            explicit = "{R} braces both hands on the wall and {G} [v_enter] him from behind with #lubricant#, holding him up by the waist while he [v_fuck] him.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Standing", 150))
        ),
        t(
            id = "l3_pen_mirror", level = 3, cats = setOf(ANAL, VISUAL),
            title = "Watching it happen",
            base = "{G} [v_enter] {R} in front of #mirror# and both of them watch the reflection rather than each other.",
            explicit = "{G} [v_enter] {R} in front of #mirror# and makes him keep his eyes on the reflection the whole time he [v_fuck] him.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gPrefs = setOf("mirror_play"), rPrefs = setOf("mirror_play"),
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT, Equipment.MIRROR),
            timers = listOf(tm("Pushing in", 45), tm("Watching", 180))
        ),
        t(
            id = "l3_pen_hold_still", level = 3, cats = setOf(ANAL, POWER),
            title = "All the way in and still",
            base = "{G} [v_enter] {R} all the way and then neither of them moves for a full minute before he starts.",
            explicit = "{G} [v_enter] {R} to the root and then holds completely still for a full minute, and {R} is not allowed to move either.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Completely still", 60), tm("Moving", 150))
        ),
        t(
            id = "l3_pen_pace_change", level = 3, cats = setOf(ANAL),
            title = "Changing pace",
            base = "{G} alternates between [adv_thrust_slow] and [adv_thrust_hard], one minute each, four times over.",
            explicit = "{G} moves [adv_thrust_slow], then [adv_thrust_hard], swapping every minute, four times over, and does not warn him before he changes.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Slow", 60), tm("Hard", 60), tm("Slow", 60), tm("Hard", 60))
        ),

        // -------------------------------------------------------- orgasm control (7)
        t(
            id = "l3_orgasm_edge", level = 3, cats = setOf(ORGASM_CONTROL),
            title = "Edging",
            base = "{G} [v_edge] three times with his hand, stopping dead at the end of each ninety-second run and waiting out the full thirty seconds before the next.",
            explicit = "{G} [v_edge] three separate times and pulls his hand away the moment each ninety-second run ends, waiting out the full thirty seconds before starting again.",
            benefit = GIVER, type = BenefitType.ORGASM_CONTROL,
            acts = setOf("edging"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Edge 1", 90), tm("Recovery 1", 30), tm("Edge 2", 90), tm("Recovery 2", 30), tm("Edge 3", 90))
        ),
        t(
            id = "l3_orgasm_denial", level = 3, cats = setOf(ORGASM_CONTROL),
            title = "Not tonight, not yet",
            base = "{G} brings {R} close with his mouth and then stops completely. {R} does not finish during this term.",
            explicit = "{G} takes {R} right to the edge with his mouth and then stops dead. {R} does not get to come during this term at all.",
            benefit = GIVER, type = BenefitType.ORGASM_CONTROL,
            acts = setOf("denial", "oral"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Up to the edge", 120), tm("Nothing at all", 60))
        ),
        t(
            id = "l3_orgasm_permission", level = 3, cats = setOf(ORGASM_CONTROL, POWER),
            title = "Asking to come",
            base = "{SUB} must ask out loud and be given permission before he finishes at any point tonight.",
            explicit = "{SUB} asks out loud and waits to be told yes before he comes at any point tonight, and does not assume the answer.",
            benefit = GIVER, type = BenefitType.PERMISSION_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("climax_permission", "permission_control"),
            timers = listOf(tm("In force for the rest of the scene", 60))
        ),
        t(
            id = "l3_orgasm_on_command", level = 3, cats = setOf(ORGASM_CONTROL, POWER),
            title = "On his word",
            base = "{DOM} strokes {SUB} with his hand and {SUB} holds on until {DOM} tells him he may finish.",
            explicit = "{DOM} strokes {SUB} with his hand and {SUB} [v_hold_back] until {DOM} tells him out loud that he can.",
            benefit = GIVER, type = BenefitType.ORGASM_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("climax_permission", "commands"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Stroking him", 120), tm("Holding on", 60))
        ),
        t(
            id = "l3_orgasm_edge_mutual", level = 3, cats = setOf(ORGASM_CONTROL, HANDS),
            title = "Edging each other",
            base = "{G} and {R} stroke each other with their hands and both stop dead at the end of each ninety-second round. Two rounds each.",
            explicit = "{G} and {R} stroke each other and both hands stop dead at the end of each ninety-second round. Two rounds each, no cheating.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("edging"),
            erection = PartyRef.BOTH,
            timers = listOf(tm("Round 1", 90), tm("Round 2", 90))
        ),
        t(
            id = "l3_orgasm_countdown", level = 3, cats = setOf(ORGASM_CONTROL, POWER),
            title = "Counted down",
            base = "{DOM} counts {SUB} down out loud from ten while stroking him, and {SUB} holds on to the end of the count.",
            explicit = "{DOM} counts {SUB} down from ten out loud while stroking him hard, and {SUB} [v_hold_back] until the count reaches zero.",
            benefit = GIVER, type = BenefitType.ORGASM_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("climax_permission", "commands"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Building him up", 120), tm("The count", 30))
        ),
        t(
            id = "l3_orgasm_hands_off", level = 3, cats = setOf(ORGASM_CONTROL, POWER),
            title = "Hands off",
            base = "{SUB} keeps both hands off himself for the whole of this term, whatever {DOM} does to him.",
            explicit = "{SUB} keeps both hands off his own cock for the entire term, no matter what {DOM} does to him.",
            benefit = GIVER, type = BenefitType.PERMISSION_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("permission_control", "commands"),
            timers = listOf(tm("Hands off", 180))
        ),

        // ------------------------------------------------------------------ extras (3)
        t(
            id = "l3_toy_ice_and_toy", level = 3, cats = setOf(TOYS, SENSORY),
            title = "Cold and buzzing",
            base = "{G} runs #ice# over {R+} chest and stomach, then follows it with #vibrator# over the same ground.",
            explicit = "{G} drags #ice# over {R+} chest and stomach and then runs #vibrator# over exactly the same ground while he is still cold.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("cold_sensation", "vibration"),
            equip = setOf(Equipment.ICE, Equipment.VIBRATOR),
            timers = listOf(tm("Ice", 45), tm("Vibrator", 60))
        ),
        t(
            id = "l3_toy_plug_and_kiss", level = 3, cats = setOf(TOYS, ANAL, KISSING),
            title = "Plugged and kissed",
            base = "{G} pushes #anal_plug# into {R} with #lubricant#, then kisses him [adv_kiss_deep] for the rest of the term and touches nothing else.",
            explicit = "{G} pushes #anal_plug# into {R} with #lubricant# and then does nothing but kiss him [adv_kiss_deep] for the rest of the term, hands nowhere near his cock.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("plug", "anal_toys", "tongue_kissing"), anal = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Pushing it in", 60), tm("Kissing", 180))
        ),
        t(
            id = "l3_anal_external_grind", level = 3, cats = setOf(ANAL),
            title = "Right up against him",
            base = "{G} grinds himself against {R+} [n_hole] with #lubricant# without going in at all, for the full time.",
            explicit = "{G} slicks up with #lubricant# and grinds himself against {R+} [n_hole] without once pushing in, for the whole term.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("anal_external"),
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Against him", 150))
        )
    )
}
