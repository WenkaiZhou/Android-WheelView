plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "com.kevin.wheel.sample"
        minSdk = 14
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(fileTree(baseDir = "libs") {
        include("*.jar", "*.aar")
    })
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    implementation(project(":wheelview"))
    implementation("com.zwenkai:basedialog:1.1.3")
//    implementation("com.zwenkai:wheelview:1.0.2")
    implementation("com.google.code.gson:gson:2.8.5")

    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("com.google.android.material:material:1.3.0")

    implementation("com.zwenkai:slidingtablayout:2.1.2")
    implementation("com.zwenkai:delegationadapter:2.0.8")
    implementation("com.zwenkai:delegationadapter-extras:2.0.8")
}