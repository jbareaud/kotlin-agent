plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    id("com.gradleup.shadow") version "9.4.1"
}

dependencies {
    implementation(libs.bundles.langchain4jEcosystem)
    implementation(libs.bundles.mordantEcosystem)
    implementation(libs.classgraph)
    implementation(libs.slf4j.simple)
    implementation(project(":agent-tui"))
    implementation(project(":agent-tools"))
}

application {
    mainClass = "org.jbareaud.agent.core.AppKt"
}
