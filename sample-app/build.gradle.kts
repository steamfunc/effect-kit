plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("io.github.steamfunc.effectkit.sample.MainKt")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(project(":effect-annotations"))
    ksp(project(":effect-processor"))
}



