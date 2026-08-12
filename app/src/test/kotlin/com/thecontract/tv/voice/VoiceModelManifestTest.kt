package com.thecontract.tv.voice

import org.junit.jupiter.api.Test
import java.io.File
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Holds [VoiceModel] to the files it claims to describe.
 *
 * The model is no longer packaged, so nothing about an ordinary build touches it — which means a
 * mismatch between the manifest and `model/tts/` compiles, installs, runs, and fails for the
 * first time on somebody's television, an hour into a download, with "the voice download arrived
 * damaged". This is the only place that failure can be caught early, and it costs a hash of
 * 139 MB once per test run.
 *
 * It also stands in for the thing it cannot check: that [VoiceModel.COMMIT] still serves these
 * bytes. If the files here change and the pin does not move with them, this test goes red, which
 * is the prompt to update both together.
 */
class VoiceModelManifestTest {

    private val modelDir = File(System.getProperty("contract.rootDir") ?: "..", "model/tts")

    @Test
    fun `every file the app expects is present, the right size, and the right bytes`() {
        assertTrue(modelDir.isDirectory, "no model at ${modelDir.absolutePath}")

        VoiceModel.PARTS.forEach { part ->
            val file = File(modelDir, part.name)
            assertTrue(file.isFile, "${part.name} is missing from ${modelDir.absolutePath}")
            assertEquals(part.bytes, file.length(), "${part.name} is a different size than declared")
            assertEquals(part.sha256, sha256(file), "${part.name} is not the file the app will verify")
        }
    }

    /**
     * Nothing in the directory is left out of the manifest. A model that gained an eighth file
     * would otherwise download six-sevenths of itself and fail to load, and the missing piece
     * would not be obvious from either the manifest or the failure.
     */
    @Test
    fun `nothing in the directory is unaccounted for`() {
        val declared = VoiceModel.PARTS.map { it.name }.toSet()
        // The upstream licence travels with the model but is not loaded by the engine.
        val present = modelDir.listFiles().orEmpty().map { it.name }.toSet() - "LICENSE"
        assertEquals(emptySet(), present - declared, "files in model/tts that the app never fetches")
    }

    /** The pin is a full commit hash, not a branch name or an abbreviation that could move. */
    @Test
    fun `the download is pinned to an immutable commit`() {
        assertTrue(
            VoiceModel.COMMIT.matches(Regex("[0-9a-f]{40}")),
            "COMMIT must be a full 40-character hash so the bytes can never change: ${VoiceModel.COMMIT}"
        )
        assertTrue(VoiceModel.BASE_URL.startsWith("https://"), "the model must be fetched over HTTPS")
        assertTrue(VoiceModel.COMMIT in VoiceModel.BASE_URL, "the base URL does not use the pinned commit")
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                digest.update(buffer, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
