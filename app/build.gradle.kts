import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/**
 * Release signing is configured from `keystore.properties` in the project root, which is
 * git-ignored. Without it the release variant still assembles, unsigned, so the build never
 * depends on a secret being present. See BUILD.md for the signing procedure.
 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}
val hasReleaseSigning = keystoreProperties.getProperty("storeFile") != null

/**
 * Commits on the current branch. Falls back to 1 when git is unavailable — a source archive
 * still builds, it simply cannot tell one build from another.
 */
val buildNumber: Int = runCatching {
    val p = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()
    val out = p.inputStream.bufferedReader().readText().trim()
    p.waitFor()
    out.toInt()
}.getOrDefault(1)

android {
    namespace = "com.thecontract.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.thecontract.tv"
        // API 26 covers every Nvidia Shield TV that can run current Shield Experience releases,
        // and is the level at which notification channels and foreground services exist.
        minSdk = 26
        targetSdk = 35
        // Every build must carry a higher versionCode than the one before it. A launcher caches
        // an app's banner, icon and label keyed by package and only re-reads them when the
        // version changes, so shipping several different builds all as versionCode 1 leaves a
        // television showing the artwork from whichever one it happened to see first. Commit
        // count rises monotonically and is reproducible from the source tree.
        versionCode = buildNumber
        versionName = "1.0.$buildNumber"
        resourceConfigurations += listOf("en")
        // The Shield and every other current Android TV box are 64-bit ARM. Shipping the other
        // three ABIs of the speech runtime would add about 90 MB to an already large artifact
        // for hardware this app does not run on.
        ndk { abiFilters += "arm64-v8a" }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            // Verbose logging is compiled out of release builds entirely (section 11).
            buildConfigField("boolean", "VERBOSE_LOGGING", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "VERBOSE_LOGGING", "false")
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        // ONNX Runtime and sherpa-onnx are 24 MB of arm64 native library uncompressed, which the
        // modern default stores in the APK verbatim so it can be mapped straight from it. Storing
        // them compressed instead costs a one-time extraction at install and takes the APK from
        // 33.0 MiB to 17.7 MiB — the difference between a build that can be handed over and one
        // that cannot, which for a sideloaded app matters more than a few milliseconds of launch.
        jniLibs { useLegacyPackaging = true }
        resources {
            excludes += setOf(
                "META-INF/*.kotlin_module",
                "META-INF/AL2.0",
                "META-INF/LGPL2.1",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*"
            )
        }
    }

    sourceSets["main"].java.srcDirs("src/main/kotlin")
    sourceSets["test"].java.srcDirs("src/test/kotlin")

    lint {
        abortOnError = false
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // VoiceModelManifestTest checks the checked-in model against what the app expects to
    // download, and has to find it from wherever Gradle runs the tests.
    systemProperty("contract.rootDir", rootDir.absolutePath)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
