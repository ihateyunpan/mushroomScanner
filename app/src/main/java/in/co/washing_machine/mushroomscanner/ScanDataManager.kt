package `in`.co.washing_machine.mushroomscanner

import android.content.Context
import android.text.Html
import android.text.Spanned
import androidx.lifecycle.MutableLiveData

object ScanDataManager {
    // 1. 扫描到的菌子列表
    val mushroomList = MutableLiveData<MutableList<String>>(mutableListOf())

    // 2. 日志列表 (改为存储 CharSequence 以支持 Spannable/Html 样式)
    val logList = MutableLiveData<CharSequence>("")

    private val logBuilder = StringBuilder()

    // 3. 扫描区域配置 (存储上下百分比，0.0 - 1.0)
    var scanRegionConfig: Pair<Float, Float>? = null

    // 4. 【新增】自定义滑动相关配置
    var gestureProfile: GestureProfile? = null
    var waitAfterScrollMs: Int = 200 // 默认 200ms

    // 重置信号
    private var onResetListener: (() -> Unit)? = null

    // --- 持久化相关常量 ---
    private const val PREFS_NAME = "MushroomPrefs"
    private const val KEY_TOP_RATIO = "top_ratio"
    private const val KEY_BOTTOM_RATIO = "bottom_ratio"
    private const val KEY_HAS_CONFIG = "has_config"

    // 新增 Key
    private const val KEY_GESTURE_START_X = "g_sx"
    private const val KEY_GESTURE_START_Y = "g_sy"
    private const val KEY_GESTURE_END_X = "g_ex"
    private const val KEY_GESTURE_END_Y = "g_ey"
    private const val KEY_GESTURE_DURATION = "g_dur"
    private const val KEY_WAIT_MS = "wait_ms"

    // 数据类：存储归一化的手势坐标 (0.0~1.0)，适配屏幕旋转
    data class GestureProfile(
        val startX: Float, val startY: Float,
        val endX: Float, val endY: Float,
        val duration: Long
    )

    // --- 操作方法 ---

    fun addMushroom(name: String) {
        val currentList = mushroomList.value ?: mutableListOf()
        if (!currentList.contains(name)) {
            currentList.add(name)
            mushroomList.postValue(currentList)
        }
    }

    /**
     * 添加日志，支持简单的 HTML 格式
     * 例如: "<font color='#FF0000'>Error</font>"
     */
    fun addLog(msg: String) {
        // 将普通换行符替换为 <br> 以便 Html.fromHtml 识别
        // 但为了日志清晰，这里我们每次 addLog 视为一行
        // 如果 msg 包含 Raw 结果且以“菌”结尾，高亮处理
        val processedMsg = processLogMessage(msg)
        
        logBuilder.append(processedMsg).append("<br>")
        
        // 解析 HTML 为 Spanned
        val spanned: Spanned = Html.fromHtml(logBuilder.toString(), Html.FROM_HTML_MODE_COMPACT)
        logList.postValue(spanned)
    }

    private fun processLogMessage(msg: String): String {
        // 1. 如果是 RAW 日志，且以 '菌' 结尾
        if (msg.startsWith("RAW:") && msg.trim().endsWith("菌")) {
            // 高亮为橙色
            return "<font color='#FF9800'><b>$msg</b></font>"
        }
        
        // 2. 如果是匹配成功的日志 (精确或模糊匹配)
        if (msg.contains("✨ 模糊匹配") || msg.contains("🎯 精确匹配")) {
             // 高亮为绿色
             return "<font color='#4CAF50'><b>$msg</b></font>"
        }
        
        // 3. 错误日志
        if (msg.contains("❌") || msg.contains("🛑")) {
            return "<font color='#F44336'>$msg</font>"
        }
        
        // 4. 普通日志，处理颜色转义等
        // 将普通文本中的 < > 转义，防止干扰 HTML 解析 (简单处理)
        return msg.replace("<", "&lt;").replace(">", "&gt;")
    }

    fun updateScanRegion(topRatio: Float, bottomRatio: Float) {
        scanRegionConfig = Pair(topRatio, bottomRatio)
    }

    fun updateGesture(profile: GestureProfile) {
        gestureProfile = profile
    }

    fun updateWaitTime(ms: Int) {
        waitAfterScrollMs = ms
    }

    fun saveConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // 保存区域
        scanRegionConfig?.let {
            editor.putFloat(KEY_TOP_RATIO, it.first)
            editor.putFloat(KEY_BOTTOM_RATIO, it.second)
            editor.putBoolean(KEY_HAS_CONFIG, true)
        }

        // 保存手势
        gestureProfile?.let {
            editor.putFloat(KEY_GESTURE_START_X, it.startX)
            editor.putFloat(KEY_GESTURE_START_Y, it.startY)
            editor.putFloat(KEY_GESTURE_END_X, it.endX)
            editor.putFloat(KEY_GESTURE_END_Y, it.endY)
            editor.putLong(KEY_GESTURE_DURATION, it.duration)
        }

        // 保存等待时间
        editor.putInt(KEY_WAIT_MS, waitAfterScrollMs)

        editor.apply()
    }

    fun loadConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 加载区域
        if (prefs.getBoolean(KEY_HAS_CONFIG, false)) {
            val top = prefs.getFloat(KEY_TOP_RATIO, 0.1f)
            val bottom = prefs.getFloat(KEY_BOTTOM_RATIO, 0.1f)
            scanRegionConfig = Pair(top, bottom)
        }

        // 加载手势
        if (prefs.contains(KEY_GESTURE_START_X)) {
            gestureProfile = GestureProfile(
                startX = prefs.getFloat(KEY_GESTURE_START_X, 0.5f),
                startY = prefs.getFloat(KEY_GESTURE_START_Y, 0.8f),
                endX = prefs.getFloat(KEY_GESTURE_END_X, 0.5f),
                endY = prefs.getFloat(KEY_GESTURE_END_Y, 0.4f),
                duration = prefs.getLong(KEY_GESTURE_DURATION, 1000L)
            )
        }

        // 加载等待时间
        waitAfterScrollMs = prefs.getInt(KEY_WAIT_MS, 200)
    }

    fun setOnResetListener(listener: () -> Unit) {
        onResetListener = listener
    }

    fun triggerResetFloatingWindow() {
        onResetListener?.invoke()
    }

    fun clearAll() {
        clearMushrooms()
        clearLogs()
    }

    fun clearMushrooms() {
        mushroomList.postValue(mutableListOf())
    }

    fun clearLogs() {
        logBuilder.setLength(0)
        logList.postValue("")
    }
}