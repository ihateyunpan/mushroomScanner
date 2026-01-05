#include "Native.h"
#include "pipeline.h"
#include <android/log.h>
#include <android/bitmap.h>

// 【新增】定义日志宏，确保能打印出 LOGE
#define LOG_TAG "PaddleDebug"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// 辅助函数：保存 Mat 到文件
void debug_save_mat(const cv::Mat &mat, std::string name, std::string saveDir) {
    if (mat.empty()) return;
    // 拼接路径： /storage/.../files/debug_native_crop.jpg
    std::string fullPath = saveDir + "/debug_native_" + name + ".jpg";

    // 如果是 RGBA，OpenCV 保存时通常需要转成 BGR，否则颜色会反（变蓝）
    cv::Mat saveImg;
    if (mat.channels() == 4) {
        cv::cvtColor(mat, saveImg, cv::COLOR_RGBA2BGR);
    } else {
        saveImg = mat;
    }

    bool success = cv::imwrite(fullPath, saveImg);
    if (success) {
        LOGE("✅ Native图片已保存: %s", fullPath.c_str());
    } else {
        LOGE("❌ Native保存失败，请检查路径权限: %s", fullPath.c_str());
    }
}

// 简化的 Bitmap 转 Mat (依然需要 OpenCV 的 Mat 结构，但只是作为容器)
cv::Mat bitmap_to_cv_mat(JNIEnv *env, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels = 0;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return cv::Mat();
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return cv::Mat();
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return cv::Mat();

    // CV_8UC4 对应 RGBA
    cv::Mat rgba(info.height, info.width, CV_8UC4, pixels);
    cv::Mat bgr;
    // 需要 OpenCV 进行颜色空间转换 (RGBA -> BGR) 因为模型训练用的是 BGR
    cv::cvtColor(rgba, bgr, cv::COLOR_RGBA2BGR);

    AndroidBitmap_unlockPixels(env, bitmap);
    return bgr;
}

