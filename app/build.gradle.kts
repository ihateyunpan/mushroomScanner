plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "in.co.washing_machine.mushroomscanner"
    compileSdk = 34 // 或你使用的版本
    ndkVersion = "21.4.7075529"

    defaultConfig {
        applicationId = "in.co.washing_machine.mushroomscanner"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "0.99.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        applicationVariants.all {
            val variant = this
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    if (variant.buildType.name == "release") {
                        // 定义文件名格式，例如: MushroomScanner_v1.1.0.apk
                        val appName = "MushroomScanner"
                        val versionName = variant.versionName
                        val fileName = "${appName}_v${versionName}_${variant.versionCode}.apk"
                        output.outputFileName = fileName
                    }
                }
        }

        // 【关键】指定 NDK 架构
        ndk {
            abiFilters.add("arm64-v8a")
        }

        // 【关键】CMake 参数
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++11 -frtti -fexceptions -Wno-format")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
    }

    // 【新增】解决 STL 库冲突，只打包一个
    packaging {
        resources {
            // 解决 libc++_shared.so 冲突
//            pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
            // 如果还有其他冲突，也在这里添加，例如：
//            pickFirsts.add("**/libpaddle_light_api_shared.so")
//            pickFirsts.add("**/libopencv_java4.so")
        }
    }

    // 【关键】指定 CMakeLists.txt 路径
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "4.1.2"
        }
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

    // 【关键】确保 .so 库能被找到
    // 如果你按照上面的目录结构放置了 OpenCV 和 PaddleLite，CMake 会自动链接
    // 但有时候需要显式指定 jniLibs 目录让 Gradle 知道去哪里打包 .so
    sourceSets {
        getByName("main") {
            // 如果你把 .so 复制到了 src/main/jniLibs，保持这行
            // 如果没有，Gradle 会尝试从 CMake 的链接结果中抓取，但推荐手动复制一份到 jniLibs
//            jniLibs.srcDirs("src/main/jniLibs")
        }
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
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // 你的其他依赖...
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}