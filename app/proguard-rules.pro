# kotlinx.serialization — keep generated serializers for content models and nav routes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class com.kairos.app.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.kairos.app.**$$serializer { *; }

# Tink (via androidx.security-crypto) references compile-time-only annotations.
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
# OkHttp optional platform integrations.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
