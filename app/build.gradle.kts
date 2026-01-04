plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    // 这里设置你的新包名作为 namespace
    namespace = "in.co.washing_machine.mushroomscanner"
    compileSdk = 34 // 根据你的 AS 版本，可能是 33 或 34

    defaultConfig {
        // 应用 ID，通常与 namespace 一致
        applicationId = "in.co.washing_machine.mushroomscanner"
        minSdk = 24 // 建议 24+，如果为了自动截图方便最好 30+
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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

    // 开启 ViewBinding (可选，如果你习惯用 findViewById 可以不加，但推荐加上)
    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // 基础依赖
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // --- 核心新增依赖 ---

    // Google ML Kit 中文 OCR (必须用圆括号和双引号)
    implementation("com.google.mlkit:text-recognition-chinese:16.0.0")

    // 协程 (Kotlin Coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // 测试依赖 (默认生成)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}