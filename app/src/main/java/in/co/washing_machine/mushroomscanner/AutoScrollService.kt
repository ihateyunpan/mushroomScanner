package `in`.co.washing_machine.mushroomscanner

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.core.app.NotificationCompat
import `in`.co.washing_machine.mushroomscanner.ocr.OcrResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class AutoScrollService : AccessibilityService() {

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    private val BASE_LAYOUT_FLAGS = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

    private var floatingView: View? = null
    private var floatParams: WindowManager.LayoutParams? = null
    private var overlayLayout: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var overlayView: OverlayView? = null

    // UI Components
    private var layoutMenuLeft: View? = null
    private var layoutMenuRight: View? = null
    private var layoutLogs: View? = null
    private var tvLogs: TextView? = null
    private var scrollView: ScrollView? = null
    private var layoutSettingsMenu: LinearLayout? = null
    private var etWaitTime: EditText? = null
    private var viewDimTop: View? = null
    private var viewDimBottom: View? = null
    private var handleTop: ImageView? = null
    private var handleBottom: ImageView? = null
    private var btnConfirmRegion: Button? = null
    private var tvConfigHint: TextView? = null
    private var viewGestureTouch: View? = null
    private var tvGestureHint: TextView? = null
    private var layoutGestureConfirm: View? = null
    private var layoutNotificationBar: View? = null
    private var tvOverlayNotification: TextView? = null

    private var isScanning = false
    private var isPaused = false
    private var isCheckingEnvironment = false

    enum class ConfigState { NONE, MENU, REGION, GESTURE }

    private var configState = ConfigState.NONE

    private val collectedMushrooms = mutableSetOf<String>()
    private var scrollCount = 0
    private var screenHeight = 0
    private var screenWidth = 0
    private var tempGesture: ScanDataManager.GestureProfile? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate() {
        super.onCreate()
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenHeight = metrics.heightPixels
        screenWidth = metrics.widthPixels

        ScanDataManager.loadConfig(this)
        ScanDataManager.setOnResetListener { resetFloatingWindows() }
    }

    override fun onServiceConnected() {
        Log.d("Mushroom", "Service Connected")

        // 启动前台服务保活
        startForegroundServiceNotification()

        tryRequestIgnoreBatteryOptimizations()

        serviceScope.launch(Dispatchers.IO) {
            initPaddleOcr()
        }

        setupWindows()
        registerSyncListeners()

        val savedList = ScanDataManager.mushroomList.value ?: emptyList()
        collectedMushrooms.addAll(savedList)
    }

    private fun startForegroundServiceNotification() {
        val channelId = "MushroomServiceChannel"
        val channelName = "菌子扫描服务"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            chan.lightColor = Color.BLUE
            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("菌子扫描仪正在运行")
            .setContentText("点击此处管理服务")
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(110, notification)
    }

    private fun tryRequestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            val packageName = packageName
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:$packageName")
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    appendLog("🔋 请允许“忽略电池优化”以防止杀后台")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun registerSyncListeners() {
        ScanDataManager.setOnClearMushroomsListener {
            collectedMushrooms.clear()
        }
        ScanDataManager.setOnClearLogsListener {
            serviceScope.launch(Dispatchers.Main) {
                tvLogs?.text = ""
            }
        }
    }

    private suspend fun initPaddleOcr() {
        // 调用 Core 初始化
        val result = OcrCore.init(this@AutoScrollService)

        // 切换回主线程处理 UI 日志
        withContext(Dispatchers.Main) {
            when (result) {
                is OcrCore.InitResult.Success -> {
                    appendLog("✅ PaddleOCR 引擎初始化成功")
                }

                is OcrCore.InitResult.Failure -> {
                    appendLog("❌ 初始化失败: ${result.reason}")
                }

                is OcrCore.InitResult.Error -> {
                    appendLog("❌ OCR 初始化异常: ${result.e.message}")
                }
            }
        }
    }

    private fun setupWindows() {
        val themeContext = ContextThemeWrapper(this, R.style.Theme_MushroomScanner)
        overlayLayout =
            LayoutInflater.from(themeContext).inflate(R.layout.layout_scan_overlay, null)

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

        val statusBarHeight = getStatusBarHeight()
        val params = layoutNotificationBar?.layoutParams as? FrameLayout.LayoutParams
        if (params != null) {
            params.topMargin = statusBarHeight
            layoutNotificationBar?.layoutParams = params
        }

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

        etWaitTime?.setOnClickListener { focusInput() }

        viewDimTop = overlayLayout?.findViewById(R.id.view_dim_top)
        viewDimBottom = overlayLayout?.findViewById(R.id.view_dim_bottom)
        handleTop = overlayLayout?.findViewById(R.id.handle_top)
        handleBottom = overlayLayout?.findViewById(R.id.handle_bottom)
        btnConfirmRegion = overlayLayout?.findViewById(R.id.btn_confirm_region)
        tvConfigHint = overlayLayout?.findViewById(R.id.tv_config_hint)
        btnConfirmRegion?.setOnClickListener { returnToSettingsMenu() }

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

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        if (result == 0) result = (24 * resources.displayMetrics.density).toInt()
        return result
    }

    private fun resetFloatingWindows() {
        try {
            if (floatingView != null) windowManager.removeView(floatingView)
            if (overlayLayout != null) windowManager.removeView(overlayLayout)
        } catch (e: Exception) {
        }
        setupWindows()
        registerSyncListeners()
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

    private fun openSettingsMenu() {
        if (isScanning) return
        configState = ConfigState.MENU
        collapseMenu()
        floatingView?.visibility = View.GONE
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

    private fun showRegionConfig() {
        configState = ConfigState.REGION
        layoutSettingsMenu?.visibility = View.GONE
        overlayParams?.flags = BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager.updateViewLayout(overlayLayout, overlayParams)
        viewDimTop?.visibility = View.VISIBLE; viewDimBottom?.visibility = View.VISIBLE
        handleTop?.visibility = View.VISIBLE; handleBottom?.visibility = View.VISIBLE
        btnConfirmRegion?.visibility = View.VISIBLE; tvConfigHint?.visibility = View.VISIBLE
        layoutNotificationBar?.visibility = View.VISIBLE
        tvOverlayNotification?.text = "请拖动调整扫描区域"
        val (currentTop, currentBottom) = ScanDataManager.scanRegionConfig ?: Pair(0.1f, 0.1f)
        updateRegionUI(currentTop, currentBottom)
        setupRegionDrag(handleTop!!, true); setupRegionDrag(handleBottom!!, false)
    }

    private fun showGestureConfig() {
        configState = ConfigState.GESTURE
        layoutSettingsMenu?.visibility = View.GONE
        overlayParams?.flags = BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        windowManager.updateViewLayout(overlayLayout, overlayParams)
        viewGestureTouch?.visibility = View.VISIBLE; tvGestureHint?.visibility = View.VISIBLE
        layoutGestureConfirm?.visibility = View.GONE
        overlayView?.clearGesturePath()
    }

    private fun returnToSettingsMenu() {
        viewDimTop?.visibility = View.GONE; viewDimBottom?.visibility = View.GONE
        handleTop?.visibility = View.GONE; handleBottom?.visibility = View.GONE
        btnConfirmRegion?.visibility = View.GONE; tvConfigHint?.visibility = View.GONE
        layoutNotificationBar?.visibility = View.GONE
        viewGestureTouch?.visibility = View.GONE; tvGestureHint?.visibility = View.GONE
        layoutGestureConfirm?.visibility = View.GONE
        overlayView?.clearGesturePath()
        layoutSettingsMenu?.visibility = View.VISIBLE; configState = ConfigState.MENU
        overlayParams?.flags = BASE_LAYOUT_FLAGS
        overlayParams?.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        windowManager.updateViewLayout(overlayLayout, overlayParams)
    }

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
        overlayParams?.flags =
            BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        windowManager.updateViewLayout(overlayLayout, overlayParams)
        floatingView?.visibility = View.VISIBLE
    }

    private fun updateRegionUI(topRatio: Float, bottomRatio: Float) {
        val topHeight = (screenHeight * topRatio).toInt()
        val bottomHeight = (screenHeight * bottomRatio).toInt()
        viewDimTop?.layoutParams?.height = topHeight; viewDimTop?.requestLayout()
        viewDimBottom?.layoutParams?.height = bottomHeight; viewDimBottom?.requestLayout()
        (handleTop?.layoutParams as FrameLayout.LayoutParams).topMargin = topHeight
        (handleBottom?.layoutParams as FrameLayout.LayoutParams).bottomMargin = bottomHeight
        handleTop?.requestLayout(); handleBottom?.requestLayout()
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

    private fun setupGestureRecording() {
        viewGestureTouch?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tempGesture = ScanDataManager.GestureProfile(event.rawX, event.rawY, 0f, 0f, 0)
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_UP -> {
                    val startX = tempGesture?.startX ?: 0f;
                    val startY = tempGesture?.startY ?: 0f
                    val endX = event.rawX;
                    val endY = event.rawY
                    overlayView?.showGesturePath(startX, startY, endX, endY)
                    val duration = 500L
                    tempGesture = ScanDataManager.GestureProfile(
                        startX / screenWidth,
                        startY / screenHeight,
                        endX / screenWidth,
                        endY / screenHeight,
                        duration
                    )
                    tvGestureHint?.visibility = View.GONE; layoutGestureConfirm?.visibility =
                        View.VISIBLE; viewGestureTouch?.visibility = View.GONE
                    performRecordedScroll(tempGesture!!, isTestRun = true)
                    return@setOnTouchListener true
                }
            }
            true
        }
    }

    private fun retryGesture() {
        overlayView?.clearGesturePath()
        layoutGestureConfirm?.visibility = View.GONE; viewGestureTouch?.visibility = View.VISIBLE
        tvGestureHint?.visibility = View.VISIBLE; tempGesture = null
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (isScanning) {
                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    stopScanning("🔊 检测到音量键，停止扫描")
                    return true
                }
            }
            if (configState != ConfigState.NONE && event.keyCode == KeyEvent.KEYCODE_BACK) {
                if (configState == ConfigState.MENU) exitConfigMode(false) else returnToSettingsMenu()
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    private fun toggleScan() {
        if (isScanning) {
            stopScanning("用户手动停止")
        } else {
            if (!OcrCore.isInitialized) {
                Toast.makeText(this, "⚠️ OCR 引擎尚未初始化或初始化失败", Toast.LENGTH_SHORT).show()
                serviceScope.launch(Dispatchers.IO) { initPaddleOcr() }
                return
            }
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

                serviceScope.launch(Dispatchers.IO) {
                    val pageCheckRes = OcrCore.isTargetPage(
                        bitmap,
                        bitmap.height,
                        File(getExternalFilesDir(null), "debug_images").toString()
                    )
                    if (pageCheckRes.msg != "") {
                        appendLog(pageCheckRes.msg)
                    }
                    withContext(Dispatchers.Main) {
                        if (pageCheckRes.isTarget) {
                            isCheckingEnvironment = false
                            startScanning()
                        } else {
                            isCheckingEnvironment = false
                            showTemporaryNotification("❌ 不在图鉴页面")
                            restorePanelAndLog("❌ 不在图鉴页面", wasMenuVisible)
                        }
                    }
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
        showNotificationText("🚀 扫描已开始 (PaddleOCR)")

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
                            stopScanning("截图失败: $code")
                        }
                    })
            } else {
                stopScanning("系统版本过低，不支持截图API")
            }
        }
    }

    private fun processScanImage(fullBitmap: Bitmap) {
        if (!isScanning) return
        val (topRatio, bottomRatio) = ScanDataManager.scanRegionConfig ?: Pair(0.0f, 0.0f)

        val cropY = (fullBitmap.height * topRatio).toInt()
        val cropHeight = (fullBitmap.height * (1.0f - topRatio - bottomRatio)).toInt()

        if (cropHeight <= 0) {
            stopScanning("区域无效: h=$cropHeight"); return
        }

        serviceScope.launch(Dispatchers.IO) {
            try {
                val results = OcrCore.scanBitmap(
                    fullBitmap, cropY, cropHeight,
                    File(getExternalFilesDir(null), "debug_images").toString()
                )

                withContext(Dispatchers.Main) {
                    if (!isScanning) return@withContext

                    val matchedRects = mutableListOf<Rect>()
                    val matchedNamesUI = mutableListOf<String>()

                    for (msg in results.msgs) {
                        appendLog(msg)
                    }

                    for (match in results.matches) {
                        if (collectedMushrooms.contains(match.name)) {
                            appendLog("忽略已存在: $match.name")
                        } else {
                            val logPrefix = if (match.isFuzzy) "✨ 模糊匹配" else "🎯 精确匹配"
                            appendLog("$logPrefix: ${match.name}")
                            collectedMushrooms.add(match.name)
                            ScanDataManager.addMushroom(match.name, this@AutoScrollService)
                            matchedNamesUI.add(match.name)
                            matchedRects.add(match.rect)
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
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { stopScanning("OCR 运行出错: ${e.message}") }
            }
        }
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
            serviceScope.launch {
                overlayParams?.flags =
                    BASE_LAYOUT_FLAGS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(overlayLayout, overlayParams)
                delay(200)
                dispatchScroll(path, g.duration) {
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

    private fun showNotificationText(msg: String) {
        serviceScope.launch { tvOverlayNotification?.text = msg }
    }

    private fun showTemporaryNotification(msg: String) {
        serviceScope.launch {
            layoutNotificationBar?.visibility = View.VISIBLE; tvOverlayNotification?.text = msg
            delay(3000)
            if (!isScanning) layoutNotificationBar?.visibility = View.GONE
        }
    }

    private fun restorePanelAndLog(msg: String, wasMenuVisible: Boolean) {
        isCheckingEnvironment = false; updateToggleButtonIcon(R.drawable.ic_play)
        if (wasMenuVisible) expandMenu(); appendLog(msg)
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
        ScanDataManager.clearAll(this@AutoScrollService)
        Toast.makeText(this, "数据已清空", Toast.LENGTH_SHORT).show()
    }

    private fun goHome() {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        collapseMenu()
    }

    private fun toggleLogVisibility() {
        layoutLogs?.visibility =
            if (layoutLogs?.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    // 【修改】简化 appendLog，无需外部传入 Context
    // 内部直接使用 this@AutoScrollService
    private fun appendLog(msg: String) {
        ScanDataManager.addLog(msg, this@AutoScrollService)
        serviceScope.launch {
            tvLogs?.append("$msg\n")
            scrollView?.post {
                scrollView?.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun takeScreenshotSingle(onResult: (Bitmap?) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    try {
                        // 1. 获取硬件位图 (Hardware Bitmap)
                        // 注意：硬件位图的像素存储在显存中，C++ 无法直接读取，强读会报 SEGV_ACCERR
                        val hardwareBitmap =
                            Bitmap.wrapHardwareBuffer(result.hardwareBuffer, result.colorSpace)

                        // 2. 【关键修复】强制转换为软件位图 (Software Bitmap)
                        // 使用 copy(Config.ARGB_8888, true) 会将像素数据从显存拷贝到内存
                        // true 表示 mutable (可变)，这对 OpenCV 也是必须的
                        val softwareBitmap = hardwareBitmap?.copy(Bitmap.Config.ARGB_8888, true)

                        // 3. 必须立即关闭 HardwareBuffer，否则会内存泄漏
                        result.hardwareBuffer.close()

                        // 硬件位图对象本身也可以回收了（虽然 GC 会做，但显式回收是个好习惯）
                        hardwareBitmap?.recycle()

                        // 4. 检查结果并回调
                        if (softwareBitmap != null) {
                            // 双重检查配置，确保万无一失
                            if (softwareBitmap.config == Bitmap.Config.ARGB_8888) {
                                onResult(softwareBitmap)
                            } else {
                                Log.e(
                                    "Mushroom",
                                    "截图格式异常: ${softwareBitmap.config}，期望 ARGB_8888"
                                )
                                onResult(null)
                            }
                        } else {
                            Log.e("Mushroom", "截图转换为软件位图失败")
                            onResult(null)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.e("Mushroom", "截图处理发生异常: ${e.message}")
                        onResult(null)
                    }
                }

                override fun onFailure(errorCode: Int) {
                    Log.e("Mushroom", "截图失败，错误码: $errorCode")
                    onResult(null)
                }
            })
        } else {
            Log.e("Mushroom", "系统版本低于 Android 11 (R)，不支持此截图 API")
            onResult(null)
        }
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    // 【关键修复】确保 onDestroy 不调用 System.exit 或 killProcess
    override fun onDestroy() {
        ScanDataManager.setOnClearMushroomsListener {}
        ScanDataManager.setOnClearLogsListener {}
        OcrCore.release()
        super.onDestroy()
        if (floatingView != null) windowManager.removeView(floatingView)
        if (overlayLayout != null) windowManager.removeView(overlayLayout)
    }
}