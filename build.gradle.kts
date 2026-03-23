plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.intellij") version "2.1.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
}

intellij {
    version.set("2025.3")
    type.set("IC") // IC = Community Edition, IU = Ultimate
    plugins.set(listOf())
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs += listOf("-Xjvm-default=all")
    }

    patchPluginXml {
        sinceBuild.set("253")
        untilBuild.set("263.*")
    }
}
