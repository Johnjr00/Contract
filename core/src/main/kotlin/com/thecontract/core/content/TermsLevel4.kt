package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.GIVER
import com.thecontract.core.model.BenefitParty.MUTUAL
import com.thecontract.core.model.BenefitParty.RECEIVER
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.BODY_WORSHIP
import com.thecontract.core.model.Category.BONDAGE
import com.thecontract.core.model.Category.IMPACT
import com.thecontract.core.model.Category.LANGUAGE
import com.thecontract.core.model.Category.ORAL
import com.thecontract.core.model.Category.ORGASM_CONTROL
import com.thecontract.core.model.Category.POWER
import com.thecontract.core.model.Category.RIMMING
import com.thecontract.core.model.Category.ROLEPLAY
import com.thecontract.core.model.Category.SENSORY
import com.thecontract.core.model.Category.TOYS
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.Term

/**
 * Act IV — Authority. Bondage, impact, service protocols, ownership and control.
 * 63 terms (specification floor for level 4: 40).
 */
internal object TermsLevel4 {

    val terms: List<Term> = listOf(

        // ------------------------------------------------------------- bondage (10)
        t(
            id = "l4_bond_blindfold_scene", level = 4, cats = setOf(BONDAGE, SENSORY),
            title = "Blindfolded and used",
            base = "{DOM} puts #blindfold# on {SUB} and strokes him with hands and mouth for the full time without saying what comes next.",
            explicit = "{DOM} puts #blindfold# on {SUB} and uses his hands and mouth on him, telling him nothing and switching between the two every thirty seconds.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("blindfold", "commands"),
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Hands", 90), tm("Mouth", 90))
        ),
        t(
            id = "l4_bond_cuffs_wrists", level = 4, cats = setOf(BONDAGE),
            title = "Cuffed",
            base = "{DOM} puts #cuffs# on {SUB+} wrists in front of him and keeps him there while he uses his hands on him.",
            explicit = "{DOM} locks #cuffs# on {SUB+} wrists, keeps his arms pinned above his head and uses his hands on him until the timer ends.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint"),
            equip = setOf(Equipment.CUFFS),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Cuffed and stroked", 180))
        ),
        t(
            id = "l4_bond_cuffs_furniture", level = 4, cats = setOf(BONDAGE),
            title = "Held to the furniture",
            base = "{DOM} secures {SUB+} wrists with #cuffs# to #sturdy_chair# and stays with him for the whole term.",
            explicit = "{DOM} fastens {SUB+} wrists with #cuffs# to #sturdy_chair#, stays with him the whole time, and uses his hands on his chest, stomach and thighs before he touches his cock at all.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint"),
            equip = setOf(Equipment.CUFFS, Equipment.STURDY_CHAIR),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Secured and teased", 120), tm("Stroked hard", 120))
        ),
        t(
            id = "l4_bond_rope_wrists", level = 4, cats = setOf(BONDAGE),
            title = "Rope on his wrists",
            base = "{DOM} binds {SUB+} wrists with #rope#, tight enough that he cannot pull them free, before he starts on him.",
            explicit = "{DOM} binds {SUB+} wrists with #rope# tight enough that he cannot pull them free, then uses his hands on him until the timer ends.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint"),
            equip = setOf(Equipment.ROPE),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Tying him", 60), tm("Stroking him", 180))
        ),
        t(
            id = "l4_bond_spreader", level = 4, cats = setOf(BONDAGE, ANAL),
            title = "Held open",
            base = "{DOM} puts {SUB} on #spreader_bar# so his legs stay apart, then strokes him with his hands and mouth.",
            explicit = "{DOM} fixes {SUB} onto #spreader_bar# so his legs cannot close, then works over his thighs, his balls and his cock in turn with hands and mouth, a full minute on each.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "anal_external"),
            equip = setOf(Equipment.SPREADER_BAR),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Set up", 45), tm("Opened up", 180))
        ),
        t(
            id = "l4_bond_collar", level = 4, cats = setOf(BONDAGE, POWER),
            title = "Collared",
            base = "{DOM} puts #collar# on {SUB}, says out loud what wearing it means for the rest of the night, and it stays on for the rest of the scene.",
            explicit = "{DOM} buckles #collar# onto {SUB}, says out loud what wearing it means for the rest of the night, and it does not come off again.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("collaring", "ownership_language"),
            equip = setOf(Equipment.COLLAR),
            timers = listOf(tm("Collaring him", 60)),
            says = listOf(
                "While this is on, you do what I say.",
                "This stays on until I take it off.",
                "You are mine for as long as you are wearing this.",
                "Once this is buckled, you do not decide anything tonight.",
                "This is how the rest of the night works."
            ),
            saysExplicit = listOf(
                "While this is on your neck, you do exactly what I say.",
                "This stays on until I take it off, and not a second before.",
                "You are mine for as long as you are wearing this.",
                "Once this is buckled you do not decide a fucking thing tonight.",
                "This is how the rest of the night goes."
            )
        ),
        t(
            id = "l4_bond_leash", level = 4, cats = setOf(BONDAGE, POWER),
            title = "Collar and leash",
            base = "{DOM} clips #leash# to #collar# and uses it to move {SUB} into each position he wants.",
            explicit = "{DOM} clips #leash# onto #collar# and moves {SUB} around by it into every position he wants him in.",
            benefit = RECEIVER, type = BenefitType.HANDLING_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("collaring", "commands", "ownership_language"),
            equip = setOf(Equipment.COLLAR, Equipment.LEASH),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Position 1", 60), tm("Position 2", 60), tm("Position 3", 60)),
            positions = listOf(
                "Down onto his knees at his feet.",
                "On all fours at his side.",
                "Bent over the edge of the bed.",
                "Kneeling upright with his chest out and his hands behind him.",
                "Face down with his hips pulled up."
            )
        ),
        t(
            id = "l4_bond_restrained_oral", level = 4, cats = setOf(BONDAGE, ORAL),
            title = "Tied and sucked",
            base = "{SUB} is restrained with #cuffs# and {DOM} [v_suck] him, ninety seconds slow then two minutes hard, ignoring what {SUB} asks for.",
            explicit = "{SUB} is locked in #cuffs# and {DOM} [v_suck] him, ninety seconds slow then two minutes hard, and ignores every single thing {SUB} asks for.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "oral"),
            equip = setOf(Equipment.CUFFS),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Slow", 90), tm("Hard", 120))
        ),
        t(
            id = "l4_bond_restrained_toy", level = 4, cats = setOf(BONDAGE, TOYS),
            title = "Tied and fucked with a toy",
            base = "{SUB} is restrained with #rope# and {DOM} uses #TOY# on him for the whole term.",
            explicit = "{SUB} is tied down with #rope# and {DOM} runs #TOY# on him for the entire term, moving it to a different part of his body every thirty seconds.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint"), toy = true,
            equip = setOf(Equipment.ROPE),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Tied", 45), tm("Toy on him", 180))
        ),
        t(
            id = "l4_bond_restrained_rim", level = 4, cats = setOf(BONDAGE, RIMMING, ANAL),
            title = "Tied and eaten out",
            base = "{SUB} is restrained face down with #cuffs# and {DOM} [v_rim] him for the full time.",
            explicit = "{SUB} is cuffed face down and {DOM} spreads him and [v_rim] him for the whole term while he cannot move an inch.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "rimming"),
            equip = setOf(Equipment.CUFFS),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Tied and eaten out", 180))
        ),

        // -------------------------------------------------------------- impact (10)
        t(
            id = "l4_impact_hand_counted", level = 4, cats = setOf(IMPACT),
            title = "Counted by hand",
            base = "{DOM} [v_spank] {SUB} ten times with an open hand and {SUB} counts each one out loud.",
            explicit = "{DOM} [v_spank] {SUB+} ass ten times with an open hand and {SUB} counts every single one out loud, or it starts again.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("spanking", "commands"),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Ten counted", 90))
        ),
        t(
            id = "l4_impact_over_knee", level = 4, cats = setOf(IMPACT, POWER),
            title = "Over his knee",
            base = "{DOM} puts {SUB} over his knee and [v_spank] him, starting light and building, and holds him in place throughout.",
            explicit = "{DOM} drags {SUB} over his knee, holds him down and [v_spank] him, starting light and building over the full timer.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("spanking"),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Warm-up", 60), tm("Building", 60), tm("Full strength", 45))
        ),
        t(
            id = "l4_impact_paddle", level = 4, cats = setOf(IMPACT),
            title = "Paddle",
            base = "{DOM} warms {SUB} up by hand first, then uses #paddle# on him in sets of five with a pause between each set.",
            explicit = "{DOM} warms {SUB+} ass up by hand, then swings #paddle# across it in sets of five, pausing a full ten seconds between sets.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys", "spanking"),
            equip = setOf(Equipment.PADDLE),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Warm-up by hand", 60), tm("Set 1", 30), tm("Set 2", 30), tm("Set 3", 30))
        ),
        t(
            id = "l4_impact_flogger", level = 4, cats = setOf(IMPACT),
            title = "Flogger",
            base = "{DOM} swings #flogger# across {SUB+} back, ass and thighs in a steady rhythm, building over three rounds.",
            explicit = "{DOM} swings #flogger# across {SUB+} back, ass and thighs in a steady, building rhythm over three rounds and does not pause between rounds.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys"),
            equip = setOf(Equipment.FLOGGER),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Round 1 light", 60), tm("Round 2 medium", 60), tm("Round 3 hard", 45))
        ),
        t(
            id = "l4_impact_crop", level = 4, cats = setOf(IMPACT),
            title = "Crop",
            base = "{DOM} uses #crop# on {SUB+} thighs and ass, one stroke at a time, and names the next spot before each one.",
            explicit = "{DOM} runs #crop# over {SUB+} thighs and ass one stroke at a time, naming exactly where the next one is landing before it does.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys"),
            equip = setOf(Equipment.CROP),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Thighs", 60), tm("Ass", 60))
        ),
        t(
            id = "l4_impact_warmup_sequence", level = 4, cats = setOf(IMPACT),
            title = "Full warm-up",
            base = "{DOM} takes {SUB} through a full warm-up: open hand, then #paddle#, then #flogger#, one minute each.",
            explicit = "{DOM} takes {SUB} through the whole set — open hand, then #paddle#, then #flogger# — a minute each and harder every time.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("spanking", "impact_toys"),
            equip = setOf(Equipment.PADDLE, Equipment.FLOGGER),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Open hand", 60), tm("Paddle", 60), tm("Flogger", 60))
        ),
        t(
            id = "l4_impact_thighs", level = 4, cats = setOf(IMPACT),
            title = "Inner thighs",
            base = "{DOM} [v_spank] the inside of each of {SUB+} thighs by hand, alternating sides, thirty seconds each.",
            explicit = "{DOM} [v_spank] the inside of each of {SUB+} thighs by hand, swapping sides every thirty seconds and keeping him open the whole time.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("spanking"),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Left thigh", 30), tm("Right thigh", 30), tm("Left thigh again", 30), tm("Right thigh again", 30))
        ),
        t(
            id = "l4_impact_during_oral", level = 4, cats = setOf(IMPACT, ORAL),
            title = "Spanked while he sucks",
            base = "{SUB} kneels and [v_suck] {DOM}, and {DOM} [v_spank] him once every thirty seconds throughout.",
            explicit = "{SUB} stays on his knees and [v_suck] {DOM}, and {DOM} [v_spank] him once every thirty seconds without breaking his rhythm.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("oral", "kneeling"), gPrefs = setOf("spanking_receive"), rPrefs = setOf("spanking_give"),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("On his knees", 150))
        ),
        t(
            id = "l4_impact_clamps_and_hand", level = 4, cats = setOf(IMPACT, TOYS),
            title = "Clamps and hands",
            base = "{DOM} puts #nipple_clamps# on {SUB} and [v_spank] him while they stay on.",
            explicit = "{DOM} puts #nipple_clamps# on {SUB}, leaves them on and [v_spank] him through the whole thing.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("nipple_clamps", "spanking"),
            equip = setOf(Equipment.NIPPLE_CLAMPS),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Clamps on", 45), tm("Spanking", 90))
        ),
        t(
            id = "l4_impact_blindfolded", level = 4, cats = setOf(IMPACT, SENSORY, BONDAGE),
            title = "He does not see it coming",
            base = "{SUB} wears #blindfold# and {DOM} [v_spank] him by hand, leaving anything from two to ten seconds between strokes and never the same gap twice in a row.",
            explicit = "{SUB} wears #blindfold# and {DOM} [v_spank] him by hand, leaving anything from two to ten seconds between strokes and never the same gap twice in a row.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("blindfold", "spanking"),
            equip = setOf(Equipment.BLINDFOLD),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Blindfolded spanking", 120))
        ),

        // ---------------------------------------------------- service protocols (8)
        t(
            id = "l4_service_kneel_and_wait", level = 4, cats = setOf(POWER),
            title = "Kneel and wait",
            base = "{SUB} kneels with his hands behind his back and waits, without speaking, until {DOM} tells him to get up.",
            explicit = "{SUB} kneels with his hands locked behind his back and waits in silence until {DOM} tells him to get up.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("kneeling", "sexual_service", "commands"),
            timers = listOf(tm("Waiting", 120))
        ),
        t(
            id = "l4_service_position_on_command", level = 4, cats = setOf(POWER),
            title = "Positions on command",
            base = "{DOM} names a position and {SUB} is in it within five seconds. Four positions, one minute each.",
            explicit = "{DOM} names a position and {SUB} is in it inside five seconds. Four positions, a minute each, and he does not answer back.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"),
            timers = listOf(tm("Position 1", 60), tm("Position 2", 60), tm("Position 3", 60), tm("Position 4", 60)),
            positions = listOf(
                "Kneeling upright, hands behind his back, eyes down.",
                "On all fours with his forehead on the bed.",
                "On his back, knees to his chest, holding them there himself.",
                "Bent over the edge of the bed with his legs apart.",
                "Standing with both hands flat on the wall and his feet back."
            )
        ),
        t(
            id = "l4_service_hands_behind", level = 4, cats = setOf(POWER),
            title = "Hands behind his back",
            base = "{SUB} keeps his hands behind his back for the whole term while {DOM} uses his hands and mouth on his chest, stomach, thighs and cock.",
            explicit = "{SUB} keeps his hands behind his back for the entire term and does not move them once, whatever {DOM} does to him.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "sexual_service"),
            timers = listOf(tm("Hands behind his back", 180))
        ),
        t(
            id = "l4_service_present", level = 4, cats = setOf(POWER, ANAL),
            title = "Presenting",
            base = "{SUB} presents himself face down with his knees apart on command and holds it while {DOM} inspects and touches him.",
            explicit = "{SUB} gets face down with his knees apart the moment he is told, and holds it while {DOM} looks him over and puts his hands on him.",
            benefit = RECEIVER, type = BenefitType.HANDLING_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "anal_external"),
            timers = listOf(tm("Presenting", 120))
        ),
        t(
            id = "l4_service_ask_to_speak", level = 4, cats = setOf(POWER, LANGUAGE),
            title = "Silence unless asked",
            base = "{DOM} asks {SUB} a direct question five times during this term, and {SUB} says nothing at all except to answer the one he was asked.",
            explicit = "{DOM} asks {SUB} a direct question five times, and {SUB} stays silent throughout except to answer the one he was asked, in as few words as it takes.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "permission_control"),
            timers = listOf(tm("Silence", 180)),
            says = listOf(
                "Do you want me to keep going?",
                "Where do you want my hands?",
                "How does that feel?",
                "Do you want it harder or softer?",
                "Are you still comfortable?"
            ),
            saysExplicit = listOf(
                "Do you want more of that?",
                "Where do you want my hands next?",
                "How does that feel?",
                "Harder, or slower?",
                "Are you still good?"
            )
        ),
        t(
            id = "l4_service_worship_on_command", level = 4, cats = setOf(POWER, BODY_WORSHIP),
            title = "Worship on command",
            base = "{DOM} names a part of his body and {SUB} puts his mouth on it for a slow count of twenty, then {DOM} names the next.",
            explicit = "{DOM} names a part of himself and {SUB} puts his mouth on it and stays there for a slow count of twenty, then {DOM} names the next.",
            benefit = GIVER, type = BenefitType.SERVICE_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "body_worship", "sexual_service"),
            timers = listOf(tm("First part", 60), tm("Second part", 60), tm("Third part", 60))
        ),
        t(
            id = "l4_service_kneel_and_suck_on_command", level = 4, cats = setOf(POWER, ORAL),
            title = "On his knees on command",
            base = "{DOM} tells {SUB} to kneel and [v_suck] him, and {SUB} stops and starts exactly when he is told.",
            explicit = "{DOM} puts {SUB} on his knees and {SUB} [v_suck] him, stopping and starting exactly on command and not a second before or after.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("oral", "kneeling"), gPrefs = setOf("commands_receive"), rPrefs = setOf("commands_give"),
            timers = listOf(tm("On", 45), tm("Off", 20), tm("On", 45), tm("Off", 20), tm("On", 60))
        ),
        t(
            id = "l4_service_roleplay_authority", level = 4, cats = setOf(POWER, ROLEPLAY),
            title = "Playing it straight",
            base = "{DOM} gives {SUB} an order every thirty seconds and {SUB} carries each one out without speaking, except to answer him. Neither of them breaks character until the timer ends.",
            explicit = "{DOM} gives {SUB} an order every thirty seconds and {SUB} does each one without a word, except to answer him. Neither of them drops character for a second until the timer ends.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("roleplay_lead", "commands"),
            gPrefs = setOf("authority_roleplay"), rPrefs = setOf("authority_roleplay"),
            blocks = setOf(Boundary.NO_ROLEPLAY),
            timers = listOf(tm("In character", 180))
        ),

        // ------------------------------------------------------------ ownership (6)
        t(
            id = "l4_own_declaration", level = 4, cats = setOf(POWER, LANGUAGE),
            title = "Saying it out loud",
            base = "{SUB} says out loud who he belongs to tonight, and {DOM} [v_own] in return.",
            explicit = "{SUB} says out loud exactly who he belongs to tonight, and {DOM} [v_own] straight back at him.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("ownership_language"),
            timers = listOf(tm("Saying it", 60)),
            says = listOf(
                "I belong to you tonight.",
                "I am yours for the rest of the night.",
                "Tonight I am yours.",
                "You have me until the morning.",
                "I am yours, and I will do what you say."
            ),
            saysExplicit = listOf(
                "I belong to you tonight.",
                "I am yours, all fucking night.",
                "Tonight I am yours to use.",
                "You own me until the morning.",
                "I am yours and I will do whatever you tell me."
            )
        ),
        t(
            id = "l4_own_marking_kisses", level = 4, cats = setOf(POWER, BODY_WORSHIP),
            title = "Claimed",
            base = "{DOM} kisses his way over {SUB+} neck, chest and hips, naming each place as his as he goes.",
            explicit = "{DOM} kisses his way over {SUB+} neck, chest and hips and names every single place as his while he does it.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("ownership_language", "neck_kissing", "chest_nipple_kissing"),
            timers = listOf(tm("Neck", 45), tm("Chest", 45), tm("Hips", 45))
        ),
        t(
            id = "l4_own_degradation", level = 4, cats = setOf(LANGUAGE, POWER),
            title = "Talked down to",
            base = "{DOM} talks {SUB} down the whole time he strokes his cock with his hand, and does not go quiet once.",
            explicit = "{DOM} talks {SUB} down the entire time he strokes his cock with his hand, and does not stop talking at any point.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("degradation", "dirty_talk"),
            blocks = setOf(Boundary.NO_DEGRADATION),
            timers = listOf(tm("Talking and stroking him", 150))
        ),
        t(
            id = "l4_own_praise_protocol", level = 4, cats = setOf(LANGUAGE, POWER),
            title = "Earned praise",
            base = "{DOM} [v_praise] only when {SUB} does exactly what he was told, and says nothing at all otherwise.",
            explicit = "{DOM} [v_praise] only when {SUB} gets it exactly right, and stays completely silent every other time.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("explicit_praise", "commands"),
            timers = listOf(tm("First task", 60), tm("Second task", 60), tm("Third task", 60)),
            says = listOf(
                "That is exactly what I asked for.",
                "Good.",
                "Right the first time.",
                "That is what I wanted.",
                "Perfect."
            ),
            saysExplicit = listOf(
                "That is exactly what I fucking asked for.",
                "Good boy.",
                "Right the first time.",
                "That is what I wanted from you.",
                "Perfect."
            )
        ),
        t(
            id = "l4_own_collar_service", level = 4, cats = setOf(POWER, BONDAGE),
            title = "Collared and serving",
            base = "{SUB} wears #collar# and serves {DOM} with his mouth for the whole term, moving from his cock to his balls and back on command.",
            explicit = "{SUB} wears #collar# and serves {DOM} with his mouth for the whole term, moving from his cock to his balls and back on command, without being asked twice.",
            benefit = GIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("collaring", "sexual_service", "ownership_language"),
            equip = setOf(Equipment.COLLAR),
            timers = listOf(tm("Serving", 180))
        ),
        t(
            id = "l4_own_rules_for_the_night", level = 4, cats = setOf(POWER),
            title = "Rules for the night",
            base = "{DOM} sets three rules for the rest of the scene and {SUB} repeats them back before they take effect.",
            explicit = "{DOM} lays down three rules for the rest of the night and {SUB} repeats every one of them back before they start.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "permission_control"),
            timers = listOf(tm("Setting the rules", 60), tm("Repeating them back", 45))
        ),

        // -------------------------------------------------------------- control (6)
        t(
            id = "l4_control_permission_everything", level = 4, cats = setOf(POWER, ORGASM_CONTROL),
            title = "Permission for everything",
            base = "{SUB} asks permission before touching {DOM}, before moving, and before finishing, for the rest of the scene.",
            explicit = "{SUB} asks before he touches {DOM}, before he moves and before he comes, for the rest of the night, every time.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("permission_control", "climax_permission"),
            timers = listOf(tm("In force", 60))
        ),
        t(
            id = "l4_control_edge_restrained", level = 4, cats = setOf(ORGASM_CONTROL, BONDAGE),
            title = "Edged while tied",
            base = "{SUB} is restrained with #cuffs# and {DOM} [v_edge] three times, taking both hands off him completely each time.",
            explicit = "{SUB} is locked in #cuffs# and {DOM} [v_edge] three separate times, stopping dead each time and taking both hands off him.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "edging"),
            equip = setOf(Equipment.CUFFS),
            erection = PartyRef.RECEIVER,
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Edge 1", 90), tm("Edge 2", 90), tm("Edge 3", 90))
        ),
        t(
            id = "l4_control_denial_protocol", level = 4, cats = setOf(ORGASM_CONTROL),
            title = "Denied and kept there",
            base = "{DOM} brings {SUB} close four times and does not let him finish at any point during this term.",
            explicit = "{DOM} takes {SUB} to the edge four times and does not let him come once during the whole term.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("denial", "edging"),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Edge 1", 60), tm("Edge 2", 60), tm("Edge 3", 60), tm("Edge 4", 60))
        ),
        t(
            id = "l4_control_blindfold_commands", level = 4, cats = setOf(POWER, SENSORY),
            title = "Blind and following",
            base = "{SUB} wears #blindfold# and follows every instruction {DOM} gives without being able to see what is coming.",
            explicit = "{SUB} wears #blindfold# and does exactly what {DOM} tells him, with no idea what is coming next, for the whole term.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("blindfold", "commands"),
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Following blind", 180))
        ),
        t(
            id = "l4_control_toy_pace", level = 4, cats = setOf(POWER, TOYS),
            title = "Three settings, one minute each",
            base = "{DOM} uses #TOY# on {SUB} through all three settings, one minute on each, lowest to highest. {SUB} may ask for a change, but the order does not alter.",
            explicit = "{DOM} runs #TOY# on {SUB} through all three settings, one minute on each, lowest to highest. {SUB} can ask all he likes; the order does not alter.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"), toy = true,
            timers = listOf(tm("Setting 1", 60), tm("Setting 2", 60), tm("Setting 3", 60))
        ),
        t(
            id = "l4_control_capture_scene", level = 4, cats = setOf(ROLEPLAY, BONDAGE),
            title = "Caught",
            base = "{DOM} ties {SUB} down with #rope#, tells him he is not going anywhere, and uses his hands and mouth on him while he stays tied.",
            explicit = "{DOM} ties {SUB} down with #rope#, tells him he is not going anywhere, and uses his hands and mouth on his chest, stomach and thighs while he stays tied, until the timer ends.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "roleplay_lead"),
            gPrefs = setOf("capture_roleplay"), rPrefs = setOf("capture_roleplay"),
            equip = setOf(Equipment.ROPE),
            blocks = setOf(Boundary.NO_ROLEPLAY, Boundary.NO_RESTRAINTS),
            timers = listOf(tm("The capture", 60), tm("Tied and stroked", 180))
        ),

        // ---------------------------------------------------------------- added (8)
        t(
            id = "l4_bond_hands_and_oral", level = 4, cats = setOf(BONDAGE, ORAL),
            title = "Tied and used",
            base = "{G} binds {R+} hands, then [v_suck] him until the timer ends, keeping him where he is with a hand on his hip.",
            explicit = "{G} ties {R+} hands, then [v_suck] his cock and holds his hip down so he stays where he is put.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("restraint", "oral"),
            equip = setOf(Equipment.CUFFS),
            timers = listOf(tm("Tied", 150))
        ),
        t(
            id = "l4_impact_counted_and_soothed", level = 4, cats = setOf(IMPACT),
            title = "Struck and soothed",
            base = "{G} [v_spank] {R} ten times, then rubs the same ground with an open hand for a slow count of twenty, and repeats that three times over.",
            explicit = "{G} [v_spank] {R} ten times, then works the heat out of the same ground with an open palm, three rounds of it.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            acts = setOf("spanking"),
            timers = listOf(tm("First round", 60), tm("Second round", 60), tm("Third round", 60))
        ),
        t(
            id = "l4_power_blindfold_and_wait", level = 4, cats = setOf(POWER, SENSORY),
            title = "Blind and waiting",
            base = "{G} blindfolds {R} and touches him only now and then, leaving long gaps so {R} never knows when the next one is coming.",
            explicit = "{G} blindfolds {R} and touches him at random, leaving long gaps between, so he never knows where or when the next one lands.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("blindfold", "sexual_service"),
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Waiting", 180))
        ),
        t(
            id = "l4_orgasm_permission_only", level = 4, cats = setOf(ORGASM_CONTROL, POWER),
            title = "Only when told",
            base = "{G} works {R} to the edge and back four times, stopping the moment {R} says he is close, and {R} does not come.",
            explicit = "{G} takes {R} to the edge and back four times, hand off him the second he says he is close, and {R} does not come.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("climax_permission", "edging", "permission_control"),
            timers = listOf(tm("At his say-so", 240))
        ),
        t(
            id = "l4_rim_hard_and_held", level = 4, cats = setOf(RIMMING, POWER),
            title = "Held open",
            base = "{G} holds {R} open with both hands and [v_rim_hard] him, and {R} keeps his position until the timer ends.",
            explicit = "{G} holds {R} open with both hands and [v_rim_hard] him, and {R} holds the position for the full timer.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("rimming", "commands"),
            timers = listOf(tm("Held open", 150))
        ),
        t(
            id = "l4_toy_two_at_once", level = 4, cats = setOf(TOYS),
            title = "Two at once",
            base = "{G} works a toy into {R} and holds a vibrator against him at the same time, keeping both going until the timer ends.",
            explicit = "{G} [v_use_toy] a toy in {R} and holds a vibrator on his cock at the same time, both going for the full timer.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("anal_toys", "vibration"), anal = true,
            equip = setOf(Equipment.VIBRATOR, Equipment.LUBRICANT), toy = true,
            timers = listOf(tm("Both", 180))
        ),
        t(
            id = "l4_lang_told_what_he_is", level = 4, cats = setOf(LANGUAGE, POWER),
            title = "Told what he is",
            base = "{G} [v_talk_dirty] to {R} the whole time he works him, and {R} answers every time he is asked something.",
            explicit = "{G} [v_talk_dirty] to {R} throughout and makes him answer, out loud, every time he is asked something.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("dirty_talk", "sexual_service"),
            timers = listOf(tm("Talking", 150))
        ),

        // ---------------------------------------------------------------- added (16)
        t(
            id = "l4_bond_cuffed_and_mouthed", level = 4, cats = setOf(BONDAGE, ORAL),
            title = "Cuffed and stopped short",
            base = "{DOM} locks #cuffs# on {SUB+} wrists above his head, then [v_suck] {SUB+} cock and comes off him for a slow count of twenty every time {SUB} says he is close. {SUB} does not come during this term.",
            explicit = "{DOM} locks #cuffs# on {SUB+} wrists above his head, then [v_suck] {SUB+} cock and pulls his mouth off for a slow count of twenty every time {SUB} says he is close. {SUB} does not come at any point in this term.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "oral", "edging", "denial"),
            equip = setOf(Equipment.CUFFS),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Cuffed", 45), tm("Mouth on him", 180))
        ),
        t(
            id = "l4_bond_rope_ankles", level = 4, cats = setOf(BONDAGE),
            title = "Ankles apart",
            base = "{DOM} binds {SUB+} ankles apart with #rope# so his legs cannot close, then works the inside of his thighs, his balls and his cock with both hands, a full minute on each, and stays beside him throughout.",
            explicit = "{DOM} ties {SUB+} ankles apart with #rope# so his legs cannot close, then works the inside of his thighs, his balls and his cock with both hands, a minute on each, and does not leave his side.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint"),
            equip = setOf(Equipment.ROPE),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Tying him", 45), tm("Thighs", 60), tm("Balls", 60), tm("Cock", 60))
        ),
        t(
            id = "l4_bond_blindfold_named_toy", level = 4, cats = setOf(BONDAGE, SENSORY, TOYS),
            title = "Told where it is going",
            base = "{SUB} wears #blindfold#. {DOM} names out loud where #TOY# is going next, waits a slow count of five and puts it there, working through {SUB+} nipples, his stomach, the inside of each thigh and his cock in that order.",
            explicit = "{SUB} wears #blindfold#. {DOM} says out loud where #TOY# is going next, waits a slow count of five and puts it there, working through {SUB+} nipples, his stomach, the inside of each thigh and his cock in that order.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("blindfold", "commands"), toy = true,
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Nipples", 45), tm("Stomach", 45), tm("Thighs", 45), tm("Cock", 45))
        ),
        t(
            id = "l4_bond_collar_and_kneel", level = 4, cats = setOf(BONDAGE, POWER),
            title = "Collared on his knees",
            base = "{DOM} buckles #collar# onto {SUB} and puts him on his knees. {SUB} holds that position with his hands behind his back while {DOM} works {SUB+} shoulders, his hair and the back of his neck with both hands.",
            explicit = "{DOM} buckles #collar# onto {SUB} and puts him on his knees. {SUB} holds that position with his hands locked behind his back while {DOM} works {SUB+} shoulders, his hair and the back of his neck with both hands until the timer ends.",
            benefit = RECEIVER, type = BenefitType.HANDLING_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("collaring", "kneeling", "hair_pulling"),
            equip = setOf(Equipment.COLLAR),
            timers = listOf(tm("Collared", 45), tm("Kneeling", 150))
        ),
        t(
            id = "l4_impact_twenty_by_hand", level = 4, cats = setOf(IMPACT),
            title = "Twenty, five at a time",
            base = "{DOM} [v_spank] {SUB} twenty times with an open hand in sets of five, alternating cheeks, and rubs the same ground with an open palm for a slow count of ten between sets.",
            explicit = "{DOM} [v_spank] {SUB+} ass twenty times with an open hand in sets of five, alternating cheeks, and works the heat into the same ground with an open palm for a slow count of ten between sets.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("spanking"),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Set 1", 45), tm("Set 2", 45), tm("Set 3", 45), tm("Set 4", 45))
        ),
        t(
            id = "l4_impact_paddle_counted_ten", level = 4, cats = setOf(IMPACT),
            title = "Ten with the paddle",
            base = "{DOM} warms {SUB} up with an open hand for a full minute, then lands ten strokes with #paddle#, and {SUB} counts each one out loud before the next lands.",
            explicit = "{DOM} warms {SUB+} ass up with an open hand for a full minute, then lands ten strokes with #paddle#, and {SUB} counts every one out loud before the next one lands.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys", "spanking", "commands"),
            equip = setOf(Equipment.PADDLE),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Warm-up by hand", 60), tm("Ten counted", 90))
        ),
        t(
            id = "l4_impact_crop_named_spots", level = 4, cats = setOf(IMPACT),
            title = "Named before it lands",
            base = "{DOM} names the exact spot on {SUB} out loud, waits a slow count of three, then lands one stroke of #crop# there, and works through both thighs and both cheeks in turn.",
            explicit = "{DOM} names the exact spot on {SUB} out loud, waits a slow count of three, then lands one stroke of #crop# on it, and works through both thighs and both cheeks in turn.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys"),
            equip = setOf(Equipment.CROP),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Left thigh", 45), tm("Right thigh", 45), tm("Left cheek", 45), tm("Right cheek", 45))
        ),
        t(
            id = "l4_impact_light_then_hard", level = 4, cats = setOf(IMPACT),
            title = "Light, then half, then full",
            base = "{DOM} [v_spank] {SUB} lightly for the first timer, at half strength for the second and at full strength for the third, and changes nothing before a timer ends.",
            explicit = "{DOM} [v_spank] {SUB+} ass lightly for the first timer, at half strength for the second and at full strength for the third, and changes nothing before a timer ends.",
            benefit = RECEIVER, type = BenefitType.IMPACT_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("spanking"),
            blocks = setOf(Boundary.NO_PAIN),
            timers = listOf(tm("Light", 60), tm("Half strength", 60), tm("Full strength", 45))
        ),
        t(
            id = "l4_service_five_positions_held", level = 4, cats = setOf(POWER),
            title = "Five positions, held",
            base = "{DOM} names a position out loud and {SUB} is in it inside five seconds and holds it without moving for thirty seconds, then {DOM} names the next. Five positions in all.",
            explicit = "{DOM} names a position out loud and {SUB} is in it inside five seconds and holds it dead still for thirty seconds before {DOM} names the next. Five positions in all.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"),
            timers = listOf(
                tm("Position 1", 30), tm("Position 2", 30), tm("Position 3", 30),
                tm("Position 4", 30), tm("Position 5", 30)
            ),
            positions = listOf(
                "Kneeling upright with both hands behind his back.",
                "On all fours with his elbows down and his head low.",
                "Flat on his back with his knees held to his chest.",
                "Standing with his feet apart and his hands on his head.",
                "Bent over the edge of the bed with his legs open."
            )
        ),
        t(
            id = "l4_service_kneel_and_present", level = 4, cats = setOf(POWER, ANAL),
            title = "Down and open",
            base = "{DOM} tells {SUB} to go down onto his elbows with his knees apart, and {SUB} holds that while {DOM} runs both hands over his back, his ass and the inside of his thighs and rubs his hole from the outside with a thumb slicked in #lubricant#.",
            explicit = "{DOM} puts {SUB} down onto his elbows with his knees apart and {SUB} holds it while {DOM} runs both hands over his back, his ass and the inside of his thighs and rubs his hole from the outside with a thumb slicked in #lubricant#.",
            benefit = RECEIVER, type = BenefitType.HANDLING_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "anal_external"),
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Presenting", 60), tm("Hands on him", 120))
        ),
        t(
            id = "l4_service_answers_out_loud", level = 4, cats = setOf(POWER, LANGUAGE),
            title = "Answers, out loud",
            base = "{DOM} works {SUB+} chest, stomach and thighs with both hands and puts a direct question to him every thirty seconds, and {SUB} answers out loud in a full sentence every time.",
            explicit = "{DOM} works {SUB+} chest, stomach and thighs with both hands and puts a direct question to him every thirty seconds, and {SUB} answers out loud in a full sentence every single time.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "permission_control"),
            timers = listOf(tm("Questions and hands", 180)),
            says = listOf(
                "Do you want me to carry on?",
                "Where do you want my hands next?",
                "How close are you right now?",
                "Is that too much, or not enough?",
                "Do you want it slower?"
            ),
            saysExplicit = listOf(
                "Do you want more of that?",
                "Where do you want my hands next?",
                "How close are you right now?",
                "Is that too much, or not enough?",
                "Do you want it slower, or harder?"
            )
        ),
        t(
            id = "l4_own_named_as_his", level = 4, cats = setOf(POWER, LANGUAGE),
            title = "Named as his",
            base = "{DOM} puts a hand on five parts of {SUB} in turn — the back of his neck, his chest, his stomach, his ass, his cock — and says out loud that each one is his before he moves to the next.",
            explicit = "{DOM} puts a hand on five parts of {SUB} in turn — the back of his neck, his chest, his stomach, his ass, his cock — and says out loud that every one of them is his before he moves on.",
            benefit = MUTUAL, type = BenefitType.MUTUAL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("ownership_language"),
            timers = listOf(tm("Five parts", 150)),
            says = listOf(
                "This is mine tonight.",
                "So is this.",
                "All of this belongs to me until the morning.",
                "Every part of you I put a hand on is mine.",
                "This one most of all."
            ),
            saysExplicit = listOf(
                "This is mine tonight.",
                "So is this, and you know it.",
                "All of this belongs to me until the morning.",
                "Every part of you I put a hand on is fucking mine.",
                "This one most of all."
            )
        ),
        t(
            id = "l4_own_inspection", level = 4, cats = setOf(POWER, BODY_WORSHIP),
            title = "Looked over",
            base = "{DOM} stands {SUB} up with his hands behind his head, then moves him into three positions by the shoulder and the hip, holds each for a slow count of twenty and runs both hands over him from shoulders to knees in every one.",
            explicit = "{DOM} stands {SUB} up with his hands behind his head, then puts him into three positions by the shoulder and the hip, holds each for a slow count of twenty and runs both hands over him from shoulders to knees in every one.",
            benefit = RECEIVER, type = BenefitType.HANDLING_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "ownership_language"),
            timers = listOf(tm("Position 1", 60), tm("Position 2", 60), tm("Position 3", 60)),
            positions = listOf(
                "Standing straight with his hands laced behind his head.",
                "Turned side on with one shoulder pulled back.",
                "Bent forward at the waist with both hands on the bed.",
                "Kneeling upright with his chest out.",
                "Facing away with his forehead against the wall."
            )
        ),
        t(
            id = "l4_control_mouth_on_command", level = 4, cats = setOf(POWER, ORAL),
            title = "On and off, on his word",
            base = "{SUB} kneels and [v_suck] {DOM+} cock, stops dead the moment {DOM} says stop and starts again the moment he says go. {DOM} calls it six times across the term.",
            explicit = "{SUB} kneels and [v_suck] {DOM+} cock, stops dead the second {DOM} says stop and starts again the second he says go, and {DOM} calls it six times across the term.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("oral", "kneeling"),
            gPrefs = setOf("commands_receive"), rPrefs = setOf("commands_give"),
            timers = listOf(tm("On his word", 180))
        ),
        t(
            id = "l4_control_blind_edges", level = 4, cats = setOf(ORGASM_CONTROL, SENSORY),
            title = "Edged blind",
            base = "{SUB} wears #blindfold# and {DOM} [v_edge] three times, taking both hands off him for a slow count of thirty each time, and says nothing at all in between.",
            explicit = "{SUB} wears #blindfold# and {DOM} [v_edge] three separate times, both hands off him for a slow count of thirty each time, and says nothing to him at all in between.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("blindfold", "edging", "denial"),
            equip = setOf(Equipment.BLINDFOLD),
            erection = PartyRef.RECEIVER,
            timers = listOf(tm("Edge 1", 75), tm("Edge 2", 75), tm("Edge 3", 75))
        ),
        t(
            id = "l4_control_counted_denial", level = 4, cats = setOf(ORGASM_CONTROL, POWER),
            title = "Counted down and stopped",
            base = "{DOM} strokes {SUB} with a slick hand and counts out loud from twenty down to one. On one his hand comes off completely, and {SUB} does not come during this term.",
            explicit = "{DOM} strokes {SUB} with a slick hand and counts out loud from twenty down to one. On one his hand comes off him completely, and {SUB} does not come at any point in this term.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("edging", "denial", "commands"),
            timers = listOf(tm("Building up", 120), tm("The count", 60))
        )
    )
}
