plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.jetbrainsKotlinAndroid)
  alias(libs.plugins.ksp)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "com.inasweaterpoorlyknit.core.ui"
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
  buildFeatures {
    compose = true
    viewBinding = true
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }
}

dependencies {
  implementation(projects.core.model)

  implementation(libs.androidx.core.ktx)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3.adaptive)
  implementation(libs.androidx.compose.material3.adaptive.layout)
  implementation(libs.androidx.compose.material3.adaptive.navigation)
  implementation(libs.androidx.compose.material3.windowSizeClass)
  implementation(libs.androidx.compose.runtime.tracing)
  implementation(libs.androidx.compose.animation)

  // Coil
  implementation(libs.coil)
  implementation(libs.coil.compose)
}
