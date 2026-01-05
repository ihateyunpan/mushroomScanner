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
import kotlin.math.max

class AutoScrollService : AccessibilityService() {

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    // 基础布局 Flag
    private val BASE_LAYOUT_FLAGS = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

    // 窗口引用
    private var floatingView: View? = null
    private var floatParams: WindowManager.LayoutParams? = null
    private var overlayLayout: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var overlayView: OverlayView? = null

    // UI 组件
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

    // 状态控制
    private var isScanning = false
    private var isPaused = false
    private var isCheckingEnvironment = false

    enum class ConfigState { NONE, MENU, REGION, GESTURE }

    private var configState = ConfigState.NONE

    // 扫描数据
    private val collectedMushrooms = mutableSetOf<String>()
    private var scrollCount = 0
    private var screenHeight = 0
    private var screenWidth = 0
    private var tempGesture: ScanDataManager.GestureProfile? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // --- PaddleOCR 引擎 ---
    private val ocrEngine by lazy { Native() }
    private var isOcrInitialized = false

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

        // 初始化 PaddleOCR (在 IO 线程进行)
        serviceScope.launch(Dispatchers.IO) {
            initPaddleOcr()
        }

        setupWindows()

        // 【新增】注册数据同步监听器
        registerSyncListeners()
    }

    /**
     * 注册来自 ScanDataManager 的清空事件监听
     * 这样无论是 App 点击清空，还是悬浮窗点击清空，都会触发这里的逻辑
     */
    private fun registerSyncListeners() {
        // 1. 监听清空菌子列表事件
        ScanDataManager.setOnClearMushroomsListener {
            collectedMushrooms.clear() // 清空本地去重集合
            // 可选：显示提示
            // showNotificationText("列表已重置")
        }

        // 2. 监听清空日志事件
        ScanDataManager.setOnClearLogsListener {
            serviceScope.launch(Dispatchers.Main) {
                tvLogs?.text = "" // 清空悬浮窗日志显示
            }
        }
    }

    /**
     * 初始化 OCR 引擎：复制模型文件并调用 Native.init
     */
    private suspend fun initPaddleOcr() {
        try {
            val filesDir = getExternalFilesDir(null)?.absolutePath ?: return

            // 1. 复制模型文件到私有目录
            // 注意：请确保 assets 目录结构正确:
            // src/main/assets/models/ch_ppocr_mobile_v2.0_det_slim_opt.nb
            // src/main/assets/models/ch_ppocr_mobile_v2.0_rec_slim_opt.nb
            // src/main/assets/labels/custom_keys.txt
            // src/main/assets/config.txt (如果有)

            val detPath = copyAssetResource("models/ch_ppocr_mobile_v2.0_det_slim_opt.nb")
            val recPath = copyAssetResource("models/ch_ppocr_mobile_v2.0_rec_slim_opt.nb")
            val clsPath = ""
            val labelPath = copyAssetResource("labels/ppocr_keys_v1.txt")
            val configPath = copyAssetResource("config.txt") // 确保 assets 根目录或 models 下有 config.txt

            if (detPath.isEmpty() || recPath.isEmpty() || labelPath.isEmpty()) {
                withContext(Dispatchers.Main) { appendLog("❌ 模型文件复制失败，请检查 Assets") }
                return
            }

            // 2. 初始化 Native 引擎
            // 注意 Native.init 返回 false 表示成功 (ctx != 0)，返回 true 表示失败
            val initFailed = ocrEngine.init(
                this@AutoScrollService,
                detPath,
                clsPath,
                recPath,
                configPath,
                labelPath,
                4, // 线程数
                "LITE_POWER_HIGH"
            )

            isOcrInitialized = !initFailed

            withContext(Dispatchers.Main) {
                if (isOcrInitialized) {
                    appendLog("✅ PaddleOCR 引擎初始化成功")
                } else {
                    appendLog("❌ PaddleOCR 引擎初始化失败")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                appendLog("❌ OCR 初始化异常: ${e.message}")
            }
        }
    }

    private fun copyAssetResource(assetPath: String): String {
        try {
            val fileName = File(assetPath).name
            val outFile = File(getExternalFilesDir(null), fileName)
            // 如果文件已存在且大小 > 0，可以跳过复制（开发调试时建议每次覆盖，或者检查版本）
            if (outFile.exists() && outFile.length() > 0) return outFile.absolutePath

            assets.open(assetPath).use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            return outFile.absolutePath
        } catch (e: Exception) {
            Log.e("Mushroom", "Copy asset failed: $assetPath", e)
            return ""
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
        // 【关键】重置窗口时重新注册监听，因为 View 可能被重建
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

    // ===========================
    //       配置模式逻辑
    // ===========================

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

    // ===========================
    //       扫描核心逻辑
    // ===========================

    private fun toggleScan() {
        if (isScanning) {
            stopScanning("用户手动停止")
        } else {
            if (!isOcrInitialized) {
                Toast.makeText(this, "⚠️ OCR 引擎尚未初始化或初始化失败", Toast.LENGTH_SHORT).show()
                // 尝试重新初始化
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

                // 切换到 IO 线程执行 OCR
                serviceScope.launch(Dispatchers.IO) {
                    // 使用 Native 接口识别
                    val results =
                        ocrEngine.runImage(
                            bitmap,
                            File(getExternalFilesDir(null), "debug_images").toString()
                        )
                    val allText = results.joinToString(" ") { it.label }

                    Log.d("识别出文字：", allText)
                    withContext(Dispatchers.Main) {
                        restorePanelAndLog("识别出文字：" + allText, wasMenuVisible)
                    }

                    val isTargetPage =
                        allText.contains("菌子图鉴") || (allText.contains("收集度") && allText.contains(
                            "菌子"
                        ))

                    withContext(Dispatchers.Main) {
                        if (isTargetPage) {
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

        // 打印一下裁剪参数，辅助排查
        Log.d("MushroomDebug", "全图尺寸: w=${fullBitmap.width}, h=${fullBitmap.height}")
        Log.d("MushroomDebug", "裁剪比例: top=$topRatio, bottom=$bottomRatio")

        val cropY = (fullBitmap.height * topRatio).toInt()
        val cropHeight = (fullBitmap.height * (1.0f - topRatio - bottomRatio)).toInt()

        Log.d("MushroomDebug", "计算裁剪区域: y=$cropY, h=$cropHeight")

        if (cropHeight <= 0) {
            stopScanning("区域无效: h=$cropHeight"); return
        }

        // 1. 先进行裁剪 (这步得到的 croppedBitmap 可能会共享原图内存)
        val tempBitmap = Bitmap.createBitmap(fullBitmap, 0, cropY, fullBitmap.width, cropHeight)

        // 2. 【关键修复】强制深拷贝！
        val finalBitmap = resizeTo32Multiple(tempBitmap)

        // 3. 回收临时对象 (可选，帮助 GC)
        if (tempBitmap != fullBitmap && !tempBitmap.isRecycled) {
            tempBitmap.recycle()
        }

        // --- PaddleOCR 核心处理 ---
        serviceScope.launch(Dispatchers.IO) {
            try {
                // 调用 JNI 接口
                val results: ArrayList<OcrResult> = ocrEngine.runImage(
                    finalBitmap,
                    File(getExternalFilesDir(null), "debug_images").toString()
                )

                withContext(Dispatchers.Main) {
                    if (!isScanning) return@withContext

                    val matchedRects = mutableListOf<Rect>()
                    val matchedNamesUI = mutableListOf<String>()

                    for (result in results) {
                        val ocrText = result.label.trim()
                        val confidence = result.confidence

                        // 过滤低置信度结果，根据实际效果调整阈值
                        if (ocrText.isEmpty() || confidence < 0.6f) continue

                        appendLog("RAW: $ocrText ($confidence)")

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
                            if (collectedMushrooms.contains(matchedName)) {
                                appendLog("忽略已存在: $matchedName")
                            } else {
                                val logPrefix = if (isFuzzy) "✨ 模糊匹配" else "🎯 精确匹配"
                                appendLog("$logPrefix: $matchedName")

                                collectedMushrooms.add(matchedName)
                                ScanDataManager.addMushroom(matchedName)
                                matchedNamesUI.add(matchedName)

                                // 坐标转换: OCR 结果是相对于 croppedBitmap 的，需加上 cropY
                                val rect = result.rect
                                rect.offset(0, cropY)
                                matchedRects.add(rect)
                            }
                        }
                    }

                    // UI 反馈
                    if (matchedNamesUI.isNotEmpty()) {
                        showNotificationText("发现: ${matchedNamesUI.joinToString(",")}")
                        overlayView?.updateMatchedRects(matchedRects)
                    } else {
                        showNotificationText("本页无新发现")
                        overlayView?.clearRects()
                    }

                    // 准备下一页
                    serviceScope.launch {
                        delay(200) // 稍微停顿展示框选效果
                        overlayView?.clearRects()
                        performConfiguredScroll()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    stopScanning("OCR 运行出错: ${e.message}")
                }
            } finally {
                // 用完记得回收，防止 OOM
                if (!finalBitmap.isRecycled) finalBitmap.recycle()
            }
        }
    }

    /**
     * 调试专用：保存 Bitmap 到应用私有目录
     * 路径通常为: /sdcard/Android/data/in.co.washing_machine.mushroomscanner/files/debug_images/
     */
    private fun saveDebugBitmap(bitmap: Bitmap, prefix: String) {
        try {
            // 创建 debug_images 子目录
            val debugDir = File(getExternalFilesDir(null), "debug_images")
            if (!debugDir.exists()) debugDir.mkdirs()

            val timestamp = System.currentTimeMillis()
            val fileName = "${prefix}_$timestamp.jpg"
            val file = File(debugDir, fileName)

            FileOutputStream(file).use { out ->
                // 压缩为 JPEG，质量 100
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            Log.d("MushroomDebug", "✅ 图片已保存: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("MushroomDebug", "❌ 保存图片失败", e)
        }
    }

    /**
     * 将图片宽高强制调整为 32 的倍数（向上取整）
     * 同时这也会执行 Deep Copy，解决 Stride 问题
     */
    private fun resizeTo32Multiple(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height

        // 1. 计算目标高度：向上取整到最近的 32 倍数
        // 例如：高度 50 -> 64; 高度 30 -> 32
        val targetH = if (h % 32 == 0) h else ((h / 32) + 1) * 32

        // 2. 计算目标宽度：保持比例，同时也必须是 32 的倍数
        // 保持比例是为了防止文字拉伸变形影响识别
        val ratio = targetH.toFloat() / h
        var targetW = (w * ratio).toInt()
        targetW = if (targetW % 32 == 0) targetW else ((targetW / 32) + 1) * 32

        // 避免宽度过小（OCR模型通常至少需要 32）
        targetW = max(32, targetW)

        // 如果尺寸没变，且 Config 已经是 ARGB_8888，直接返回副本以确保 Stride 重置
        if (targetW == w && targetH == h) {
            // 必须 copy 一次以解决 Stride 问题，不能直接返回原图
            return bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        Log.d("MushroomDebug", "重置尺寸: ${w}x${h} -> ${targetW}x${targetH}")

        // 3. 执行缩放 (filter=true 开启抗锯齿，效果更好)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun performConfiguredScroll() {
        if (!isScanning) return
        val gesture = ScanDataManager.gestureProfile

        if (gesture != null) {
            performRecordedScroll(gesture)
        } else {
            // 默认滚动：从下往上滑
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

    // --- Helpers (UI相关) ---
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

    // 【修改】这个方法是悬浮窗垃圾桶按钮的点击事件
    // 现在只需调用 Manager 的 clearAll，通过回调来触发本地清理，保证逻辑统一
    private fun clearData() {
        ScanDataManager.clearAll()
        // 本地的 collectedMushrooms.clear() 和 tvLogs.text = "" 会在回调中执行

        Toast.makeText(this, "数据已清空", Toast.LENGTH_SHORT).show()
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
    override fun onDestroy() {
        // 解除监听防止内存泄漏
        ScanDataManager.setOnClearMushroomsListener {}
        ScanDataManager.setOnClearLogsListener {}

        ocrEngine.release()
        super.onDestroy()
        if (floatingView != null) windowManager.removeView(floatingView)
        if (overlayLayout != null) windowManager.removeView(overlayLayout)
    }

    // 模糊匹配工具 (保留)
    object EnhancedFuzzyMatcher {
        private val CONFUSION_SETS = mapOf(
            Pair('茵', '菌') to 0.1,
            Pair('菌', '茵') to 0.1,
            Pair('菇', '姑') to 0.2,
            Pair('手', '毛') to 0.3,
            Pair('日', '曰') to 0.1,
            Pair('末', '未') to 0.1,
            Pair('土', '士') to 0.1,
            Pair('全', '金') to 0.1,
            Pair('大', '太') to 0.2,
            Pair('前', '茄') to 0.2,
            Pair('苏', '荪') to 0.1
        )

        fun findBestMatch(ocrText: String, candidates: List<String>): String? {
            var best: String? = null;
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
            var maxSim = 0.0;
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
            val n = s1.length;
            val m = s2.length;
            var dp = Array(n + 1) { DoubleArray(m + 1) }
            for (i in 0..n) dp[i][0] = i.toDouble(); for (j in 0..m) dp[0][j] = j.toDouble()
            for (i in 1..n) {
                for (j in 1..m) {
                    val c1 = s1[i - 1];
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