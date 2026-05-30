# Consumer-side proguard rules for eudi-lib-android-rqes-ui.
#
# Apps that consume this AAR and run R8 (including R8 fullMode under AGP 9+)
# pick these up automatically. They are the minimum needed to keep the SDK's
# kotlinx.serialization-based navigation configs round-tripping correctly under
# obfuscation.
#
# If you also need the SDK's annotations preserved (e.g. for your own
# reflection-based code), add `-keepattributes *Annotation*` in your own
# proguard-rules.pro.

# kotlinx.serialization generated $$serializer companions must survive R8.
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static **$Companion Companion;
    static **$* *;
}
-keep,includedescriptorclasses class **$$serializer { *; }
