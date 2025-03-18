plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)



    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation (libs.firebase.auth.ktx)
    implementation (libs.firebase.firestore.ktx)
    implementation (libs.firebase.storage.ktx)

//    Google Sign-In Dependency
    implementation (libs.play.services.auth)

    // navigation dependencies
    implementation (libs.androidx.navigation.compose)

    // Retrofit (for API calls)
    implementation (libs.retrofit)
    // Gson converter for JSON parsing
    implementation (libs.converter.gson)
    //    For logging network requests
    implementation (libs.logging.interceptor)

    // Coroutines
    implementation (libs.kotlinx.coroutines.core )
    // Coroutines core
    implementation (libs.kotlinx.coroutines.android)
    // viewmodel and livedata
    implementation (libs.androidx.lifecycle.viewmodel.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}