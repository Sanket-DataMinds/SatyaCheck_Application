# ULTRA-SAFE KOTLIN RULES - MAXIMUM STABILITY CONFIGURATION
# Keeps everything Kotlin-related to prevent any reflection or serialization crashes

# Keep absolutely everything Kotlin-related
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }
-keep enum kotlin.** { *; }
-dontwarn kotlin.**

# Keep all Kotlin reflection 
-keep class kotlin.reflect.** { *; }
-dontwarn kotlin.reflect.**

# Keep all coroutines
-keep class kotlinx.** { *; }
-dontwarn kotlinx.**

# Keep all JetBrains annotations
-keep class org.jetbrains.** { *; }
-dontwarn org.jetbrains.**

# Preserve ALL metadata and annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile
-keepattributes LineNumberTable
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes MethodParameters

# MOSHI JSON SERIALIZATION RULES - Required for API calls with Kotlin reflection
-keep class com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
    @com.squareup.moshi.* <fields>;
}
-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}
-keepnames class com.squareup.moshi.**
-keepclassmembers class * {
  @com.squareup.moshi.Json <fields>;
}

# Moshi Kotlin reflection adapter - CRITICAL for ArticleRepository
-keep class com.squareup.moshi.kotlin.** { *; }
-keep class com.squareup.moshi.kotlin.reflect.** { *; }

# DATASTORE RULES - Required to prevent app crash at startup
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.core.** { *; }
-keep class androidx.datastore.preferences.** { *; }
-keep class androidx.datastore.preferences.core.** { *; }
-dontwarn androidx.datastore.**

# Keep DataStore serializers and related classes
-keep class * extends androidx.datastore.core.Serializer { *; }
-keep class * extends androidx.datastore.preferences.core.Preferences { *; }
-keepclassmembers class androidx.datastore.preferences.core.Preferences { *; }

# Keep file operations for DataStore
-keep class java.nio.file.** { *; }
-dontwarn java.nio.file.**

# Keep the application class and its methods
-keep public class com.satyacheck.android.SatyaCheckApplication {
    public <init>();
    public void onCreate();
}

# Keep all Fragment and Activity classes
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# Keep all ViewModels
-keep public class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep the Hilt entry points
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.AndroidEntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.HiltAndroidApp class *

# Keep annotations for Hilt
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.** class *

# Keep Room Database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Keep Retrofit service interfaces
-keep,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}

# Keep data models (adjust the packages as needed)
-keep class com.satyacheck.android.data.model.** { *; }
-keep class com.satyacheck.android.domain.model.** { *; }
-keep class com.satyacheck.android.data.local.entity.** { *; }

# Prevent obfuscation of native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Gson-specific rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Kotlin specific optimizations
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    static void throwUninitializedPropertyAccessException(java.lang.String);
}

# OkHttp specific rules
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Retrofit specific rules
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}

# Gemini API rules
-keep class com.google.ai.client.generativeai.** { *; }

# ML Kit specific rules
-keep class com.google.mlkit.** { *; }

# Media3 specific rules
-keep class androidx.media3.** { *; }

# Remove debug logs in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# General optimization flags
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep important metadata for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep native libraries
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# Preserve the special static methods that are required in all enumeration classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes (adjust package as needed)
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
    <fields>;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.satyacheck.android.**$$serializer { *; }
-keepclassmembers class com.satyacheck.android.** {
    *** Companion;
}
-keepclasseswithmembers class com.satyacheck.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Remove all logging code for production
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# Compose specific rules
-keep class androidx.compose.** { *; }

# Specific optimization rules for faster app startup
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# Advanced size optimization rules
# Removed -dontshrink and -dontoptimize for better size reduction
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Remove unused resources more aggressively
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNull(java.lang.Object);
    public static void checkNotNull(java.lang.Object, java.lang.String);
    public static void checkNotNullParameter(java.lang.Object, java.lang.String);
    public static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    public static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
    public static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
}

# Aggressive removal of unused code paths
-assumenosideeffects class java.lang.StringBuilder {
    public java.lang.StringBuilder();
    public java.lang.StringBuilder(int);
    public java.lang.StringBuilder(java.lang.String);
    public java.lang.StringBuilder append(java.lang.Object);
    public java.lang.StringBuilder append(java.lang.String);
    public java.lang.StringBuilder append(java.lang.StringBuffer);
    public java.lang.StringBuilder append(char[]);
    public java.lang.StringBuilder append(char[], int, int);
    public java.lang.StringBuilder append(boolean);
    public java.lang.StringBuilder append(char);
    public java.lang.StringBuilder append(int);
    public java.lang.StringBuilder append(long);
    public java.lang.StringBuilder append(float);
    public java.lang.StringBuilder append(double);
    public java.lang.String toString();
}

# Remove all test and benchmark code from production builds
-assumenosideeffects class * {
    @org.junit.Test *;
    @androidx.benchmark.junit4.BenchmarkRule *;
}

# Optimize enum usage
-optimizations !code/simplification/enum

# Keep only necessary JNI methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# STABLE PRODUCTION PROGUARD RULES - HACKATHON WINNING CONFIGURATION
# Prioritizes app functionality and stability over extreme size optimization

# Keep the application class and its methods
-keep public class com.satyacheck.android.SatyaCheckApplication {
    public <init>();
    public void onCreate();
}

# Keep all Fragment and Activity classes
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.appcompat.app.AppCompatActivity

# Keep all ViewModels
-keep public class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep the Hilt entry points - ESSENTIAL FOR APP STARTUP
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.AndroidEntryPoint class *
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.HiltAndroidApp class *

# Keep annotations for Hilt - CRITICAL
-keep class javax.inject.** { *; }
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.** class *

# COMPREHENSIVE Room Database protection - NO CRASHES
-keep class androidx.room.** { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep ALL app data classes to prevent Room crashes
-keep class com.satyacheck.android.data.** { *; }
-keep class com.satyacheck.android.domain.** { *; }

# COMPREHENSIVE WorkManager protection
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Keep Retrofit service interfaces
-keep,allowobfuscation,allowshrinking interface * {
    @retrofit2.http.* <methods>;
}

# Prevent obfuscation of native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Gson-specific rules - SAFE CONFIGURATION
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# OkHttp specific rules
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Retrofit specific rules
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Gemini API rules
-keep class com.google.ai.client.generativeai.** { *; }

# ML Kit specific rules
-keep class com.google.mlkit.** { *; }

# Media3 specific rules
-keep class androidx.media3.** { *; }

# Keep important metadata for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep native libraries
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# Preserve the special static methods that are required in all enumeration classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
    <fields>;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Compose specific rules
-keep class androidx.compose.** { *; }

# SAFE optimization settings for stable production build
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 2
-allowaccessmodification
-dontpreverify

# Keep essential metadata
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,EnclosingMethod
