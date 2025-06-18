plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.2" apply false // VERIFICAR LA ÚLTIMA VERSIÓN
}

android {
    namespace = "com.example.miprimeraaplicacion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.miprimeraaplicacion"
        minSdk = 23
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.squareup.picasso:picasso:2.71828") // Deja solo una de estas
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    // Dependencia de Glide
    implementation ("com.github.bumptech.glide:glide:4.16.0") // Revisa la última versión si es necesario
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}