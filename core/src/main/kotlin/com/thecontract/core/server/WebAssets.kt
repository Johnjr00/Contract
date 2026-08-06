package com.thecontract.core.server

import java.io.InputStream

/**
 * Phone-controller assets, bundled inside the artifact.
 *
 * Every byte the phone loads — HTML, CSS, JavaScript — comes from here. There is no CDN, no
 * remote font, no remote script and no runtime download anywhere in the controller.
 */
object WebAssets {

    private const val ROOT = "/web"

    /** Files that may be served. An explicit list keeps path handling trivially safe. */
    val manifest: List<String> = listOf(
        "controller.html",
        "controller.css",
        "controller.js"
    )

    fun open(path: String): InputStream? {
        val clean = path.trim('/')
        if (clean !in manifest) return null
        return WebAssets::class.java.getResourceAsStream("$ROOT/$clean")
    }

    fun read(path: String): String? = open(path)?.use { it.readBytes().decodeToString() }

    /** True when every declared asset is actually present in the artifact. */
    fun verifyBundled(): List<String> = manifest.filter { open(it) == null }
}
