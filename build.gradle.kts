plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

tasks.register("validateContent") {
    group = "verification"
    description = "Runs the content validation suite (section 44 of the specification)."
    dependsOn(":core:test")
}
