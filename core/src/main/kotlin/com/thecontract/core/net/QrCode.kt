package com.thecontract.core.net

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Offline QR generation. ZXing's core artifact is a pure-Java library bundled into the APK —
 * there is no network call and no external image service anywhere in the join flow.
 *
 * The matrix is produced once and shipped to the TV renderer as rows of `0`/`1`, so the same
 * data drives the Compose canvas on the TV and the fallback renderer in a browser.
 */
object QrCode {

    fun matrix(text: String, requestedSize: Int = 41): List<String> {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, requestedSize, requestedSize, hints)
        return (0 until bits.height).map { y ->
            buildString(bits.width) {
                for (x in 0 until bits.width) append(if (bits.get(x, y)) '1' else '0')
            }
        }
    }
}
