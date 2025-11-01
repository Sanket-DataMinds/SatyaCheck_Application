plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"
}

android {
    namespace = "com.satyacheck.android"
    compileSdk = 34
    
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = false
        checkReleaseBuilds = false  // Skip lint checks for release builds
        disable += listOf("MissingTranslation", "ExtraTranslation")  // Ignore some lint checks
    }
    
    // Enable App Bundle optimization features
    bundle {
        language {
            // Split by language to reduce APK size
            enableSplit = true
        }
        density {
            // Split by screen density
            enableSplit = true
        }
        abi {
            // Split by CPU architecture
            enableSplit = true
        }
    }
    
    // Enable APK splitting for direct APK builds  
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a")  // Focus on most common architectures
            isUniversalApk = false  // Don't generate universal APK to save space
        }
        // Removed density splits as they're deprecated - use App Bundle instead
    }

    defaultConfig {
        applicationId = "com.satyacheck.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // Resource filtering for size optimization - use modern API
        // resourceConfigurations.addAll(listOf("en", "hi", "es", "fr", "de", "pt", "ru", "zh", "ar", "ja"))
        
        // Native library filtering handled by splits configuration
    }
    
    // Signing configuration for release APK
    signingConfigs {
        create("release") {
            // Using hardcoded values for hackathon demo purposes only
            // In a production app, these would come from secure environment variables
            storeFile = file("../satyacheck-keystore.jks") 
            storePassword = "satyacheck123"
            keyAlias = "satyacheck"
            keyPassword = "satyacheck123"
        }
    }
    
    buildTypes {
        val geminiApiKey = project.findProperty("GEMINI_API_KEY") as String? ?: ""
        val cloudNlApiKey = project.findProperty("CLOUD_NATURAL_LANGUAGE_API_KEY") as String? ?: ""
        val cloudVisionApiKey = project.findProperty("CLOUD_VISION_API_KEY") as String? ?: ""
        val speechToTextApiKey = project.findProperty("SPEECH_TO_TEXT_API_KEY") as String? ?: ""
        
        debug {
            // Disable minification for debug builds to prevent crashes
            isMinifyEnabled = false
            isShrinkResources = false
            // Remove debug suffix
            applicationIdSuffix = ""
            // Keep debuggable
            isDebuggable = true
            
            // Add BuildConfig fields
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
            buildConfigField("String", "CLOUD_NATURAL_LANGUAGE_API_KEY", "\"$cloudNlApiKey\"")
            buildConfigField("String", "CLOUD_VISION_API_KEY", "\"$cloudVisionApiKey\"")
            buildConfigField("String", "SPEECH_TO_TEXT_API_KEY", "\"$speechToTextApiKey\"")
        }
        
        release {
            // PRODUCTION-READY STABLE BUILD - NO OPTIMIZATION TO PREVENT CRASHES
            isMinifyEnabled = false  // COMPLETELY disable all obfuscation and optimization
            isShrinkResources = false  // NEVER shrink resources 
            // No ProGuard rules - prevents all Kotlin reflection issues
            // proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules-stable.pro")
            
            // Production settings without optimization
            isDebuggable = false
            isJniDebuggable = false
            isRenderscriptDebuggable = false
            
            // Production signing configuration
            signingConfig = signingConfigs.getByName("release")
            
            // Add API keys for release build
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
            buildConfigField("String", "CLOUD_NATURAL_LANGUAGE_API_KEY", "\"$cloudNlApiKey\"")
            buildConfigField("String", "CLOUD_VISION_API_KEY", "\"$cloudVisionApiKey\"")
            buildConfigField("String", "SPEECH_TO_TEXT_API_KEY", "\"$speechToTextApiKey\"")
            
            // Maximum stability settings
            enableUnitTestCoverage = false
            isDebuggable = false
            
            // Safe performance optimizations
            aaptOptions.cruncherEnabled = true
            
            // Version code and name for production
            versionNameSuffix = ""
        }
        
        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks.add("release")
            proguardFiles("benchmark-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Enable incremental compilation
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        // Optimize Kotlin compiler
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    
    androidResources {
        localeFilters.addAll(listOf("en", "hi", "es", "fr", "de", "pt", "ru", "zh", "ar", "ja"))
        noCompress.addAll(listOf("tflite", "lite"))
    }
    
    packaging {
        resources {
            // Exclude duplicate licenses and metadata
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/DEPENDENCIES*"
            excludes += "/META-INF/MANIFEST.MF"
            excludes += "/META-INF/*.SF"
            excludes += "/META-INF/*.DSA"
            excludes += "/META-INF/*.RSA"
            excludes += "/META-INF/maven/**"
            excludes += "/META-INF/proguard/**"
            excludes += "/META-INF/services/**"
            excludes += "/*.properties"
            excludes += "/*.txt"
            excludes += "/*.bin"
            excludes += "/*.xml"
            excludes += "/kotlin/**"
            excludes += "/kotlinx/**"
            excludes += "/okhttp3/internal/publicsuffix/NOTICE"
            // Exclude lower resolution launcher icons - keep only xxxhdpi
            excludes += "/mipmap-mdpi/ic_launcher.png"
            excludes += "/mipmap-mdpi/ic_launcher_round.png"
            excludes += "/mipmap-hdpi/ic_launcher.png"
            excludes += "/mipmap-hdpi/ic_launcher_round.png"
            excludes += "/mipmap-xhdpi/ic_launcher.png"
            excludes += "/mipmap-xhdpi/ic_launcher_round.png"
            excludes += "/mipmap-xxhdpi/ic_launcher.png"
            excludes += "/mipmap-xxhdpi/ic_launcher_round.png"
            // Keep only xxxhdpi for best quality on all devices
            // excludes += "/mipmap-xxxhdpi/ic_launcher.png"
            // excludes += "/mipmap-xxxhdpi/ic_launcher_round.png"
            // Exclude debug and testing resources
            excludes += "/DebugProbesKt.bin"
            excludes += "/kotlin-tooling-metadata.json"
        }
        dex {
            useLegacyPackaging = false
        }
        jniLibs {
            useLegacyPackaging = false
            // Pick first occurrence of duplicate files
            pickFirsts += "**/libc++_shared.so"
            pickFirsts += "**/libjsc.so"
        }
    }
    
    // Enable ViewBinding and Data Binding optimization
    buildFeatures {
        viewBinding = true
        dataBinding = false  // Only enable if absolutely needed
    }
}

dependencies {
    // Core Android dependencies with optimized versions
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-process:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    
    // Compose dependencies - use BOM to manage version conflicts, exclude unnecessary modules
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui") {
        exclude(group = "androidx.compose.ui", module = "ui-test-manifest")
    }
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Keep extended icons - they are needed by the app
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.6") {
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel")
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
    }
    
    // Pull-to-refresh with Accompanist
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
    
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // NanoHTTPD for embedded server
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    
    // Hilt for dependency injection
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room for database - use only what's needed
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // CameraX for camera functionality - use only required modules
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")
    
    // Media3 for audio processing - use only required modules
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    
    // ML Kit for on-device ML - use only required modules
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.mlkit:language-id:17.0.4")
    
    // Gemini API for AI capabilities - using current working version
    implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
    
    // Coil for image loading with memory caching
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Network stack with optimized configuration - exclude duplicate resources
    implementation("com.squareup.retrofit2:retrofit:2.9.0") {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    
    // OkHttp with optimized configuration
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    implementation("com.squareup.okhttp3:okhttp") {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    // Include logging interceptor - needed by code in all builds
    implementation("com.squareup.okhttp3:logging-interceptor")
    
    // JSON parsers - keep both for compatibility but optimize
    implementation("com.google.code.gson:gson:2.10.1")
    // Keep Moshi as code depends on it
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0") 
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    
    // Work Manager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Google Auth
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // LeakCanary for memory leak detection - debug only
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
    
    // Coroutines with optimized configuration
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Desugaring for using newer Java APIs on older Android versions
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    
    // Testing dependencies - debug and test only
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    // Debug utilities - only included in debug builds
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
