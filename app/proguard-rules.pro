# Add project specific ProGuard rules here.
# Gemini SDK
-keep class com.google.ai.client.generativeai.** { *; }
# Markwon
-keep class io.noties.markwon.** { *; }
-keep class org.commonmark.** { *; }
# Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**
# Jsoup
-keeppackagenames org.jsoup.nodes
-keep class org.jsoup.** { *; }
# Apache POI
-keep class org.apache.poi.** { *; }
-dontwarn org.apache.poi.**
# EpubLib
-keep class nl.siegmann.epublib.** { *; }
# ML Kit
-keep class com.google.mlkit.** { *; }
# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.selfproject.learningapp.**$$serializer { *; }
-keepclassmembers class com.selfproject.learningapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.selfproject.learningapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}
