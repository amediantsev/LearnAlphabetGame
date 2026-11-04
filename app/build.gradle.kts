plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.learnalphabet"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.learnalphabet"
        minSdk = 26
        targetSdk = 34
        // CI passes the release number (see tools/next-version.sh); local builds are "0.0.0-dev".
        versionCode = (findProperty("abetkaVersionCode") as String?)?.toInt() ?: 1
        versionName = (findProperty("abetkaVersionName") as String?) ?: "0.0.0-dev"
    }

    // Shared debug key (not secret) so APKs built on CI and on any Mac install over each other.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        // The APK people install: not debuggable, so Compose runs at full speed. Signed with the
        // same shared key as debug builds, so it installs over any earlier build of the app.
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
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
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
