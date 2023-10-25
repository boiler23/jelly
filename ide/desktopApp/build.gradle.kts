import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("jelly.jvm")
    id("org.jetbrains.compose")
}

group = "com.ilyabogdanovich.jelly.ide.desktop"
version = "1.0-SNAPSHOT"

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(project(":ide:shared"))
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
