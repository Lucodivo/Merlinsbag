plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.jetbrainsKotlinAndroid)
  alias(libs.plugins.hiltAndroid)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.inasweaterpoorlyknit.core.data"
  compileSdk = 34

  defaultConfig {
    minSdk = 26
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
  implementation(projects.core.database)
  implementation(projects.core.common)
  implementation(projects.core.datastore)
  implementation(projects.core.model)
  testImplementation(projects.core.testing)

  implementation(libs.androidx.core.ktx)

  // Testing
  testImplementation(libs.junit)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.androidx.espresso.core)
  testImplementation(libs.androidx.room.runtime)
  testImplementation(libs.androidx.room.ktx) // Kotlin Extensions and Coroutines support for Room
  testImplementation(libs.androidx.room.testing)
  testImplementation(libs.robolectric)

  // Hilt
  implementation(libs.hilt.android)
  testImplementation(libs.hilt.android.testing)
  testImplementation(libs.hilt.android.testing)
  ksp(libs.hilt.compiler)
  kspTest(libs.hilt.compiler)
  kspAndroidTest(libs.hilt.compiler)
}