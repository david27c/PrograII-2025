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
        id("com.google.gms.google-services") version "4.4.2" apply false
        // **************************************************************
        // >>> CAMBIO AQUÍ: Actualizamos la versión de Android Gradle Plugin <<<
        id("com.android.application") version "8.6.0" // Actualizado a 8.6.0
        // **************************************************************
        id("org.jetbrains.kotlin.android") version "1.9.0" // Mantén tu versión de Kotlin o actualiza si es necesario
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