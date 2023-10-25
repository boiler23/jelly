@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm")
    id("jelly.detekt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
