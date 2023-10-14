@file:Suppress("UnstableApiUsage")

import com.ilyabogdanovich.jelly.withVersionCatalogs
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.kotlin.dsl.withType

plugins {
    id("io.gitlab.arturbosch.detekt")
}

withVersionCatalogs {
    dependencies {
        detektPlugins(plugin.detektFormatting)
    }
}

tasks.withType<Detekt>().configureEach {
    buildUponDefaultConfig = true
    allRules = false
    parallel = true
    autoCorrect = true

    setSource(files(projectDir))
    config.setFrom(files(rootDir.resolve("config/detekt/detekt.yml")))

    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")

    reports {
        txt.required.set(true)
        xml.required.set(false)
        html.required.set(false)
    }
}
