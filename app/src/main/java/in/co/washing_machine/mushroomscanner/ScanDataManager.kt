package `in`.co.washing_machine.mushroomscanner

import android.content.Context
import android.text.Html
import android.text.Spanned
import androidx.lifecycle.MutableLiveData

object ScanDataManager {
    // 数据 LiveData
    val mushroomList = MutableLiveData<MutableList<String>>(mutableListOf())
    val logList = MutableLiveData<CharSequence>("")

    private val logBuilder = StringBuilder()

    // 配置相关
    var scanRegionConfig: Pair<Float, Float>? = null
    var gestureProfile: GestureProfile? = null
    var waitAfterScrollMs: Int = 200

    // 回调
    private var onResetListener: (() -> Unit)? = null
    private var onClearMushroomsCallback: (() -> Unit)? = null
    private var onClearLogsCallback: (() -> Unit)? = null

    // --- 持久化常量 ---
    private const val PREFS_NAME = "MushroomPrefs"

    // Config Keys
    private const val KEY_TOP_RATIO = "top_ratio"
    private const val KEY_BOTTOM_RATIO = "bottom_ratio"
    private const val KEY_HAS_CONFIG = "has_config"
    private const val KEY_GESTURE_START_X = "g_sx"
    private const val KEY_GESTURE_START_Y = "g_sy"
    private const val KEY_GESTURE_END_X = "g_ex"
    private const val KEY_GESTURE_END_Y = "g_ey"
    private const val KEY_GESTURE_DURATION = "g_dur"
    private const val KEY_WAIT_MS = "wait_ms"

    // Data Persistence Keys
    private const val KEY_SAVED_MUSHROOMS = "saved_mushrooms_set"
    private const val KEY_SAVED_LOGS = "saved_logs_html"

    data class GestureProfile(
        val startX: Float, val startY: Float,
        val endX: Float, val endY: Float,
        val duration: Long
    )

    // --- 操作方法 ---

    fun addMushroom(name: String, context: Context?) {
        val currentList = mushroomList.value ?: mutableListOf()
        if (!currentList.contains(name)) {
            currentList.add(name)
            mushroomList.postValue(currentList)
            // 【持久化】保存列表
            if (context != null) saveMushroomData(context, currentList)
        }
    }

    fun addLog(msg: String, context: Context?) {
        val processedMsg = processLogMessage(msg)
        logBuilder.append(processedMsg).append("<br>")
        val spanned: Spanned = Html.fromHtml(logBuilder.toString(), Html.FROM_HTML_MODE_COMPACT)
        logList.postValue(spanned)
        // 【持久化】保存日志 (注意：日志过多可能会影响性能，这里简单实现，生产环境建议限制长度)
        if (context != null) saveLogData(context, logBuilder.toString())
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

    // --- Config Update Methods (保持不变) ---
    fun updateScanRegion(topRatio: Float, bottomRatio: Float) {
        scanRegionConfig = Pair(topRatio, bottomRatio)
    }

    fun updateGesture(profile: GestureProfile) {
        gestureProfile = profile
    }

    fun updateWaitTime(ms: Int) {
        waitAfterScrollMs = ms
    }

    // --- Save/Load Logic ---

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

    // 【新增】保存菌子列表
    private fun saveMushroomData(context: Context, list: List<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_SAVED_MUSHROOMS, list.toSet()).apply()
    }

    // 【新增】保存日志
    private fun saveLogData(context: Context, htmlLog: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // 限制日志长度防止 SP 爆掉，只保留最近 50000 字符
        val saveStr = if (htmlLog.length > 50000) htmlLog.takeLast(50000) else htmlLog
        prefs.edit().putString(KEY_SAVED_LOGS, saveStr).apply()
    }

    fun loadConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. 加载配置
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

        // 2. 【新增】恢复菌子列表
        val savedSet = prefs.getStringSet(KEY_SAVED_MUSHROOMS, emptySet()) ?: emptySet()
        if (savedSet.isNotEmpty()) {
            val list = savedSet.toMutableList()
            mushroomList.postValue(list)
        }

        // 3. 【新增】恢复日志
        val savedLog = prefs.getString(KEY_SAVED_LOGS, "") ?: ""
        if (savedLog.isNotEmpty()) {
            logBuilder.setLength(0)
            logBuilder.append(savedLog)
            val spanned: Spanned = Html.fromHtml(logBuilder.toString(), Html.FROM_HTML_MODE_COMPACT)
            logList.postValue(spanned)
        }
    }

    // --- Listener Logic ---
    fun setOnResetListener(listener: () -> Unit) {
        onResetListener = listener
    }

    fun setOnClearMushroomsListener(listener: () -> Unit) {
        onClearMushroomsCallback = listener
    }

    fun setOnClearLogsListener(listener: () -> Unit) {
        onClearLogsCallback = listener
    }

    fun triggerResetFloatingWindow() {
        onResetListener?.invoke()
    }

    fun clearAll(context: Context) {
        clearMushrooms(context)
        clearLogs(context)
    }

    fun clearMushrooms(context: Context) {
        mushroomList.postValue(mutableListOf())
        saveMushroomData(context, emptyList()) // 清空持久化
        onClearMushroomsCallback?.invoke()
    }

    fun clearLogs(context: Context) {
        logBuilder.setLength(0)
        logList.postValue("")
        saveLogData(context, "") // 清空持久化
        onClearLogsCallback?.invoke()
    }
}