#ifdef __cplusplus
extern "C" {
#endif

// 1. Init
JNIEXPORT jlong JNICALL
Java_in_co_washing_1machine_mushroomscanner_Native_nativeInit(
        JNIEnv *env, jclass thiz, jstring jDetModelPath, jstring jClsModelPath,
        jstring jRecModelPath, jstring jConfigPath, jstring jLabelPath,
        jint cpuThreadNum, jstring jCPUPowerMode) {

    std::string detModelPath = jstring_to_cpp_string(env, jDetModelPath);
    std::string clsModelPath = jstring_to_cpp_string(env, jClsModelPath);
    std::string recModelPath = jstring_to_cpp_string(env, jRecModelPath);
    std::string configPath = jstring_to_cpp_string(env, jConfigPath);
    std::string labelPath = jstring_to_cpp_string(env, jLabelPath);
    std::string cpuPowerMode = jstring_to_cpp_string(env, jCPUPowerMode);

    LOGE("Init Paddle Start...");
    LOGE("Init Paddle: DetPath=%s", detModelPath.c_str());
    LOGE("Init Paddle: RecPath=%s", recModelPath.c_str());

    // 添加简单的文件存在性检查
    FILE *f = fopen(detModelPath.c_str(), "r");
    if (f == nullptr) {
        LOGE("❌ 文件无法打开: %s", detModelPath.c_str());
    } else {
        fclose(f);
        LOGE("✅ 文件检查通过");
    }

    // 尝试捕获异常，防止直接崩溃
    try {
        auto *p = new Pipeline(detModelPath, clsModelPath, recModelPath, cpuPowerMode,
                               cpuThreadNum, configPath, labelPath);
        LOGE("✅ Pipeline 创建成功");
        return reinterpret_cast<jlong>(p);
    } catch (const std::exception &e) {
        LOGE("❌ Paddle 初始化崩溃: %s", e.what());
        return 0;
    } catch (...) {
        LOGE("❌ Paddle 初始化发生未知崩溃");
        return 0;
    }
}

// 2. Release
JNIEXPORT jboolean JNICALL
Java_in_co_washing_1machine_mushroomscanner_Native_nativeRelease(JNIEnv *env,
                                                                 jclass thiz,
                                                                 jlong ctx) {
    if (ctx == 0) return JNI_FALSE;
    Pipeline *pipeline = reinterpret_cast<Pipeline *>(ctx);
    delete pipeline;
    return JNI_TRUE;
}

// 3. RunImage (核心业务 - 增加详细打桩)
JNIEXPORT jobject JNICALL
Java_in_co_washing_1machine_mushroomscanner_Native_nativeRunImage(
        JNIEnv *env, jclass thiz, jlong ctx, jobject bitmap, jstring jDebugDir) {

    LOGE("--- Native: runImage Start ---");

    if (ctx == 0) {
        LOGE("Error: Context is null (OCR engine not initialized)");
        return nullptr;
    }
    if (bitmap == nullptr) {
        LOGE("Error: Bitmap is null");
        return nullptr;
    }

    // 转换路径字符串
    std::string debugDir = jstring_to_cpp_string(env, jDebugDir);

    Pipeline *pipeline = reinterpret_cast<Pipeline *>(ctx);

    // 步骤1：图片转换
    LOGE("Step 1: Converting Bitmap to Mat...");
    cv::Mat img = bitmap_to_cv_mat(env, bitmap);
    if (img.empty()) {
        LOGE("Error: Converted Mat is empty (Bitmap issue)");
        return nullptr;
    }
    if (debugDir.length() > 0) {
        debug_save_mat(img, "input_check", debugDir);
    }
    LOGE("Step 1 Done. Mat size: %d x %d", img.cols, img.rows);

    // 步骤2：执行预测 (最容易崩的地方)
    LOGE("Step 2: Calling Pipeline->RunOcr...");
    std::vector<OcrResultCpp> cppResults;
    cppResults = pipeline->RunOcr(img);
    LOGE("Step 2 Done. Found %d results.", (int) cppResults.size());

    // 步骤3：结果回传 Java
    LOGE("Step 3: Converting C++ results to Java...");
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListInit = env->GetMethodID(arrayListClass, "<init>", "()V");
    jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    jobject resultList = env->NewObject(arrayListClass, arrayListInit);

    jclass resultClass = env->FindClass("in/co/washing_machine/mushroomscanner/ocr/OcrResult");
    if (resultClass == nullptr) {
        LOGE("Error: Can't find OcrResult class");
        return nullptr;
    }
    jmethodID resultInit = env->GetMethodID(resultClass, "<init>",
                                            "(Ljava/lang/String;FLjava/util/List;)V");
    jclass pointClass = env->FindClass("android/graphics/Point");
    jmethodID pointInit = env->GetMethodID(pointClass, "<init>", "(II)V");

    for (int i = 0; i < cppResults.size(); i++) {
        const auto &res = cppResults[i];
        // 打印每个结果的标签，确认识别是否成功
        LOGE("  Result %d: %s", i, res.label.c_str());

        jstring label = cpp_string_to_jstring(env, res.label);
        jobject pointsList = env->NewObject(arrayListClass, arrayListInit);

        for (const auto &pt: res.box) {
            jobject point = env->NewObject(pointClass, pointInit, static_cast<jint>(pt[0]),
                                           static_cast<jint>(pt[1]));
            env->CallBooleanMethod(pointsList, arrayListAdd, point);
            env->DeleteLocalRef(point);
        }

        jobject resultObj = env->NewObject(resultClass, resultInit, label, (jfloat) res.confidence,
                                           pointsList);
        env->CallBooleanMethod(resultList, arrayListAdd, resultObj);

        // 释放局部引用防止内存溢出
        env->DeleteLocalRef(label);
        env->DeleteLocalRef(pointsList);
        env->DeleteLocalRef(resultObj);
    }

    LOGE("--- Native: runImage Finished ---");
    return resultList;
}

#ifdef __cplusplus
}
#endif