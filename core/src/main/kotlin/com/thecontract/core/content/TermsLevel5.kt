package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.GIVER
import com.thecontract.core.model.BenefitParty.MUTUAL
import com.thecontract.core.model.BenefitParty.RECEIVER
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.ANAL
import com.thecontract.core.model.Category.BONDAGE
import com.thecontract.core.model.Category.IMPACT
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.LANGUAGE
import com.thecontract.core.model.Category.ORAL
import com.thecontract.core.model.Category.ORGASM_CONTROL
import com.thecontract.core.model.Category.POWER
import com.thecontract.core.model.Category.RIMMING
import com.thecontract.core.model.Category.ROUGH
import com.thecontract.core.model.Category.SENSORY
import com.thecontract.core.model.Category.TOYS
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.PartyConstraint
import com.thecontract.core.model.PartyRef
import com.thecontract.core.model.Term

/**
 * Act V — Enforcement. Rough play, hard sex, intense BDSM and the rules that run to the end
 * of the night. 40 terms (specification floor for level 5: 40).
 */
internal object TermsLevel5 {

    val terms: List<Term> = listOf(

        // ---------------------------------------------------------- rough play (10)
        t(
            id = "l5_rough_pin_wrists", level = 5, cats = setOf(ROUGH),
            title = "Pinned",
            base = "{G} [v_pin] {R} by the wrists and holds him there while he kisses him [adv_kiss], and {R} does not get free.",
            explicit = "{G} [v_pin] {R} by the wrists, puts his weight on him and kisses him [adv_kiss] while he tries and fails to get loose.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            acts = setOf("pinning", "hard_kissing"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pinned", 120))
        ),
        t(
            id = "l5_rough_manhandle", level = 5, cats = setOf(ROUGH, POWER),
            title = "Moved by the hips",
            base = "{G} [v_handle] {R} into three different positions without asking, and {R} lets himself be moved.",
            explicit = "{G} [v_handle] {R} into three positions without a word of warning, and {R} goes exactly where he is put.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            acts = setOf("rough_handling"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Position 1", 45), tm("Position 2", 45), tm("Position 3", 45))
        ),
        t(
            id = "l5_rough_hair_kiss", level = 5, cats = setOf(ROUGH, KISSING),
            title = "By the hair",
            base = "{G} [v_pull_hair] {R+} hair, tips his head back and kisses him [adv_kiss] on his terms only.",
            explicit = "{G} [v_pull_hair] {R+} hair, drags his head back and kisses him [adv_kiss] entirely on his own terms.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            acts = setOf("rough_hair_pulling", "hard_kissing"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("By the hair", 90))
        ),
        t(
            id = "l5_rough_shoved_to_knees", level = 5, cats = setOf(ROUGH, ORAL, POWER),
            title = "Put on his knees",
            base = "{DOM} [v_shove] {SUB} down onto his knees and has him [v_suck] him from there.",
            explicit = "{DOM} [v_shove] {SUB} down onto his knees and puts his cock in his mouth without asking.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("oral", "kneeling"),
            gPrefs = setOf("rough_handling_receive"), rPrefs = setOf("rough_handling_give"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("On his knees", 150))
        ),
        t(
            id = "l5_rough_held_down_worked", level = 5, cats = setOf(ROUGH),
            title = "Held down and stroked",
            base = "{G} holds {R} face down by the back of the shoulders and strokes him with his free hand for the full time.",
            explicit = "{G} holds {R} face down with a hand between his shoulder blades and strokes him with the other hand until the timer ends.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            acts = setOf("pinning", "rough_handling"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Held down", 150))
        ),
        t(
            id = "l5_rough_undress", level = 5, cats = setOf(ROUGH, POWER),
            title = "Stripped",
            base = "{G} gets {R} out of whatever he is wearing quickly and without ceremony, then [v_pin] him.",
            explicit = "{G} strips {R} fast and without ceremony and then [v_pin] him down before he can say anything about it.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            acts = setOf("rough_handling", "pinning"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Stripped and pinned", 90))
        ),
        t(
            id = "l5_rough_grip_and_kiss", level = 5, cats = setOf(ROUGH, KISSING),
            title = "Held by the jaw",
            base = "{G} [v_grip] {R+} jaw, holds his face still and kisses him [adv_kiss] until the timer ends.",
            explicit = "{G} [v_grip] {R+} jaw, holds his face still and kisses him [adv_kiss] until the timer ends.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            acts = setOf("rough_handling", "hard_kissing"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Held and kissed", 90))
        ),
        t(
            id = "l5_rough_scratch_and_bite", level = 5, cats = setOf(ROUGH, SENSORY),
            title = "Nails and teeth",
            base = "{G} kneads {R+} shoulders and chest with nails and teeth [adv_rough], stopping short of anything that lasts.",
            explicit = "{G} kneads {R+} shoulders and chest over with nails and teeth [adv_rough] and stops just short of leaving anything behind.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("scratching", "rough_handling"),
            blocks = setOf(Boundary.NO_ROUGH_SEX, Boundary.NO_MARKS, Boundary.NO_PAIN),
            timers = listOf(tm("Shoulders", 45), tm("Chest", 45))
        ),
        t(
            id = "l5_rough_wrestle", level = 5, cats = setOf(ROUGH),
            title = "Whoever ends up on top",
            base = "{G} and {R} wrestle for position for two minutes, and whoever wins decides what happens for the next two.",
            explicit = "{G} and {R} actually wrestle for it for two minutes, and whoever comes out on top decides what happens in the next two.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("pinning", "rough_handling"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Wrestling", 120), tm("Winner decides", 120))
        ),
        t(
            id = "l5_rough_pin_and_rim", level = 5, cats = setOf(ROUGH, RIMMING, ANAL),
            title = "Held open and eaten out",
            base = "{G} holds {R} face down, spreads him with both hands and [v_rim_hard] him while he stays pinned.",
            explicit = "{G} pins {R} face down, spreads him wide with both hands and [v_rim_hard] him while he cannot get up.",
            benefit = RECEIVER, type = BenefitType.RIMMING_RECIPIENT,
            acts = setOf("pinning", "rimming"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pinned and eaten out", 180))
        ),

        // ----------------------------------------------------------- hard oral (6)
        t(
            id = "l5_oral_hard_pace", level = 5, cats = setOf(ORAL, ROUGH),
            title = "Hard, no slowing down",
            base = "{R} fucks {G+} mouth hard and {G} keeps his lips sealed around him through the whole of it.",
            explicit = "{R} fucks {G+} mouth hard and {G} takes all of it for as long as the timer runs, without pulling off once.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "harder_oral"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Hard pace", 120))
        ),
        t(
            id = "l5_oral_hands_in_hair", level = 5, cats = setOf(ORAL, ROUGH),
            title = "Both hands in his hair",
            base = "{R} keeps both hands in {G+} hair and pulls him down onto his cock in a steady rhythm. {G} keeps his hands flat on the floor throughout.",
            explicit = "{R} keeps both hands buried in {G+} hair and pulls him down onto his cock in a steady rhythm, all the way each time. {G} keeps his hands flat on the floor and does not push back.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "deep_oral", "harder_oral", "rough_hair_pulling"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("His hands, steady rhythm", 120))
        ),
        t(
            id = "l5_oral_deep_hold", level = 5, cats = setOf(ORAL),
            title = "Held deep",
            base = "{G} [v_deep] {R} and stays down for a slow count of ten, three times over, coming up between each.",
            explicit = "{G} [v_deep] {R} and holds there for a slow count of ten, three times over, coming up properly between each one.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "deep_oral"),
            timers = listOf(tm("Round 1", 45), tm("Round 2", 45), tm("Round 3", 45))
        ),
        t(
            id = "l5_oral_kneeling_hard", level = 5, cats = setOf(ORAL, POWER, ROUGH),
            title = "Kneeling and taking it",
            base = "{SUB} kneels with his hands behind his back while {DOM} fucks {SUB+} mouth, one minute steady then two minutes hard.",
            explicit = "{SUB} kneels with his hands locked behind his back while {DOM} fucks {SUB+} mouth, one minute steady then two minutes hard, and {SUB} does not pull off.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("oral", "kneeling", "harder_oral"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Kneeling", 150))
        ),
        t(
            id = "l5_oral_restrained_hard", level = 5, cats = setOf(ORAL, BONDAGE, ROUGH),
            title = "Tied and used",
            base = "{SUB} is restrained with #cuffs# and {DOM} uses his mouth, one minute slow then ninety seconds hard.",
            explicit = "{SUB} is locked in #cuffs# and {DOM} uses his mouth, one minute slow then ninety seconds hard, until the timer runs out.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            gCon = PartyConstraint.SUBMISSIVE, rCon = PartyConstraint.DOMINANT,
            acts = setOf("oral", "restraint", "harder_oral"),
            equip = setOf(Equipment.CUFFS),
            blocks = setOf(Boundary.NO_ROUGH_SEX, Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Tied and used", 150))
        ),
        t(
            id = "l5_oral_spit_rough", level = 5, cats = setOf(ORAL, ROUGH),
            title = "Wet and hard",
            base = "{G} keeps {R+} cock [adj_wet] with his own spit and strokes him at a hard pace throughout.",
            explicit = "{G} keeps {R+} cock soaked with his own spit and strokes him at a hard pace the entire way through.",
            benefit = RECEIVER, type = BenefitType.ORAL_RECIPIENT,
            acts = setOf("oral", "spit", "harder_oral"),
            blocks = setOf(Boundary.NO_SPIT_PLAY, Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Wet and hard", 120))
        ),

        // ------------------------------------------------------------ hard sex (10)
        t(
            id = "l5_sex_hard_behind", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Hard from behind",
            base = "{G} [v_fuck_hard] {R} from behind with #lubricant#, [adv_thrust_hard], holding him by the hips.",
            explicit = "{G} [v_fuck_hard] {R} from behind with #lubricant# [adv_thrust_hard], both hands locked on his hips, and does not ease off.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_hard_on_back", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Hard on his back",
            base = "{R} lies on his back with his legs up and {G} [v_fuck_hard] him [adv_thrust_hard], face to face.",
            explicit = "{R} lies back with his legs up and {G} [v_fuck_hard] him [adv_thrust_hard], face to face, with his full weight on him.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_hard_bent_over", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Bent over and taken",
            base = "{R} bends over #sturdy_chair# and {G} [v_fuck_hard] him there [adv_thrust_hard] without letting him stand up.",
            explicit = "{R} is bent over #sturdy_chair# and {G} [v_fuck_hard] him [adv_thrust_hard] and does not let him straighten up once.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT, Equipment.STURDY_CHAIR),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_hard_hair", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Hard, with a fist in his hair",
            base = "{G} [v_fuck_hard] {R} from behind and [v_pull_hair] his hair to keep his head up throughout.",
            explicit = "{G} [v_fuck_hard] {R} from behind with a fist in his hair, keeping his head up so he has to take all of it.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex", "rough_hair_pulling"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_hard_spanked", level = 5, cats = setOf(ANAL, ROUGH, IMPACT),
            title = "Fucked and spanked",
            base = "{G} [v_fuck_hard] {R} from behind and [v_spank] him every time he slows down or pulls away.",
            explicit = "{G} [v_fuck_hard] {R} from behind and [v_spank] his ass every single time he pulls away or slows down.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex", "spanking"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX, Boundary.NO_PAIN),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_hard_restrained", level = 5, cats = setOf(ANAL, ROUGH, BONDAGE),
            title = "Tied and fucked hard",
            base = "{R} is restrained with #cuffs# and {G} [v_fuck_hard] him [adv_thrust_hard] for the full time.",
            explicit = "{R} is locked in #cuffs# and {G} [v_fuck_hard] him [adv_thrust_hard] for the entire term while he cannot move.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex", "restraint"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT, Equipment.CUFFS),
            blocks = setOf(Boundary.NO_ROUGH_SEX, Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Tied", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_hard_clamped", level = 5, cats = setOf(ANAL, ROUGH, TOYS),
            title = "Clamped and fucked",
            base = "{R} wears #nipple_clamps# while {G} [v_fuck_hard] him, and they stay on for the whole term.",
            explicit = "{R} keeps #nipple_clamps# on the entire time {G} [v_fuck_hard] him, and they do not come off early.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex", "nipple_clamps"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT, Equipment.NIPPLE_CLAMPS),
            blocks = setOf(Boundary.NO_ROUGH_SEX, Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Clamps on", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_escalating", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Slow to hardest, a minute each",
            base = "{G} starts [adv_thrust_slow] and moves up a level every minute, slow to steady to hard to hardest.",
            explicit = "{G} starts [adv_thrust_slow] and goes up a level every minute, slow to steady to hard to hardest, without ever dropping back down.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Slow", 60), tm("Steady", 60), tm("Hard", 60), tm("Hardest", 60))
        ),
        t(
            id = "l5_sex_pinned_fucked", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Pinned and fucked",
            base = "{G} [v_pin] {R+} wrists above his head and [v_fuck_hard] him from there without letting go.",
            explicit = "{G} [v_pin] {R+} wrists above his head with one hand and [v_fuck_hard] him without ever letting go.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex", "pinning"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pinned", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_sex_hard_until_told", level = 5, cats = setOf(ANAL, ROUGH, POWER),
            title = "Four minutes, no change of pace",
            base = "{G} [v_fuck_hard] {R} for the full four minutes and does not change depth or pace once.",
            explicit = "{G} [v_fuck_hard] {R} for the full four minutes and does not change depth or pace once, start to finish.",
            benefit = RECEIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "hard_sex"), anal = true,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            erection = PartyRef.GIVER,
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Hard", 240))
        ),

        // -------------------------------------------------------------- fisting (4)
        t(
            id = "l5_fist_build", level = 5, cats = setOf(ANAL),
            title = "Building up to his whole hand",
            base = "{G} uses plenty of #lubricant# and builds from two fingers to four inside {R}, adding one more every two minutes.",
            explicit = "{G} slicks his whole hand with #lubricant# and builds from two fingers to four inside {R}, one more every two minutes.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fisting"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Two", 120), tm("Three", 120), tm("Four", 120))
        ),
        t(
            id = "l5_fist_past_knuckles", level = 5, cats = setOf(ANAL),
            title = "Past the knuckles",
            base = "{G} slicks his hand with #lubricant# and pushes it into {R} past the knuckles, then holds it still for a slow count of thirty.",
            explicit = "{G} slicks his hand with #lubricant# and pushes it into {R} past the knuckles, then holds completely still for a slow count of thirty.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fisting"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 150), tm("Held still", 30), tm("Deeper", 120))
        ),
        t(
            id = "l5_fist_whole_hand", level = 5, cats = setOf(ANAL),
            title = "As much of his hand as he takes",
            base = "{G} pushes as much of his hand into {R} as it will take with #lubricant# and keeps it there for the rest of the term.",
            explicit = "{G} pushes as much of his hand into {R} as it will take with #lubricant# and keeps it buried for the rest of the term.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fisting"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 150), tm("Buried", 180))
        ),
        t(
            id = "l5_fist_turning", level = 5, cats = setOf(ANAL, ROUGH),
            title = "His hand turning inside him",
            base = "{G} pushes his hand into {R} with #lubricant# and turns it a quarter turn every thirty seconds without pulling out.",
            explicit = "{G} pushes his hand into {R} with #lubricant# and turns it a quarter turn every thirty seconds, never pulling out once.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fisting", "rough_anal"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 120), tm("Turning", 180))
        ),

        // ------------------------------------------------------------ rough anal (6)
        t(
            id = "l5_ranal_pinned", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Pinned flat and taken hard",
            base = "{G} pins {R+} shoulders down with #lubricant# and fucks him [adv_thrust_hard] for the whole term, keeping him flat.",
            explicit = "{G} pins {R+} shoulders down with #lubricant# and fucks him [adv_thrust_hard] for the whole term, keeping him flat and taking every stroke.",
            benefit = GIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "rough_anal", "pinning"), anal = true, erection = PartyRef.GIVER,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Pinned and fucked", 180))
        ),
        t(
            id = "l5_ranal_hair", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Hard, held by the hair",
            base = "{G} takes {R} from behind with #lubricant#, keeps a fist in his hair and fucks him [adv_thrust_hard] for the whole term.",
            explicit = "{G} takes {R} from behind with #lubricant#, fists his hair to hold his head up and fucks him [adv_thrust_hard] the whole way through.",
            benefit = GIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "rough_anal", "rough_hair_pulling"), anal = true, erection = PartyRef.GIVER,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_ranal_folded", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Folded up and fucked hard",
            base = "{R} goes on his back with his knees pushed to his chest and {G} fucks him [adv_thrust_hard] with #lubricant# for the whole term.",
            explicit = "{R} goes on his back with his knees pushed to his chest and {G} fucks him [adv_thrust_hard] with #lubricant#, holding him folded the whole term.",
            benefit = GIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "rough_anal"), anal = true, erection = PartyRef.GIVER,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_ranal_standing", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Bent over and pounded",
            base = "{R} bends over #sturdy_chair# and {G} fucks him [adv_thrust_hard] with #lubricant# for the whole term without stopping.",
            explicit = "{R} bends over #sturdy_chair# and holds on while {G} fucks him [adv_thrust_hard] with #lubricant# for the whole term without stopping once.",
            benefit = GIVER, type = BenefitType.PENETRATION_RECIPIENT,
            acts = setOf("topping", "rough_anal"), anal = true, erection = PartyRef.GIVER,
            gCon = PartyConstraint.TOP, rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT, Equipment.STURDY_CHAIR),
            timers = listOf(tm("Pushing in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_ranal_dildo_hard", level = 5, cats = setOf(ANAL, ROUGH, TOYS),
            title = "The dildo, hard and fast",
            base = "{G} pushes #dildo# into {R} with #lubricant# and drives it [adv_thrust_hard] for the whole term.",
            explicit = "{G} pushes #dildo# into {R} with #lubricant# and drives it [adv_thrust_hard] for the whole term without slowing once.",
            benefit = RECEIVER, type = BenefitType.TOY_RECIPIENT,
            acts = setOf("dildo", "anal_toys", "rough_anal"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.DILDO, Equipment.LUBRICANT),
            timers = listOf(tm("Pushing it in", 45), tm("Hard", 180))
        ),
        t(
            id = "l5_ranal_fingers_hard", level = 5, cats = setOf(ANAL, ROUGH),
            title = "Three fingers, hard",
            base = "{G} builds to three fingers in {R} with #lubricant# and finger-fucks him hard for the rest of the term.",
            explicit = "{G} builds to three fingers in {R} with #lubricant# and finger-fucks him hard for the rest of the term without easing off.",
            benefit = RECEIVER, type = BenefitType.FINGERING_RECIPIENT,
            acts = setOf("fingering", "rough_anal"), anal = true,
            rCon = PartyConstraint.BOTTOM,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Building up", 60), tm("Hard", 180))
        ),

        // ------------------------------------------------------- intense BDSM (8)
        t(
            id = "l5_bdsm_full_flogging", level = 5, cats = setOf(IMPACT, BONDAGE),
            title = "Full flogging",
            base = "{SUB} is restrained with #rope# and {DOM} runs #flogger# over his back, ass and thighs in four building rounds.",
            explicit = "{SUB} is tied with #rope# and {DOM} swings #flogger# across his back, ass and thighs through four rounds, harder every round.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys", "restraint"),
            equip = setOf(Equipment.FLOGGER, Equipment.ROPE),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS, Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Round 1", 60), tm("Round 2", 60), tm("Round 3", 60), tm("Round 4", 45))
        ),
        t(
            id = "l5_bdsm_paddle_series", level = 5, cats = setOf(IMPACT),
            title = "Paddle series",
            base = "{DOM} uses #paddle# on {SUB} in four sets of ten, and {SUB} counts every stroke and thanks him after each set.",
            explicit = "{DOM} swings #paddle# across {SUB+} ass in four sets of ten. He counts every stroke and thanks him after each set, or the set starts again.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys", "spanking", "commands"),
            equip = setOf(Equipment.PADDLE),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Set 1", 45), tm("Set 2", 45), tm("Set 3", 45), tm("Set 4", 45))
        ),
        t(
            id = "l5_bdsm_crop_precision", level = 5, cats = setOf(IMPACT, SENSORY),
            title = "Crop, blindfolded",
            base = "{SUB} wears #blindfold# and {DOM} runs #crop# over his thighs, ass and chest with uneven timing.",
            explicit = "{SUB} wears #blindfold# and {DOM} runs #crop# over his thighs, ass and chest with deliberately uneven timing so nothing can be braced for.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("impact_toys", "blindfold"),
            equip = setOf(Equipment.CROP, Equipment.BLINDFOLD),
            blocks = setOf(Boundary.NO_PAIN, Boundary.NO_MARKS),
            timers = listOf(tm("Thighs", 60), tm("Ass", 60), tm("Chest", 45))
        ),
        t(
            id = "l5_bdsm_full_restraint", level = 5, cats = setOf(BONDAGE, SENSORY),
            title = "Tied down completely",
            base = "{DOM} restrains {SUB} with #rope# and #spreader_bar# and uses his hands on him, mouth and #TOY# while he stays with him throughout.",
            explicit = "{DOM} ties {SUB} down with #rope# and #spreader_bar# so he cannot move at all, then uses his hands on him, mouth and #TOY# and never leaves his side.",
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint"), toy = true,
            equip = setOf(Equipment.ROPE, Equipment.SPREADER_BAR),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Tying him down", 90), tm("Hands and mouth", 120), tm("Toy", 120))
        ),
        t(
            id = "l5_bdsm_sensory_overload", level = 5, cats = setOf(SENSORY, BONDAGE, TOYS),
            title = "Everything at once",
            base = "{SUB} wears #blindfold# and is restrained with #cuffs#. {DOM} alternates #TOY#, his mouth and #ice# with no pattern.",
            explicit = "{SUB} is blindfolded with #blindfold# and locked in #cuffs#, and {DOM} throws #TOY#, his mouth and #ice# at him in no pattern at all.",
            benefit = GIVER, type = BenefitType.RESTRAINT_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "blindfold", "cold_sensation"), toy = true,
            equip = setOf(Equipment.BLINDFOLD, Equipment.CUFFS, Equipment.ICE),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Round 1", 60), tm("Round 2", 60), tm("Round 3", 60))
        ),
        t(
            id = "l5_bdsm_collar_leash_service", level = 5, cats = setOf(POWER, BONDAGE, ORAL),
            title = "On the leash",
            base = "{SUB} wears #collar# and #leash# and serves {DOM} with his mouth for the whole term, following the leash without resistance.",
            explicit = "{SUB} wears #collar# and #leash# and serves {DOM} with his mouth for the entire term, following the leash without a word of complaint.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("collaring", "oral", "sexual_service", "ownership_language"),
            equip = setOf(Equipment.COLLAR, Equipment.LEASH),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Led and serving", 180))
        ),
        t(
            id = "l5_bdsm_degradation_scene", level = 5, cats = setOf(LANGUAGE, POWER, ROUGH),
            title = "Talked down and taken",
            base = "{DOM} talks {SUB} down while he [v_handle] him, from the first second of the timer to the last.",
            explicit = "{DOM} talks {SUB} down the entire time he [v_handle] him, and does not soften it once.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("degradation", "rough_handling", "dirty_talk"),
            blocks = setOf(Boundary.NO_DEGRADATION, Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("The scene", 180))
        ),
        t(
            id = "l5_bdsm_edged_restrained_denied", level = 5, cats = setOf(ORGASM_CONTROL, BONDAGE),
            title = "Tied, edged and left there",
            base = "{SUB} is restrained with #rope# and {DOM} [v_edge] five times over, and he does not finish during this term.",
            explicit = "{SUB} is tied with #rope# and {DOM} [v_edge] five separate times, and he does not get to come once during the whole term.",
            benefit = GIVER, type = BenefitType.ORGASM_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("restraint", "edging", "denial"),
            equip = setOf(Equipment.ROPE),
            erection = PartyRef.RECEIVER,
            blocks = setOf(Boundary.NO_RESTRAINTS, Boundary.NO_ORGASM_CONTROL),
            timers = listOf(tm("Edge 1", 60), tm("Edge 2", 60), tm("Edge 3", 60), tm("Edge 4", 60), tm("Edge 5", 60))
        ),

        // ------------------------------------------------------------ final rules (6)
        t(
            id = "l5_rule_permission_all_night", level = 5, cats = setOf(POWER, ORGASM_CONTROL),
            title = "Nobody comes without permission",
            base = "For the rest of the night {SUB} does not finish without asking and being told yes.",
            explicit = "For the rest of the night {SUB} does not come without asking out loud and being told yes, no exceptions.",
            benefit = GIVER, type = BenefitType.PERMISSION_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("climax_permission", "permission_control"),
            blocks = setOf(Boundary.NO_ORGASM_CONTROL),
            timers = listOf(tm("Rule in force", 60))
        ),
        t(
            id = "l5_rule_stay_in_position", level = 5, cats = setOf(POWER),
            title = "He stays where he is put",
            base = "For the rest of the night {SUB} stays in whatever position {DOM} puts him in until he is moved.",
            explicit = "For the rest of the night {SUB} stays in exactly whatever position {DOM} puts him in until he is physically moved out of it.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"),
            timers = listOf(tm("Rule in force", 60))
        ),
        t(
            id = "l5_rule_belongs_tonight", level = 5, cats = setOf(POWER, LANGUAGE),
            title = "His for the rest of the night",
            base = "{SUB} agrees out loud [n_ownership] for the rest of the night, and {DOM} [v_own].",
            explicit = "{SUB} says out loud that he agrees to [n_ownership] for the rest of the night, and {DOM} [v_own] in return.",
            benefit = GIVER, type = BenefitType.OWNERSHIP,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("ownership_language"),
            timers = listOf(tm("Agreeing to it", 60))
        ),
        t(
            id = "l5_rule_obeys_first_time", level = 5, cats = setOf(POWER),
            title = "First time of asking",
            base = "For the rest of the night {SUB} does what he is told the first time, and {DOM} does not ask twice.",
            explicit = "For the rest of the night {SUB} does what he is told the first time. {DOM} does not ask twice, and there are consequences if he has to.",
            benefit = GIVER, type = BenefitType.COMMAND_AUTHORITY,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("commands"),
            timers = listOf(tm("Rule in force", 60))
        ),
        t(
            id = "l5_rule_no_hands", level = 5, cats = setOf(POWER, ORGASM_CONTROL),
            title = "Hands off himself",
            base = "For the rest of the night {SUB} does not touch his own cock unless {DOM} tells him to.",
            explicit = "For the rest of the night {SUB} does not put a hand on his own cock unless {DOM} tells him to.",
            benefit = GIVER, type = BenefitType.PERMISSION_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("permission_control", "commands"),
            timers = listOf(tm("Rule in force", 60))
        ),
        t(
            id = "l5_rule_report_state", level = 5, cats = setOf(POWER, LANGUAGE),
            title = "Says it out loud",
            base = "For the rest of the night {SUB} says out loud how close he is at the end of every term, in plain words.",
            explicit = "For the rest of the night {SUB} says out loud exactly how close he is at the end of every term, in plain words, every time.",
            benefit = GIVER, type = BenefitType.PERMISSION_CONTROL,
            gCon = PartyConstraint.DOMINANT, rCon = PartyConstraint.SUBMISSIVE,
            acts = setOf("permission_control", "dirty_talk"),
            timers = listOf(tm("Rule in force", 60))
        )
    )
}
