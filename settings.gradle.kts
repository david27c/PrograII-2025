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
        id("com.android.application") version "8.10.1" // Actualizado a 8.6.0
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
    // ELIMINADO: Ya que eliminaste libs.versions.toml, esta sección ya no es necesaria
    /*
    versionCatalogs {
        create("libs") {
            from(files("../.gradle/libs.versions.toml"))
        }
    }
    */
}

rootProject.name = "MiPrimeraAplicacion" // Asegúrate que sea el nombre exacto de tu proyecto
include(":app")