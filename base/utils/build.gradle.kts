plugins {
    id("jelly.jvm")
}

group = "com.ilyabogdanovich.jelly.utils"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(libs.test.junit)
    testImplementation(libs.test.kotest.assertions)
}
