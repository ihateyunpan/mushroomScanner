package `in`.co.washing_machine.mushroomscanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 识别到的目标区域
    private val matchedRects = mutableListOf<Rect>()

    // 手势路径
    private var gesturePath: Path? = null

    // 1. 扫描框画笔 (半透明绿填充)
    private val paintHighlight = Paint().apply {
        color = Color.parseColor("#4D4CAF50")
        style = Paint.Style.FILL
    }
    private val paintBorder = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    // 2. 手势轨迹画笔 (红色阴影粗线条)
    private val paintGesture = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 20f // 适中的粗细
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        alpha = 150 // 半透明
        // 增加阴影效果
        setShadowLayer(10f, 0f, 0f, Color.RED)
    }

    // 关闭硬件加速以支持 ShadowLayer (在某些 View 上需要)
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制扫描框
        synchronized(matchedRects) {
            for (rect in matchedRects) {
                canvas.drawRect(rect, paintHighlight)
                canvas.drawRect(rect, paintBorder)
            }
        }

        // 绘制手势轨迹
        gesturePath?.let { path ->
            canvas.drawPath(path, paintGesture)
        }
    }

    fun updateMatchedRects(rects: List<Rect>) {
        synchronized(matchedRects) {
            matchedRects.clear()
            matchedRects.addAll(rects)
        }
        postInvalidate()
    }

    fun clearRects() {
        synchronized(matchedRects) {
            matchedRects.clear()
        }
        postInvalidate()
    }

    // 显示手势轨迹
    fun showGesturePath(startX: Float, startY: Float, endX: Float, endY: Float) {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        gesturePath = path
        postInvalidate()
    }

    // 清除手势轨迹
    fun clearGesturePath() {
        gesturePath = null
        postInvalidate()
    }
}