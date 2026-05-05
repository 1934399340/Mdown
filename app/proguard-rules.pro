# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Flexmark
-keep class com.vladsch.flexmark.** { *; }
-dontwarn com.vladsch.flexmark.**

# iText
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Compose
-dontwarn androidx.compose.**

# Preserve line number info for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep data classes used in serialization
-keep class com.markdowneditor.data.model.** { *; }
-keep class com.markdowneditor.viewModel.**$Companion { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Suppress warnings for java.awt classes (not available on Android, referenced by iText/pdfbox)
-dontwarn java.awt.**
-dontwarn javax.imageio.**
-dontwarn org.slf4j.impl.**
