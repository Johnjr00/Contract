package com.thecontract.tv.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encryption for everything written to disk (section 42).
 *
 * The game holds private sexual preferences, so the persisted payload is encrypted with
 * AES-256-GCM under a key that is generated inside, and never leaves, the Android Keystore.
 * The key material is not extractable even with root access to the app's data directory.
 *
 * Each record carries its own random 12-byte IV, prefixed to the ciphertext.
 */
object Crypto {

    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "the-contract-session-key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    private fun keyStore(): KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    private fun existingKey(): SecretKey? =
        (keyStore().getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey

    private fun createKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                // No user authentication requirement: the television must be able to restore an
                // unfinished session after a reboot without anyone unlocking anything.
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private val key: SecretKey by lazy { existingKey() ?: createKey() }

    fun encrypt(plaintext: String): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        check(iv.size == IV_BYTES) { "unexpected GCM IV length ${iv.size}" }
        val body = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return iv + body
    }

    fun decrypt(payload: ByteArray): String? {
        if (payload.size <= IV_BYTES) return null
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                key,
                GCMParameterSpec(TAG_BITS, payload, 0, IV_BYTES)
            )
            cipher.doFinal(payload, IV_BYTES, payload.size - IV_BYTES).toString(Charsets.UTF_8)
        }.getOrNull()
    }
}
