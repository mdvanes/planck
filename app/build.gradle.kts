
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    kotlin("plugin.serialization") version "1.9.21"
    //id("androidx.navigation.safeargs")
}

android {
    namespace = "nl.mdworld.planck4"
    compileSdk = 36

    defaultConfig {
        applicationId = "nl.mdworld.planck4"
        minSdk = 30
        targetSdk = 36
        versionCode = 37
        versionName = "1.0.37"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    val navVersion = "2.9.6"
    val composeBom = platform("androidx.compose:compose-bom:2024.09.01")
    val ktorVersion = "3.3.3"

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navVersion")
    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")
    // Jetpack Compose Integration
    implementation("androidx.navigation:navigation-compose:$navVersion")

    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("eu.bambooapps:compose-material3-pullrefresh:1.1.1")
    implementation("io.ktor:ktor-client-core:${ktorVersion}")
    implementation("io.ktor:ktor-client-cio:${ktorVersion}")
    implementation("io.ktor:ktor-client-android:${ktorVersion}")
    implementation("io.ktor:ktor-client-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
    implementation("io.ktor:ktor-client-logging:${ktorVersion}")
    implementation("androidx.car.app:app:1.7.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.activity:activity-compose:1.12.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.media:media:1.7.1")
    implementation("androidx.core:core-ktx:1.17.0")
    // implementation("nl.mdworld:radio-metadata:0.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.14.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("io.mockk:mockk-android:1.14.7")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    implementation("androidx.media2:media2-session:1.3.0")
    implementation("androidx.media2:media2-player:1.3.0")

    // Lifecycle for process-wide lifecycle (needed for cleanup on background)
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(composeBom)
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Add new compilerOptions DSL for Kotlin
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}
