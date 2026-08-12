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
