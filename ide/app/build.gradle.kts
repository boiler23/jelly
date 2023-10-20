import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("jelly.jvm")
    id("org.jetbrains.compose")
}

group = "com.ilyabogdanovich.jelly.ide"
version = "1.0-SNAPSHOT"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(libs.androidx.annotation)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.swing)
    implementation(libs.squareup.okio.core)

    implementation(project(":base:logging"))
    implementation(project(":jcc:core"))

    testImplementation(libs.test.junit)
    testImplementation(libs.test.kotest.assertions)
    testImplementation(libs.test.kotlin.coroutines)
    testImplementation(libs.test.mockk.jvm)
    testImplementation(libs.squareup.okio.fakefilesystem)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Jelly"
            packageVersion = "1.0.0"
        }

        buildTypes {
            release {
                proguard {
                    configurationFiles.from(project.file("compose-desktop.pro"))
                }
            }
        }
    }
}
