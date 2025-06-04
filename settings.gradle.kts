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
        // Asegúrate de que esta línea esté aquí con la versión del plugin de Google Services.
        // La versión 4.4.1 es un valor común, pero puedes actualizarla si Android Studio
        // te sugiere una versión más reciente.
        id("com.google.gms.google-services") version "4.4.1"

        // Esta línea es para el plugin de Android Application.
        // Asegúrate de que la versión (ej. 8.5.1) coincida con la que estás usando en tu build.gradle.kts (Module: app).
        id("com.android.application") version "8.5.1"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "miPrimeraAplicacion"
include(":app")