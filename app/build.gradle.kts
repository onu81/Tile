plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 파이어베이스 연동을 위해 플러그인 추가
    id("com.google.gms.google-services")
}

android {
    namespace = "com.junhwi.tilecalc"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.junhwi.tilecalc"
        minSdk = 24
        targetSdk = 35
        // 배포를 위해 versionCode와 versionName을 업데이트합니다. (HTML v6.2 반영)
        versionCode = 16
        versionName = "6.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // 파이어베이스 BOM 및 Analytics 추가
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-analytics")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
