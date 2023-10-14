@file:Suppress("UnstableApiUsage")

package com.ilyabogdanovich.jelly

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

// hack for version catalog access
inline fun <T> Project.withVersionCatalogs(block: LibrariesForLibs.() -> T): T {
    val libs = the<LibrariesForLibs>()
    return block(libs)
}
