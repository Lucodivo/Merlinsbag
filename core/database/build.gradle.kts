plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.jetbrainsKotlinAndroid)
  alias(libs.plugins.hiltAndroid)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.inasweaterpoorlyknit.core.database"
  compileSdk = 34

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")

    ksp {
      arg("room.schemaLocation","$projectDir/schemas")
    }
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
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(project(":core:common"))
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  // TODO: REMOVE LiveData testing
  androidTestImplementation(libs.androidx.core.testing)
  androidTestImplementation(libs.kotlinx.coroutines.test)

  // Room
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx) // Kotlin Extensions and Coroutines support for Room
  implementation(libs.androidx.room.rxjava2) // RxJava2 support for Room
  implementation(libs.androidx.room.rxjava3) // RxJava3 support for Room
  implementation(libs.androidx.room.guava) // Guava support for Room, including Optional and ListenableFuture
  implementation(libs.androidx.room.paging) // Paging 3 Integration
  testImplementation(libs.androidx.room.testing) // Test helpers
  annotationProcessor(libs.androidx.room.compiler)
  ksp(libs.androidx.room.compiler)

  // Hilt
  implementation(libs.hilt.android)
  testImplementation(libs.hilt.android.testing)
  androidTestImplementation(libs.hilt.android.testing)
  ksp(libs.hilt.compiler)
  kspTest(libs.hilt.compiler)
  kspAndroidTest(libs.hilt.compiler)
}