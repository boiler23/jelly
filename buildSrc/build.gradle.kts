@file:Suppress("UnstableApiUsage")

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugin.agp)
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.compose)
    implementation(libs.plugin.detekt)

    // hack to make version catalogs accessible from conventional plugins
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
