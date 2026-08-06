/*
 * Deliberately no `plugins { }` block here.
 *
 * The Kotlin Android plugin reflects on AGP types (com.android.build.gradle.api.BaseVariant),
 * so the two must be resolved into the same classloader. Declaring either of them on the root
 * build classpath splits them across scopes and application fails. Each module therefore
 * resolves its own plugins, which also keeps the Android Gradle Plugin off the classpath
 * entirely when no Android SDK is present and only :core is configured.
 */

tasks.register("validateContent") {
    group = "verification"
    description = "Runs the content validation suite (section 44 of the specification)."
    dependsOn(":core:test")
}
