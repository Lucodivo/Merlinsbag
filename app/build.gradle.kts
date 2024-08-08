plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.navigationSafeArgs)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

android {
    namespace = "com.inasweaterpoorlyknit.merlinsbag"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.inasweaterpoorlyknit.merlinsbag"
        minSdk = 26
        targetSdk = 34
        versionCode = 15
        versionName = "1.0.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        debug {
            resValue("bool", "FIREBASE_ANALYTICS_DEACTIVATED", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
            resValue("bool", "FIREBASE_ANALYTICS_DEACTIVATED", "false")
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
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Project Modules
    implementation(projects.core.data)
    implementation(projects.core.database) // TODO: app module shouldn't rely on database but mapping every database query seems wasteful 🤷‍♀️
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.ml)
    testImplementation(projects.core.testing)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.core) // used for @Serializable compose route objects / data classes

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.runtime.tracing)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.compose)

    // In-App Google Play Reviews
    implementation(libs.review)
    implementation(libs.review.ktx)

    // Firebase (crashlytics)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.crashlytics.ktx)
    implementation(libs.google.firebase.analytics.ktx)

    // Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.material3.adaptive.navigation.suite.android)
    androidTestImplementation(libs.androidx.navigation.testing)

    androidTestImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)
    kspTest(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)

    testImplementation(libs.mockk)
}