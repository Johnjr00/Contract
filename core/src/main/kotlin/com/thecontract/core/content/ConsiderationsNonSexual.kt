package com.thecontract.core.content

import com.thecontract.core.model.Boundary
import com.thecontract.core.model.Category.BODY_WORSHIP
import com.thecontract.core.model.Category.EAR_PLAY
import com.thecontract.core.model.Category.KISSING
import com.thecontract.core.model.Category.LANGUAGE
import com.thecontract.core.model.Category.MASSAGE
import com.thecontract.core.model.Category.SENSORY
import com.thecontract.core.model.ConsiderationAction
import com.thecontract.core.model.ConsiderationFamily.EAR_PLAY as F_EAR
import com.thecontract.core.model.ConsiderationFamily.KISSING as F_KISS
import com.thecontract.core.model.ConsiderationFamily.MASSAGE as F_MASSAGE
import com.thecontract.core.model.Equipment

/**
 * Consideration actions — massage, kissing and ear play (39 of 124).
 *
 * A consideration action is performed **during** negotiation by the player who benefits more
 * from the proposed term, in order to earn the other player's signature. It is never added to
 * the final contract; only a receipt is recorded.
 *
 * Authoring convention: `{G}` performs, `{R}` receives.
 */
internal object ConsiderationsNonSexual {

