# ProGuard / R8 rules for the release build.
#
# The release build type currently ships with `minifyEnabled false`, so none of
# these rules are applied yet. They are kept here so that turning shrinking on
# later does not immediately break the app.

# ---------------------------------------------------------------- Kotlin ----
-dontwarn kotlin.**

# Coroutines' internal service loader entries.
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# --------------------------------------------------- Jetpack Compose ----
# Compose ships its own consumer rules, but these keep the reflection-based
# helpers and the saved-state machinery intact when R8 runs.
-keepclassmembers class androidx.compose.runtime.** { *; }
-keep class androidx.compose.runtime.saveable.** { *; }
-dontwarn androidx.compose.**

# ------------------------------------------------------------ Firebase ----
# Firebase uses reflection to read the generated values from google-services.
-keep class com.samuel.farmos.BuildConfig { *; }
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# The Google Sign-In credential manager resolves its provider classes by name.
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn androidx.credentials.**

# ------------------------------------------------------------ CameraX ----
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# ---------------------------------------------------------- WorkManager ----
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**
