plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.agenticmarketer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.agenticmarketer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    
    // AI & Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    
    // UI & Animations
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation(libs.material)
    implementation("com.airbnb.android:lottie:6.7.1")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    
    // Image loading
    implementation(libs.glide)
    
    // Coroutines & Lifecycle
    implementation(libs.coroutines.android)
    implementation(libs.lifecycle.viewmodel)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    
    // WorkManager
    implementation(libs.work.runtime)
    
    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
