# Benchmark proguard rules - used for performance testing builds

# Keep all classes in the app package
-keep class com.satyacheck.android.** { *; }

# Keep Benchmark classes
-keep class androidx.benchmark.** { *; }

# Don't obfuscate but do shrink resources and optimize
-dontobfuscate

# These options allow benchmarks to be as efficient as possible
# while still maintaining reasonable debugging capabilities
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
