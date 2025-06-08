// settings.gradle.kts (ubicación: raíz del proyecto)

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.gms.google-services") version "4.4.2"
        id("com.android.application") version "8.5.1" // AJUSTA ESTA VERSIÓN SI ES DIFERENTE EN TU PROYECTO
        id("org.jetbrains.kotlin.android") version "1.9.0" // AJUSTA ESTA VERSIÓN SI USAS OTRA
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MiPrimeraAplicacion" // Asegúrate que sea el nombre exacto de tu proyecto
include(":app")