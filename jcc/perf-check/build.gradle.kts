plugins {
    kotlin("jvm")
}

group = "com.ilyabogdanovich.jelly.jcc.perf-check"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(project(":jcc:core"))
    implementation(project(":utils"))
}
