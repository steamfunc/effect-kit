plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
}

val releaseVersion = property("releaseVersion") as String
val projectVersion = if (System.getenv("BUILD_TYPE") == "RELEASE") releaseVersion else "$releaseVersion-SNAPSHOT"

subprojects {
    group = "com.github.steamfunc"
    version = projectVersion

    repositories {
        mavenCentral()
    }
}
