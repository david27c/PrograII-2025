// build.gradle.kts (nivel módulo: app, ubicación: app/build.gradle.kts)

plugins {
    id("com.android.application") // Ya no está duplicado
    id("org.jetbrains.kotlin.android") // Si tu proyecto usa código fuente Kotlin, esta línea es necesaria
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.miprimeraaplicacion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.miprimeraaplicacion"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Añadimos o modificamos este bloque para la compatibilidad de Kotlin JVM
    kotlinOptions {
        jvmTarget = "11" // Asegura que Kotlin compile para JVM 11, consistente con Java 11
    }
}

dependencies {
    // Firebase BoM (usa la versión más reciente)
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // Firebase products
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.inappmessaging)
    implementation(libs.google.firebase.inappmessaging)

    // Otras dependencias
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.runtime.android)
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.firebaseui:firebase-ui-storage:9.0.0")

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.mediation.test.suite)
}