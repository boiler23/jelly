pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }
}

rootProject.name = "Jelly"

include(":base:logging")
include(":base:utils")
include(":ide:androidApp")
include(":ide:desktopApp")
include(":ide:shared")
include(":jcc:core")
include(":jcc:perf-check")
include(":jcc:viewer")
