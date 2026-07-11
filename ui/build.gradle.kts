plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.connect.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    implementation(Libs.composeUi)
    implementation(Libs.composeMaterial3)
    implementation(project(":ai"))
    implementation(Libs.coroutinesCore)
    implementation(Libs.hiltAndroid)
    kapt(Libs.hiltCompiler)
    implementation(project(":core"))
}
