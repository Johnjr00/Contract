package com.thecontract.tv.voice

/**
 * The seven files that make up the voice, and where to get them.
 *
 * The model is not in the APK. Bundled it made the build 152.6 MiB, which is past what any
 * available route could deliver; left out, the build is 17.7 MiB. So it is fetched once, on the
 * first television that needs it, and lives in the app's private storage from then on. Nothing
 * about the running game changes: after that one download the app is as offline as it ever was.
 *
 * The download source is this repository itself, pinned to [COMMIT]. That is a deliberate choice
 * over the upstream sherpa-onnx release, which publishes only a single `.tar.bz2` — Android has
 * no bzip2 decoder in the platform, so that route costs a compression library and turns seven
 * resumable downloads into one atomic 129 MB one. Seven plain files need nothing but
 * `HttpURLConnection`.
 *
 * Pinning to a commit rather than a branch matters twice over: the bytes can never change under
 * a half-finished download, and [sha256] can therefore be an assertion rather than a hope.
 */
object VoiceModel {

    /**
     * The commit that carries `model/tts/`. Pinned, so the files this app verifies against are
     * the exact files it fetches; if the model is ever replaced, this moves with it.
     */
    const val COMMIT = "51b132ef40e96572599b075dcd8dde2950759372"

    const val BASE_URL = "https://raw.githubusercontent.com/johnjr00/contract/$COMMIT/model/tts"

    /** Both the assets subdirectory and the private-storage directory are named this. */
    const val DIR_NAME = "tts"

    /**
     * One file of the model.
     *
     * [bytes] is checked before [sha256] only because it is free — a truncated download is the
     * common failure and catching it without hashing 139 MB is worth the extra field.
     */
    data class Part(val name: String, val bytes: Long, val sha256: String)

    // Named once, because these are used twice over — as the manifest to download and verify,
    // and as the paths handed to sherpa-onnx. A typo that only showed up in one of the two
    // places would be a runtime failure on a television rather than a compile error here.
    const val TTS_JSON = "tts.json"
    const val UNICODE_INDEXER = "unicode_indexer.bin"
    const val VOICE_STYLE = "voice.bin"
    const val DURATION_PREDICTOR = "duration_predictor.int8.onnx"
    const val VOCODER = "vocoder.int8.onnx"
    const val TEXT_ENCODER = "text_encoder.int8.onnx"
    const val VECTOR_ESTIMATOR = "vector_estimator.int8.onnx"

    /**
     * Smallest first. A download that dies partway then leaves the cheap files done and only the
     * large ones to retry, and progress moves visibly in the first seconds rather than sitting at
     * zero through a 78 MB file.
     */
    val PARTS = listOf(
        Part(TTS_JSON, 8253, "42078d3aef1cd43ab43021f3c54f47d2d75ceb4e75f627f118890128b06a0d09"),
        Part(UNICODE_INDEXER, 262144, "8402ca48e5189a8950138580b0fff64db6f072f24ac07cd54ba8b2fbb9883b30"),
        Part(VOICE_STYLE, 517168, "67d5209b0ee8ce6c74105ffbe12fe6a7628aea3b4ba2fcb308a4a67938a93ce8"),
        Part(DURATION_PREDICTOR, 3700147, "c3eb91414d5ff8a7a239b7fe9e34e7e2bf8a8140d8375ffb14718b1c639325db"),
        Part(VOCODER, 25991073, "e923d60f53f95eb1ce235f1dc33ec56d9c057823c96fa6f8acf98f32b0da6152"),
        Part(TEXT_ENCODER, 36416150, "c7befd5ea8c3119769e8a6c1486c4edc6a3bc8365c67621c881bbb774b9902ff"),
        Part(VECTOR_ESTIMATOR, 78400833, "20cd86fa5c6effedfda0e7cffe5b0569ca401c440a0c3a1d72bf39286c0db3fd")
    )

    val TOTAL_BYTES: Long = PARTS.sumOf { it.bytes }
}
