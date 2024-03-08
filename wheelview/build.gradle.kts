
plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 14
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

}

dependencies {
    compileOnly("androidx.appcompat:appcompat:1.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.21")
}

// Release maven configuration
apply(plugin = "com.vanniktech.maven.publish")