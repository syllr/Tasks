plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.13.1"
}

group = "com.shenyuanlaolarou"
version = "1.0.0"
description = "Task management tool directly inside IntelliJ IDEA. Manage your to-do tasks, track progress, and organize your work without leaving the IDE. Supports both project-level and global user-level tasks."

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:2.10.1")
    intellijPlatform {
        intellijIdea("2025.3.4")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "TodoTasks"
        name = "TodoTasks"
        version = project.version.toString()
        description = "Task management tool directly inside IntelliJ IDEA. Manage your to-do tasks, track progress, and organize your work without leaving the IDE. Supports both project-level and global user-level tasks."
        vendor {
            name = "shenyuanlaolarou"
        }

        ideaVersion {
            sinceBuild = "253"
            untilBuild = "263.*"
        }
    }

    signing {
        privateKeyFile = file("cert/private-key.pem")
        certificateChainFile = file("cert/certificate.pem")
        // Password is optional for self-signed certificate, can be empty
        password.set("")
    }

    publishing {
        // Token will be provided via environment variable
        token = providers.environmentVariable("ORG_GRADLE_PROJECT_intellijPlatformPublishingToken")
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
        }
    }

    // Disable buildSearchableOptions - not needed for this simple plugin
    buildSearchableOptions {
        enabled = false
    }
}
