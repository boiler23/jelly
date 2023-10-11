plugins {
    kotlin("jvm")
}

group = "com.ilyabogdanovich.jelly.jcc"
version = "1.0-SNAPSHOT"

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
                "-package", "com.ilyabogdanovich.jelly.jcc",
                "-o", outputDir.path,
            )
        )
        super.exec()
    }
}

tasks.register<GenerateParserTask>("generateParser") {
    grammarFile = project.file("Jcc.g4")
    outputDir = project.file("build/gen/antlr4/com/ilyabogdanovich/jelly/jcc")
}

tasks.named("compileKotlin").configure {
    dependsOn(tasks.named("generateParser"))
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

    implementation(project(":utils"))

    implementation(libs.kotlin.coroutines.core)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.kotest.assertions)
    testImplementation(libs.test.kotlin.coroutines)
    testImplementation(libs.test.mockk.jvm)
}
