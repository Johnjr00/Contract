package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.GIVER
import com.thecontract.core.model.BenefitParty.MUTUAL
import com.thecontract.core.model.BenefitParty.RECEIVER
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.HANDS
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.MASSAGE
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
 * 56 terms (specification floor for level 3: 40).
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
            timers = listOf(tm("Slicking him up", 90)),
            says = listOf(
                "I am getting you ready for me.",
                "This is so you can take me later.",
                "You are being opened up so I can fuck you tonight.",
                "By the end of this you are going to take all of me.",
                "I am doing this now so nothing stops me later."
            ),
            saysExplicit = listOf(
                "I am getting your hole ready for my cock.",
                "This is so you can take every inch of me later.",
                "You are being opened up so I can fuck you properly tonight.",
                "By the end of this you are taking all of me and you are going to like it.",
                "I am doing this now so nothing stops me later."
            )
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
            base = "{R} holds himself open on his knees while {G} rubs his hole with #lubricant#, then works one finger into him, and says out loud what he sees.",
            explicit = "{R} gets on his knees and holds himself open with his own hands while {G} rubs his hole with #lubricant# and then pushes one finger into him, and describes out loud exactly what he is looking at.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering", "anal_external", "dirty_talk"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Held open", 90), tm("Opened up", 90)),
            says = listOf(
                "You are completely open right now.",
                "I can see everything.",
                "You are already slick.",
                "You tighten every time I touch you there.",
                "Nothing about you is hidden from me right now."
            ),
            saysExplicit = listOf(
                "Your hole is wide open right now.",
                "I can see every bit of you.",
                "You are dripping already.",
                "You clench every fucking time I touch it.",
                "There is nothing about you I cannot see right now."
            )
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
            base = "{G} opens {R} up with #lubricant#, then [v_insert] #anal_plug# and leaves it there, and works his cock with an oiled hand until the timer ends.",
            explicit = "{G} opens {R} up with #lubricant#, then [v_insert] #anal_plug# and leaves it in him while he [v_stroke] his cock with an oiled hand for the rest of the timer.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("plug", "anal_toys", "sexual_service"), anal = true,
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
            explicit = "{G} [v_insert] #prostate_toy# with #lubricant#, leaves it running inside {R} and kisses him through it with his hands nowhere near his cock.",
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
            base = "{G} puts #cock_ring# on {R} and then strokes and sucks his cock while it stays on.",
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
            id = "l3_orgasm_edge", level = 3, cats = setOf(ORGASM_CONTROL),
            title = "Edging",
            base = "{G} takes {R} in his hand and [v_edge] three times, stopping dead at the end of each ninety-second run and waiting out the full thirty seconds before the next.",
            explicit = "{G} takes {R} in his hand and [v_edge] three separate times, pulling his hand away the moment each ninety-second run ends and waiting out the full thirty seconds before starting again.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("edging"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Edge 1", 90), tm("Recovery 1", 30), tm("Edge 2", 90), tm("Recovery 2", 30), tm("Edge 3", 90))
        ),
        t(
            id = "l3_orgasm_denial", level = 3, cats = setOf(ORGASM_CONTROL),
            title = "Not tonight, not yet",
            base = "{G} brings {R} close with his mouth and then stops completely. {R} does not finish during this term.",
            explicit = "{G} takes {R} right to the edge with his mouth and then stops dead. {R} does not get to come during this term at all.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("denial", "oral"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Up to the edge", 120), tm("Nothing at all", 60))
        ),
        t(
            id = "l3_orgasm_permission", level = 3, cats = setOf(ORGASM_CONTROL, POWER),
            title = "Asking to come",
            base = "{SUB} must ask out loud and be given permission before he finishes at any point tonight.",
            explicit = "{SUB} asks out loud and waits to be told yes before he comes at any point tonight, every single time.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("climax_permission", "permission_control"),
            timers = listOf(tm("In force for the rest of the scene", 60))
        ),
        t(
            id = "l3_orgasm_on_command", level = 3, cats = setOf(ORGASM_CONTROL, POWER),
            title = "On his word",
            base = "{DOM} strokes {SUB} with his hand and {SUB} holds on until {DOM} tells him he may finish.",
            explicit = "{DOM} strokes {SUB} with his hand and {SUB} [v_hold_back] until {DOM} tells him out loud that he can.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("climax_permission", "commands"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Stroking him", 120), tm("Holding on", 60)),
            says = listOf(
                "You can finish now.",
                "Go ahead.",
                "Now.",
                "Let go.",
                "You have got it, finish."
            ),
            saysExplicit = listOf(
                "Come. Now.",
                "Go on, let it go.",
                "You can come.",
                "Now, and do not hold anything back.",
                "Give it to me."
            )
        ),
        t(
            id = "l3_orgasm_edge_mutual", level = 3, cats = setOf(ORGASM_CONTROL, HANDS),
            title = "Edging each other",
            base = "{G} and {R} stroke each other with their hands and both stop dead at the end of each ninety-second round. Two rounds each.",
            explicit = "{G} and {R} stroke each other and both hands stop dead at the end of each ninety-second round. Two rounds each, and neither hand starts again before the next round does.",
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
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("climax_permission", "commands"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Building him up", 120), tm("The count", 30))
        ),
        t(
            id = "l3_orgasm_hands_off", level = 3, cats = setOf(ORGASM_CONTROL, POWER),
            title = "Hands off",
            base = "{DOM} works {SUB+} cock with an oiled hand, sixty seconds slow and sixty seconds fast, three times over, and {SUB} keeps both hands flat on the bed and does not touch himself once.",
            explicit = "{DOM} [v_stroke] {SUB+} cock with an oiled hand, sixty seconds slow then sixty seconds fast, three times over, and {SUB} keeps both hands flat on the bed and does not touch his own cock once.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("permission_control", "commands", "sexual_service"),
            timers = listOf(tm("Slow", 60), tm("Fast", 60), tm("Slow again", 60))
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
        ),

        // ---------------------------------------------------------------- added (8)
        t(
            id = "l3_anal_finger_slow_open", level = 3, cats = setOf(ANAL),
            title = "One finger, no hurry",
            base = "{G} [v_finger] {R} and keeps to the same slow pace from the first second to the last.",
            explicit = "{G} works one finger into {R} and keeps the same slow pace throughout, no faster at the end than at the start.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("One finger", 120))
        ),
        t(
            id = "l3_anal_finger_and_stroke", level = 3, cats = setOf(ANAL, HANDS),
            title = "Inside and out",
            base = "{G} [v_finger] {R} with one hand and works his cock with the other, keeping both hands moving together.",
            explicit = "{G} has one hand working fingers into {R} and the other on his cock, both moving together, for the full timer.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering", "sexual_service"), anal = true,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Both hands", 150))
        ),
        t(
            id = "l3_toy_plug_and_oral", level = 3, cats = setOf(TOYS, ORAL),
            title = "Filled and sucked",
            base = "{G} works a plug into {R} and then [v_suck] him with it still in, until the timer ends.",
            explicit = "{G} works #anal_plug# into {R} and then [v_suck] his cock with it still in him for the whole of the timer.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("plug", "oral"), anal = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.LUBRICANT),
            timers = listOf(tm("Filled", 150))
        ),
        t(
            id = "l3_toy_vibe_and_hand", level = 3, cats = setOf(TOYS, HANDS),
            title = "Toy and hand",
            base = "{G} holds a vibrator against {R+} cock with one hand and works him with the other, and does not let either stop.",
            explicit = "{G} holds a vibrator on {R+} cock and [v_stroke] him with his free hand, neither one stopping until the timer does.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("vibration", "sexual_service"),
            equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Toy and hand", 120))
        ),
        t(
            id = "l3_orgasm_stop_at_the_edge", level = 3, cats = setOf(ORGASM_CONTROL),
            title = "Three times to the edge",
            base = "{G} [v_edge] three times, stopping the moment {R} says he is close, and lets {R} finish on the third only if he asks for it.",
            explicit = "{G} [v_edge] three times and takes his hand off {R} the second he says he is close, and lets {R} finish on the third only if he asks.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("edging", "sexual_service"),
            timers = listOf(tm("First", 60), tm("Second", 60), tm("Third", 60))
        ),
        t(
            id = "l3_rim_then_finger_then_toy", level = 3, cats = setOf(RIMMING, ANAL, TOYS),
            title = "In that order",
            base = "{G} [v_rim] {R}, then [v_finger] him, then works a toy into him, moving on only when each timer ends.",
            explicit = "{G} [v_rim] {R} with his tongue, then works fingers into him, then a toy, moving on at each timer and not before.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("rimming", "fingering", "anal_toys"), anal = true,
            equip = setOf(Equipment.LUBRICANT), toy = true,
            timers = listOf(tm("Tongue", 90), tm("Fingers", 90), tm("Toy", 90))
        ),

        // ---------------------------------------------------------------- added (4)
        t(
            id = "l3_oral_two_minutes_each", level = 3, cats = setOf(ORAL),
            title = "Two minutes each way",
            base = "{G} [v_suck] {R} slowly for two minutes, then as hard and fast as he can for two more, and does not change over early or late.",
            explicit = "{G} [v_suck] {R+} cock slowly for two minutes, then as hard and fast as he can manage for two more, changing over exactly on the timer.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "harder_oral"),
            timers = listOf(tm("Slow", 120), tm("Hard", 120))
        ),
        t(
            id = "l3_anal_three_fingers_count", level = 3, cats = setOf(ANAL),
            title = "One finger at a time",
            base = "{G} [v_finger] {R} with #lubricant#, one finger for the first timer, two for the second and three for the third, and adds the next only when the clock says so.",
            explicit = "{G} works fingers into {R} with #lubricant# — one, then two, then three — adding the next only when its timer starts and not before.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("One finger", 90), tm("Two fingers", 90), tm("Three fingers", 90))
        ),
        t(
            id = "l3_rim_and_stroke_together", level = 3, cats = setOf(RIMMING, HANDS),
            title = "Tongue and hand at once",
            base = "{R} goes face down with his hips up. {G} [v_rim] him and works his cock with an oiled hand at the same time, both keeping the same rhythm.",
            explicit = "{R} goes face down with his hips up and {G} [v_rim] him while he [v_stroke] his cock with an oiled hand, both in the same rhythm, neither stopping until the timer does.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming", "sexual_service"),
            timers = listOf(tm("Tongue and hand", 180))
        ),
        t(
            id = "l3_toy_vibe_four_places", level = 3, cats = setOf(TOYS),
            title = "Four places, thirty seconds each",
            base = "{G} holds the vibrator on {R+} nipples, then the inside of each thigh, then his balls, then the underside of his cock, thirty seconds on each and no longer.",
            explicit = "{G} holds the vibrator on {R+} nipples, the inside of each thigh, his balls and the underside of his cock in that order, thirty seconds on each and not a second more.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("vibration"),
            equip = setOf(Equipment.VIBRATOR),
            timers = listOf(tm("Nipples", 30), tm("Thighs", 30), tm("Balls", 30), tm("Cock", 30))
        ),

        // ---------------------------------------------------------------- added (16)
        t(
            id = "l3_anal_two_fingers_counted", level = 3, cats = setOf(ANAL),
            title = "Ten strokes, then held",
            base = "{G} slicks two fingers with #lubricant#, gives {R} ten slow strokes with them, then holds them still and buried for a slow count of twenty, and does that three times over.",
            explicit = "{G} slicks two fingers with #lubricant#, gives {R} ten slow strokes, then holds them buried and dead still for a slow count of twenty, three rounds of it.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Round 1", 60), tm("Round 2", 60), tm("Round 3", 60))
        ),
        t(
            id = "l3_anal_finger_knees_held", level = 3, cats = setOf(ANAL),
            title = "Knees to his chest",
            base = "{R} lies on his back and holds both knees against his own chest. {G} slicks up with #lubricant# and [v_finger] {R}, then adds a second finger when the second timer starts, and {R} holds his knees where they are throughout.",
            explicit = "{R} lies on his back and pulls both knees to his own chest. {G} slicks up with #lubricant# and [v_finger] {R}, adds a second finger when the second timer starts, and {R} does not let his knees drop before the timer ends.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("One finger", 90), tm("Two fingers", 120))
        ),
        t(
            id = "l3_anal_rim_and_two_fingers", level = 3, cats = setOf(ANAL, RIMMING),
            title = "Tongue, then tongue and fingers",
            base = "{G} [v_rim] {R} for the first timer, then keeps his tongue working on him and slides two slick fingers into him with #lubricant# for the second.",
            explicit = "{G} [v_rim] {R} for the first timer, then keeps his tongue on him and pushes two slick fingers into him with #lubricant# for the second.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming", "fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Tongue only", 120), tm("Tongue and fingers", 150))
        ),
        t(
            id = "l3_anal_held_still_inside", level = 3, cats = setOf(ANAL),
            title = "Still, for a count of thirty",
            base = "{G} warms #lubricant# in his hands, works one finger into {R} and then holds it completely still, buried, for a slow count of thirty before he moves it at all. He does that three times over.",
            explicit = "{G} warms #lubricant# in his hands, pushes one finger into {R} and holds it dead still for a slow count of thirty before he moves it, three times over.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("First", 60), tm("Second", 60), tm("Third", 60))
        ),
        t(
            id = "l3_anal_fingers_and_kissing", level = 3, cats = setOf(ANAL, KISSING),
            title = "Fingered and kissed",
            base = "{G} lies face to face with {R}, works two slick fingers into him with #lubricant# and kisses him [adv_kiss_deep] the entire time, and does not break the kiss before the timer ends.",
            explicit = "{G} lies face to face with {R}, pushes two slick fingers into him with #lubricant# and keeps kissing him [adv_kiss_deep] the whole way through, and does not break off before the timer ends.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering", "tongue_kissing"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Fingered and kissed", 180))
        ),
        t(
            id = "l3_anal_three_positions", level = 3, cats = setOf(ANAL),
            title = "Ninety seconds a position",
            base = "{G} slicks two fingers with #lubricant#, works them into {R} and keeps them in him through three positions in this order: face down, then on his back with his knees up, then on his side. {G} moves him on only when a timer ends.",
            explicit = "{G} works two slick fingers into {R} with #lubricant# and keeps them in him through three positions in this order: face down, on his back with his knees up, then on his side. {G} moves him on only when a timer ends.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Face down", 90), tm("On his back", 90), tm("On his side", 90))
        ),
        t(
            id = "l3_toy_plug_and_back_rub", level = 3, cats = setOf(TOYS, ANAL, MASSAGE),
            title = "Plugged, then worked",
            base = "{G} opens {R} up with #lubricant#, [v_insert] #anal_plug#, then puts him face down and, with the plug still in him, [v_massage] his back and shoulders [adv_pressure_firm].",
            explicit = "{G} opens {R} up with #lubricant# and [v_insert] #anal_plug#, then puts him face down and [v_knead] his back and shoulders [adv_pressure_deep] while the plug stays in him.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("plug", "anal_toys", "massage_upper_back"), anal = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Opening him up", 60), tm("The plug in", 45), tm("Back and shoulders", 180))
        ),
        t(
            id = "l3_toy_dildo_counted", level = 3, cats = setOf(TOYS, ANAL),
            title = "Twenty, then held",
            base = "{G} pushes #dildo# into {R} with #lubricant#, gives him twenty slow strokes with it, then holds it all the way in for a slow count of twenty, and repeats that three times.",
            explicit = "{G} pushes #dildo# into {R} with #lubricant#, gives him twenty slow strokes, then holds it all the way in for a slow count of twenty, three rounds of it.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Round 1", 75), tm("Round 2", 75), tm("Round 3", 75))
        ),
        t(
            id = "l3_toy_vibe_on_the_plug", level = 3, cats = setOf(TOYS, ANAL),
            title = "Buzzing against the plug",
            base = "{G} works #anal_plug# into {R} with #lubricant#, then holds #vibrator# against the base of it [adv_toy_pace] and keeps it there for the whole of the second timer.",
            explicit = "{G} works #anal_plug# into {R} with #lubricant#, then presses #vibrator# hard against the base of it [adv_toy_pace] and does not lift it off before the timer ends.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("plug", "anal_toys", "vibration"), anal = true,
            equip = setOf(Equipment.ANAL_PLUG, Equipment.VIBRATOR, Equipment.LUBRICANT),
            rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("The plug in", 60), tm("Vibrator on the base", 150))
        ),
        t(
            id = "l3_toy_two_in_turn", level = 3, cats = setOf(TOYS, ANAL),
            title = "One toy, then the other",
            base = "{G} uses #dildo# on {R} with #lubricant# for the first timer, draws it out, then [v_insert] #prostate_toy# and leaves it in him for the rest of the term.",
            explicit = "{G} fucks {R} with #dildo# and plenty of #lubricant# for the first timer, pulls it out, then [v_insert] #prostate_toy# and leaves it in him for the rest of the term.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys"), anal = true,
            equip = setOf(Equipment.DILDO, Equipment.PROSTATE_TOY, Equipment.LUBRICANT),
            rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("The dildo", 150), tm("The prostate toy", 180))
        ),
        t(
            id = "l3_toy_ring_and_vibrator", level = 3, cats = setOf(TOYS),
            title = "Ring on, toy running",
            base = "{G} puts #cock_ring# on {R}, then holds #vibrator# against the underside of his cock [adv_toy_pace] and moves it every thirty seconds between the head, the base and his balls.",
            explicit = "{G} puts #cock_ring# on {R} and runs #vibrator# on the underside of his cock [adv_toy_pace], moving it every thirty seconds between the head, the base and his balls.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("cock_ring", "vibration"),
            equip = setOf(Equipment.COCK_RING, Equipment.VIBRATOR),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Underside", 30), tm("Head", 30), tm("Base", 30), tm("Balls", 30))
        ),
        t(
            id = "l3_toy_clamps_and_mouth", level = 3, cats = setOf(TOYS, ORAL),
            title = "Clamped and sucked",
            base = "{G} sucks {R+} nipples until they are hard, puts #nipple_clamps# on him, then [v_suck] {R+} cock with the clamps staying on for the rest of the term.",
            explicit = "{G} sucks {R+} nipples hard, clips #nipple_clamps# onto them, then [v_suck] {R+} cock and leaves the clamps exactly where they are.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("nipple_clamps", "nipple_stimulation", "oral"),
            equip = setOf(Equipment.NIPPLE_CLAMPS),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Mouth first", 60), tm("Clamps on", 45), tm("Sucked", 120))
        ),
        t(
            id = "l3_toy_wand_on_his_hole", level = 3, cats = setOf(TOYS, ANAL),
            title = "Wand against his hole",
            base = "{R} kneels with his chest down. {G} slicks him with #lubricant#, holds #massage_wand# against {R+} [n_hole] [adv_toy_pace], and works one finger into him alongside it for the second timer.",
            explicit = "{R} kneels with his chest down and his ass up. {G} slicks him with #lubricant#, presses #massage_wand# against his hole [adv_toy_pace] and pushes one finger into him alongside it for the second timer.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("massage_wand", "vibration", "anal_external", "fingering"), anal = true,
            equip = setOf(Equipment.MASSAGE_WAND, Equipment.LUBRICANT), rCon = PartyConstraint.BOTTOM,
            timers = listOf(tm("Wand only", 90), tm("Wand and finger", 120))
        ),
        t(
            id = "l3_orgasm_edge_by_mouth", level = 3, cats = setOf(ORGASM_CONTROL, ORAL),
            title = "Three times with his mouth",
            base = "{G} [v_suck] {R+} cock until {R} says out loud that he is close, then comes off him completely for a slow count of thirty. Three rounds of that, and {R} does not come during this term.",
            explicit = "{G} [v_suck] {R+} cock until {R} says out loud that he is close, then pulls his mouth off him for a slow count of thirty. Three rounds of it, and {R} does not come during this term.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "edging", "denial"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Round 1", 90), tm("Round 2", 90), tm("Round 3", 90))
        ),
        t(
            id = "l3_orgasm_five_minutes_no_finish", level = 3, cats = setOf(ORGASM_CONTROL, HANDS),
            title = "Five minutes, and no finish",
            base = "{G} keeps a slick hand on {R+} cock for five straight minutes, stops the moment {R} says he is close and starts again after a slow count of twenty. {R} does not come during this term.",
            explicit = "{G} keeps a slick hand on {R+} cock for five straight minutes, stops dead every time {R} says he is close and starts again after a slow count of twenty. {R} does not come at any point in this term.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("edging", "denial"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("First half", 150), tm("Second half", 150))
        ),
        t(
            id = "l3_orgasm_ten_then_thirty", level = 3, cats = setOf(ORGASM_CONTROL),
            title = "Ten strokes, thirty off",
            base = "{G} gives {R} ten slow strokes with a slick hand, then lifts his hand right off for thirty seconds, and runs that cycle six times over. {R} keeps both hands behind his own head.",
            explicit = "{G} gives {R} ten slow strokes with a slick hand, then takes his hand right off for thirty seconds, six cycles of it, and {R} keeps both hands behind his own head.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("edging", "denial"),
            timers = listOf(tm("Cycles 1 and 2", 120), tm("Cycles 3 and 4", 120), tm("Cycles 5 and 6", 120))
        )
    )
}
