import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("jelly.jvm")
}

group = "com.ilyabogdanovich.jelly.base.logging"
version = "1.0-SNAPSHOT"

/**
 * Task to generate the logging config, used by the source code.
 * It checks the "env" parameter value and writes it to the Config.kt.
 */
abstract class GenerateConfig : DefaultTask() {
    @Input
    lateinit var env: String

    @OutputDirectory
    lateinit var outputDir: File

    @TaskAction
    fun run() {
        println("env = $env")
        outputDir.mkdirs()
        val file = File(outputDir, "Config.kt")
        file.delete()
        file.createNewFile()
        file.writeText(
            """
                package com.ilyabogdanovich.jelly.logging
                
                const val ENABLE_LOGGING = ${env == "debug"}
            """.trimIndent()
        )
    }
}

tasks.register<GenerateConfig>("generateConfig") {
    env = properties["env"]?.toString() ?: "debug"
    outputDir = project.file("build/gen/config")
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.withType<GenerateConfig>())
}

sourceSets {
    main {
        kotlin {
            srcDir("build/gen/config")
        }
    }
}
