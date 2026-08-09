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
 * 42 terms (specification floor for level 1: 42).
 */
internal object TermsLevel1 {

    val terms: List<Term> = listOf(

        // ---------------------------------------------------------------- massage (15)
        t(
            id = "l1_massage_neck_shoulders", level = 1, cats = setOf(MASSAGE),
            title = "Neck and shoulders",
            base = "{R} lies face down. {G} [v_massage] {R+} neck and shoulders [adv_pressure_firm], thumbs either side of the spine, moving from the base of the skull out to the point of each shoulder.",
            explicit = "{R} goes face down and stays there. {G} straddles his hips and [v_knead] his neck and shoulders [adv_pressure_firm], thumbs either side of the spine, from the base of the skull out to each shoulder, until the muscle stops fighting him.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_neck_shoulders"),
            timers = listOf(tm("Neck", 60), tm("Left shoulder", 45), tm("Right shoulder", 45))
        ),
        t(
            id = "l1_massage_upper_back", level = 1, cats = setOf(MASSAGE),
            title = "Upper back",
            base = "{G} [v_massage] {R+} upper back [adv_pressure_deep], following the inside edge of each shoulder blade and then out along the top of the ribs.",
            explicit = "{R} stays face down with his arms above his head. {G} [v_knead] his upper back [adv_pressure_deep], digging along the inside edge of each shoulder blade, then out along the top of the ribs, and does not let up early.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_upper_back", "massage_deep"),
            timers = listOf(tm("Left shoulder blade", 60), tm("Right shoulder blade", 60), tm("Across the ribs", 45))
        ),
        t(
            id = "l1_massage_lower_back", level = 1, cats = setOf(MASSAGE),
            title = "Lower back",
            base = "{G} [v_massage] {R+} lower back [adv_pressure_firm] with the heels of both hands, moving from the waist down to the top of the buttocks.",
            explicit = "{G} plants the heels of both hands on {R+} lower back and [v_knead] it [adv_pressure_deep], waist down to the top of his ass, until he is loose enough to make noise about it.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_lower_back"),
            timers = listOf(tm("Left side of the lower back", 60), tm("Right side of the lower back", 60))
        ),
        t(
            id = "l1_massage_scalp", level = 1, cats = setOf(MASSAGE),
            title = "Scalp and hair",
            base = "{R} lies with his head in {G+} lap. {G} [v_massage] {R+} scalp [adv_pressure_light] with his fingertips and draws his fingers slowly through {R+} hair.",
            explicit = "{R} puts his head in {G+} lap and {G} rubs his scalp [adv_pressure_light] with his fingertips, then [v_pull_hair] his hair and holds it just long enough for him to feel it.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_scalp"), gPrefs = setOf(), rPrefs = setOf(),
            timers = listOf(tm("Scalp", 90), tm("Hairline and temples", 45))
        ),
        t(
            id = "l1_massage_jaw_face", level = 1, cats = setOf(MASSAGE),
            title = "Jaw and face",
            base = "{G} [v_massage] {R+} jaw, temples and the muscle in front of each ear [adv_pressure_light], holding {R+} face in both hands.",
            explicit = "{G} takes {R+} face in both hands and kneads his jaw, temples and the muscle in front of each ear [adv_pressure_light], keeping eye contact the whole time.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_jaw_face"),
            timers = listOf(tm("Jaw", 45), tm("Temples", 45), tm("In front of each ear", 30))
        ),
        t(
            id = "l1_massage_arms_hands", level = 1, cats = setOf(MASSAGE),
            title = "Arms and hands",
            base = "{G} [v_massage] each of {R+} arms from shoulder to wrist, then kneads the palm and every finger of each hand.",
            explicit = "{G} takes each of {R+} arms in turn, kneads it from shoulder to wrist [adv_pressure_firm], then opens the palm and pulls on every finger until the joints give.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_arms_hands"),
            timers = listOf(tm("Left arm", 45), tm("Left hand", 30), tm("Right arm", 45), tm("Right hand", 30))
        ),
        t(
            id = "l1_massage_calves_feet", level = 1, cats = setOf(MASSAGE),
            title = "Calves and feet",
            base = "{G} [v_massage] each of {R+} calves [adv_pressure_firm], then presses the arch and heel of each foot with both thumbs.",
            explicit = "{G} kneads each of {R+} calves [adv_pressure_deep], then digs both thumbs into the arch and heel of each foot until he swears at him for it.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_calves_feet"),
            blocks = setOf(Boundary.NO_FOOT_PLAY),
            timers = listOf(tm("Left calf", 45), tm("Left foot", 45), tm("Right calf", 45), tm("Right foot", 45))
        ),
        t(
            id = "l1_massage_buttocks_hips", level = 1, cats = setOf(MASSAGE),
            title = "Buttocks and hips",
            base = "{G} [v_massage] {R+} buttocks and hips [adv_pressure_deep], kneading the outside of each hip and then the full muscle of each cheek.",
            explicit = "{G} [v_knead] {R+} ass and hips [adv_pressure_deep], rubs the outside of each hip, then takes a full handful of each cheek and does not hurry.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_buttocks_hips"),
            timers = listOf(tm("Left hip", 45), tm("Right hip", 45), tm("Left cheek", 45), tm("Right cheek", 45))
        ),
        t(
            id = "l1_massage_inner_thighs", level = 1, cats = setOf(MASSAGE),
            title = "Inner thighs",
            base = "{R} lies on his back with his legs apart. {G} [v_massage] each inner thigh from the knee up, stopping short of the groin each time.",
            explicit = "{R} lies on his back with his legs open. {G} kneads each inner thigh from the knee up [adv_pressure_firm], stopping just short of his groin every single time, until he is pushing his hips up for more.",
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
            explicit = "{G} warms #massage_oil# in his hands, pours it down {R+} spine and kneads his whole back in long, greedy strokes, shoulders to lower back and back up, until he is slick from neck to waist.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_general", "massage_oil"),
            equip = setOf(Equipment.MASSAGE_OIL),
            timers = listOf(tm("Shoulders", 60), tm("Upper back", 60), tm("Lower back", 60), tm("Full length of the back", 45))
        ),
        t(
            id = "l1_massage_candle", level = 1, cats = setOf(MASSAGE, SENSORY),
            title = "Massage candle",
            base = "{G} drips warm wax from #massage_candle# along {R+} back and shoulders, then [v_massage] it in [adv_pressure_firm].",
            explicit = "{G} tips #massage_candle# and drips the warm wax down {R+} spine and across his shoulders, listens to him react, then pushes it into the skin [adv_pressure_firm].",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("warm_wax", "massage_candle", "massage_general"),
            equip = setOf(Equipment.MASSAGE_CANDLE),
            timers = listOf(tm("Wax down the spine", 30), tm("Shoulders", 60), tm("Lower back", 60))
        ),
        t(
            id = "l1_massage_wand_back", level = 1, cats = setOf(MASSAGE),
            title = "Wand on the back",
            base = "{G} runs #massage_wand# over {R+} shoulders, spine and lower back [adv_toy_pace], holding it in place on each knot for a slow count of ten.",
            explicit = "{G} runs #massage_wand# over {R+} shoulders, down his spine and into his lower back [adv_toy_pace], and holds it hard against every spot that makes him swear.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_wand", "massage_general"),
            equip = setOf(Equipment.MASSAGE_WAND),
            timers = listOf(tm("Shoulders", 60), tm("Spine", 45), tm("Lower back", 60))
        ),
        t(
            id = "l1_massage_light_full", level = 1, cats = setOf(MASSAGE, SENSORY),
            title = "Light-pressure full body",
            base = "{G} kneads {R+} whole body [adv_pressure_light] with his fingertips only, from shoulders to feet, never pressing hard.",
            explicit = "{G} covers {R+} whole body with fingertips only [adv_pressure_light], shoulders down to his feet, deliberately too light, until he is asking for more pressure.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_light", "massage_general"),
            timers = listOf(tm("Back and shoulders", 60), tm("Arms", 45), tm("Legs", 60))
        ),
        t(
            id = "l1_massage_deep_back", level = 1, cats = setOf(MASSAGE),
            title = "Deep-pressure back",
            base = "{G} uses forearms and elbows to knead {R+} back [adv_pressure_deep], holding on each tight spot until it releases.",
            explicit = "{G} puts his forearms and elbows into {R+} back [adv_pressure_deep] and leans on every knot until it lets go, whatever noise he makes about it.",
            benefit = RECEIVER, type = BenefitType.MASSAGE_RECIPIENT,
            acts = setOf("massage_deep", "massage_upper_back"),
            timers = listOf(tm("Left side of the back", 75), tm("Right side of the back", 75))
        ),

        // ---------------------------------------------------------------- kissing (10)
        t(
            id = "l1_kiss_long_makeout", level = 1, cats = setOf(KISSING),
            title = "Long make-out",
            base = "{G} and {R} kiss [adv_kiss_deep] with nothing else happening — no hands below the waist, no moving on.",
            explicit = "{G} and {R} make out [adv_kiss_deep] and do nothing else. No hands below the waist, no moving on, no coming up for air until the timer says so.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out"),
            timers = listOf(tm("Making out", 180))
        ),
        t(
            id = "l1_kiss_tongue", level = 1, cats = setOf(KISSING),
            title = "Tongue-heavy kissing",
            base = "{G} kisses {R} [adv_kiss_deep], leading with his tongue the whole time while {R} follows.",
            explicit = "{G} kisses {R} [adv_kiss_deep], all tongue, and leads throughout. {R} takes it and does not try to take over.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("tongue_kissing"),
            timers = listOf(tm("Kissing", 120))
        ),
        t(
            id = "l1_kiss_hard", level = 1, cats = setOf(KISSING),
            title = "Hard kissing",
            base = "{G} takes {R+} face in both hands and kisses him [adv_kiss], not letting him pull back.",
            explicit = "{G} takes {R+} face in both hands, kisses him [adv_kiss] and does not let him pull back until he is done with him.",
            benefit = RECEIVER, type = BenefitType.KISS_RECIPIENT,
            acts = setOf("hard_kissing"),
            timers = listOf(tm("Kissing", 90))
        ),
        t(
            id = "l1_kiss_neck", level = 1, cats = setOf(KISSING),
            title = "Neck",
            base = "{G} kisses and sucks {R+} neck [adv_kiss], from behind the ear down to the collarbone and back up.",
            explicit = "{G} gets behind {R}, holds him still and kisses and sucks his neck [adv_kiss], behind the ear down to the collarbone and back, using teeth on the way up.",
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
            explicit = "{G} and {R} take turns running the kiss. Two minutes each. Whoever is following keeps his hands flat on the bed and takes it.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out", "tongue_kissing"),
            timers = listOf(tm("{G} leads", 120), tm("{R} leads", 120))
        ),
        t(
            id = "l1_kiss_blindfold", level = 1, cats = setOf(KISSING, SENSORY),
            title = "Blindfolded kissing",
            base = "{R} wears #blindfold#. {G} kisses him [adv_kiss] and moves between mouth, neck and chest without warning.",
            explicit = "{R} wears #blindfold# and does not get to know what is coming. {G} kisses him [adv_kiss], moving between his mouth, neck and chest, leaving gaps on purpose.",
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
            explicit = "{G} takes one of {R+} ears in his mouth [adv_kiss], rubs the outer edge with his lips, then sucks the lobe and does not let go early.",
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
            explicit = "{G} [v_tongue] the edge and the inside of each of {R+} ears, wet and unhurried, and goes nowhere else until the timer ends.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Left ear", 45), tm("Right ear", 45))
        ),
        t(
            id = "l1_ear_whispered_praise", level = 1, cats = setOf(EAR_PLAY, LANGUAGE),
            title = "Whispered praise in his ear",
            base = "{G} sucks {R+} ear and, between passes, [tone_whisper] into it and [v_praise].",
            explicit = "{G} sucks {R+} ear and, in the gaps, [tone_whisper] straight into it and [v_praise], close enough that he feels every word.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "explicit_praise"),
            timers = listOf(tm("Ear and whispering", 90))
        ),
        t(
            id = "l1_ear_moaning", level = 1, cats = setOf(EAR_PLAY, LANGUAGE),
            title = "Moaning into his ear",
            base = "{G} presses his mouth to {R+} ear and [tone_moan] into it while his hands stay on {R+} chest.",
            explicit = "{G} puts his mouth against {R+} ear and [tone_moan] straight into it, hands flat on his chest, and keeps going until {R} is squirming under him.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play"),
            timers = listOf(tm("Moaning into the ear", 60))
        ),
        t(
            id = "l1_ear_neck_combo", level = 1, cats = setOf(EAR_PLAY, KISSING),
            title = "Ear and neck together",
            base = "{G} alternates between {R+} ear and the side of his neck [adv_kiss], swapping every fifteen seconds, never settling in one place.",
            explicit = "{G} sucks {R+} ear then the side of his neck [adv_kiss] and keeps swapping every fifteen seconds, never settling in one place, so he can never settle.",
            benefit = RECEIVER, type = BenefitType.EAR_PLAY_RECIPIENT,
            acts = setOf("ear_play", "neck_kissing"),
            timers = listOf(tm("Left ear and neck", 60), tm("Right ear and neck", 60))
        ),
        t(
            id = "l1_ear_reciprocal", level = 1, cats = setOf(EAR_PLAY),
            title = "Reciprocal ear play",
            base = "{G} and {R} suck each other's ears at the same time, lying face to face on their sides.",
            explicit = "{G} and {R} lie face to face on their sides and suck each other's ears at the same time, and neither one stops first.",
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
            explicit = "{G} moves down {R+} body with hands and mouth and says out loud, part by part, exactly what he likes about it. He does not skip anything.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("body_worship", "explicit_praise"),
            timers = listOf(tm("Chest and arms", 60), tm("Stomach", 45), tm("Thighs", 60))
        ),
        t(
            id = "l1_worship_nipples", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Nipples, sucked and pinched",
            base = "{G} sucks and pinches {R+} nipples [adv_pace], one at a time and then both.",
            explicit = "{G} sucks and pinches {R+} nipples [adv_pace] with fingers and mouth, one at a time and then both together, and keeps going past the point where he starts pushing his chest up.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("nipple_stimulation"),
            timers = listOf(tm("Left nipple", 45), tm("Right nipple", 45), tm("Both together", 45))
        ),
        t(
            id = "l1_worship_scratching", level = 1, cats = setOf(BODY_WORSHIP, SENSORY),
            title = "Scratching",
            base = "{G} drags his nails down {R+} back and chest [adv_pace], light enough to leave nothing behind.",
            explicit = "{G} drags his nails down {R+} back and across his chest [adv_pace], hard enough to make him arch and light enough to leave nothing behind.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("scratching"),
            blocks = setOf(Boundary.NO_MARKS),
            timers = listOf(tm("Back", 45), tm("Chest and stomach", 45))
        ),
        t(
            id = "l1_worship_hair", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Hands in his hair",
            base = "{G} runs his hands through {R+} hair and [v_pull_hair] it, tipping {R+} head back to expose his throat.",
            explicit = "{G} [v_pull_hair] {R+} hair, tips his head back to bare his throat and holds him there while he kisses it.",
            benefit = RECEIVER, type = BenefitType.HAND_STIMULATION_RECIPIENT,
            acts = setOf("hair_pulling", "neck_kissing"),
            timers = listOf(tm("Hair and throat", 90))
        ),
        t(
            id = "l1_worship_feet", level = 1, cats = setOf(BODY_WORSHIP),
            title = "Feet",
            base = "{G} kneads each of {R+} feet with his hands and then his mouth, arch first, then each toe.",
            explicit = "{G} kneads each of {R+} feet with his hands and then his mouth, arch first, then every toe in turn, and takes his time over it.",
            benefit = RECEIVER, type = BenefitType.SERVICE_RECIPIENT,
            acts = setOf("foot_stimulation", "foot_kissing"),
            blocks = setOf(Boundary.NO_FOOT_PLAY),
            timers = listOf(tm("Left foot", 60), tm("Right foot", 60))
        ),
        t(
            id = "l1_sensory_feather", level = 1, cats = setOf(SENSORY),
            title = "Feather over the body",
            base = "{R} lies still with his eyes closed. {G} runs #feather# over his chest, stomach and inner thighs [adv_slow].",
            explicit = "{R} lies still with his eyes shut and does not move. {G} runs #feather# over his chest, stomach and inner thighs [adv_slow] until holding still is genuinely hard.",
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
            explicit = "{G} and {R} put on #porn#, make out through the whole thing and keep their hands above each other's waists no matter how bad it gets.",
            benefit = MUTUAL, type = BenefitType.MUTUAL, mutual = true,
            acts = setOf("long_making_out"),
            gPrefs = setOf("watching_porn"), rPrefs = setOf("watching_porn"),
            equip = setOf(Equipment.PORN),
            timers = listOf(tm("Watching and kissing", 180))
        )
    )
}
