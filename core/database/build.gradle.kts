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
    ksp {
      arg("room.schemaLocation","$projectDir/schemas")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
}

dependencies {

  // Project Modules
  androidTestImplementation(projects.core.common) // Test helpers

  implementation(libs.androidx.core.ktx)

  // Testing
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  // Room
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx) // Kotlin Extensions and Coroutines support for Room
  implementation(libs.androidx.room.paging) // Paging 3 Integration
  testImplementation(libs.androidx.room.testing)
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