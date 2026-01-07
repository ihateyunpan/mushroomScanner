package `in`.co.washing_machine.mushroomscanner

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoAnalysisActivity : AppCompatActivity() {

    // UI 组件
    private lateinit var tvVideoPath: TextView
    private lateinit var btnSelectVideo: Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvLogs: TextView
    private lateinit var etFrameStep: EditText

    private var selectedVideoUri: Uri? = null
    private var isAnalyzing = false

    // 协程作用域 (运行在 Main 线程)
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // 视频选择回调
    private val selectVideoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedVideoUri = uri
                tvVideoPath.text = uri.path ?: "已选择视频"
                btnStart.isEnabled = true
                appendLogWithUI("✅ 视频已加载: $uri")
                tvStatus.text = "视频就绪，请点击开始分析"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_analysis)
        initViews()
        initOcrEngine()
    }

    private fun initViews() {
        tvVideoPath = findViewById(R.id.tv_video_path)
        btnSelectVideo = findViewById(R.id.btn_select_video)
        btnStart = findViewById(R.id.btn_start_analysis)
        btnStop = findViewById(R.id.btn_stop_analysis)
        progressBar = findViewById(R.id.progress_bar)
        tvStatus = findViewById(R.id.tv_status)
        tvLogs = findViewById(R.id.tv_logs)
        etFrameStep = findViewById(R.id.et_frame_step)

        btnSelectVideo.setOnClickListener { handleSelectVideoClick() }
        btnStart.setOnClickListener { startAnalysis() }
        btnStop.setOnClickListener { stopAnalysis() }
    }

    private fun initOcrEngine() {
        scope.launch(Dispatchers.IO) {
            appendLogWithUI("🔄 正在初始化 OCR 引擎...")
            val result = OcrCore.init(this@VideoAnalysisActivity)
            withContext(Dispatchers.Main) {
                when (result) {
                    is OcrCore.InitResult.Success -> appendLogWithUI("✅ OCR 引擎初始化成功")
                    is OcrCore.InitResult.Failure -> appendLogWithUI("❌ OCR 初始化失败: ${result.reason}")
                    is OcrCore.InitResult.Error -> appendLogWithUI("❌ OCR 初始化异常: ${result.e.message}")
                }
            }
        }
    }

    private fun handleSelectVideoClick() {
        AlertDialog.Builder(this)
            .setTitle("数据处理")
            .setMessage("“菌种列表”和“运行日志”与 APP 主页面共享。\n\n是否要在导入视频前清空现有的列表和日志？")
            .setPositiveButton("是，清空") { _, _ ->
                ScanDataManager.clearAll(this)
                appendLogWithUI("🗑️ 已清空主页列表和日志")
                openVideoPicker()
            }
            .setNegativeButton("否，保留") { _, _ ->
                appendLogWithUI("💾 保留现有数据")
                openVideoPicker()
            }
            .setNeutralButton("取消", null)
            .show()
    }

    private fun openVideoPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "video/*" }
        selectVideoLauncher.launch(intent)
    }

    private fun startAnalysis() {
        if (selectedVideoUri == null) return

        val frameStepStr = etFrameStep.text.toString()
        val frameStep = frameStepStr.toIntOrNull()?.takeIf { it > 0 } ?: 5

        if (!OcrCore.isInitialized) {
            appendLogWithUI("⚠️ OCR 尚未初始化，正在重试...")
            scope.launch { OcrCore.init(this@VideoAnalysisActivity); startAnalysis() }
            return
        }

        isAnalyzing = true
        btnStart.visibility = Button.GONE
        btnSelectVideo.isEnabled = false
        etFrameStep.isEnabled = false
        btnStop.visibility = Button.VISIBLE
        tvLogs.text = ""
        appendLogWithUI("🚀 开始分析视频 (抽帧间隔: $frameStep)...")

        scope.launch(Dispatchers.IO) {
            val outputBuilder = StringBuilder()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            outputBuilder.append("=== 视频分析报告 ($timestamp) ===\n")

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(this@VideoAnalysisActivity, selectedVideoUri)

                val durationStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val totalDurationUs = (durationStr?.toLongOrNull() ?: 0L) * 1000 // 微秒
                val totalDurationMs = totalDurationUs / 1000

                val fpsStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
                        ?: "30"
                val fps = fpsStr.toFloatOrNull() ?: 30f
                val frameDurationUs = (1_000_000 / fps).toLong()
                val stepDurationUs = frameDurationUs * frameStep

                val totalDurationFmt = formatDuration(totalDurationMs) // 格式化总时长

                appendLogWithUI(
                    "ℹ️ 视频 FPS: ${
                        String.format(
                            "%.2f",
                            fps
                        )
                    }, 时长: $totalDurationFmt"
                )
                appendLogWithUI("ℹ️ 采样间隔: ${stepDurationUs / 1000}ms (每 $frameStep 帧)")

                withContext(Dispatchers.Main) {
                    progressBar.max = (totalDurationMs / 1000).toInt() // 进度条单位：秒
                    progressBar.progress = 0
                }

                var currentUs = 0L
                var processedFrames = 0
                val sessionNewDiscovery = mutableSetOf<String>()

                // 【新增】记录分析开始的系统时间，用于计算 ETA
                val analysisStartTimeMs = System.currentTimeMillis()

                while (currentUs < totalDurationUs && isAnalyzing) {
                    try {
                        val fullBitmap = retriever.getFrameAtTime(
                            currentUs,
                            MediaMetadataRetriever.OPTION_CLOSEST
                        )

                        if (fullBitmap != null) {
                            val (topRatio, bottomRatio) = ScanDataManager.scanRegionConfig ?: Pair(
                                0.0f,
                                0.0f
                            )
                            val cropY = (fullBitmap.height * topRatio).toInt()
                            val cropHeight =
                                (fullBitmap.height * (1.0f - topRatio - bottomRatio)).toInt()

                            if (cropHeight > 0) {
                                val results = OcrCore.scanBitmap(fullBitmap, cropY, cropHeight, "")
                                val currentMs = currentUs / 1000
                                val timeFmt = formatDuration(currentMs) // 格式化当前时间

                                // 同步 Log
                                for (msg in results.msgs) {
                                    ScanDataManager.addLog(
                                        "[$timeFmt] $msg",
                                        this@VideoAnalysisActivity
                                    )
                                }

                                // 识别名字
                                for (match in results.matches) {
                                    val name = match.name
                                    ScanDataManager.addMushroom(name, this@VideoAnalysisActivity)

                                    if (!sessionNewDiscovery.contains(name)) {
                                        sessionNewDiscovery.add(name)
                                        val logMsg =
                                            "第 $processedFrames 帧 [$timeFmt] 新发现: <$name>"
                                        appendLogWithUI(logMsg)
                                        outputBuilder.append(logMsg).append("\n")
                                        ScanDataManager.addLog(
                                            "✨ 视频分析发现: $name",
                                            this@VideoAnalysisActivity
                                        )
                                    }
                                }

                                // 【新增】计算进度和 ETA
                                if (processedFrames % 5 == 0) { // 每处理 5 帧更新一次 UI，避免过于频繁
                                    val elapsedRealTime =
                                        System.currentTimeMillis() - analysisStartTimeMs
                                    val progressRatio = currentUs.toDouble() / totalDurationUs

                                    // 估算剩余时间
                                    val remainingMs = if (progressRatio > 0.001) {
                                        ((elapsedRealTime / progressRatio) - elapsedRealTime).toLong()
                                    } else {
                                        0L
                                    }

                                    val percentStr = String.format("%.1f", progressRatio * 100)
                                    val etaStr = formatDuration(remainingMs)

                                    withContext(Dispatchers.Main) {
                                        progressBar.progress = (currentMs / 1000).toInt()
                                        // 格式: [00:01:30]/[00:10:00] (15.0%)
                                        // 预计剩余: 00:08:30
                                        tvStatus.text =
                                            "进度: [$timeFmt] / [$totalDurationFmt] ($percentStr%)\n预计剩余: $etaStr"
                                    }
                                }
                            }
                            fullBitmap.recycle()
                        }
                    } catch (e: Exception) {
                        appendLogWithUI("⚠️ 第 $processedFrames 帧处理异常: ${e.message}")
                    }

                    currentUs += stepDurationUs
                    processedFrames++
                }

                val saveFile = File(getExternalFilesDir(null), "Video_OCR_Report_$timestamp.txt")
                FileOutputStream(saveFile).use { it.write(outputBuilder.toString().toByteArray()) }
                appendLogWithUI("✅ 分析完成! 报告已保存: ${saveFile.name}")
                appendLogWithUI("🎉 共发现 ${sessionNewDiscovery.size} 种菌子")

            } catch (e: Exception) {
                e.printStackTrace()
                appendLogWithUI("❌ 严重错误: ${e.message}")
            } finally {
                retriever.release()
                stopAnalysis()
            }
        }
    }

    private fun stopAnalysis() {
        isAnalyzing = false
        scope.launch(Dispatchers.Main) {
            btnStart.visibility = Button.VISIBLE
            btnSelectVideo.isEnabled = true
            etFrameStep.isEnabled = true
            btnStop.visibility = Button.GONE
            progressBar.progress = 0
            tvStatus.text = "分析已停止/结束"
        }
    }

    private fun appendLogWithUI(msg: String) {
        scope.launch {
            tvLogs.append("$msg\n")
            val scroll = tvLogs.parent as? ScrollView
            scroll?.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    // 【新增】辅助函数：毫秒转 HH:mm:ss
    private fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}