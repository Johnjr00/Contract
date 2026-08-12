plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

application {
    // Runs the embedded server standalone for manual testing: ./gradlew :core:run
    mainClass.set("com.thecontract.core.devserver.DevServerMainKt")
}

// Java 17 bytecode: required by D8/R8 when :app packages this module into the APK, while
// still building on any JDK 17+ toolchain (this environment has 21).
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.coroutines.core)
    api(libs.nanohttpd)
    api(libs.nanohttpd.websocket)
    api(libs.zxing.core)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    maxHeapSize = "2g"
    // Lets the readout generators be pointed somewhere other than the module's build directory:
    //   ./gradlew :core:test --tests '*QuickPlaythroughsReadout' -Dcontract.readout=docs/x.txt
    providers.systemProperty("contract.readout").orNull?.let { systemProperty("contract.readout", it) }
    systemProperty("contract.rootDir", rootDir.absolutePath)
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
