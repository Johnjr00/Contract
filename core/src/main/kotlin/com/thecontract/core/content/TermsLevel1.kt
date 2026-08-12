package com.thecontract.core.content

import com.thecontract.core.model.BenefitParty.MUTUAL
import com.thecontract.core.model.BenefitParty.RECEIVER
import com.thecontract.core.model.BenefitType
import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.BODY_WORSHIP
import com.thecontract.core.model.Category.EAR_PLAY
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.LANGUAGE
import com.thecontract.core.model.Category.MASSAGE
import com.thecontract.core.model.Category.SENSORY
import com.thecontract.core.model.Category.VISUAL
import com.thecontract.core.model.Equipment
import com.thecontract.core.model.Term

/**
 * Act I — Chemistry. Massage, kissing, ear play and body worship.
 * 66 terms (specification floor for level 1: 42).
 */
internal object TermsLevel1 {

    val terms: List<Term> = listOf(

        // ---------------------------------------------------------------- massage (15)
        t(
            id = "l1_massage_neck_shoulders", level = 1, cats = setOf(MASSAGE),
            title = "Neck and shoulders",
            base = "{R} lies face down. {G} [v_massage] {R+} neck and shoulders [adv_pressure_firm], thumbs either side of the spine, moving from the base of the skull out to the point of each shoulder.",
            explicit = "{R} goes face down and stays there. {G} straddles his hips and [v_knead] his neck and shoulders, thumbs either side of the spine and never on it, working from the base of the skull out to each shoulder, hard across the shoulders and light on the neck itself, for the full timer.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_neck_shoulders"),
            timers = listOf(tm("Neck", 60), tm("Left shoulder", 45), tm("Right shoulder", 45))
        ),
        t(
            id = "l1_massage_upper_back", level = 1, cats = setOf(MASSAGE),
            title = "Upper back",
            base = "{G} [v_massage] {R+} upper back [adv_pressure_deep], following the inside edge of each shoulder blade and then out along the top of the ribs.",
            explicit = "{R} stays face down with his arms above his head. {G} [v_knead] his upper back [adv_pressure_deep], digging along the inside edge of each shoulder blade, then out along the top of the ribs, and keeps the pressure on until the timer ends.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_upper_back", "massage_deep"),
            timers = listOf(tm("Left shoulder blade", 60), tm("Right shoulder blade", 60), tm("Across the ribs", 45))
        ),
        t(
            id = "l1_massage_lower_back", level = 1, cats = setOf(MASSAGE),
            title = "Lower back",
            base = "{G} [v_massage] {R+} lower back [adv_pressure_firm], using the heels of both hands, moving from the waist down to the top of the buttocks.",
            explicit = "{G} plants the heels of both hands on {R+} lower back and [v_knead] it [adv_pressure_deep], waist down to the top of his ass, for the full timer.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_lower_back"),
            timers = listOf(tm("Left side of the lower back", 60), tm("Right side of the lower back", 60))
        ),
        t(
            id = "l1_massage_scalp", level = 1, cats = setOf(MASSAGE),
            title = "Scalp and hair",
            base = "{R} lies with his head in {G+} lap. {G} [v_massage] {R+} scalp [adv_pressure_light], using his fingertips only, then draws his fingers slowly through {R+} hair.",
            explicit = "{R} puts his head in {G+} lap. {G} rubs his scalp [adv_pressure_light], using his fingertips only, then [v_pull_hair] his hair and holds it tight for a slow count of five before he lets go. He does that three times before the timer ends.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_scalp"), gPrefs = setOf(), rPrefs = setOf(),
            timers = listOf(tm("Scalp", 90), tm("Hairline and temples", 45))
        ),
        t(
            id = "l1_massage_jaw_face", level = 1, cats = setOf(MASSAGE),
            title = "Jaw and face",
            base = "{G} [v_massage] {R+} jaw, temples and the muscle in front of each ear [adv_pressure_light], holding {R+} face in both hands.",
            explicit = "{G} takes {R+} face in both hands and rubs his jaw, temples and the muscle in front of each ear [adv_pressure_light], keeping eye contact the whole time.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_jaw_face"),
            timers = listOf(tm("Jaw", 45), tm("Temples", 45), tm("In front of each ear", 30))
        ),
        t(
            id = "l1_massage_arms_hands", level = 1, cats = setOf(MASSAGE),
            title = "Arms and hands",
            base = "{G} [v_massage] each of {R+} arms from shoulder to wrist, then kneads the palm and every finger of each hand.",
            explicit = "{G} takes each of {R+} arms in turn, kneads it from shoulder to wrist [adv_pressure_firm], then opens the palm and pulls on every finger in turn.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_arms_hands"),
            timers = listOf(tm("Left arm", 45), tm("Left hand", 30), tm("Right arm", 45), tm("Right hand", 30))
        ),
        t(
            id = "l1_massage_calves_feet", level = 1, cats = setOf(MASSAGE),
            title = "Calves and feet",
            base = "{G} [v_massage] each of {R+} calves [adv_pressure_firm], then presses the arch and heel of each foot with both thumbs.",
            explicit = "{G} kneads each of {R+} calves [adv_pressure_deep], then digs both thumbs into the arch and heel of each foot for the rest of the timer.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_calves_feet"),
            blocks = setOf(Boundary.NO_FOOT_PLAY),
            timers = listOf(tm("Left calf", 45), tm("Left foot", 45), tm("Right calf", 45), tm("Right foot", 45))
        ),
        t(
            id = "l1_massage_buttocks_hips", level = 1, cats = setOf(MASSAGE),
            title = "Buttocks and hips",
            base = "{G} [v_massage] {R+} buttocks and hips [adv_pressure_deep], kneading the outside of each hip and then the full muscle of each cheek.",
            explicit = "{G} [v_knead] {R+} ass and hips [adv_pressure_deep], rubs the outside of each hip, then takes a full handful of each cheek and squeezes it for a slow count of ten before he moves to the other.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_buttocks_hips"),
            timers = listOf(tm("Left hip", 45), tm("Right hip", 45), tm("Left cheek", 45), tm("Right cheek", 45))
        ),
        t(
            id = "l1_massage_inner_thighs", level = 1, cats = setOf(MASSAGE),
            title = "Inner thighs",
            base = "{R} lies on his back with his legs apart. {G} [v_massage] each inner thigh from the knee up, stopping short of the groin each time.",
            explicit = "{R} lies on his back with his legs open. {G} kneads each inner thigh from the knee up [adv_pressure_firm], stopping just short of his groin every single time, for the full timer.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_inner_thighs"),
            timers = listOf(tm("Left inner thigh", 60), tm("Right inner thigh", 60))
        ),
        t(
            id = "l1_massage_chest", level = 1, cats = setOf(MASSAGE),
            title = "Chest and pecs",
            base = "{G} [v_massage] {R+} chest and pecs [adv_pressure_firm], moving out from the breastbone to each shoulder.",
            explicit = "{G} straddles {R+} waist and kneads his chest and pecs [adv_pressure_firm], out from the breastbone to each shoulder, dragging his thumbs across the nipples on every pass.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_chest"),
            timers = listOf(tm("Left pec", 45), tm("Right pec", 45), tm("Breastbone and collarbones", 30))
        ),
        t(
            id = "l1_massage_oil_full", level = 1, cats = setOf(MASSAGE),
            title = "Oiled back sequence",
            base = "{G} warms #massage_oil# in his hands and [v_massage] {R+} whole back, moving from shoulders to lower back and back up in long strokes.",
            explicit = "{G} warms #massage_oil# in his hands, pours it down {R+} spine and kneads his whole back in strokes that run its full length, shoulders to lower back and back up again, until he is slick from neck to waist.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_general", "massage_oil"),
            equip = setOf(Equipment.MASSAGE_OIL),
            timers = listOf(tm("Shoulders", 60), tm("Upper back", 60), tm("Lower back", 60), tm("Full length of the back", 45))
        ),
        t(
            id = "l1_massage_candle", level = 1, cats = setOf(MASSAGE, SENSORY),
            title = "Massage candle",
            base = "{G} drips warm wax from #massage_candle# along {R+} back and shoulders, then [v_massage] it in [adv_pressure_firm].",
            explicit = "{G} tips #massage_candle# and drips the warm wax down {R+} spine and across his shoulders, waits a slow count of five, then presses it into the skin [adv_pressure_firm].",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("warm_wax", "massage_candle", "massage_general"),
            equip = setOf(Equipment.MASSAGE_CANDLE),
            timers = listOf(tm("Wax down the spine", 30), tm("Shoulders", 60), tm("Lower back", 60))
        ),
        t(
            id = "l1_massage_wand_back", level = 1, cats = setOf(MASSAGE),
            title = "Wand on the back",
            base = "{G} runs #massage_wand# over {R+} shoulders, spine and lower back [adv_toy_pace], stopping to hold it in place for a slow count of ten on the top of each shoulder, on the middle of the spine, and on each side of the lower back.",
            explicit = "{G} runs #massage_wand# over {R+} shoulders, down his spine and into his lower back [adv_toy_pace], and presses it in hard for a slow count of ten on the top of each shoulder, on the middle of the spine, and on each side of the lower back.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_wand", "massage_general"),
            equip = setOf(Equipment.MASSAGE_WAND),
            timers = listOf(tm("Shoulders", 60), tm("Spine", 45), tm("Lower back", 60))
        ),
        t(
            id = "l1_massage_light_full", level = 1, cats = setOf(MASSAGE, SENSORY),
            title = "Light-pressure full body",
            base = "{G} runs his fingertips over {R+} whole body [adv_pressure_light], from shoulders to feet, and uses nothing but his fingertips.",
            explicit = "{G} runs his fingertips over {R+} whole body [adv_pressure_light], shoulders down to his feet, light enough that his fingers never dent the skin, for the full timer.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_light", "massage_general"),
            timers = listOf(tm("Back and shoulders", 60), tm("Arms", 45), tm("Legs", 60))
        ),
        t(
            id = "l1_massage_deep_back", level = 1, cats = setOf(MASSAGE),
            title = "Deep-pressure back",
            base = "{G} uses forearms and elbows to knead {R+} back [adv_pressure_deep], holding for a slow count of ten on each shoulder blade, on the middle of the back and on the top of each hip.",
            explicit = "{G} puts his forearms and elbows into {R+} back [adv_pressure_deep] and leans his weight on each shoulder blade, on the middle of the back and on the top of each hip in turn, a slow count of ten on each, and does not lift off before the count is finished.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_deep", "massage_upper_back"),
            timers = listOf(tm("Left side of the back", 75), tm("Right side of the back", 75))
        ),

        // ---------------------------------------------------------------- kissing (10)
        t(
            id = "l1_kiss_long_makeout", level = 1, cats = setOf(KISSING),
            title = "Long make-out",
            base = "{G} and {R} kiss deeply and slowly with nothing else happening — no hands below the waist, no moving on.",
            explicit = "{G} and {R} make out deep and open-mouthed and do nothing else. No hands below the waist, no moving on, no coming up for air until the timer says so.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out"),
            timers = listOf(tm("Making out", 180))
        ),
        t(
            id = "l1_kiss_tongue", level = 1, cats = setOf(KISSING),
            title = "Tongue-heavy kissing",
            base = "{G} kisses {R} [adv_kiss_deep], leading with his tongue the whole time while {R} follows.",
            explicit = "{G} kisses {R} [adv_kiss_deep], all tongue, and leads throughout. {R} follows and does not take over the kiss at any point.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("tongue_kissing"),
            timers = listOf(tm("Kissing", 120))
        ),
        t(
            id = "l1_kiss_hard", level = 1, cats = setOf(KISSING),
            title = "Hard kissing",
            base = "{G} takes {R+} face in both hands and kisses him [adv_kiss], not letting him pull back.",
            explicit = "{G} takes {R+} face in both hands, kisses him [adv_kiss] and does not let him pull back until the timer ends.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("hard_kissing"),
            timers = listOf(tm("Kissing", 90))
        ),
        t(
            id = "l1_kiss_neck", level = 1, cats = setOf(KISSING),
            title = "Neck",
            base = "{G} kisses and sucks {R+} neck [adv_kiss], from behind the ear down to the collarbone and back up.",
            explicit = "{G} gets behind {R}, holds him still and kisses and sucks his neck [adv_kiss], from behind the ear down to the collarbone and back up again, and closes his teeth on the skin at the collarbone and again behind the ear on every pass.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("neck_kissing"),
            timers = listOf(tm("Left side of the neck", 60), tm("Right side of the neck", 60))
        ),
        t(
            id = "l1_kiss_chest_nipples", level = 1, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Chest and nipples",
            base = "{G} kisses {R+} chest and sucks each nipple [adv_kiss], alternating between them.",
            explicit = "{G} kisses down {R+} chest and sucks each nipple [adv_kiss], sucking one while his fingers pinch the other, and swaps every thirty seconds.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("chest_nipple_kissing", "nipple_stimulation"),
            timers = listOf(tm("Left nipple", 60), tm("Right nipple", 60))
        ),
        t(
            id = "l1_kiss_stomach_hips", level = 1, cats = setOf(KISSING),
            title = "Stomach and hips",
            base = "{G} kisses {R+} stomach and hip bones [adv_kiss], following the line of muscle down each side and stopping at the waistband.",
            explicit = "{G} kisses {R+} stomach and hip bones [adv_kiss], follows the line of muscle down each side with his tongue and stops dead at the waistband every time.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("stomach_hip_kissing"),
            timers = listOf(tm("Stomach", 60), tm("Left hip", 45), tm("Right hip", 45))
        ),
        t(
            id = "l1_kiss_inner_thighs", level = 1, cats = setOf(KISSING),
            title = "Inner thighs",
            base = "{G} kisses up each of {R+} inner thighs [adv_kiss], stopping short of the groin each time.",
            explicit = "{G} kisses and [v_bite] his way up each of {R+} inner thighs [adv_kiss], stops short of his groin every time, and starts again from the knee.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("inner_thigh_kissing"),
            timers = listOf(tm("Left inner thigh", 60), tm("Right inner thigh", 60))
        ),
        t(
            id = "l1_kiss_trail", level = 1, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Head-to-hip trail",
            base = "{G} lays [n_kiss_trail] from {R+} mouth to his hips without missing his neck, chest, stomach or either hip bone.",
            explicit = "{G} lays [n_kiss_trail] from {R+} mouth all the way to his hips and does not skip his neck, chest, stomach or either hip bone on the way down.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("neck_kissing", "chest_nipple_kissing", "stomach_hip_kissing"),
            timers = listOf(tm("Mouth and neck", 45), tm("Chest", 45), tm("Stomach and hips", 45))
        ),
        t(
            id = "l1_kiss_reciprocal", level = 1, cats = setOf(KISSING),
            title = "Reciprocal make-out",
            base = "{G} and {R} take turns leading: two minutes each, and the one following keeps his hands still.",
            explicit = "{G} and {R} take turns running the kiss. Two minutes each. Whoever is following keeps his hands flat on the bed and does not lead.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out", "tongue_kissing"),
            timers = listOf(tm("{G} leads", 120), tm("{R} leads", 120))
        ),
        t(
            id = "l1_kiss_blindfold", level = 1, cats = setOf(KISSING, SENSORY),
            title = "Blindfolded kissing",
            base = "{R} wears #blindfold#. {G} kisses him [adv_kiss], moving between his mouth, neck and chest and changing place every fifteen seconds without saying which is next.",
            explicit = "{R} wears #blindfold#. {G} kisses him [adv_kiss], moving between his mouth, neck and chest, changing place every fifteen seconds, and says nothing at all before he moves.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("blindfold", "tongue_kissing", "neck_kissing"),
            equip = setOf(Equipment.BLINDFOLD),
            timers = listOf(tm("Mouth", 45), tm("Neck", 45), tm("Chest", 45))
        ),

        // ---------------------------------------------------------------- ear play (8)
        t(
            id = "l1_ear_one", level = 1, cats = setOf(EAR_PLAY),
            title = "One ear",
            base = "{G} sucks one of {R+} ears [adv_kiss] — lips along the outer edge, then the lobe.",
            explicit = "{G} takes one of {R+} ears in his mouth [adv_kiss], rubs the outer edge with his lips, then sucks the lobe and keeps it in his mouth until the timer ends.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Outer edge", 45), tm("Lobe", 45))
        ),
        t(
            id = "l1_ear_both", level = 1, cats = setOf(EAR_PLAY),
            title = "Both ears",
            base = "{G} sucks both of {R+} ears in turn [adv_kiss], keeping his hands on {R+} jaw to hold his head still.",
            explicit = "{G} holds {R+} head still by the jaw and sucks both ears in turn [adv_kiss]. {R} is not allowed to turn away from either one.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Left ear", 60), tm("Right ear", 60))
        ),
        t(
            id = "l1_ear_licking", level = 1, cats = setOf(EAR_PLAY),
            title = "Licking his ears",
            base = "{G} [v_tongue] the edge and inside of each of {R+} ears, slowly, and does not move anywhere else.",
            explicit = "{G} [v_tongue] the edge and the inside of each of {R+} ears, wet and slow, and puts his mouth nowhere else until the timer ends.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Left ear", 45), tm("Right ear", 45))
        ),
        t(
            id = "l1_ear_whispered_praise", level = 1, cats = setOf(EAR_PLAY, LANGUAGE),
            title = "Praise in his ear",
            base = "{G} sucks {R+} ear and, between passes, [tone_whisper] into it and [v_praise].",
            explicit = "{G} sucks {R+} ear and, in the gaps, [tone_whisper] straight into it and [v_praise], with his lips touching the ear as he says it.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "explicit_praise"),
            timers = listOf(tm("Ear and whispering", 90)),
            says = listOf(
                "You are so good at this.",
                "That is exactly right.",
                "I could stay here all night.",
                "You feel incredible.",
                "Do not stop doing that."
            ),
            saysExplicit = listOf(
                "You are so fucking good at this.",
                "That is exactly right, good boy.",
                "You take that so well.",
                "You feel fucking incredible.",
                "Do not stop, not for a second."
            ),
        ),
        t(
            id = "l1_ear_moaning", level = 1, cats = setOf(EAR_PLAY, LANGUAGE),
            title = "Moaning into his ear",
            base = "{G} presses his mouth to {R+} ear and [tone_moan] into it while his hands stay on {R+} chest.",
            explicit = "{G} puts his mouth against {R+} ear and [tone_moan] straight into it, hands flat on {R+} chest, and keeps going for the full timer.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Moaning into the ear", 60))
        ),
        t(
            id = "l1_ear_neck_combo", level = 1, cats = setOf(EAR_PLAY, KISSING),
            title = "Ear and neck together",
            base = "{G} alternates between {R+} ear and the side of his neck [adv_kiss], swapping every fifteen seconds.",
            explicit = "{G} sucks {R+} ear then the side of his neck [adv_kiss] and keeps swapping every fifteen seconds until the timer ends.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "neck_kissing"),
            timers = listOf(tm("Left ear and neck", 60), tm("Right ear and neck", 60))
        ),
        t(
            id = "l1_ear_reciprocal", level = 1, cats = setOf(EAR_PLAY),
            title = "Reciprocal ear play",
            base = "{G} and {R} suck each other's ears at the same time, lying face to face on their sides.",
            explicit = "{G} and {R} lie face to face on their sides and suck each other's ears at the same time, and neither one stops before the timer ends.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("ear_play"),
            timers = listOf(tm("Both ears at once", 90))
        ),
        t(
            id = "l1_ear_during_massage", level = 1, cats = setOf(EAR_PLAY, MASSAGE),
            title = "Ear play during a massage",
            base = "{G} [v_massage] {R+} shoulders, then leans down and sucks his ear without stopping his hands.",
            explicit = "{G} keeps kneading {R+} shoulders [adv_pressure_firm] and leans down to suck his ear with his mouth at the same time. His hands do not stop.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "massage_neck_shoulders"),
            timers = listOf(tm("Shoulders only", 45), tm("Shoulders and ear together", 90))
        ),

        // ---------------------------------------------------- body worship and sensory (9)
        t(
            id = "l1_worship_head_to_toe", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Body worship",
            base = "{G} moves his way down {R+} body with hands and mouth and names one thing he likes about each part he reaches.",
            explicit = "{G} moves down {R+} body with hands and mouth and says out loud, part by part, exactly what he likes about it. He does not skip a single part.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("body_worship", "explicit_praise"),
            timers = listOf(tm("Chest and arms", 60), tm("Stomach", 45), tm("Thighs", 60)),
            says = listOf(
                "I love your shoulders.",
                "This is my favourite part of you.",
                "I think about your chest all the time.",
                "Your stomach drives me mad.",
                "I could spend an hour on your thighs."
            ),
            saysExplicit = listOf(
                "I love your fucking shoulders.",
                "This is my favourite part of you.",
                "I think about your chest all the time.",
                "Your stomach drives me fucking mad.",
                "I could spend an hour on these thighs."
            ),
        ),
        t(
            id = "l1_worship_nipples", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Nipples, sucked and pinched",
            base = "{G} sucks and pinches {R+} nipples [adv_pace], one at a time and then both.",
            explicit = "{G} sucks and pinches {R+} nipples [adv_pace] with fingers and mouth, one at a time and then both together, and keeps going for the full timer.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("nipple_stimulation"),
            timers = listOf(tm("Left nipple", 45), tm("Right nipple", 45), tm("Both together", 45))
        ),
        t(
            id = "l1_worship_scratching", level = 1, cats = setOf(BODY_WORSHIP, SENSORY),
            title = "Scratching",
            base = "{G} drags his nails down {R+} back and chest with the flat of the nail rather than the point, light enough to leave no mark at all.",
            explicit = "{G} drags his nails down {R+} back and across his chest, hard enough to raise a white line and light enough that the line is gone inside a minute.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("scratching"),
            blocks = setOf(Boundary.NO_MARKS),
            timers = listOf(tm("Back", 45), tm("Chest and stomach", 45))
        ),
        t(
            id = "l1_worship_hair", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Hands in his hair",
            base = "{G} [v_pull_hair] {R+} hair and uses it to tip {R+} head back, holding him there with his throat exposed for the full timer.",
            explicit = "{G} [v_pull_hair] {R+} hair, tips his head back to bare his throat and holds him there while he kisses it.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("hair_pulling", "neck_kissing"),
            timers = listOf(tm("Hair and throat", 90))
        ),
        t(
            id = "l1_worship_feet", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Feet",
            base = "{G} kneads each of {R+} feet with his hands and then his mouth, arch first, then each toe.",
            explicit = "{G} kneads each of {R+} feet with his hands and then his mouth, arch first, then every toe in turn, spending a slow count of ten on each toe.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("foot_stimulation", "foot_kissing"),
            blocks = setOf(Boundary.NO_FOOT_PLAY),
            timers = listOf(tm("Left foot", 60), tm("Right foot", 60))
        ),
        t(
            id = "l1_sensory_feather", level = 1, cats = setOf(SENSORY),
            title = "Feather over the body",
            base = "{R} lies still with his eyes closed. {G} runs #feather# over his chest, stomach and inner thighs [adv_slow].",
            explicit = "{R} lies still with his eyes shut and does not move. {G} runs #feather# over his chest, stomach and inner thighs [adv_slow] for the full timer.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("body_worship"),
            equip = setOf(Equipment.FEATHER),
            timers = listOf(tm("Chest", 45), tm("Stomach", 45), tm("Inner thighs", 45))
        ),
        t(
            id = "l1_sensory_ice", level = 1, cats = setOf(SENSORY),
            title = "Ice on the chest",
            base = "{G} runs #ice# over {R+} chest, nipples and stomach, then follows the same path with his mouth.",
            explicit = "{G} runs #ice# over {R+} chest, nipples and stomach, then goes back over the exact same path with his hot mouth and lets him feel the difference.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("cold_sensation", "nipple_stimulation"),
            equip = setOf(Equipment.ICE),
            timers = listOf(tm("Ice on the chest", 45), tm("Mouth over the same path", 45))
        ),
        t(
            id = "l1_visual_mirror_kiss", level = 1, cats = setOf(VISUAL, KISSING),
            title = "Kissing in the mirror",
            base = "{G} and {R} kiss in front of #mirror#, and both of them watch instead of closing their eyes.",
            explicit = "{G} and {R} kiss in front of #mirror# and both keep their eyes open on the reflection the whole time. Nobody gets to look away.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out"),
            gPrefs = setOf("mirror_play"), rPrefs = setOf("mirror_play"),
            equip = setOf(Equipment.MIRROR),
            timers = listOf(tm("Kissing at the mirror", 120))
        ),
        t(
            id = "l1_visual_porn_kissing", level = 1, cats = setOf(VISUAL, KISSING),
            title = "Watching together",
            base = "{G} and {R} put on #porn# and make out through it without touching each other below the waist.",
            explicit = "{G} and {R} put on #porn#, make out through the whole thing and keep both hands above each other's waists from the first second to the last.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out"),
            gPrefs = setOf("watching_porn"), rPrefs = setOf("watching_porn"),
            equip = setOf(Equipment.PORN),
            timers = listOf(tm("Watching and kissing", 180))
        ),

        // ---------------------------------------------------------------- added (8)
        t(
            id = "l1_massage_seated_shoulders", level = 1, cats = setOf(MASSAGE),
            title = "Sat between his knees",
            base = "{R} sits on the floor between {G+} knees. {G} [v_massage] {R+} shoulders [adv_pressure_firm], working out from the base of the neck to the point of each shoulder and back again.",
            explicit = "{R} sits on the floor between {G+} knees and stays there. {G} [v_knead] his shoulders [adv_pressure_deep], base of the neck out to each shoulder and back, for the full timer.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_neck_shoulders"),
            timers = listOf(tm("Shoulders", 150))
        ),
        t(
            id = "l1_massage_hands_fingers", level = 1, cats = setOf(MASSAGE),
            title = "One hand at a time",
            base = "{G} takes one of {R+} hands in both of his and [v_massage] the palm [adv_pressure_firm], then every finger from the base to the tip, before starting on the other.",
            explicit = "{G} takes one of {R+} hands in both of his and [v_knead] the palm [adv_pressure_firm], then works every finger from base to tip, then does the other hand.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_arms_hands"),
            timers = listOf(tm("Left hand", 60), tm("Right hand", 60))
        ),
        t(
            id = "l1_kiss_jaw_and_throat", level = 1, cats = setOf(KISSING),
            title = "Jaw to collarbone",
            base = "{G} kisses {R+} jaw and throat [adv_kiss], moving from just under the ear down to the collarbone and back up again.",
            explicit = "{G} works {R+} jaw and throat with his mouth [adv_kiss_deep], ear down to the collarbone and back up, and does not stop until the timer does.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("neck_kissing"),
            timers = listOf(tm("Jaw and throat", 120))
        ),
        t(
            id = "l1_kiss_hands_on_face", level = 1, cats = setOf(KISSING),
            title = "Hands on his face",
            base = "{G} and {R} make out [adv_kiss], and neither of them moves his hands from the other's face or hair for the whole of it.",
            explicit = "{G} and {R} make out deeply, tongues and all, and keep their hands on each other's face and hair, nowhere else, for the full timer.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out", "tongue_kissing"),
            timers = listOf(tm("Making out", 180))
        ),
        t(
            id = "l1_ear_breath_and_tongue", level = 1, cats = setOf(EAR_PLAY),
            title = "Breath and tongue",
            base = "{G} works {R+} ear with his tongue and with his breath, alternating between the two, one hand keeping {R+} head still.",
            explicit = "{G} [v_tongue] {R+} ear and breathes into it in turn, one hand holding his head where it is, for the full timer.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Left ear", 60), tm("Right ear", 60))
        ),
        t(
            id = "l1_worship_back_and_spine", level = 1, cats = setOf(BODY_WORSHIP),
            title = "The length of his back",
            base = "{R} lies face down. {G} works his mouth and hands up {R+} back, starting at the base of the spine and finishing at the back of the neck.",
            explicit = "{R} goes face down and stays there. {G} [v_worship] his back with hands and mouth, base of the spine up to the neck, for the full timer.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("body_worship"),
            timers = listOf(tm("Base to shoulders", 90), tm("Shoulders to neck", 60))
        ),
        t(
            id = "l1_sensory_warm_and_cold", level = 1, cats = setOf(SENSORY),
            title = "Warm hands, cold hands",
            base = "{G} runs both hands over {R+} chest and stomach, then holds one hand round a glass of iced water for a slow count of twenty and goes over the same ground again so {R} feels the difference.",
            explicit = "{G} runs both hands over {R+} chest and stomach, then chills one hand round a glass of iced water and covers the same ground again, slowly, so he feels every bit of the difference.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("cold_sensation"),
            timers = listOf(tm("Warm", 60), tm("Cold", 90))
        ),
        t(
            id = "l1_worship_shoulders_and_arms", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Arms and shoulders",
            base = "{G} works his mouth and hands over {R+} shoulders and arms, taking one arm at a time from the shoulder down to the wrist.",
            explicit = "{G} [v_worship] {R+} shoulders and arms with his mouth and hands, one arm at a time, shoulder down to the wrist, for the full timer.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("body_worship"),
            timers = listOf(tm("Left arm", 75), tm("Right arm", 75))
        ),

        // ---------------------------------------------------------------- added (16)
        t(
            id = "l1_massage_neck_face_up", level = 1, cats = setOf(MASSAGE),
            title = "Neck, face up",
            base = "{R} lies on his back with his head resting in {G+} hands. {G} [v_massage] the back of {R+} neck and the base of his skull [adv_pressure_light], fingertips either side of the spine and never on it, and holds the whole weight of {R+} head in his palms.",
            explicit = "{R} lies on his back and puts his head in {G+} hands. {G} works the back of {R+} neck and the base of his skull with light, steady pressure, fingertips either side of the spine and never on it, and carries the whole weight of his head in his palms until the timer ends.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_neck_shoulders"),
            timers = listOf(tm("Base of the skull", 60), tm("Left side of the neck", 45), tm("Right side of the neck", 45))
        ),
        t(
            id = "l1_massage_ribs_and_sides", level = 1, cats = setOf(MASSAGE),
            title = "Ribs and sides",
            base = "{R} lies on one side. {G} [v_massage] {R+} ribs and waist with firm, even pressure, following each rib out from the spine to the front, and when the first timer ends {R} rolls over and {G} does the other side the same way.",
            explicit = "{R} lies on one side and {G} [v_knead] {R+} ribs and waist [adv_pressure_deep], following each rib out from the spine to the front. When the first timer ends {R} rolls over and {G} does the other side exactly the same way.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_upper_back", "massage_lower_back"),
            timers = listOf(tm("First side", 90), tm("Second side", 90))
        ),
        t(
            id = "l1_massage_hamstrings", level = 1, cats = setOf(MASSAGE),
            title = "Backs of his thighs",
            base = "{R} lies face down. {G} [v_massage] the back of each of {R+} thighs [adv_pressure_deep], working from behind the knee up to the crease of the buttock and back down again.",
            explicit = "{R} goes face down and stays there. {G} [v_knead] the back of each of {R+} thighs [adv_pressure_deep], behind the knee up to the crease of his ass and back down, and keeps the pressure on until the timer ends.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_inner_thighs", "massage_deep"),
            timers = listOf(tm("Left thigh", 75), tm("Right thigh", 75))
        ),
        t(
            id = "l1_massage_counted_points", level = 1, cats = setOf(MASSAGE),
            title = "Six points, twenty each",
            base = "{R} lies face down. {G} presses both thumbs into six points on {R+} back in turn — the top of each shoulder, either side of the spine between the shoulder blades, and either side of the lower back — and holds each one for a slow count of twenty before he moves to the next.",
            explicit = "{R} goes face down. {G} puts both thumbs into six points on {R+} back in turn — the top of each shoulder, either side of the spine between the shoulder blades, and either side of the lower back — leaning his weight into each one for a slow count of twenty before he moves on.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_upper_back", "massage_lower_back", "massage_deep"),
            timers = listOf(tm("Shoulders", 60), tm("Beside the spine", 60), tm("Lower back", 60))
        ),
        t(
            id = "l1_massage_candle_chest", level = 1, cats = setOf(MASSAGE, SENSORY),
            title = "Candle on his chest",
            base = "{G} drips warm wax from #massage_candle# across {R+} chest and stomach, waits a slow count of five, then [v_massage] it into the skin with slow, even pressure.",
            explicit = "{G} tips #massage_candle# and drips the warm wax across {R+} chest and stomach, waits a slow count of five, then rubs it into the skin with slow, hard pressure and does not stop before the timer does.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("warm_wax", "massage_candle", "massage_chest"),
            equip = setOf(Equipment.MASSAGE_CANDLE),
            timers = listOf(tm("Wax across the chest", 30), tm("Chest", 60), tm("Stomach", 60))
        ),
        t(
            id = "l1_massage_wand_legs", level = 1, cats = setOf(MASSAGE),
            title = "Wand down his legs",
            base = "{G} runs #massage_wand# down the back of each of {R+} legs [adv_toy_pace] and holds it in place for a slow count of ten on each hamstring and again behind each knee.",
            explicit = "{G} runs #massage_wand# down the back of each of {R+} legs [adv_toy_pace] and presses it in hard for a slow count of ten on each hamstring and again behind each knee.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_wand", "massage_general"),
            equip = setOf(Equipment.MASSAGE_WAND),
            timers = listOf(tm("Left leg", 60), tm("Right leg", 60))
        ),
        t(
            id = "l1_massage_oiled_arms_seated", level = 1, cats = setOf(MASSAGE),
            title = "Oiled arms and shoulders",
            base = "{R} sits upright. {G} warms #massage_oil# in his hands and [v_massage] {R+} shoulders and both arms, one arm at a time, shoulder down to the wrist and back up.",
            explicit = "{R} sits upright and {G} warms #massage_oil# in his hands, then [v_knead] {R+} shoulders and both arms, one arm at a time, shoulder down to the wrist and back up, until the timer ends.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_arms_hands", "massage_oil"),
            equip = setOf(Equipment.MASSAGE_OIL),
            timers = listOf(tm("Shoulders", 45), tm("Left arm", 60), tm("Right arm", 60))
        ),
        t(
            id = "l1_kiss_down_the_spine", level = 1, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Down his spine",
            base = "{R} lies face down. {G} lays [n_kiss_trail] from the back of {R+} neck all the way down his spine to the small of his back, then works back up the same line.",
            explicit = "{R} goes face down and stays there. {G} lays [n_kiss_trail] from the back of {R+} neck down his spine to the small of his back, then works back up the same line and does not miss a single vertebra.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("neck_kissing", "body_worship"),
            timers = listOf(tm("Down", 75), tm("Back up", 75))
        ),
        t(
            id = "l1_kiss_ten_places", level = 1, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Ten places, ten seconds each",
            base = "{G} kisses {R} in ten places in this order — his mouth, his jaw, his throat, each collarbone, each nipple, his stomach, each hip bone — and holds each kiss for a slow count of ten.",
            explicit = "{G} kisses {R} in ten places in this order — his mouth, his jaw, his throat, each collarbone, each nipple, his stomach, each hip bone — and keeps his mouth on each one for a slow count of ten.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("neck_kissing", "chest_nipple_kissing", "stomach_hip_kissing"),
            timers = listOf(tm("Mouth and jaw", 45), tm("Throat and collarbones", 45), tm("Nipples", 45), tm("Stomach and hips", 45))
        ),
        t(
            id = "l1_kiss_against_the_wall", level = 1, cats = setOf(KISSING),
            title = "Against the wall",
            base = "{G} puts {R} back against the wall, plants a hand either side of his head and kisses him [adv_kiss_deep], and {R} keeps both hands flat on the wall behind him.",
            explicit = "{G} pushes {R} back against the wall, plants a hand either side of his head and kisses him [adv_kiss_deep] for the whole timer, and {R} keeps both hands flat on the wall behind him.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("hard_kissing", "tongue_kissing"),
            timers = listOf(tm("Against the wall", 120))
        ),
        t(
            id = "l1_kiss_slow_then_hard", level = 1, cats = setOf(KISSING),
            title = "Slow first, hard second",
            base = "{G} kisses {R} [adv_kiss] for the first two minutes and [adv_kiss_deep] for the second two, and changes over exactly when the first timer ends.",
            explicit = "{G} kisses {R} [adv_kiss] for the first two minutes, then [adv_kiss_deep] for the second two, and changes over exactly when the first timer ends and not before.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("long_making_out", "hard_kissing"),
            timers = listOf(tm("Slow", 120), tm("Hard", 120))
        ),
        t(
            id = "l1_ear_lobes_counted", level = 1, cats = setOf(EAR_PLAY),
            title = "Twenty seconds an ear",
            base = "{G} takes one of {R+} ears in his mouth for a slow count of twenty, then the other, and keeps swapping between them until the timer ends. {R} keeps his hands at his sides throughout.",
            explicit = "{G} takes one of {R+} ears in his mouth for a slow count of twenty, then the other, and keeps swapping between them until the timer ends. {R} keeps his hands flat at his sides and does not touch {G} once.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Swapping ears", 150))
        ),
        t(
            id = "l1_ear_behind_and_jaw", level = 1, cats = setOf(EAR_PLAY, KISSING),
            title = "Behind the ear",
            base = "{G} [v_tongue] the skin behind each of {R+} ears and along the hinge of his jaw, slowly and lightly, one side for the first timer and the other for the second.",
            explicit = "{G} [v_tongue] the skin behind each of {R+} ears and down the hinge of his jaw, wet and slow, one side for the first timer and the other for the second, and puts his mouth nowhere else.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "neck_kissing"),
            timers = listOf(tm("Left side", 60), tm("Right side", 60))
        ),
        t(
            id = "l1_worship_stomach_and_ribs", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Stomach and ribs",
            base = "{R} lies on his back with his arms above his head. {G} works his mouth and hands over {R+} stomach, ribs and the line of each hip, and puts his mouth nowhere below the waistband.",
            explicit = "{R} lies on his back with his arms above his head and does not move them. {G} [v_worship] {R+} stomach, ribs and the line of each hip with mouth and hands, and does not go below the waistband once.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("body_worship"),
            timers = listOf(tm("Stomach", 60), tm("Ribs", 45), tm("Hips", 45))
        ),
        t(
            id = "l1_sensory_feather_and_breath", level = 1, cats = setOf(SENSORY),
            title = "Feather, then breath",
            base = "{R} lies still with his eyes shut. {G} runs #feather# over {R+} chest, stomach and inner thighs [adv_slow], then covers the same ground again with nothing but his breath, his mouth an inch off the skin the whole way.",
            explicit = "{R} lies still with his eyes shut and does not move. {G} runs #feather# over {R+} chest, stomach and inner thighs [adv_slow], then goes back over the same ground with nothing but his breath, his mouth an inch off the skin the whole way.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("body_worship"),
            equip = setOf(Equipment.FEATHER),
            timers = listOf(tm("Feather", 90), tm("Breath", 90))
        ),
        t(
            id = "l1_visual_mirror_hands", level = 1, cats = setOf(VISUAL, BODY_WORSHIP),
            title = "Watching his own chest",
            base = "{G} stands behind {R} in front of #mirror# and runs both hands over {R+} chest, stomach and hips. Both of them watch the reflection and neither one looks away.",
            explicit = "{G} stands behind {R} at #mirror# and runs both hands over {R+} chest, stomach and hips. Both of them keep their eyes on the reflection and neither one looks away before the timer ends.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("body_worship"),
            gPrefs = setOf("mirror_play"), rPrefs = setOf("mirror_play"),
            equip = setOf(Equipment.MIRROR),
            timers = listOf(tm("Chest", 45), tm("Stomach", 45), tm("Hips", 45))
        )
    )
}
