import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.jetbrainsKotlinAndroid)
  alias(libs.plugins.hiltAndroid)
  alias(libs.plugins.ksp)
  alias(libs.plugins.protobuf)
}

android {
  namespace = "com.inasweaterpoorlyknit.core.datastore"
  compileSdk = 34
  defaultConfig {
    minSdk = 26
    consumerProguardFiles("consumer-proguard-rules.pro")
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
  implementation(projects.core.common)

  implementation(libs.androidx.core.ktx)

  // Proto DataStore
  implementation(libs.androidx.datastore)
  implementation(libs.protobuf.kotlin.lite)

  // Testing
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)

  // Hilt
  implementation(libs.hilt.android)
  testImplementation(libs.hilt.android.testing)
  androidTestImplementation(libs.hilt.android.testing)
  ksp(libs.hilt.compiler)
  kspTest(libs.hilt.compiler)
  kspAndroidTest(libs.hilt.compiler)
}

protobuf {
  protoc {
    artifact = libs.protobuf.protoc.get().toString()
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        register("java") {
          option("lite")
        }
        register("kotlin") {
          option("lite")
        }
      }
    }
  }
}

// NOTE: Pulled from ksp issues. Ensures that proto datastore generated files are included in the source set.
// https://github.com/google/ksp/issues/1590
androidComponents {
  onVariants(selector().all()) { variant ->
    afterEvaluate {
      val capName = variant.name.capitalized()
      tasks.getByName<KotlinCompile>("ksp${capName}Kotlin") {
        setSource(tasks.getByName("generate${capName}Proto").outputs)
      }
    }
  }
}