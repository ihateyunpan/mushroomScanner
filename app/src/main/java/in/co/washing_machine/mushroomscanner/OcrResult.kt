package `in`.co.washing_machine.mushroomscanner.ocr

import android.graphics.Point
import android.graphics.Rect

/**
 * 封装 PaddleOCR 返回的单条结果
 */
data class OcrResult(
    val label: String,      // 识别出的文字
    val confidence: Float,  // 置信度 (0.0 - 1.0)
    val points: List<Point> // 文字框的四个角坐标
) {
    // 辅助属性：将点转换为 Android 的 Rect 对象
    val rect: Rect
        get() {
            if (points.isEmpty()) return Rect()
            val left = points.minOf { it.x }
            val top = points.minOf { it.y }
            val right = points.maxOf { it.x }
            val bottom = points.maxOf { it.y }
            return Rect(left, top, right, bottom)
        }
}