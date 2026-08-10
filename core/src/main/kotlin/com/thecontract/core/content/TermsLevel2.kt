package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.GIVER
import com.thecontract.core.model.BenefitParty.MUTUAL
import com.thecontract.core.model.BenefitParty.RECEIVER
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.BODY_WORSHIP
import com.thecontract.core.model.Category.HANDS
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.LANGUAGE
import com.thecontract.core.model.Category.MASSAGE
import com.thecontract.core.model.Category.ORAL
import com.thecontract.core.model.Category.POWER
import com.thecontract.core.model.Category.RIMMING
import com.thecontract.core.model.Category.SENSORY
import com.thecontract.core.model.Category.SPIT
import com.thecontract.core.model.Category.TOYS
import com.thecontract.core.model.Category.VISUAL
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.Term

/**
 * Act II — Access. Oral, rimming, hand stimulation, service and permission.
 * 40 terms (specification floor for level 2: 40).
 */
internal object TermsLevel2 {

    val terms: List<Term> = listOf(

        // ---------------------------------------------------------------- oral (13)
        t(
            id = "l2_oral_slow", level = 2, cats = setOf(ORAL),
            title = "Slow oral",
            base = "{G} [v_suck_slow] {R+} cock and does not speed up once, keeping one hand flat on {R+} stomach so he stays where he is.",
            explicit = "{G} gets between {R+} legs and [v_suck_slow] his cock, one hand pinning his stomach down so he cannot chase it, and does not speed up once.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral"),
            timers = listOf(tm("Slow oral", 120))
        ),
        t(
            id = "l2_oral_kneeling", level = 2, cats = setOf(ORAL, POWER),
            title = "On his knees",
            base = "{R} stands. {G} kneels in front of him and [v_suck] {R+} cock, hands behind his own back the whole time.",
            explicit = "{R} stands and {G} goes down on his knees in front of him, hands locked behind his own back, and [v_suck] {R+} cock for the full timer.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "kneeling"),
            timers = listOf(tm("On his knees", 120))
        ),
        t(
            id = "l2_oral_deep", level = 2, cats = setOf(ORAL),
            title = "Deep oral",
            base = "{G} [v_deep] {R+} cock in stages, taking it further on each pass than the last, and comes up for air between passes rather than during one.",
            explicit = "{G} [v_deep] {R+} cock, further on every pass, and comes up for air between each pass and not during one.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "deep_oral"),
            timers = listOf(tm("Deeper", 90), tm("Holding him deep", 45))
        ),
        t(
            id = "l2_oral_plus_hand", level = 2, cats = setOf(ORAL, HANDS),
            title = "Mouth and hand",
            base = "{G} [v_suck] the head of {R+} cock while his hand [v_stroke] the rest, keeping both moving at the same rhythm.",
            explicit = "{G} [v_suck] the head of {R+} cock and [v_stroke] the rest with his fist at the same time, both moving together, and does not break rhythm.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral"),
            timers = listOf(tm("Mouth and hand together", 150))
        ),
        t(
            id = "l2_oral_eye_contact", level = 2, cats = setOf(ORAL, POWER),
            title = "Eyes up",
            base = "{G} [v_suck] {R+} cock and keeps his eyes on {R+} face the entire time. If he looks away, he starts the timer again.",
            explicit = "{G} [v_suck] {R+} cock with his eyes locked on {R+} face the whole time. He looks away, the timer goes back to the start.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "sexual_service"),
            timers = listOf(tm("Eyes up", 120))
        ),
        t(
            id = "l2_oral_tease_stop", level = 2, cats = setOf(ORAL),
            title = "Stop and start",
            base = "{G} [v_suck] {R+} cock for thirty seconds, comes off for ten, and repeats the cycle four times.",
            explicit = "{G} [v_suck] {R+} cock for thirty seconds, pulls off for ten and touches nothing during the gap, four times over, and does not shorten a single gap.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral"),
            timers = listOf(
                tm("Round 1 on", 30), tm("Round 1 off", 10),
                tm("Round 2 on", 30), tm("Round 2 off", 10),
                tm("Round 3 on", 30), tm("Round 3 off", 10),
                tm("Round 4 on", 30)
            )
        ),
        t(
            id = "l2_oral_balls", level = 2, cats = setOf(ORAL),
            title = "Cock and balls",
            base = "{G} rolls {R+} balls with his mouth first, then moves up the shaft and [v_suck] him for the rest of the timer, taking him as deep as he can on every stroke.",
            explicit = "{G} rolls {R+} balls with his mouth, then drags his tongue up the shaft and only then [v_suck] him, taking him as deep as he can on every stroke for the rest of the timer.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral"),
            timers = listOf(tm("Balls", 60), tm("Up the shaft", 30), tm("Cock", 90))
        ),
        t(
            id = "l2_oral_chair", level = 2, cats = setOf(ORAL, POWER),
            title = "In the chair",
            base = "{R} sits in #sturdy_chair# with his legs open. {G} kneels between them and [v_suck] him, thirty seconds slow then thirty seconds fast, on repeat.",
            explicit = "{R} sits back in #sturdy_chair# with his legs wide open. {G} kneels between them and [v_suck] him, thirty seconds slow then thirty seconds fast, on repeat, without a break.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "kneeling"),
            equip = setOf(Equipment.STURDY_CHAIR),
            timers = listOf(tm("In the chair", 150))
        ),
        t(
            id = "l2_oral_blindfolded", level = 2, cats = setOf(ORAL, SENSORY),
            title = "Blindfolded oral",
            base = "{R} wears #blindfold# throughout. {G} [v_suck] him, alternating twenty seconds deep and twenty seconds shallow.",
            explicit = "{R} wears #blindfold# and {G} says nothing to him from start to finish. {G} [v_suck] him, alternating twenty seconds deep and twenty seconds shallow.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "blindfold"),
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Blindfolded oral", 150))
        ),
        t(
            id = "l2_oral_head_held", level = 2, cats = setOf(ORAL, POWER),
            title = "Hands on his head",
            base = "{G} [v_suck] {R+} cock while {R} rests both hands on {G+} head and pulls him all the way down every fifth stroke.",
            explicit = "{G} [v_suck] {R+} cock and {R} keeps both hands on {G+} head, pulling him all the way down every fifth stroke. {G} keeps his hands behind his own back throughout.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "deep_oral"), rPrefs = setOf("commands_give"),
            timers = listOf(tm("Hands on his head", 120))
        ),
        t(
            id = "l2_oral_ice", level = 2, cats = setOf(ORAL, SENSORY),
            title = "Cold then hot",
            base = "{G} holds #ice# in his mouth for a slow count of ten, then [v_suck] {R+} cock, and repeats once the cold has gone.",
            explicit = "{G} holds #ice# in his mouth for a slow count of twenty, then [v_suck] {R+} cock cold, and does the whole thing again once his mouth has been warm for forty-five seconds.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "cold_sensation"),
            equip = setOf(Equipment.ICE),
            timers = listOf(tm("Cold round 1", 45), tm("Warm round 1", 45), tm("Cold round 2", 45), tm("Warm round 2", 45))
        ),
        t(
            id = "l2_oral_mirror", level = 2, cats = setOf(ORAL, VISUAL),
            title = "Oral at the mirror",
            base = "{R} stands facing #mirror#. {G} kneels in front of him and [v_suck] him while {R} watches the reflection.",
            explicit = "{R} stands facing #mirror# and watches everything. {G} kneels and [v_suck] him, and {R} does not get to look anywhere else.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "kneeling"),
            gPrefs = setOf("mirror_play"), rPrefs = setOf("mirror_play"),
            equip = setOf(Equipment.MIRROR),
            timers = listOf(tm("At the mirror", 120))
        ),
        t(
            id = "l2_oral_spit", level = 2, cats = setOf(ORAL, SPIT),
            title = "Wet",
            base = "{G} keeps {R+} cock [adj_wet] with his own spit throughout and never lets it dry.",
            explicit = "{G} spits on {R+} cock, keeps it [adj_wet] the whole way through and goes back for more every time it starts to dry.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "spit"),
            blocks = setOf(Boundary.NO_SPIT_PLAY),
            timers = listOf(tm("Wet oral", 120))
        ),

        // ---------------------------------------------------------------- rimming (6)
        t(
            id = "l2_rim_basic", level = 2, cats = setOf(RIMMING, ANAL),
            title = "Rimming",
            base = "{R} lies face down. {G} spreads him open and [v_rim] him [adv_slow], hands on both cheeks throughout.",
            explicit = "{R} goes face down and {G} spreads him wide open and [v_rim] him [adv_slow], keeping a hand on each cheek so he stays open.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming"),
            timers = listOf(tm("Rimming", 180))
        ),
        t(
            id = "l2_rim_deep", level = 2, cats = setOf(RIMMING, ANAL),
            title = "Deeper rimming",
            base = "{G} [v_rim_hard] {R}, pushing his tongue in rather than only over the outside.",
            explicit = "{G} [v_rim_hard] {R}, gets his tongue inside him and keeps it there rather than only licking the outside.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming"),
            timers = listOf(tm("Outside", 45), tm("Pushing in", 120))
        ),
        t(
            id = "l2_rim_on_back", level = 2, cats = setOf(RIMMING, ANAL),
            title = "On his back",
            base = "{R} lies on his back with his knees pulled up. {G} [v_rim] him from there so {R} can watch.",
            explicit = "{R} lies on his back and pulls his own knees up to his chest. {G} [v_rim] him from there and {R} watches every second of it.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming"),
            timers = listOf(tm("Rimming on his back", 150))
        ),
        t(
            id = "l2_rim_plus_stroke", level = 2, cats = setOf(RIMMING, HANDS, ANAL),
            title = "Rimming and stroking",
            base = "{G} [v_rim] {R} while one hand reaches round and [v_stroke] his cock at the same rhythm.",
            explicit = "{G} [v_rim] {R} and, reaching round, [v_stroke] his cock in the same rhythm, so he gets both at once.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming"),
            timers = listOf(tm("Rimming only", 60), tm("Rimming and stroking", 120))
        ),
        t(
            id = "l2_rim_chair", level = 2, cats = setOf(RIMMING, ANAL),
            title = "Over the edge",
            base = "{R} bends over #sturdy_chair#. {G} kneels behind him and [v_rim] him from there.",
            explicit = "{R} bends over #sturdy_chair# and holds on. {G} kneels behind him, spreads him with both hands and [v_rim] him from there for the full timer.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming"),
            equip = setOf(Equipment.STURDY_CHAIR),
            timers = listOf(tm("Over the edge", 150))
        ),
        t(
            id = "l2_rim_reciprocal", level = 2, cats = setOf(RIMMING, ANAL),
            title = "Both at once",
            base = "{G} and {R} get into a position where they can use their tongues on each other at the same time, and both keep going for the full time.",
            explicit = "{G} and {R} get into position and use their tongues on each other at the same time. Neither one stops before the timer ends.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("rimming"),
            timers = listOf(tm("Both at once", 180))
        ),

        // ---------------------------------------------------------------- hands (7)
        t(
            id = "l2_hand_stroke", level = 2, cats = setOf(HANDS),
            title = "Hand only",
            base = "{G} [v_stroke] {R+} cock [adv_grip], using plenty of #lubricant#, twenty seconds slow then twenty seconds fast, on repeat.",
            explicit = "{G} [v_stroke] {R+} cock [adv_grip], using plenty of #lubricant#, twenty seconds slow then twenty seconds fast, on repeat, and does not break the cycle.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("massage_groin"),
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Hand only", 150))
        ),
        t(
            id = "l2_hand_mutual", level = 2, cats = setOf(HANDS),
            title = "Both hands",
            base = "{G} and {R} lie face to face and stroke each other's cocks with a slick fist and plenty of #lubricant#, each of them matching the pace the other sets.",
            explicit = "{G} and {R} lie face to face and stroke each other's cocks with a slick fist and plenty of #lubricant#, each of them matching the pace the other sets, for the full timer.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("massage_groin"),
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Both hands", 150))
        ),
        t(
            id = "l2_hand_groin_massage", level = 2, cats = setOf(HANDS, BODY_WORSHIP),
            title = "Groin massage",
            base = "{G} [v_massage] the whole area around {R+} cock — hip creases, the base, the skin behind the balls — without touching the shaft at all.",
            explicit = "{G} kneads the whole area around {R+} cock with #massage_oil# — hip creases, the base, the skin behind his balls — and does not touch the shaft once.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_groin"),
            equip = setOf(Equipment.MASSAGE_OIL),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Left hip crease", 45), tm("Right hip crease", 45), tm("Base and behind the balls", 60))
        ),
        t(
            id = "l2_hand_balls", level = 2, cats = setOf(HANDS),
            title = "Balls",
            base = "{G} cups {R+} balls in an open hand [adv_pressure_light], rolls them in his palm and never closes his fingers on them.",
            explicit = "{G} cups {R+} balls in an open hand [adv_pressure_light], rolls them in his palm, then pulls them gently down and away from his body for a slow count of five, three times over.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("massage_groin"),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Balls", 90))
        ),
        t(
            id = "l2_hand_sleeve", level = 2, cats = setOf(HANDS, TOYS),
            title = "The sleeve",
            base = "{G} uses #sleeve# on {R+} cock, changing speed every thirty seconds between slow and fast.",
            explicit = "{G} slides #sleeve# down {R+} cock and swaps between slow and fast every thirty seconds for the full timer.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("massage_groin"),
            equip = setOf(Equipment.SLEEVE, Equipment.LUBRICANT),
            erection = PartyRef.RECEIVER,
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Slow", 30), tm("Fast", 30), tm("Slow", 30), tm("Fast", 30))
        ),
        t(
            id = "l2_hand_oiled_massage_to_sexual", level = 2, cats = setOf(HANDS, BODY_WORSHIP),
            title = "Massage that turns",
            base = "{G} starts with a full-back massage using #massage_oil#, works down over {R+} lower back and hips, and finishes with his oiled hand on {R+} cock, without ever lifting both hands off him.",
            explicit = "{G} starts on {R+} back with #massage_oil#, works down over his lower back and ass, then takes his cock in his oiled hand and stays there for the rest of the term, without ever lifting both hands off him.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_general", "massage_oil", "massage_to_sexual"),
            equip = setOf(Equipment.MASSAGE_OIL),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Back", 90), tm("Lower back and hips", 60), tm("Turning it", 90))
        ),
        t(
            id = "l2_hand_porn_together", level = 2, cats = setOf(HANDS, VISUAL),
            title = "Watching and stroking",
            base = "{G} and {R} put on #porn# and stroke each other with their hands, both watching the screen rather than each other.",
            explicit = "{G} and {R} put on #porn# and stroke each other with their hands, eyes on the screen the entire time, no looking at each other.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("massage_groin"),
            gPrefs = setOf("watching_porn"), rPrefs = setOf("watching_porn"),
            equip = setOf(Equipment.PORN),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Watching and stroking", 180))
        ),

        // ------------------------------------------------- service, power, permission (10)
        t(
            id = "l2_service_kneel", level = 2, cats = setOf(POWER),
            title = "Kneeling service",
            base = "{SUB} kneels in front of {DOM} and stays there, hands on his own thighs, until {DOM} tells him to get up.",
            explicit = "{SUB} goes down on his knees in front of {DOM}, hands flat on his own thighs, and stays exactly there until {DOM} says otherwise.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("kneeling", "sexual_service"),
            timers = listOf(tm("Kneeling", 120))
        ),
        t(
            id = "l2_service_undress", level = 2, cats = setOf(POWER, BODY_WORSHIP),
            title = "Undressing him",
            base = "{SUB} undresses {DOM} completely, one item at a time, and kisses whatever he uncovers before moving on.",
            explicit = "{SUB} strips {DOM} one item at a time and puts his mouth on whatever he uncovers before he is allowed to move on to the next.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("sexual_service", "body_worship"),
            timers = listOf(tm("Undressing", 120))
        ),
        t(
            id = "l2_service_ask_permission", level = 2, cats = setOf(POWER),
            title = "Asking first",
            base = "For the length of this term, {SUB} asks out loud before he touches {DOM} anywhere, and waits for an answer each time.",
            explicit = "For the whole of this term {SUB} asks out loud before he puts a hand anywhere on {DOM}, and waits for an actual answer every single time.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("permission_control", "commands"),
            timers = listOf(tm("Asking first", 180))
        ),
        t(
            id = "l2_service_hold_position", level = 2, cats = setOf(POWER),
            title = "Holding position",
            base = "{DOM} puts {SUB} face down with his knees apart and his arms stretched above his head. {SUB} [v_hold_pos] while {DOM} runs both hands over his back, his ass and the backs of his thighs.",
            explicit = "{DOM} puts {SUB} face down with his knees apart and his arms stretched above his head. {SUB} [v_hold_pos] while {DOM} uses his hands and mouth on his back, his ass and the inside of his thighs for the full timer.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"),
            timers = listOf(tm("Holding position", 120))
        ),
        t(
            id = "l2_service_worship_on_knees", level = 2, cats = setOf(POWER, BODY_WORSHIP),
            title = "Worship from the floor",
            base = "{SUB} kneels and kisses {DOM+} thighs, stomach and hips, without touching {DOM+} cock at all.",
            explicit = "{SUB} kneels and kisses {DOM+} thighs, stomach and hips and is not allowed anywhere near his cock at any point in the term.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("kneeling", "body_worship", "stomach_hip_kissing"),
            timers = listOf(tm("Thighs", 60), tm("Stomach", 45), tm("Hips", 45))
        ),
        t(
            id = "l2_power_commands", level = 2, cats = setOf(POWER),
            title = "Doing as he is told",
            base = "{DOM} gives {SUB} a short instruction every thirty seconds and {SUB} carries each one out without comment.",
            explicit = "{DOM} [v_order] {SUB} into a new position every thirty seconds, naming it out loud, and {SUB} moves into it without a word back.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"),
            timers = listOf(tm("Round 1", 30), tm("Round 2", 30), tm("Round 3", 30), tm("Round 4", 30))
        ),
        t(
            id = "l2_power_praise", level = 2, cats = setOf(POWER, LANGUAGE),
            title = "Told exactly how good he is",
            base = "{G} strokes {R} with his hands and [v_praise], again every thirty seconds, and does not go quiet in between.",
            explicit = "{G} strokes {R} with his hands and [v_praise], again every twenty seconds, and does not go quiet once.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("explicit_praise"),
            timers = listOf(tm("Talking and touching", 120))
        ),
        t(
            id = "l2_power_dirty_talk", level = 2, cats = setOf(POWER, LANGUAGE),
            title = "Talking him through it",
            base = "{G} [v_talk_dirty] with his mouth at {R+} ear, describing exactly what he intends to do later, while his hands stay still.",
            explicit = "{G} puts his mouth to {R+} ear and [v_talk_dirty], spelling out exactly what he is going to do to him later, and keeps his hands completely still while he does it.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("dirty_talk", "ear_play"),
            timers = listOf(tm("Talking at his ear", 120))
        ),
        t(
            id = "l2_anal_external", level = 2, cats = setOf(ANAL),
            title = "Outside only",
            base = "{G} rubs {R+} [n_hole] from the outside with a slick thumb, circling and pressing, and does not go in.",
            explicit = "{G} rubs {R+} [n_hole] from the outside with a slick thumb, circling and pressing, and does not put anything inside him at any point in the term.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("anal_external"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Outside only", 120))
        ),
        t(
            id = "l2_anal_massage_hole", level = 2, cats = setOf(ANAL, MASSAGE),
            title = "Opening him up from the outside",
            base = "{G} uses #lubricant# and [v_massage] the ring of muscle around {R+} hole [adv_pressure_light] for the full timer, and does not go in.",
            explicit = "{G} slicks {R} with #lubricant# and rubs the ring of muscle around his hole [adv_pressure_light] for the full timer, and does not go in at any point.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_anus", "anal_external"),
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Rubbing the muscle", 120))
        ),

        // ---------------------------------------------------------------- extras (4)
        t(
            id = "l2_oral_harder_intro", level = 2, cats = setOf(ORAL),
            title = "A harder pace",
            base = "{R} fucks {G+} mouth faster than usual and {G} keeps his lips sealed around him for the full time without pulling off.",
            explicit = "{R} fucks {G+} mouth hard and {G} keeps his lips sealed around him for the full time, without pulling off once.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "harder_oral"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Hard pace", 90))
        ),
        t(
            id = "l2_oral_hair_grip", level = 2, cats = setOf(ORAL, POWER),
            title = "A hand in his hair",
            base = "{G} [v_suck] {R+} cock while {R} keeps one hand closed in {G+} hair and uses it to set how deep he goes.",
            explicit = "{G} [v_suck] {R+} cock and {R} keeps a fist in his hair the whole time, using it to set how deep he goes on every stroke.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "hair_pulling"),
            timers = listOf(tm("Hand in his hair", 120))
        ),
        t(
            id = "l2_kiss_after_oral", level = 2, cats = setOf(ORAL, KISSING),
            title = "Back up to his mouth",
            base = "{G} [v_suck] {R} for two minutes, then comes all the way back up his body with [n_kiss_trail] and kisses him [adv_kiss_deep].",
            explicit = "{G} [v_suck] {R} for two minutes, then kisses all the way back up his body with [n_kiss_trail] and kisses him [adv_kiss_deep] so he tastes himself.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "tongue_kissing"),
            timers = listOf(tm("Oral", 120), tm("Back up his body", 45), tm("Kissing", 45))
        ),
        t(
            id = "l2_service_music", level = 2, cats = setOf(POWER, BODY_WORSHIP),
            title = "One track",
            base = "{G} puts on #music# and kneads {R+} whole body with his hands for the length of one track without speaking.",
            explicit = "{G} puts on #music#, kneads {R+} whole body with his hands for one full track and neither of them says a word the entire time.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("body_worship", "massage_general"),
            equip = setOf(Equipment.MUSIC),
            timers = listOf(tm("One track", 180))
        )
    )
}
