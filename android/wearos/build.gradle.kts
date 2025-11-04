plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "com.runningcity"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.runningcity"
        minSdk = 30
        targetSdk = 33  // ✅ 33으로 변경 (Galaxy Watch 6 최적화)
        versionCode = 1
        versionName = "1.0"

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
//    useLibrary("wear-sdk") // Jetpack Compose 기반의 Wear OS 프로젝트에서는 예전식 “wear-sdk”는 필요하지 않음 (Compose + Horologist + Tiles 등으로 모두 대체됨)
    buildFeatures {
        compose = true
    }

    lint {
        abortOnError = false
        warningsAsErrors = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    kapt {
        correctErrorTypes = true
    }
}


dependencies {
    implementation(libs.play.services.wearable)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.wear.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.tiles)
    implementation(libs.androidx.tiles.material)
    implementation(libs.androidx.tiles.tooling.preview)
    implementation(libs.horologist.compose.tools)
    implementation(libs.horologist.tiles)
    implementation(libs.androidx.watchface.complications.data.source.ktx)

    // Health Services
    implementation("androidx.health:health-services-client:1.1.0-alpha03")

    // 위치 서비스 (GPS)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Coroutines (비동기 처리 - 필수!)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ViewModel (데이터 관리)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    // 👆👆👆 여기까지 추가! 👆👆👆

    //fusedlocation 사용용
    implementation("com.google.android.gms:play-services-location:21.0.1")
    //워치와 폰 연결용
    implementation("com.google.android.gms:play-services-wearable:18.0.0")

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.tooling)

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Room Kotlin Extensions and Coroutines support
    implementation("androidx.room:room-ktx:2.6.1")

    // 테스트용 (필요 시)
    testImplementation("androidx.room:room-testing:2.6.1")
}