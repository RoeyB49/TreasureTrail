plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    alias(libs.plugins.google.services) // Enables annotation processing for Room
}

android {
    compileSdk = 35
    namespace = "com.example.treasuretrail" // Namespace declared as required

    defaultConfig {
        applicationId = "com.example.treasuretrail"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Firebase
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)

    // Room Database: runtime and Kotlin extensions
    implementation(libs.androidx.room.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    // Room Compiler for annotation processing
    kapt(libs.androidx.room.compiler)

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Picasso for image loading
    implementation("com.squareup.picasso:picasso:2.8")

    apply(plugin = "com.google.gms.google-services")

}
