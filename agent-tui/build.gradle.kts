plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    //id("com.gradleup.shadow") version "9.4.1"
}

dependencies {
    implementation(libs.bundles.mordantEcosystem)
    implementation(libs.slf4j.simple)
}
