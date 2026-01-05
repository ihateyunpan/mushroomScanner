package in.co.washing_machine.mushroomscanner;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

import in.co.washing_machine.mushroomscanner.ocr.OcrResult;

public class Native {
    static {
        // 尝试加载库，名称取决于你的 Gradle 配置，通常是 "Native" 或 "paddle_lite_jni"
        try {
            System.loadLibrary("Native");
        } catch (UnsatisfiedLinkError e) {
            try {
                System.loadLibrary("paddle_lite_jni");
            } catch (UnsatisfiedLinkError e2) {
                e2.printStackTrace();
            }
        }
    }

    private long ctx = 0;

    // 初始化
    public boolean init(Context mContext,
                        String detModelPath,
                        String clsModelPath,
                        String recModelPath,
                        String configPath,
                        String labelPath,
                        int cpuThreadNum,
                        String cpuPowerMode) {
        ctx = nativeInit(
                detModelPath,
                clsModelPath,
                recModelPath,
                configPath,
                labelPath,
                cpuThreadNum,
                cpuPowerMode);
        return ctx == 0;
    }

    // 释放资源
    public boolean release() {
        if (ctx == 0) {
            return false;
        }
        boolean res = nativeRelease(ctx);
        ctx = 0;
        return res;
    }

    // 【新增】接收 Bitmap 并返回结果
    public ArrayList<OcrResult> runImage(Bitmap bitmap, String debugDir) {
        if (ctx == 0 || bitmap == null) {
            return new ArrayList<>();
        }
        return nativeRunImage(ctx, bitmap, debugDir);
    }

    // --- Native 方法声明 ---

    public static native long nativeInit(String detModelPath,
                                         String clsModelPath,
                                         String recModelPath,
                                         String configPath,
                                         String labelPath,
                                         int cpuThreadNum,
                                         String cpuPowerMode);

    public static native boolean nativeRelease(long ctx);

    public static native ArrayList<OcrResult> nativeRunImage(long ctx, Bitmap bitmap, String debugDir);
}