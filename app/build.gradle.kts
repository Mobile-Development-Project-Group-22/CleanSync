plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // Firebase plugin
}

android {
    namespace = "com.example.cleansync"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cleansync"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Use the latest version compatible with your Compose version
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")
    // Firebase
    implementation(platform(libs.firebase.bom)) // Firebase BoM
    implementation(libs.com.google.firebase.firebase.auth.ktx) // Firebase Auth
    implementation(libs.google.firebase.firestore.ktx) // Firestore
    implementation(libs.google.firebase.storage.ktx) // Firebase Storage
    implementation(libs.firebase.crashlytics.buildtools) // Crashlytics (optional)
    implementation("com.google.firebase:firebase-messaging-ktx")
    // FirebaseUI Auth
    implementation(libs.firebase.ui.auth) // FirebaseUI for Auth

    // Google Play Services Auth (for Google Sign-In)
    implementation(libs.play.services.auth)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Retrofit (for API calls)
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // Gson converter
    implementation(libs.logging.interceptor) // OkHttp logging interceptor

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.espresso.core)

    // Coil (for image loading)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Credential Manager libraries
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    // Google Play Services Location (for GPS)
    // Google Play Services Location (for GPS)
    implementation("com.google.android.gms:play-services-location:21.0.1")

// Accompanist permissions for Compose
    implementation("com.google.accompanist:accompanist-permissions:0.31.6-rc")

// JSON (if needed for manual parsing, optional with Retrofit + Gson)
    implementation("org.json:json:20210307")




    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}