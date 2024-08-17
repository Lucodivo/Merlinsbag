plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.jetbrainsKotlinAndroid)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.inasweaterpoorlyknit.core.ml"
  compileSdk = 34
  defaultConfig {
    minSdk = 26
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
  testImplementation(projects.core.testing)

  implementation(libs.androidx.core.ktx)

  // ML Kit
  implementation(libs.play.services.mlkit.subject.segmentation)

  // testing
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.mockk)

  // Hilt
  implementation(libs.hilt.android)
  testImplementation(libs.hilt.android.testing)
  testImplementation(libs.hilt.android.testing)
  ksp(libs.hilt.compiler)
  kspTest(libs.hilt.compiler)
  kspAndroidTest(libs.hilt.compiler)

}
