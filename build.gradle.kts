// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.9.21"
    //id("androidx.navigation.safeargs")
}



//buildscript {
//    dependencies {
//        classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.0")
//    }
//}