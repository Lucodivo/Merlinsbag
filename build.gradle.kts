// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.navigationSafeArgs) apply false
    alias(libs.plugins.hiltAndroid) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidLibrary) apply false
}

buildscript {
    dependencies {
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }
}