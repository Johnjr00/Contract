/*
 * The Contract - Android TV app with local-network phone controllers.
 *
 * Module layout
 * -------------
 *   :core  Pure-JVM Kotlin. Content library, explicitness/style engine, rules engine,
 *          server-authoritative state machine, embedded HTTP + WebSocket server,
 *          wire protocol, timer engine, phone-controller web assets.
 *          Depends only on Maven Central artifacts, so it compiles and its full test
 *          suite (including real end-to-end multiplayer games over real sockets) runs
 *          on any JDK 17+ machine with no Android SDK installed.
 *
 *   :app   Android TV application. Compose-for-TV UI, Room persistence, foreground
 *          service that hosts :core's server, QR rendering, network-interface
 *          detection, boot receiver, Android Keystore encryption.
 *          Requires the Android SDK and Google Maven.
 *
 * :app is only wired into the build when an Android SDK is actually present, so that
 * `./gradlew :core:test` works in a bare JVM environment. See README.md.
 */

pluginManagement {
    val androidSdkDir: String? = run {
        val props = java.util.Properties()
        val f = file("local.properties")
        if (f.exists()) f.inputStream().use(props::load)
        props.getProperty("sdk.dir")
            ?: System.getenv("ANDROID_HOME")
            ?: System.getenv("ANDROID_SDK_ROOT")
    }
    val hasAndroidSdk = androidSdkDir != null && file(androidSdkDir).isDirectory
    gradle.extra["hasAndroidSdk"] = hasAndroidSdk

    repositories {
        if (hasAndroidSdk) {
            google {
                content {
                    includeGroupByRegex("com\\.android.*")
                    includeGroupByRegex("com\\.google.*")
                    includeGroupByRegex("androidx.*")
                }
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (gradle.extra["hasAndroidSdk"] as Boolean) {
            google {
                content {
                    includeGroupByRegex("com\\.android.*")
                    includeGroupByRegex("com\\.google.*")
                    includeGroupByRegex("androidx.*")
                }
            }
        }
        mavenCentral()
    }
}

rootProject.name = "TheContract"

include(":core")

if (gradle.extra["hasAndroidSdk"] as Boolean) {
    include(":app")
} else {
    logger.lifecycle(
        "[TheContract] No Android SDK found (local.properties sdk.dir / ANDROID_HOME / " +
            "ANDROID_SDK_ROOT). Configuring :core only. Install the SDK to build the TV app."
    )
}
