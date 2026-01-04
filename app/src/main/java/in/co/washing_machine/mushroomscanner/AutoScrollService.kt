package `in`.co.washing_machine.mushroomscanner

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Display
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

class AutoScrollService : AccessibilityService() {

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    // 基础布局 Flag：确保悬浮窗全屏覆盖，包含状态栏区域
    private val BASE_LAYOUT_FLAGS = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

    // 窗口引用
    private var floatingView: View? = null
    private var floatParams: WindowManager.LayoutParams? = null
    private var overlayLayout: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var overlayView: OverlayView? = null

    // 悬浮球内控件
    private var layoutMenuLeft: View? = null
    private var layoutMenuRight: View? = null
    private var layoutLogs: View? = null
    private var tvLogs: TextView? = null
    private var scrollView: ScrollView? = null

    // --- 设置菜单 UI ---
    private var layoutSettingsMenu: LinearLayout? = null
    private var etWaitTime: EditText? = null

    // --- 区域配置 UI ---
    private var viewDimTop: View? = null
    private var viewDimBottom: View? = null
    private var handleTop: ImageView? = null
    private var handleBottom: ImageView? = null
    private var btnConfirmRegion: Button? = null
    private var tvConfigHint: TextView? = null

    // --- 手势配置 UI ---
    private var viewGestureTouch: View? = null
    private var tvGestureHint: TextView? = null
    private var layoutGestureConfirm: View? = null

    // --- 通知 UI ---
    private var layoutNotificationBar: View? = null
    private var tvOverlayNotification: TextView? = null

    // 状态控制
    private var isScanning = false
    private var isPaused = false
    private var isCheckingEnvironment = false

    // 配置状态枚举
    enum class ConfigState { NONE, MENU, REGION, GESTURE }

    private var configState = ConfigState.NONE

    // 扫描数据
    private val collectedMushrooms = mutableSetOf<String>()
    private var scrollCount = 0
    private var screenHeight = 0
    private var screenWidth = 0

    // 临时记录手势
    private var tempGesture: ScanDataManager.GestureProfile? = null

    private val recognizer by lazy {
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    }
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()

        // 获取真实的屏幕高度（包含状态栏/导航栏）
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenHeight = metrics.heightPixels
        screenWidth = metrics.widthPixels

