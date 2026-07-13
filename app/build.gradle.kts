plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.connect.app"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
    implementation(project(":ui"))
    implementation(Libs.activityCompose)
    implementation(Libs.composeMaterial3)
    implementation(Libs.navigationCompose)
    implementation(Libs.hiltNavigationCompose)
    implementation(project(":ai"))
    implementation(project(":watch"))
    implementation(project(":health"))
    implementation(project(":settings"))
    implementation(project(":bluetooth"))
    implementation(project(":notifications"))
    implementation(project(":sport"))
    implementation(Libs.coroutinesCore)
    implementation(Libs.hiltAndroid)
    ksp(Libs.hiltCompiler)
    implementation(project(":core"))
}
