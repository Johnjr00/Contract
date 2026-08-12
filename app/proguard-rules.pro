# kotlinx.serialization keeps its generated serializers via companion objects.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisible*Annotations
-dontnote kotlinx.serialization.**

-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.thecontract.core.**$$serializer { *; }
-keepclassmembers class com.thecontract.core.** {
    *** Companion;
}

# NanoHTTPD reflects on nothing, but its optional SSL and temp-file plumbing references
# classes that may be absent.
-dontwarn fi.iki.elonen.**
-keep class fi.iki.elonen.** { *; }

# ZXing core is pure Java and fully reachable, but its reflective format lookups are safest kept.
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# sherpa-onnx reads these classes from C++, by name, and nothing in Kotlin references the fields
# it looks up — so R8 sees them as free to rename and does.
#
# `newFromFile` walks the config object with GetFieldID/GetObjectField. A renamed field makes that
# lookup return null, and using a null field id is not a Java exception a caller could catch: the
# runtime aborts the whole process with SIGABRT. On a release build that is a crash a few seconds
# after launch, as soon as the voice is loaded, with no stack trace in the app's own code — which
# is what made it look for a long time like a fault in the narration thread rather than in the
# build. Debug builds never showed it because minification is off there.
#
# The whole package is kept rather than the fields alone: every config class is walked the same
# way, `GeneratedAudio` is constructed from native code, and the next model type added would
# otherwise reintroduce this silently.
-keep class com.k2fsa.sherpa.onnx.** { *; }
-keepclasseswithmembernames,includedescriptorclasses class com.k2fsa.sherpa.onnx.** {
    native <methods>;
}

# Room generated implementations.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-dontwarn androidx.room.paging.**

# Release builds must not log private profile answers or contract text. android.util.Log calls
# are stripped from release output entirely.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}
