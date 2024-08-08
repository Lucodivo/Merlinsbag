plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
  namespace = "com.inasweaterpoorlyknit.core.testing"
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
  implementation(libs.junit)
  implementation(libs.kotlinx.coroutines.test)
}