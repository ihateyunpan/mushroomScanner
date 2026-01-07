package `in`.co.washing_machine.mushroomscanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import `in`.co.washing_machine.mushroomscanner.ocr.OcrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.max

/**
 * OCR 核心引擎单例
 * 负责：模型初始化、图片预处理、OCR推理、模糊匹配
 * 供 AutoScrollService 和 VideoAnalysisActivity 复用
 */
object OcrCore {
    val engine = Native()

    @Volatile
    var isInitialized = false
        private set // 外部只能读取，不能修改

    // 模糊匹配逻辑 (从 AutoScrollService 移入)
    object EnhancedFuzzyMatcher {
        private val CONFUSION_SETS = ConfusionData.CONFUSION_SETS
        private val LOW_COST_MISSING_CHARS = ConfusionData.LOW_COST_MISSING_CHARS

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
                val dist = weightedLevenshtein(source, target); return 1.0 - (dist / target.length)
            }
            var maxSim = 0.0;
            val windowSize = target.length
            for (i in 0..source.length - windowSize) {
                val sub = source.substring(i, i + windowSize)
                val dist = weightedLevenshtein(sub, target);
                val sim = 1.0 - (dist / max(sub.length, target.length))
                if (sim > maxSim) maxSim = sim
            }
            return maxSim
        }

        private fun weightedLevenshtein(source: String, target: String): Double {
            val n = source.length
            val m = target.length
            val dp = Array(n + 1) { DoubleArray(m + 1) }

            // 初始化：从空字符串变成 target 需要“插入”字符
            dp[0][0] = 0.0
            for (j in 1..m) {
                // 如果 target 中的这个字是 '一'，那么 OCR 没读到它(source为空)，我们只罚 0.2 分
                val charCost = LOW_COST_MISSING_CHARS[target[j - 1]] ?: 1.0
                dp[0][j] = dp[0][j - 1] + charCost
            }
            // 初始化：从 source 变成空字符串需要“删除”字符 (OCR 多读了字)
            for (i in 1..n) {
                dp[i][0] = i.toDouble() // 多读字符通常按 1.0 罚分，除非你也想配置
            }

            for (i in 1..n) {
                for (j in 1..m) {
                    val c1 = source[i - 1]
                    val c2 = target[j - 1]

                    // 1. 替换成本 (Substitution) - 保持原有 ConfusionData 逻辑
                    val subCost = if (c1 == c2) 0.0 else CONFUSION_SETS[Pair(c1, c2)] ?: 1.0

                    // 2. 插入成本 (Insertion) - 对应 OCR 漏读了 target 中的字符 c2
                    // 如果 c2 是 '一'，成本就是 0.2，否则是 1.0
                    val insCost = LOW_COST_MISSING_CHARS[c2] ?: 1.0

                    // 3. 删除成本 (Deletion) - 对应 OCR 多读了字符 c1
                    val delCost = 1.0

                    dp[i][j] = minOf(
                        dp[i - 1][j] + delCost,    // 删除 (OCR多字)
                        dp[i][j - 1] + insCost,    // 插入 (OCR少字) <- 关键修复点
                        dp[i - 1][j - 1] + subCost // 替换
                    )
                }
            }
            return dp[n][m]
        }
    }

    // --- 定义初始化结果状态 ---
    sealed class InitResult {
        object Success : InitResult() // 1. 成功
        data class Failure(val reason: String) : InitResult() // 2. 逻辑失败 (文件缺失或引擎返回false)
        data class Error(val e: Throwable) : InitResult() // 3. 异常崩溃 (Exception)
    }

    // --- 核心初始化逻辑 ---
    suspend fun init(context: Context): InitResult {
        // 如果已经初始化过，直接返回成功，避免重复加载
        if (isInitialized) return InitResult.Success

        return withContext(Dispatchers.IO) {
            try {
                // 1. 拷贝模型文件
                val detPath =
                    copyAssetResource(context, "models/ch_ppocr_mobile_v2.0_det_slim_opt.nb")
                val recPath =
                    copyAssetResource(context, "models/ch_ppocr_mobile_v2.0_rec_slim_opt.nb")
                val clsPath = ""
                val labelPath = copyAssetResource(context, "labels/ppocr_keys_v1.txt")
                val configPath = copyAssetResource(context, "config.txt")

                // 2. 检查文件完整性
                if (detPath.isEmpty() || recPath.isEmpty() || labelPath.isEmpty()) {
                    return@withContext InitResult.Failure("模型文件复制失败，请检查 Assets")
                }

                // 3. 调用 Native 初始化
                // 注意：根据你的原始代码，engine.init 返回 true 代表 FAILED (失败)
                val initFailed = engine.init(
                    context,
                    detPath,
                    clsPath,
                    recPath,
                    configPath,
                    labelPath,
                    4,
                    "LITE_POWER_HIGH"
                )

                // 4. 处理结果
                if (!initFailed) {
                    isInitialized = true
                    return@withContext InitResult.Success
                } else {
                    return@withContext InitResult.Failure("PaddleOCR 引擎底层初始化返回失败")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // 5. 捕获异常
                return@withContext InitResult.Error(e)
            }
        }
    }

    // 释放资源
    fun release() {
        if (isInitialized) {
            engine.release()
            isInitialized = false
        }
    }

    data class PageCheckResult(
        val isTarget: Boolean,      // 是否是目标页面
        val msg: String = ""
    )

    fun isTargetPage(rawBitmap: Bitmap, bitmapHeight: Int, debugDir: String = ""): PageCheckResult {
        val results = engine.runImage(rawBitmap, debugDir)
        val allText = results.joinToString(" ") { it.label }
//                    appendLog(results.joinToString(" "))
        var isTargetPage = false
        var msg = ""

        val titleResult = results.find { it.label.contains("菌子图鉴") }
        if (titleResult != null) {
            val centerY = titleResult.rect.centerY()
            if (centerY < bitmapHeight / 2) {
                isTargetPage = true
                msg = "✅ 识别到标题位于上半部: y=$centerY"
            } else {
                msg = "⚠️ 发现标题但位置靠下: y=$centerY"
            }
        }

        if (!isTargetPage) {
            if (allText.contains("收集度") && allText.contains("菌")) {
                isTargetPage = true
            }
        }

        return PageCheckResult(isTargetPage, msg)
    }

    data class MatchItem(
        val name: String,
        val isFuzzy: Boolean,
        val rect: Rect, // 相对于传入 Bitmap 的坐标（如果是裁剪后的图，需要外部自己加 Offset，或者我们在 scanBitmap 里处理）
        val confidence: Float
    )

    data class ScanResult(
        val matches: List<MatchItem>,
        val msgs: List<String> // 保留原始结果用于日志
    )

    fun scanBitmap(
        fullBitmap: Bitmap,
        cropY: Int,
        cropHeight: Int,
        saveDebugDir: String = ""
    ): ScanResult {
        val matches = ArrayList<MatchItem>()
        val msgs = ArrayList<String>()

        // 1. 初步裁剪
        val tempBitmap = Bitmap.createBitmap(fullBitmap, 0, cropY, fullBitmap.width, cropHeight)

        // 2. 【核心修复】调整尺寸为32倍数 + 解决 Stride 问题
        val finalBitmap = resizeTo32Multiple(tempBitmap)

        if (tempBitmap != fullBitmap && !tempBitmap.isRecycled) {
            tempBitmap.recycle()
        }

        try {
            val results: ArrayList<OcrResult> = engine.runImage(
                finalBitmap,
                saveDebugDir
            )

            for (result in results) {
                val ocrText = result.label.trim()
                val confidence = result.confidence

                if (ocrText.isEmpty() || confidence < 0.6f) continue

                msgs.add("RAW: $ocrText ($confidence)")

                var matchedName: String? = null
                var isFuzzy = false

                val exactMatch = MushroomData.allNames.find { ocrText.contains(it) }

                if (exactMatch != null) {
                    matchedName = exactMatch
                } else {
                    val fuzzyMatch =
                        EnhancedFuzzyMatcher.findBestMatch(ocrText, MushroomData.allNames)
                    if (fuzzyMatch != null) {
                        matchedName = fuzzyMatch
                        isFuzzy = true
                    }
                }

                if (matchedName != null) {
                    val rect = result.rect
                    rect.offset(0, cropY)
                    matches.add(MatchItem(matchedName, isFuzzy, rect, confidence))
                }
            }

            return ScanResult(matches, msgs)

        } finally {
            if (!finalBitmap.isRecycled) finalBitmap.recycle()
        }
    }

    private fun copyAssetResource(context: Context, assetPath: String): String {
        try {
            val fileName = File(assetPath).name
            val outFile = File(context.getExternalFilesDir(null), fileName)
            if (outFile.exists() && outFile.length() > 0) return outFile.absolutePath

            context.assets.open(assetPath).use { input ->
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

    /**
     * 将图片宽高强制调整为 32 的倍数（向上取整）
     * 同时执行 Deep Copy，解决 Stride 问题和 OCR 检测模型 Bug
     */
    private fun resizeTo32Multiple(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height

        val targetH = if (h % 32 == 0) h else ((h / 32) + 1) * 32
        val ratio = targetH.toFloat() / h
        var targetW = (w * ratio).toInt()
        targetW = if (targetW % 32 == 0) targetW else ((targetW / 32) + 1) * 32
        targetW = max(32, targetW)

        if (targetW == w && targetH == h) {
            // 必须 copy 一次以解决 Stride 问题，不能直接返回原图
            return bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}