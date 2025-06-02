val implementation: Unit
    get() {
        TODO()
    }

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.miprimeraaplicacion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.miprimeraaplicacion"
        minSdk = 22
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
    implementation(libs.firebase.inappmessaging)
    implementation(libs.room.runtime.android)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.mediation.test.suite)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation(platform("com.google.firebase:firebase-database"))
    implementation(platform("com.google.firebase:firebase-messaging"))
    implementation(platform("com.google.firebase:firebase-storage"))
    implementation(platform("com.firebaseui:firebase-ui-storage:9.0.0"))
    implementation (platform("com.squareup.picasso:picasso:2.71828"))
}