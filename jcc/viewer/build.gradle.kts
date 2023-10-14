plugins {
    id("jelly.jvm")
}

group = "com.ilyabogdanovich.jelly.jcc.viewer"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":jcc:core"))
}
