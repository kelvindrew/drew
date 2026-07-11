import os

modules = ["app", "core", "data", "domain", "ui", "bluetooth", "watch", "ai", "health", "sport", "notifications", "camera", "battery", "weather", "calendar", "settings", "backup", "diagnostics"]

def write_file(path, content):
    dir_name = os.path.dirname(path)
    if dir_name:
        os.makedirs(dir_name, exist_ok=True)
    with open(path, "w") as f:
        f.write(content.strip() + "\n")

# Settings.gradle.kts
settings_content = """
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "ConnectPremium"
""" + "\n".join([f'include(":{m}")' for m in modules])
write_file("settings.gradle.kts", settings_content)

# build.gradle.kts (root)
root_build_content = """
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
    }
}
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}
"""
write_file("build.gradle.kts", root_build_content)

# gradle.properties
gradle_properties = """
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
"""
write_file("gradle.properties", gradle_properties)

# buildSrc
write_file("buildSrc/build.gradle.kts", """
plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}
""")
write_file("buildSrc/src/main/kotlin/Dependencies.kt", """
object Versions {
    const val compose = "1.5.1"
    const val hilt = "2.48"
    const val room = "2.5.2"
    const val coroutines = "1.7.3"
    const val coil = "2.4.0"
    const val healthConnect = "1.0.0-alpha11"
}

object Libs {
    const val composeUi = "androidx.compose.ui:ui:${Versions.compose}"
    const val composeMaterial3 = "androidx.compose.material3:material3:1.1.2"
    const val hiltAndroid = "com.google.dagger:hilt-android:${Versions.hilt}"
    const val hiltCompiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val coilCompose = "io.coil-kt:coil-compose:${Versions.coil}"
    const val gemini = "com.google.ai.client.generativeai:generativeai:0.1.2"
    const val healthConnect = "androidx.health.connect:connect-client:${Versions.healthConnect}"
    const val dataStore = "androidx.datastore:datastore-preferences:1.0.0"
    const val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
}
""")

for mod in modules:
    is_app = (mod == "app")
    plugin_id = "com.android.application" if is_app else "com.android.library"
    core_dep = 'implementation(project(":core"))' if mod != "core" else ''
    target_sdk_block = 'targetSdk = 34\n        versionCode = 1\n        versionName = "1.0"' if is_app else ''

    build_gradle = f"""
plugins {{
    id("{plugin_id}")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
}}

android {{
    namespace = "com.connect.{mod}"
    compileSdk = 34

    defaultConfig {{
        minSdk = 26
        {target_sdk_block}
    }}
    compileOptions {{
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }}
    kotlinOptions {{
        jvmTarget = "17"
    }}
    buildFeatures {{
        compose = true
    }}
    composeOptions {{
        kotlinCompilerExtensionVersion = "1.5.3"
    }}
}}

dependencies {{
    implementation(Libs.coroutinesCore)
    implementation(Libs.hiltAndroid)
    {core_dep}
}}
"""
    write_file(f"{mod}/build.gradle.kts", build_gradle)

    manifest = f"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.connect.{mod}">
</manifest>
"""
    write_file(f"{mod}/src/main/AndroidManifest.xml", manifest)
