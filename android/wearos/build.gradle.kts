plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.runningcity"
    compileSdk = 34  // ✅ 이렇게 수정!

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

    // 위치 서비스 (GPS) - 중복 제거하고 최신 버전 하나만!
    implementation("com.google.android.gms:play-services-location:21.3.0")  // ✅ 최신 버전

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Wearable
    implementation("com.google.android.gms:play-services-wearable:18.1.0")

    // Room Database
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    //////////////////////////////////////////////////////////////
    // 워치 -> 모바일 데이터 전달하기 위해 추가한 부분
    // Gson for JSON serialization (워치 -> 모바일 데이터 전송용)
    implementation("com.google.code.gson:gson:2.10.1")
    //////////////////////////////////////////////////////////////

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.tiles.tooling)

    // Design
    implementation("androidx.compose.material:material-icons-core:1.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.0")

    // Wear Compose
    implementation("androidx.wear.compose:compose-foundation:1.3.0")
    implementation("androidx.wear.compose:compose-material:1.3.0") // <- Surface 여기 있음

    // Compose BOM (모바일 Compose용)과 섞는다면 버전 충돌 없게 유지
    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
}