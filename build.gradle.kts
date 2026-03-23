plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.yourname"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    intellijPlatform {
        intellijIdeaCommunity("2025.3")
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "Tasks"
        version = project.version.toString()
        description = "Task management tool for IntelliJ IDEA"
        vendor {
            name = "Your Name"
        }

        ideaVersion {
            sinceBuild = "253"
            untilBuild = "263.*"
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjvm-default=all")
        }
    }
}
