import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
}

group = "com.ilyabogdanovich.jelly.ide.shared"
version = "1.0-SNAPSHOT"

kotlin {
    androidTarget()

    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.materialIconsExtended)
                implementation(compose.preview)
                implementation(libs.androidx.annotation)
                implementation(libs.kotlin.coroutines.core)
                implementation(libs.squareup.okio.core)
                implementation(project(":base:logging"))
                implementation(project(":jcc:core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.test.junit)
                implementation(libs.test.kotest.assertions)
                implementation(libs.test.kotlin.coroutines)
                implementation(libs.test.mockk.jvm)
                implementation(libs.squareup.okio.fakefilesystem)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.kotlin.coroutines.swing)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.viewModel)
            }
        }
    }
}

android {
    namespace = "com.ilyabogdanovich.jelly.ide.shared"

    compileSdk = libs.versions.android.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.sdk.min.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}