    val actions: List<ConsiderationAction> = listOf(

        // ------------------------------------------------------------- massage (16)
        c(
            id = "cn_mass_scalp", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Scalp massage",
            base = "{G} rubs {R+} scalp with his fingertips [adv_pressure_light], covering the whole head.",
            explicit = "{G} takes {R+} head in both hands and rubs his scalp [adv_pressure_light] until he goes loose.",
            acts = setOf("massage_scalp"),
            timers = listOf(tm("Scalp", 90))
        ),
        c(
            id = "cn_mass_jaw", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Jaw and face",
            base = "{G} kneads {R+} jaw, temples and cheekbones [adv_pressure_light] with his thumbs.",
            explicit = "{G} holds {R+} face and kneads his jaw, temples and cheekbones [adv_pressure_light] with both thumbs.",
            acts = setOf("massage_jaw_face"),
            timers = listOf(tm("Jaw", 45), tm("Temples", 45))
        ),
        c(
            id = "cn_mass_neck_shoulders", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Neck and shoulders",
            base = "{G} kneads {R+} neck and both shoulders [adv_pressure_firm], thumbs either side of the spine.",
            explicit = "{G} [v_knead] {R+} neck and shoulders [adv_pressure_firm], thumbs either side of the spine, until the muscle gives.",
            acts = setOf("massage_neck_shoulders"),
            timers = listOf(tm("Neck", 60), tm("Left shoulder", 45), tm("Right shoulder", 45))
        ),
        c(
            id = "cn_mass_upper_back", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Upper back",
            base = "{G} kneads {R+} upper back [adv_pressure_deep], following the inside edge of each shoulder blade.",
            explicit = "{G} [v_knead] {R+} upper back [adv_pressure_deep], digging along the inside edge of each shoulder blade.",
            acts = setOf("massage_upper_back"),
            timers = listOf(tm("Left shoulder blade", 60), tm("Right shoulder blade", 60))
        ),
        c(
            id = "cn_mass_lower_back", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Lower back",
            base = "{G} kneads {R+} lower back [adv_pressure_firm] with the heels of both hands.",
            explicit = "{G} puts the heels of both hands into {R+} lower back and presses it [adv_pressure_deep].",
            acts = setOf("massage_lower_back"),
            timers = listOf(tm("Left side", 60), tm("Right side", 60))
        ),
        c(
            id = "cn_mass_arms_hands", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Arms and hands",
            base = "{G} kneads each of {R+} arms from shoulder to wrist, then the palm and every finger.",
            explicit = "{G} kneads each of {R+} arms shoulder to wrist [adv_pressure_firm], then opens the palm and pulls every finger.",
            acts = setOf("massage_arms_hands"),
            timers = listOf(tm("Left arm", 45), tm("Left hand", 30), tm("Right arm", 45), tm("Right hand", 30))
        ),
        c(
            id = "cn_mass_buttocks_hips", intensity = 2, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Buttocks and hips",
            base = "{G} kneads {R+} buttocks and hips [adv_pressure_deep], one side at a time.",
            explicit = "{G} takes a full handful of each of {R+} cheeks and [v_knead] them [adv_pressure_deep], one side at a time.",
            acts = setOf("massage_buttocks_hips"),
            timers = listOf(tm("Left hip", 45), tm("Right hip", 45), tm("Left cheek", 45), tm("Right cheek", 45))
        ),
        c(
            id = "cn_mass_anus", intensity = 3, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Opening him up from the outside",
            base = "{G} uses #lubricant# and rubs the ring of muscle around {R+} hole [adv_pressure_light], without going in.",
            explicit = "{G} slicks {R} with #lubricant# and rubs the muscle around his hole [adv_pressure_light] until it starts to give, and still does not go in.",
            acts = setOf("massage_anus", "anal_external"),
            equip = setOf(Equipment.LUBRICANT),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Rubbing the muscle", 120))
        ),
        c(
            id = "cn_mass_inner_thighs", intensity = 2, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Inner thighs",
            base = "{G} kneads each of {R+} inner thighs from the knee up, stopping short of the groin.",
            explicit = "{G} kneads each of {R+} inner thighs from the knee up [adv_pressure_firm] and stops just short of his groin every time.",
            acts = setOf("massage_inner_thighs"),
            timers = listOf(tm("Left inner thigh", 60), tm("Right inner thigh", 60))
        ),
        c(
            id = "cn_mass_calves_feet", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Calves and feet",
            base = "{G} kneads each of {R+} calves, then the arch and heel of each foot with both thumbs.",
            explicit = "{G} kneads each of {R+} calves [adv_pressure_deep], then digs both thumbs into each arch and heel.",
            acts = setOf("massage_calves_feet"),
            blocks = setOf(Boundary.NO_FOOT_PLAY),
            timers = listOf(tm("Left calf", 45), tm("Left foot", 45), tm("Right calf", 45), tm("Right foot", 45))
        ),
        c(
            id = "cn_mass_groin", intensity = 3, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Groin",
            base = "{G} kneads the hip creases and the area around {R+} cock without touching the shaft.",
            explicit = "{G} rubs {R+} hip creases and everything around his cock and deliberately never touches the shaft.",
            acts = setOf("massage_groin"),
            blocks = setOf(Boundary.MASSAGE_NONSEXUAL),
            timers = listOf(tm("Left hip crease", 45), tm("Right hip crease", 45), tm("Around the base", 60))
        ),
        c(
            id = "cn_mass_oil", intensity = 2, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Oiled back",
            base = "{G} warms #massage_oil# and kneads {R+} whole back in long strokes, shoulders to waist.",
            explicit = "{G} warms #massage_oil#, pours it down {R+} spine and kneads his whole back in long, greedy strokes.",
            acts = setOf("massage_oil", "massage_general"),
            equip = setOf(Equipment.MASSAGE_OIL),
            timers = listOf(tm("Shoulders", 60), tm("Mid back", 60), tm("Lower back", 60))
        ),
        c(
            id = "cn_mass_candle", intensity = 2, family = F_MASSAGE, cats = setOf(MASSAGE, SENSORY),
            title = "Massage candle",
            base = "{G} drips warm wax from #massage_candle# along {R+} back, then presses it in [adv_pressure_firm].",
            explicit = "{G} tips #massage_candle# down {R+} spine, listens to him react, then presses the wax in [adv_pressure_firm].",
            acts = setOf("massage_candle", "warm_wax"),
            equip = setOf(Equipment.MASSAGE_CANDLE),
            timers = listOf(tm("Wax", 30), tm("Pushing it in", 90))
        ),
        c(
            id = "cn_mass_wand", intensity = 2, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Massage wand",
            base = "{G} runs #massage_wand# over {R+} shoulders, spine and lower back [adv_toy_pace].",
            explicit = "{G} runs #massage_wand# over {R+} shoulders, spine and lower back [adv_toy_pace] and holds it on every spot that makes him swear.",
            acts = setOf("massage_wand"),
            equip = setOf(Equipment.MASSAGE_WAND),
            timers = listOf(tm("Shoulders", 60), tm("Spine", 45), tm("Lower back", 60))
        ),
        c(
            id = "cn_mass_reciprocal", intensity = 1, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Massage each other",
            base = "{G} and {R} knead each other's necks and shoulders at the same time, sitting face to face.",
            explicit = "{G} and {R} sit face to face and knead each other's necks and shoulders at once, and neither stops first.",
            acts = setOf("massage_neck_shoulders"), mutual = true,
            timers = listOf(tm("Both at once", 120))
        ),
        c(
            id = "cn_mass_four_areas", intensity = 2, family = F_MASSAGE, cats = setOf(MASSAGE),
            title = "Four areas, timed",
            base = "{G} kneads four areas of {R+} body in turn: shoulders, lower back, buttocks and each thigh.",
            explicit = "{G} handles {R} in four timed passes — shoulders, lower back, ass, then each thigh — and does not cut any of them short.",
            acts = setOf("massage_general", "massage_neck_shoulders", "massage_lower_back"),
            timers = listOf(tm("Shoulders", 60), tm("Lower back", 45), tm("Left thigh", 30), tm("Right thigh", 30))
        ),

        // ------------------------------------------------------------- kissing (8)
        c(
            id = "cn_kiss_slow", intensity = 1, family = F_KISS, cats = setOf(KISSING),
            title = "Slow making out",
            base = "{G} kisses {R} [adv_kiss_deep] and keeps his hands on {R+} face and neck only.",
            explicit = "{G} kisses {R} [adv_kiss_deep] with both hands on his face and neck and nowhere else at all.",
            acts = setOf("long_making_out"),
            timers = listOf(tm("Making out", 120))
        ),
        c(
            id = "cn_kiss_tongue", intensity = 1, family = F_KISS, cats = setOf(KISSING),
            title = "Tongue-heavy making out",
            base = "{G} kisses {R} [adv_kiss_deep], leading with his tongue throughout.",
            explicit = "{G} kisses {R} [adv_kiss_deep], all tongue, and leads throughout while {R} follows.",
            acts = setOf("tongue_kissing"),
            timers = listOf(tm("Kissing", 120))
        ),
        c(
            id = "cn_kiss_hard", intensity = 2, family = F_KISS, cats = setOf(KISSING),
            title = "Hard kissing",
            base = "{G} takes {R+} face in both hands and kisses him [adv_kiss] without letting him pull back.",
            explicit = "{G} takes {R+} face in both hands, kisses him [adv_kiss] and does not let him pull away until he is finished.",
            acts = setOf("hard_kissing"),
            timers = listOf(tm("Kissing", 90))
        ),
        c(
            id = "cn_kiss_mouth_neck_chest", intensity = 1, family = F_KISS, cats = setOf(KISSING),
            title = "Mouth, neck, chest",
            base = "{G} uses {R+} mouth, then his neck, then his chest with his mouth, one minute each.",
            explicit = "{G} uses {R+} mouth, neck and chest in turn [adv_kiss], a minute each, and does not rush any of them.",
            acts = setOf("tongue_kissing", "neck_kissing", "chest_nipple_kissing"),
            timers = listOf(tm("Mouth", 60), tm("Neck", 60), tm("Chest", 60))
        ),
        c(
            id = "cn_kiss_inner_thigh", intensity = 2, family = F_KISS, cats = setOf(KISSING),
            title = "Inner-thigh kisses",
            base = "{G} kisses up each of {R+} inner thighs and stops short of the groin each time.",
            explicit = "{G} kisses and [v_bite] his way up each of {R+} inner thighs and stops dead short of his groin every single time.",
            acts = setOf("inner_thigh_kissing"),
            timers = listOf(tm("Left inner thigh", 60), tm("Right inner thigh", 60))
        ),
        c(
            id = "cn_kiss_chest_nipples", intensity = 2, family = F_KISS, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Chest and nipple worship",
            base = "{G} kisses {R+} chest and sucks each nipple [adv_kiss], alternating between them.",
            explicit = "{G} kisses {R+} chest and sucks his nipples [adv_kiss], sucking one while his fingers pinch the other.",
            acts = setOf("chest_nipple_kissing", "nipple_stimulation"),
            timers = listOf(tm("Left nipple", 60), tm("Right nipple", 60))
        ),
        c(
            id = "cn_kiss_body_trail", intensity = 2, family = F_KISS, cats = setOf(KISSING, BODY_WORSHIP),
            title = "Body trail",
            base = "{G} lays [n_kiss_trail] from {R+} mouth down to his hips without skipping anything.",
            explicit = "{G} lays [n_kiss_trail] from {R+} mouth all the way to his hips and skips nothing on the way down.",
            acts = setOf("neck_kissing", "chest_nipple_kissing", "stomach_hip_kissing"),
            timers = listOf(tm("Mouth and neck", 45), tm("Chest", 45), tm("Stomach and hips", 45))
        ),
        c(
            id = "cn_kiss_reciprocal", intensity = 1, family = F_KISS, cats = setOf(KISSING),
            title = "Kissing each other",
            base = "{G} and {R} take turns leading the kiss, ninety seconds each, and the one following keeps still.",
            explicit = "{G} and {R} take turns running the kiss, ninety seconds each, and whoever is following keeps completely still.",
            acts = setOf("long_making_out"), mutual = true,
            timers = listOf(tm("{G} leads", 90), tm("{R} leads", 90))
        ),

        // ------------------------------------------------------------ ear play (15)
        c(
            id = "cn_ear_one", intensity = 1, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "One ear",
            base = "{G} sucks one of {R+} ear — outer edge first, then the lobe.",
            explicit = "{G} takes one of {R+} ears in his mouth, rubs the outer edge, then sucks the lobe and holds it.",
            acts = setOf("ear_play"),
            timers = listOf(tm("Outer edge", 45), tm("Lobe", 45))
        ),
        c(
            id = "cn_ear_both", intensity = 1, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Both ears",
            base = "{G} sucks both of {R+} ears in turn, holding his head still by the jaw.",
            explicit = "{G} holds {R+} head still by the jaw and sucks both ears in turn, and he is not allowed to turn away.",
            acts = setOf("ear_play"),
            timers = listOf(tm("Left ear", 60), tm("Right ear", 60))
        ),
        c(
            id = "cn_ear_licking", intensity = 2, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Licking his ears",
            base = "{G} [v_tongue] the edge and inside of each of {R+} ears and goes nowhere else.",
            explicit = "{G} [v_tongue] the edge and inside of each of {R+} ears, wet and slow, and goes nowhere else at all.",
            acts = setOf("ear_play"),
            timers = listOf(tm("Left ear", 45), tm("Right ear", 45))
        ),
        c(
            id = "cn_ear_praise", intensity = 1, family = F_EAR, cats = setOf(EAR_PLAY, LANGUAGE),
            title = "Whispered praise",
            base = "{G} sucks {R+} ear and, between passes, [tone_whisper] into it and [v_praise].",
            explicit = "{G} sucks {R+} ear and, in the gaps, [tone_whisper] into it and [v_praise] close enough for him to feel every word.",
            acts = setOf("ear_play", "explicit_praise"),
            timers = listOf(tm("Ear and whispering", 90))
        ),
        c(
            id = "cn_ear_instructions", intensity = 3, family = F_EAR, cats = setOf(EAR_PLAY, LANGUAGE),
            title = "Explicit instructions in his ear",
            base = "{G} sucks {R+} ear and [v_talk_dirty], spelling out exactly what he intends to do to him later.",
            explicit = "{G} sucks {R+} ear and [v_talk_dirty], spelling out in detail exactly what he is going to do to him later.",
            acts = setOf("ear_play", "dirty_talk"),
            timers = listOf(tm("Talking at his ear", 120))
        ),
        c(
            id = "cn_ear_moaning", intensity = 2, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Moaning into his ear",
            base = "{G} presses his mouth to {R+} ear and [tone_moan] into it, hands flat on {R+} chest.",
            explicit = "{G} puts his mouth on {R+} ear and [tone_moan] straight into it with both hands flat on his chest.",
            acts = setOf("ear_play"),
            timers = listOf(tm("Moaning", 60))
        ),
        c(
            id = "cn_ear_growling", intensity = 3, family = F_EAR, cats = setOf(EAR_PLAY, LANGUAGE),
            title = "Growling into his ear",
            base = "{G} sucks {R+} ear and [tone_whisper] into it low enough that {R} feels it more than hears it.",
            explicit = "{G} sucks {R+} ear and [tone_whisper] into it so low that he feels it more than he hears it.",
            acts = setOf("ear_play", "dirty_talk"),
            timers = listOf(tm("Growling", 60))
        ),
        c(
            id = "cn_ear_neck", intensity = 1, family = F_EAR, cats = setOf(EAR_PLAY, KISSING),
            title = "Ear and neck",
            base = "{G} alternates between {R+} ear and the side of his neck, never staying long in one place.",
            explicit = "{G} swaps between {R+} ear and the side of his neck constantly so he can never settle into either.",
            acts = setOf("ear_play", "neck_kissing"),
            timers = listOf(tm("Left ear and neck", 60), tm("Right ear and neck", 60))
        ),
        c(
            id = "cn_ear_reciprocal", intensity = 1, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Ear play on each other",
            base = "{G} and {R} lie face to face on their sides and suck each other's ears at the same time.",
            explicit = "{G} and {R} lie face to face and suck each other's ears at once, and neither one stops first.",
            acts = setOf("ear_play"), mutual = true,
            timers = listOf(tm("Both at once", 90))
        ),
        c(
            id = "cn_ear_with_massage", intensity = 2, family = F_EAR, cats = setOf(EAR_PLAY, MASSAGE),
            title = "Ear play during a massage",
            base = "{G} sucks {R+} shoulders and leans down to suck his ear without stopping his hands.",
            explicit = "{G} keeps kneading {R+} shoulders [adv_pressure_firm] and sucks his ear at the same time. His hands do not stop.",
            acts = setOf("ear_play", "massage_neck_shoulders"),
            timers = listOf(tm("Shoulders only", 45), tm("Shoulders and ear", 90))
        ),
        c(
            id = "cn_ear_with_oral", intensity = 3, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Ear play, then his mouth lower down",
            base = "{G} sucks {R+} ear for a minute, then moves his way down and [v_suck] him, then comes back up to the ear.",
            explicit = "{G} sucks {R+} ear for a minute, goes down and [v_suck] him, then comes straight back up to the ear.",
            acts = setOf("ear_play", "oral"),
            timers = listOf(tm("Ear", 60), tm("Mouth lower down", 90), tm("Back to the ear", 60))
        ),
        c(
            id = "cn_ear_with_fingering", intensity = 4, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Ear play while he is fingered",
            base = "{G} sucks {R+} ear while he [v_finger] him with #lubricant#, both at the same rhythm.",
            explicit = "{G} sucks {R+} ear while he [v_finger] him with #lubricant#, both moving in the same rhythm.",
            acts = setOf("ear_play", "fingering"), anal = true,
            equip = setOf(Equipment.LUBRICANT),
            timers = listOf(tm("Ear only", 45), tm("Ear and fingers", 120))
        ),
        c(
            id = "cn_ear_with_toy", intensity = 4, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Ear play with a toy on him",
            base = "{G} sucks {R+} ear while using #TOY# on him.",
            explicit = "{G} sucks {R+} ear and runs #TOY# on him at the same time, and neither stops.",
            acts = setOf("ear_play"), toy = true,
            timers = listOf(tm("Ear and toy", 120))
        ),
        c(
            id = "cn_ear_with_restraint", intensity = 4, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Ear play while he is held",
            base = "{R} is restrained with #cuffs# and {G} sucks both of {R+} ears for the full time.",
            explicit = "{R} is locked in #cuffs# and {G} sucks both of {R+} ears for the whole time, and he cannot do a thing about it.",
            acts = setOf("ear_play", "restraint"),
            equip = setOf(Equipment.CUFFS),
            blocks = setOf(Boundary.NO_RESTRAINTS),
            timers = listOf(tm("Left ear", 60), tm("Right ear", 60))
        ),
        c(
            id = "cn_ear_rough", intensity = 5, family = F_EAR, cats = setOf(EAR_PLAY),
            title = "Ear play while he is held down",
            base = "{G} [v_pin] {R} and sucks his ear while he stays pinned, [tone_whisper] into it throughout.",
            explicit = "{G} [v_pin] {R} down and sucks his ear while he stays pinned, [tone_whisper] into it the whole time.",
            acts = setOf("ear_play", "pinning"),
            blocks = setOf(Boundary.NO_ROUGH_SEX),
            timers = listOf(tm("Pinned and stroked", 120))
        )
    )
}