        ScanDataManager.loadConfig(this)
        ScanDataManager.setOnResetListener { resetFloatingWindows() }
    }

    override fun onServiceConnected() {
        Log.d("Mushroom", "Service Connected")
        setupWindows()
    }

    private fun setupWindows() {
        val themeContext = ContextThemeWrapper(this, R.style.Theme_MushroomScanner)
        overlayLayout =
            LayoutInflater.from(themeContext).inflate(R.layout.layout_scan_overlay, null)

        // 初始化 Overlay 参数：基础全屏 Flag + 默认穿透 (不可聚焦 & 不可触摸)
        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        bindOverlayControls()

        floatingView =
            LayoutInflater.from(themeContext).inflate(R.layout.layout_floating_window, null)
        floatParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = screenWidth - 150
            y = 300
        }

        bindFloatingControls()

        try {
            windowManager.addView(overlayLayout, overlayParams)
            windowManager.addView(floatingView, floatParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bindOverlayControls() {
        overlayView = overlayLayout?.findViewById(R.id.overlay_view)
        layoutNotificationBar = overlayLayout?.findViewById(R.id.layout_notification_bar)
        tvOverlayNotification = overlayLayout?.findViewById(R.id.tv_overlay_notification)

        // 【关键修复】动态获取状态栏高度，并设置给通知栏的 TopMargin
        // 这样通知栏就会自动下移，避开刘海/挖孔
        val statusBarHeight = getStatusBarHeight()
        val params = layoutNotificationBar?.layoutParams as? FrameLayout.LayoutParams
        if (params != null) {
            params.topMargin = statusBarHeight
            layoutNotificationBar?.layoutParams = params
        }

        // 菜单
        layoutSettingsMenu = overlayLayout?.findViewById(R.id.layout_settings_menu)
        etWaitTime = overlayLayout?.findViewById(R.id.et_wait_time)

        overlayLayout?.findViewById<Button>(R.id.btn_config_region)
            ?.setOnClickListener { showRegionConfig() }
        overlayLayout?.findViewById<Button>(R.id.btn_config_gesture)
            ?.setOnClickListener { showGestureConfig() }
        overlayLayout?.findViewById<Button>(R.id.btn_close_settings)
            ?.setOnClickListener { exitConfigMode(false) }
        overlayLayout?.findViewById<Button>(R.id.btn_save_settings)
            ?.setOnClickListener { exitConfigMode(true) }

        // 点击输入框时主动请求焦点
        etWaitTime?.setOnClickListener { focusInput() }

        // 区域配置
        viewDimTop = overlayLayout?.findViewById(R.id.view_dim_top)
        viewDimBottom = overlayLayout?.findViewById(R.id.view_dim_bottom)
        handleTop = overlayLayout?.findViewById(R.id.handle_top)
        handleBottom = overlayLayout?.findViewById(R.id.handle_bottom)
        btnConfirmRegion = overlayLayout?.findViewById(R.id.btn_confirm_region)
        tvConfigHint = overlayLayout?.findViewById(R.id.tv_config_hint)
        btnConfirmRegion?.setOnClickListener { returnToSettingsMenu() }

        // 手势配置
        viewGestureTouch = overlayLayout?.findViewById(R.id.view_gesture_touch)
        tvGestureHint = overlayLayout?.findViewById(R.id.tv_gesture_hint)
        layoutGestureConfirm = overlayLayout?.findViewById(R.id.layout_gesture_confirm)

        setupGestureRecording()

        overlayLayout?.findViewById<Button>(R.id.btn_retry_gesture)
            ?.setOnClickListener { retryGesture() }
        overlayLayout?.findViewById<Button>(R.id.btn_confirm_gesture)?.setOnClickListener {
            tempGesture?.let { ScanDataManager.updateGesture(it) }
            returnToSettingsMenu()
        }
    }

    // 【新增】获取状态栏高度的辅助方法
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        // 如果获取失败，给一个保底值 24dp
        if (result == 0) {
            result = (24 * resources.displayMetrics.density).toInt()
        }
        return result
    }

    private fun resetFloatingWindows() {
        try {
            if (floatingView != null) windowManager.removeView(floatingView)
            if (overlayLayout != null) windowManager.removeView(overlayLayout)
        } catch (e: Exception) {
        }
        setupWindows()
    }

    private fun bindFloatingControls() {
        val layoutIcon = floatingView?.findViewById<View>(R.id.layout_icon)
        layoutMenuLeft = floatingView?.findViewById(R.id.layout_menu_left)
        layoutMenuRight = floatingView?.findViewById(R.id.layout_menu_right)
        layoutLogs = floatingView?.findViewById(R.id.layout_logs)
        tvLogs = floatingView?.findViewById(R.id.tv_logs)
        scrollView = tvLogs?.parent as? ScrollView

        bindMenuButtons(layoutMenuLeft)
        bindMenuButtons(layoutMenuRight)

        if (layoutIcon != null) setupDragListener(layoutIcon)
    }

    private fun bindMenuButtons(parent: View?) {
        if (parent == null) return
        parent.findViewById<ImageButton>(R.id.btn_toggle)?.setOnClickListener { toggleScan() }
        parent.findViewById<ImageButton>(R.id.btn_clear)?.setOnClickListener { clearData() }
        parent.findViewById<ImageButton>(R.id.btn_home)?.setOnClickListener { goHome() }
        parent.findViewById<ImageButton>(R.id.btn_log)?.setOnClickListener { toggleLogVisibility() }
        parent.findViewById<ImageButton>(R.id.btn_settings)
            ?.setOnClickListener { openSettingsMenu() }
    }

    // ===========================
    //       配置模式状态机
    // ===========================

    // 1. 打开主设置菜单
    private fun openSettingsMenu() {
        if (isScanning) return
        configState = ConfigState.MENU

        collapseMenu()
        floatingView?.visibility = View.GONE

        // 菜单模式：需要键盘，所以只保留 BASE_LAYOUT，移除 NOT_FOCUSABLE
        overlayParams?.flags = BASE_LAYOUT_FLAGS
        overlayParams?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        windowManager.updateViewLayout(overlayLayout, overlayParams)

        layoutSettingsMenu?.visibility = View.VISIBLE
        etWaitTime?.setText(ScanDataManager.waitAfterScrollMs.toString())

        focusInput()
    }

    private fun focusInput() {
        etWaitTime?.post {
            etWaitTime?.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etWaitTime, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    // 2. 进入区域配置
    private fun showRegionConfig() {
        configState = ConfigState.REGION
        layoutSettingsMenu?.visibility = View.GONE

        // 区域配置：需要触摸 (拖动)，不需要键盘 (加 NOT_FOCUSABLE)
        overlayParams?.flags = BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager.updateViewLayout(overlayLayout, overlayParams)

        viewDimTop?.visibility = View.VISIBLE
        viewDimBottom?.visibility = View.VISIBLE
        handleTop?.visibility = View.VISIBLE
        handleBottom?.visibility = View.VISIBLE
        btnConfirmRegion?.visibility = View.VISIBLE
        tvConfigHint?.visibility = View.VISIBLE
        layoutNotificationBar?.visibility = View.VISIBLE
        tvOverlayNotification?.text = "我是通知栏，请保留"

        val (currentTop, currentBottom) = ScanDataManager.scanRegionConfig ?: Pair(0.1f, 0.1f)
        updateRegionUI(currentTop, currentBottom)

        setupRegionDrag(handleTop!!, true)
        setupRegionDrag(handleBottom!!, false)
    }

    // 3. 进入手势配置
    private fun showGestureConfig() {
        configState = ConfigState.GESTURE
        layoutSettingsMenu?.visibility = View.GONE

        // 手势配置：需要触摸，不需要键盘
        overlayParams?.flags = BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager.updateViewLayout(overlayLayout, overlayParams)

        viewGestureTouch?.visibility = View.VISIBLE
        tvGestureHint?.visibility = View.VISIBLE
        layoutGestureConfirm?.visibility = View.GONE
        overlayView?.clearGesturePath()
    }

    // 返回主菜单
    private fun returnToSettingsMenu() {
        viewDimTop?.visibility = View.GONE
        viewDimBottom?.visibility = View.GONE
        handleTop?.visibility = View.GONE
        handleBottom?.visibility = View.GONE
        btnConfirmRegion?.visibility = View.GONE
        tvConfigHint?.visibility = View.GONE
        layoutNotificationBar?.visibility = View.GONE

        viewGestureTouch?.visibility = View.GONE
        tvGestureHint?.visibility = View.GONE
        layoutGestureConfirm?.visibility = View.GONE
        overlayView?.clearGesturePath()

        layoutSettingsMenu?.visibility = View.VISIBLE
        configState = ConfigState.MENU

        // 菜单模式：恢复可聚焦 (键盘)
        overlayParams?.flags = BASE_LAYOUT_FLAGS
        overlayParams?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        windowManager.updateViewLayout(overlayLayout, overlayParams)
    }

    // 退出配置模式
    private fun exitConfigMode(save: Boolean) {
        if (save) {
            val waitMs = etWaitTime?.text?.toString()?.toIntOrNull() ?: 200
            ScanDataManager.updateWaitTime(waitMs)
            ScanDataManager.saveConfig(this)
            Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show()
        }

        configState = ConfigState.NONE
        layoutSettingsMenu?.visibility = View.GONE

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etWaitTime?.windowToken, 0)

        // 恢复默认状态：穿透 (不可聚焦 & 不可触摸)
        overlayParams?.flags =
            BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        windowManager.updateViewLayout(overlayLayout, overlayParams)

        floatingView?.visibility = View.VISIBLE
    }

    // --- 区域配置逻辑 ---
    private fun updateRegionUI(topRatio: Float, bottomRatio: Float) {
        val topHeight = (screenHeight * topRatio).toInt()
        val bottomHeight = (screenHeight * bottomRatio).toInt()

        viewDimTop?.layoutParams?.height = topHeight
        viewDimTop?.requestLayout()
        viewDimBottom?.layoutParams?.height = bottomHeight
        viewDimBottom?.requestLayout()

        (handleTop?.layoutParams as FrameLayout.LayoutParams).topMargin = topHeight
        (handleBottom?.layoutParams as FrameLayout.LayoutParams).bottomMargin = bottomHeight
        handleTop?.requestLayout()
        handleBottom?.requestLayout()
    }

    private fun setupRegionDrag(handle: View, isTop: Boolean) {
        handle.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE) {
                val rawY = event.rawY
                if (isTop) {
                    val ratio = (rawY / screenHeight).coerceIn(0.05f, 0.4f)
                    ScanDataManager.updateScanRegion(
                        ratio,
                        ScanDataManager.scanRegionConfig?.second ?: 0.1f
                    )
                    updateRegionUI(ratio, ScanDataManager.scanRegionConfig!!.second)
                } else {
                    val ratio = ((screenHeight - rawY) / screenHeight).coerceIn(0.05f, 0.4f)
                    ScanDataManager.updateScanRegion(
                        ScanDataManager.scanRegionConfig?.first ?: 0.1f, ratio
                    )
                    updateRegionUI(ScanDataManager.scanRegionConfig!!.first, ratio)
                }
            }
            true
        }
    }

    // --- 手势录制逻辑 ---
    private fun setupGestureRecording() {
        viewGestureTouch?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tempGesture = ScanDataManager.GestureProfile(event.rawX, event.rawY, 0f, 0f, 0)
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    val startX = tempGesture?.startX ?: 0f
                    val startY = tempGesture?.startY ?: 0f
                    val endX = event.rawX
                    val endY = event.rawY

                    overlayView?.showGesturePath(startX, startY, endX, endY)

                    val duration = 500L
                    tempGesture = ScanDataManager.GestureProfile(
                        startX / screenWidth, startY / screenHeight,
                        endX / screenWidth, endY / screenHeight,
                        duration
                    )

                    tvGestureHint?.visibility = View.GONE
                    layoutGestureConfirm?.visibility = View.VISIBLE
                    viewGestureTouch?.visibility = View.GONE

                    // 立即回放测试
                    performRecordedScroll(tempGesture!!, isTestRun = true)

                    return@setOnTouchListener true
                }
            }
            true
        }
    }

    private fun retryGesture() {
        overlayView?.clearGesturePath()
        layoutGestureConfirm?.visibility = View.GONE
        viewGestureTouch?.visibility = View.VISIBLE
        tvGestureHint?.visibility = View.VISIBLE
        tempGesture = null
    }

    // ===========================
    //       按键监听
    // ===========================
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (isScanning) {
                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    stopScanning("🔊 检测到音量键，停止扫描")
                    return true
                }
            }
            if (configState != ConfigState.NONE && event.keyCode == KeyEvent.KEYCODE_BACK) {
                if (configState == ConfigState.MENU) {
                    exitConfigMode(false)
                } else {
                    returnToSettingsMenu()
                }
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    // ===========================
    //       扫描逻辑
    // ===========================

    private fun toggleScan() {
        if (isScanning) {
            stopScanning("用户手动停止")
        } else {
            if (ScanDataManager.scanRegionConfig == null) {
                Toast.makeText(this, "⚠️ 初次使用请先配置扫描区域", Toast.LENGTH_LONG).show()
                openSettingsMenu()
                showRegionConfig()
                return
            }
            if (!isCheckingEnvironment) {
                checkEnvironmentAndStart()
            }
        }
    }

    private fun checkEnvironmentAndStart() {
        if (isCheckingEnvironment) return
        isCheckingEnvironment = true
        appendLog("🔍 正在检测当前页面...")
        serviceScope.launch {
            val wasMenuVisible =
                (layoutMenuLeft?.visibility == View.VISIBLE || layoutMenuRight?.visibility == View.VISIBLE)
            collapseMenu()
            delay(150)

            takeScreenshotSingle { bitmap ->
                if (bitmap == null) {
                    isCheckingEnvironment = false
                    showTemporaryNotification("❌ 截图失败")
                    restorePanelAndLog("❌ 截图失败", wasMenuVisible)
                    return@takeScreenshotSingle
                }

                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val allText = visionText.text
                        val isTargetPage =
                            allText.contains("菌子图鉴") || (allText.contains("收集度") && allText.contains(
                                "菌子"
                            ))

                        if (isTargetPage) {
                            isCheckingEnvironment = false
                            startScanning()
                        } else {
                            isCheckingEnvironment = false
                            showTemporaryNotification("❌ 不在图鉴页面")
                            restorePanelAndLog("❌ 不在图鉴页面", wasMenuVisible)
                        }
                    }
                    .addOnFailureListener {
                        isCheckingEnvironment = false
                        restorePanelAndLog("❌ 检测错误", wasMenuVisible)
                    }
            }
        }
    }

    private fun startScanning() {
        isScanning = true
        isPaused = false
        scrollCount = 0
        updateToggleButtonIcon(R.drawable.ic_stop)
        layoutNotificationBar?.visibility = View.VISIBLE
        showNotificationText("🚀 扫描已开始...")

        val (topRatio, bottomRatio) = ScanDataManager.scanRegionConfig ?: Pair(0.0f, 0.0f)

        serviceScope.launch {
            updateRegionUI(topRatio, bottomRatio)
            viewDimTop?.visibility = View.VISIBLE
            viewDimBottom?.visibility = View.VISIBLE
            delay(500)
            viewDimTop?.visibility = View.GONE
            viewDimBottom?.visibility = View.GONE
            scanLoop()
        }
    }

    private fun stopScanning(reason: String) {
        isScanning = false
        isPaused = false
        updateToggleButtonIcon(R.drawable.ic_play)
        showTemporaryNotification("🛑 $reason")

        serviceScope.launch {
            delay(3000)
            layoutNotificationBar?.visibility = View.GONE
            overlayView?.clearRects()
        }

        val resultString = collectedMushrooms.joinToString("\n")
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Mushrooms", resultString))
        appendLog("✅ 扫描结束: $reason")
        appendLog("共收集 ${collectedMushrooms.size} 个")
        expandMenu()
    }

    private fun scanLoop() {
        if (!isScanning) return
        serviceScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(result: ScreenshotResult) {
                            val bitmap =
                                Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)
                            val copy = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
                            result.hardwareBuffer.close()
                            if (copy != null) processScanImage(copy) else stopScanning("截图为空")
                        }

                        override fun onFailure(code: Int) {
                            stopScanning("截图失败")
                        }
                    })
            }
        }
    }

    private fun processScanImage(fullBitmap: Bitmap) {
        if (!isScanning) return
        val (topRatio, bottomRatio) = ScanDataManager.scanRegionConfig ?: Pair(0.0f, 0.0f)
        val cropY = (fullBitmap.height * topRatio).toInt()
        val cropHeight = (fullBitmap.height * (1.0f - topRatio - bottomRatio)).toInt()

        if (cropHeight <= 0) {
            stopScanning("区域无效"); return
        }

        val croppedBitmap = Bitmap.createBitmap(fullBitmap, 0, cropY, fullBitmap.width, cropHeight)
        val image = InputImage.fromBitmap(croppedBitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val matchedRects = mutableListOf<Rect>()
                val matchedNamesUI = mutableListOf<String>()

                for (block in visionText.textBlocks) {
                    val ocrText = block.text.trim()
                    if (ocrText.isEmpty()) continue

                    appendLog("RAW: ${ocrText.replace("\n", " ")}")

                    var matchedName: String? = null
                    var isFuzzy = false

                    // A. 精确匹配
                    val exactMatch = MushroomData.allNames.find { ocrText.contains(it) }

                    if (exactMatch != null) {
                        matchedName = exactMatch
                    } else {
                        // B. 模糊匹配
                        val fuzzyMatch =
                            EnhancedFuzzyMatcher.findBestMatch(ocrText, MushroomData.allNames)
                        if (fuzzyMatch != null) {
                            matchedName = fuzzyMatch
                            isFuzzy = true
                        }
                    }

                    if (matchedName != null) {
                        // 去重逻辑
                        if (collectedMushrooms.contains(matchedName)) {
                            appendLog("忽略已存在: $matchedName")
                        } else {
                            val logPrefix = if (isFuzzy) "✨ 模糊匹配" else "🎯 精确匹配"
                            appendLog("$logPrefix: $matchedName")

                            collectedMushrooms.add(matchedName)
                            ScanDataManager.addMushroom(matchedName)
                            matchedNamesUI.add(matchedName)

                            block.boundingBox?.let { rect ->
                                // 修正坐标
                                rect.offset(0, cropY)
                                matchedRects.add(rect)
                            }
                        }
                    }
                }

                if (matchedNamesUI.isNotEmpty()) {
                    showNotificationText("发现: ${matchedNamesUI.joinToString(",")}")
                    overlayView?.updateMatchedRects(matchedRects)
                } else {
                    showNotificationText("本页无新发现")
                    overlayView?.clearRects()
                }

                serviceScope.launch {
                    delay(200)
                    overlayView?.clearRects()
                    performConfiguredScroll()
                }
            }
            .addOnFailureListener { stopScanning("识别出错") }
    }

    private fun performConfiguredScroll() {
        if (!isScanning) return
        val gesture = ScanDataManager.gestureProfile

        if (gesture != null) {
            performRecordedScroll(gesture)
        } else {
            val path = Path().apply {
                moveTo(screenWidth / 2f, screenHeight * 0.8f)
                lineTo(screenWidth / 2f, screenHeight * 0.4f)
            }
            dispatchScroll(path, 1000L)
        }
    }

    private fun performRecordedScroll(
        g: ScanDataManager.GestureProfile,
        isTestRun: Boolean = false
    ) {
        val path = Path().apply {
            moveTo(g.startX * screenWidth, g.startY * screenHeight)
            lineTo(g.endX * screenWidth, g.endY * screenHeight)
        }

        if (isTestRun) {
            // 试运行：确保 Overlay 穿透 (保留基础 Layout Flag)
            serviceScope.launch {
                overlayParams?.flags =
                    BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(overlayLayout, overlayParams)

                delay(200)

                dispatchScroll(path, g.duration) {
                    // 恢复配置模式：保留基础 Layout Flag，移除 Not Focusable
                    serviceScope.launch {
                        overlayParams?.flags =
                            BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        windowManager.updateViewLayout(overlayLayout, overlayParams)
                    }
                }
            }
        } else {
            dispatchScroll(path, g.duration)
        }
    }

    private fun dispatchScroll(path: Path, duration: Long, onFinish: (() -> Unit)? = null) {
        val gestureBuilder = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        dispatchGesture(gestureBuilder, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                onFinish?.invoke()
                if (onFinish == null) {
                    serviceScope.launch {
                        delay(ScanDataManager.waitAfterScrollMs.toLong())
                        scanLoop()
                    }
                }
            }
        }, null)
    }

    // --- Helpers ---
    private fun showNotificationText(msg: String) {
        serviceScope.launch { tvOverlayNotification?.text = msg }
    }

    private fun showTemporaryNotification(msg: String) {
        serviceScope.launch {
            layoutNotificationBar?.visibility = View.VISIBLE
            tvOverlayNotification?.text = msg
            delay(3000)
            if (!isScanning) layoutNotificationBar?.visibility = View.GONE
        }
    }

    private fun restorePanelAndLog(msg: String, wasMenuVisible: Boolean) {
        isCheckingEnvironment = false
        updateToggleButtonIcon(R.drawable.ic_play)
        if (wasMenuVisible) expandMenu()
        appendLog(msg)
    }

    private fun toggleMenu() {
        if (layoutMenuLeft?.visibility == View.VISIBLE || layoutMenuRight?.visibility == View.VISIBLE) collapseMenu() else expandMenu()
    }

    private fun collapseMenu() {
        layoutMenuLeft?.visibility = View.GONE; layoutMenuRight?.visibility =
            View.GONE; layoutLogs?.visibility = View.GONE
    }

    private fun expandMenu() {
        if ((floatParams?.x ?: 0) < screenWidth / 2) {
            layoutMenuLeft?.visibility = View.GONE; layoutMenuRight?.visibility = View.VISIBLE
        } else {
            layoutMenuRight?.visibility = View.GONE; layoutMenuLeft?.visibility = View.VISIBLE
        }
    }

    private fun setupDragListener(view: View) {
        view.setOnTouchListener(object : OnTouchListener {
            private var ix = 0;
            private var iy = 0;
            private var itx = 0f;
            private var ity = 0f;
            private var ic = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        ix = floatParams!!.x; iy = floatParams!!.y; itx = event.rawX; ity =
                            event.rawY; ic = true; return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - itx).toInt();
                        val dy = (event.rawY - ity).toInt()
                        if (abs(dx) > 10 || abs(dy) > 10) ic = false
                        floatParams!!.x = ix + dx; floatParams!!.y = iy + dy
                        windowManager.updateViewLayout(floatingView, floatParams); return true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (ic) toggleMenu(); return true
                    }
                }
                return false
            }
        })
    }

    private fun updateToggleButtonIcon(resId: Int) {
        layoutMenuLeft?.findViewById<ImageButton>(R.id.btn_toggle)?.setImageResource(resId)
        layoutMenuRight?.findViewById<ImageButton>(R.id.btn_toggle)?.setImageResource(resId)
    }

    private fun clearData() {
        ScanDataManager.clearAll(); collectedMushrooms.clear(); tvLogs?.text = ""; Toast.makeText(
            this,
            "数据已清空",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun goHome() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ); collapseMenu()
    }

    private fun toggleLogVisibility() {
        layoutLogs?.visibility =
            if (layoutLogs?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun appendLog(msg: String) {
        ScanDataManager.addLog(msg); serviceScope.launch {
            tvLogs?.append("$msg\n"); scrollView?.post {
            scrollView?.fullScroll(
                View.FOCUS_DOWN
            )
        }
        }
    }

    private fun takeScreenshotSingle(onResult: (Bitmap?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(r: ScreenshotResult) {
                    val b = Bitmap.wrapHardwareBuffer(r.hardwareBuffer, r.colorSpace)
                    onResult(b?.copy(Bitmap.Config.ARGB_8888, true)); r.hardwareBuffer.close()
                }

                override fun onFailure(i: Int) {
                    onResult(null)
                }
            })
        } else onResult(null)
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}
    override fun onDestroy() {
        super.onDestroy(); if (floatingView != null) windowManager.removeView(floatingView); if (overlayLayout != null) windowManager.removeView(
            overlayLayout
        )
    }

    object EnhancedFuzzyMatcher {
        private val CONFUSION_SETS = mapOf(
            Pair('茵', '菌') to 0.1, Pair('菌', '茵') to 0.1,
            Pair('菇', '姑') to 0.2, Pair('手', '毛') to 0.3,
            Pair('日', '曰') to 0.1, Pair('末', '未') to 0.1,
            Pair('土', '士') to 0.1, Pair('全', '金') to 0.1,
            Pair('大', '太') to 0.2, Pair('前', '茄') to 0.2,
            Pair('苏', '荪') to 0.1
        )

        fun findBestMatch(ocrText: String, candidates: List<String>): String? {
            var best: String? = null
            var maxScore = 0.0
            for (target in candidates) {
                if (abs(ocrText.length - target.length) > 3) continue
                val score = calculateMaxWeightedSimilarity(ocrText, target)
                if (score >= 0.75) {
                    if (score > maxScore) {
                        maxScore = score; best = target
                    } else if (abs(score - maxScore) < 0.001) {
                        if (target.length > (best?.length ?: 0)) best = target
                    }
                }
            }
            return best
        }

        private fun calculateMaxWeightedSimilarity(source: String, target: String): Double {
            if (source.length < target.length) {
                if (target.length - source.length > 2) return 0.0
                val dist = weightedLevenshtein(source, target)
                return 1.0 - (dist / target.length)
            }
            var maxSim = 0.0
            val windowSize = target.length
            for (i in 0..source.length - windowSize) {
                val sub = source.substring(i, i + windowSize)
                val dist = weightedLevenshtein(sub, target)
                val sim = 1.0 - (dist / max(sub.length, target.length))
                if (sim > maxSim) maxSim = sim
            }
            return maxSim
        }

        private fun weightedLevenshtein(s1: String, s2: String): Double {
            val n = s1.length
            val m = s2.length
            var dp = Array(n + 1) { DoubleArray(m + 1) }
            for (i in 0..n) dp[i][0] = i.toDouble()
            for (j in 0..m) dp[0][j] = j.toDouble()
            for (i in 1..n) {
                for (j in 1..m) {
                    val c1 = s1[i - 1]
                    val c2 = s2[j - 1]
                    val cost = if (c1 == c2) 0.0 else CONFUSION_SETS[Pair(c1, c2)] ?: 1.0
                    dp[i][j] =
                        minOf(dp[i - 1][j] + 1.0, dp[i][j - 1] + 1.0, dp[i - 1][j - 1] + cost)
                }
            }
            return dp[n][m]
        }
    }
}