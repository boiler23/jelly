plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(compose.material)
                implementation(libs.androidx.activity)
                implementation(libs.androidx.appcompat)
                implementation(libs.androidx.lifecycle.runtime)
                implementation(libs.androidx.viewModel)
                implementation(libs.squareup.okio.core)
                implementation(project(":ide:shared"))
                implementation(project(":jcc:core"))
            }
        }
    }
}

android {
    namespace = "com.ilyabogdanovich.jelly.ide.android"

    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        applicationId = "com.ilyabogdanovich.jelly.ide"
        minSdk = libs.versions.android.sdk.min.get().toInt()
        targetSdk = libs.versions.android.sdk.target.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
