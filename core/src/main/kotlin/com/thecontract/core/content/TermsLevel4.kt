package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.GIVER
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
 * 40 terms (specification floor for level 4: 40).
 */
internal object TermsLevel4 {

    val terms: List<Term> = listOf(

        // ------------------------------------------------------------- bondage (10)
        t(
            id = "l4_bond_blindfold_scene", level = 4, cats = setOf(BONDAGE, SENSORY),
            title = "Blindfolded and used",
            base = "{DOM} puts #blindfold# on {SUB} and strokes him with hands and mouth for the full time without saying what comes next.",
            explicit = "{DOM} puts #blindfold# on {SUB} and uses his hands and mouth on him, telling him nothing and switching between the two every thirty seconds.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("blindfold", "commands"),
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Hands", 90), tm("Mouth", 90))
        ),
        t(
            id = "l4_bond_cuffs_wrists", level = 4, cats = setOf(BONDAGE),
            title = "Cuffed",
            base = "{DOM} puts #cuffs# on {SUB+} wrists in front of him and keeps him there while he uses his hands on him.",
            explicit = "{DOM} locks #cuffs# on {SUB+} wrists, keeps his arms pinned above his head and uses his hands on him while he cannot do a thing about it.",
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
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
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
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
            explicit = "{DOM} binds {SUB+} wrists with #rope# tight enough that he cannot pull them free, then uses his hands on him while he can do nothing but take it.",
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
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
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "anal_external"),
            equip = setOf(Equipment.SPREADER_BAR),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Set up", 45), tm("Opened up", 180))
        ),
        t(
            id = "l4_bond_collar", level = 4, cats = setOf(BONDAGE, POWER),
            title = "Collared",
            base = "{DOM} puts #collar# on {SUB} and it stays on for the rest of the scene.",
            explicit = "{DOM} buckles #collar# onto {SUB}, says out loud what wearing it means for the rest of the night, and it does not come off again.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("collaring", "ownership_language"),
            equip = setOf(Equipment.COLLAR),
            timers = listOf(tm("Collaring him", 60))
        ),
        t(
            id = "l4_bond_leash", level = 4, cats = setOf(BONDAGE, POWER),
            title = "Collar and leash",
            base = "{DOM} clips #leash# to #collar# and uses it to move {SUB} into each position he wants.",
            explicit = "{DOM} clips #leash# onto #collar# and moves {SUB} around by it into every position he wants him in.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("collaring", "commands", "ownership_language"),
            equip = setOf(Equipment.COLLAR, Equipment.LEASH),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Position 1", 60), tm("Position 2", 60), tm("Position 3", 60))
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
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.SERVICE_RECIPIENT,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("kneeling", "sexual_service", "commands"),
            timers = listOf(tm("Waiting", 120))
        ),
        t(
            id = "l4_service_position_on_command", level = 4, cats = setOf(POWER),
            title = "Positions on command",
            base = "{DOM} names a position and {SUB} takes it within five seconds. Four positions, one minute each.",
            explicit = "{DOM} names a position and {SUB} is in it inside five seconds. Four positions, a minute each, no arguing.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"),
            timers = listOf(tm("Position 1", 60), tm("Position 2", 60), tm("Position 3", 60), tm("Position 4", 60))
        ),
        t(
            id = "l4_service_hands_behind", level = 4, cats = setOf(POWER),
            title = "Hands behind his back",
            base = "{SUB} keeps his hands behind his back for the whole term while {DOM} uses his hands and mouth on his chest, stomach, thighs and cock.",
            explicit = "{SUB} keeps his hands behind his back for the entire term and does not move them once, whatever {DOM} does to him.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "sexual_service"),
            timers = listOf(tm("Hands behind his back", 180))
        ),
        t(
            id = "l4_service_present", level = 4, cats = setOf(POWER, ANAL),
            title = "Presenting",
            base = "{SUB} presents himself face down with his knees apart on command and holds it while {DOM} inspects and touches him.",
            explicit = "{SUB} gets face down with his knees apart the moment he is told, and holds it while {DOM} looks him over and puts his hands on him.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "anal_external"),
            timers = listOf(tm("Presenting", 120))
        ),
        t(
            id = "l4_service_ask_to_speak", level = 4, cats = setOf(POWER, LANGUAGE),
            title = "Silence unless asked",
            base = "{SUB} does not speak during this term unless {DOM} asks him a direct question.",
            explicit = "{SUB} says nothing at all for this term unless {DOM} asks him a direct question, and then answers only the question he was asked.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands", "permission_control"),
            timers = listOf(tm("Silence", 180))
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.OWNERSHIP,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("ownership_language"),
            timers = listOf(tm("Saying it", 60))
        ),
        t(
            id = "l4_own_marking_kisses", level = 4, cats = setOf(POWER, BODY_WORSHIP),
            title = "Claimed",
            base = "{DOM} kisses his way over {SUB+} neck, chest and hips, naming each place as his as he goes.",
            explicit = "{DOM} kisses his way over {SUB+} neck, chest and hips and names every single place as his while he does it.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("ownership_language", "neck_kissing", "chest_nipple_kissing"),
            timers = listOf(tm("Neck", 45), tm("Chest", 45), tm("Hips", 45))
        ),
        t(
            id = "l4_own_degradation", level = 4, cats = setOf(LANGUAGE, POWER),
            title = "Talked down to",
            base = "{DOM} talks {SUB} down the whole time he strokes his cock with his hand, and does not go quiet once.",
            explicit = "{DOM} talks {SUB} down the entire time he strokes his cock with his hand, and never once softens it.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("explicit_praise", "commands"),
            timers = listOf(tm("First task", 60), tm("Second task", 60), tm("Third task", 60))
        ),
        t(
            id = "l4_own_collar_service", level = 4, cats = setOf(POWER, BONDAGE),
            title = "Collared and serving",
            base = "{SUB} wears #collar# and serves {DOM} with his mouth for the whole term, moving from his cock to his balls and back on command.",
            explicit = "{SUB} wears #collar# and serves {DOM} with his mouth for the whole term, moving from his cock to his balls and back on command, without being asked twice.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.PERMISSION_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("permission_control", "climax_permission"),
            timers = listOf(tm("In force", 60))
        ),
        t(
            id = "l4_control_edge_restrained", level = 4, cats = setOf(ORGASM_CONTROL, BONDAGE),
            title = "Edged while tied",
            base = "{SUB} is restrained with #cuffs# and {DOM} [v_edge] three times, taking both hands off him completely each time.",
            explicit = "{SUB} is locked in #cuffs# and {DOM} [v_edge] three separate times, stopping dead each time and leaving him there.",
            benefit = GIVER, type = BenefitType.ORGASM_CONTROL,
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
            benefit = GIVER, type = BenefitType.ORGASM_CONTROL,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
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
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"), toy = true,
            timers = listOf(tm("Setting 1", 60), tm("Setting 2", 60), tm("Setting 3", 60))
        ),
        t(
            id = "l4_control_capture_scene", level = 4, cats = setOf(ROLEPLAY, BONDAGE),
            title = "Caught",
            base = "{DOM} ties {SUB} down with #rope#, tells him he is not going anywhere, and uses his hands and mouth on him while he stays tied.",
            explicit = "{DOM} ties {SUB} down with #rope#, tells him he is not going anywhere, and uses his hands and mouth on his chest, stomach and thighs while he stays tied, until the timer ends.",
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "roleplay_lead"),
            gPrefs = setOf("capture_roleplay"), rPrefs = setOf("capture_roleplay"),
            equip = setOf(Equipment.ROPE),
            blocks = setOf(Boundary.NO_ROLEPLAY, Boundary.NO_RESTRAINTS),
            timers = listOf(tm("The capture", 60), tm("Tied and stroked", 180))
        )
    )
}
