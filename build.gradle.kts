// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // 파이어베이스 구글 서비스를 위해 추가합니다.
    id("com.google.gms.google-services") version "4.4.2" apply false
}