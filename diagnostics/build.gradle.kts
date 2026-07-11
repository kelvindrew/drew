plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.connect.diagnostics"
    compileSdk = 34

    defaultConfig {
        minSdk = 26

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    implementation(Libs.composeRuntime)
    implementation(Libs.composeUi)
    implementation(Libs.coroutinesCore)
    implementation(Libs.hiltAndroid)
    ksp(Libs.hiltCompiler)
    implementation(project(":core"))
}
