plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.diatonomy"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.diatonomy"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.okhttp)
    implementation(libs.work.runtime.ktx)
    implementation("net.cacheux.nvplib:nvplib-core:0.1.2")
    implementation("net.cacheux.nvplib:nvplib-nfc:0.1.2")
}

afterEvaluate {
    tasks.register<Copy>("renameDebugApk") {
        dependsOn("assembleDebug")
        from("$buildDir/outputs/apk/debug")
        include("*.apk")
        into("$buildDir/outputs/apk/renamed")
        rename { "DiaTonomy-debug.apk" }
    }

    tasks.register<Copy>("renameReleaseApk") {
        dependsOn("assembleRelease")
        from("$buildDir/outputs/apk/release")
        include("*.apk")
        into("$buildDir/outputs/apk/renamed")
        rename { "DiaTonomy-release.apk" }
    }

    tasks.named("assembleDebug") {
        finalizedBy("renameDebugApk")
    }

    tasks.named("assembleRelease") {
        finalizedBy("renameReleaseApk")
    }
}