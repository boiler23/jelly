import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("jelly.jvm")
}

group = "com.ilyabogdanovich.jelly.jcc.core"
version = "1.0-SNAPSHOT"

/**
 * Task to generate the ANTLR's parsing code.
 * Takes [grammarFile] in G4 format as an input, and writes the generated code into the [outputDir].
 */
abstract class GenerateParserTask : JavaExec() {
    @InputFile
    lateinit var grammarFile: File

    @OutputDirectory
    lateinit var outputDir: File

    override fun exec() {
        classpath(project.files("libs/antlr-4.13.1-complete.jar"))
        args(
            listOf(
                grammarFile.path,
                "-Dlanguage=Java",
                "-package",
                "com.ilyabogdanovich.jelly.jcc.core.antlr",
                "-o",
                outputDir.path,
            )
        )
        super.exec()
    }
}

tasks.register<GenerateParserTask>("generateParser") {
    grammarFile = project.file("Jcc.g4")
    outputDir = project.file("build/gen/antlr4/com/ilyabogdanovich/jelly/jcc/core/antlr")
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.withType<GenerateParserTask>())
}

sourceSets {
    main {
        java {
            srcDir("build/gen/antlr4")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to "*.jar")))

    implementation(project(":base:utils"))

    implementation(libs.kotlin.coroutines.core)
    implementation(libs.apache.fastmath)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.kotest.assertions)
    testImplementation(libs.test.kotlin.coroutines)
    testImplementation(libs.test.mockk.jvm)
}
