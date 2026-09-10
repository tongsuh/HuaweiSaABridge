plugins {
    id("com.android.application")
}

android {
    namespace = "nodomain.freeyourgadget.gadgetbridge"
    compileSdk = 34

    defaultConfig {
        applicationId = "nodomain.freeyourgadget.gadgetbridge"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-huawei-saa"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // 纯原生 Android SDK 与基础组件，零多余依赖，极其轻量
}
