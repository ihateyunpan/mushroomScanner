package `in`.co.washing_machine.mushroomscanner

import android.content.Context
import android.text.Html
import android.text.Spanned
import androidx.lifecycle.MutableLiveData

object ScanDataManager {
    // 1. 扫描到的菌子列表
    val mushroomList = MutableLiveData<MutableList<String>>(mutableListOf())

    // 2. 日志列表
    val logList = MutableLiveData<CharSequence>("")

    private val logBuilder = StringBuilder()

    // 3. 扫描区域配置
    var scanRegionConfig: Pair<Float, Float>? = null

    // 4. 自定义滑动配置
    var gestureProfile: GestureProfile? = null
    var waitAfterScrollMs: Int = 200

    // 监听器
    private var onResetListener: (() -> Unit)? = null

    // 【新增】清空事件的监听器
    private var onClearMushroomsCallback: (() -> Unit)? = null
    private var onClearLogsCallback: (() -> Unit)? = null

    // --- 持久化相关常量 ---
    private const val PREFS_NAME = "MushroomPrefs"
    private const val KEY_TOP_RATIO = "top_ratio"
    private const val KEY_BOTTOM_RATIO = "bottom_ratio"
    private const val KEY_HAS_CONFIG = "has_config"
    private const val KEY_GESTURE_START_X = "g_sx"
    private const val KEY_GESTURE_START_Y = "g_sy"
    private const val KEY_GESTURE_END_X = "g_ex"
    private const val KEY_GESTURE_END_Y = "g_ey"
    private const val KEY_GESTURE_DURATION = "g_dur"
    private const val KEY_WAIT_MS = "wait_ms"

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

    fun addLog(msg: String) {
        val processedMsg = processLogMessage(msg)
        logBuilder.append(processedMsg).append("<br>")
        val spanned: Spanned = Html.fromHtml(logBuilder.toString(), Html.FROM_HTML_MODE_COMPACT)
        logList.postValue(spanned)
    }

    private fun processLogMessage(msg: String): String {
        if (msg.startsWith("RAW:") && msg.trim().endsWith("菌")) {
            return "<font color='#FF9800'><b>$msg</b></font>"
        }
        if (msg.contains("✨ 模糊匹配") || msg.contains("🎯 精确匹配")) {
            return "<font color='#4CAF50'><b>$msg</b></font>"
        }
        if (msg.contains("❌") || msg.contains("🛑")) {
            return "<font color='#F44336'>$msg</font>"
        }
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
        scanRegionConfig?.let {
            editor.putFloat(KEY_TOP_RATIO, it.first)
            editor.putFloat(KEY_BOTTOM_RATIO, it.second)
            editor.putBoolean(KEY_HAS_CONFIG, true)
        }
        gestureProfile?.let {
            editor.putFloat(KEY_GESTURE_START_X, it.startX)
            editor.putFloat(KEY_GESTURE_START_Y, it.startY)
            editor.putFloat(KEY_GESTURE_END_X, it.endX)
            editor.putFloat(KEY_GESTURE_END_Y, it.endY)
            editor.putLong(KEY_GESTURE_DURATION, it.duration)
        }
        editor.putInt(KEY_WAIT_MS, waitAfterScrollMs)
        editor.apply()
    }

    fun loadConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_HAS_CONFIG, false)) {
            val top = prefs.getFloat(KEY_TOP_RATIO, 0.1f)
            val bottom = prefs.getFloat(KEY_BOTTOM_RATIO, 0.1f)
            scanRegionConfig = Pair(top, bottom)
        }
        if (prefs.contains(KEY_GESTURE_START_X)) {
            gestureProfile = GestureProfile(
                startX = prefs.getFloat(KEY_GESTURE_START_X, 0.5f),
                startY = prefs.getFloat(KEY_GESTURE_START_Y, 0.8f),
                endX = prefs.getFloat(KEY_GESTURE_END_X, 0.5f),
                endY = prefs.getFloat(KEY_GESTURE_END_Y, 0.4f),
                duration = prefs.getLong(KEY_GESTURE_DURATION, 1000L)
            )
        }
        waitAfterScrollMs = prefs.getInt(KEY_WAIT_MS, 200)
    }

    fun setOnResetListener(listener: () -> Unit) {
        onResetListener = listener
    }

    // 【新增】设置清空列表的回调
    fun setOnClearMushroomsListener(listener: () -> Unit) {
        onClearMushroomsCallback = listener
    }

    // 【新增】设置清空日志的回调
    fun setOnClearLogsListener(listener: () -> Unit) {
        onClearLogsCallback = listener
    }

    fun triggerResetFloatingWindow() {
        onResetListener?.invoke()
    }

    // 清空所有 (会被悬浮窗的垃圾桶按钮调用)
    fun clearAll() {
        clearMushrooms()
        clearLogs()
    }

    // 清空菌子列表 (会被App的清空按钮调用)
    fun clearMushrooms() {
        mushroomList.postValue(mutableListOf())
        // 通知 Service 清空本地去重集合
        onClearMushroomsCallback?.invoke()
    }

    // 清空日志 (会被App的清空按钮调用)
    fun clearLogs() {
        logBuilder.setLength(0)
        logList.postValue("")
        // 通知 Service 清空悬浮窗文本
        onClearLogsCallback?.invoke()
    }
}