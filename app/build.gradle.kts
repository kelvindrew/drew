plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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
    implementation(project(":ui"))
    implementation(Libs.activityCompose)
    implementation(Libs.navigationCompose)
    implementation(project(":ai"))
    implementation(project(":watch"))
    implementation(project(":health"))
    implementation(project(":settings"))
    implementation(project(":bluetooth"))
    implementation(project(":notifications"))
    implementation(project(":sport"))
    implementation(Libs.coroutinesCore)
    implementation(Libs.hiltAndroid)
    implementation(project(":core"))
}
