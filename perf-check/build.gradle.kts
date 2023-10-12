plugins {
    kotlin("jvm")
}

group = "com.ilyabogdanovich.jelly.perf-check"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotlin.coroutines.core)
    implementation(project(":jcc"))
    implementation(project(":utils"))
}
