package `in`.co.washing_machine.mushroomscanner

import android.app.AppOpsManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var pageResults: LinearLayout
    private lateinit var pageLogs: LinearLayout
    private lateinit var tabResults: Button
    private lateinit var tabLogs: Button

    private lateinit var tvResultHeader: TextView
    private lateinit var tvResultList: TextView
    private lateinit var tvMainLogs: TextView

    private lateinit var btnResetFloating: Button
    private lateinit var btnExportTxt: Button
    private lateinit var btnExportLogsTxt: Button

    private var pendingExportContent: String = ""

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                saveContentToUri(uri, pendingExportContent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 启动时不强制跳转，只提示
        checkPermissions(autoRedirect = false)
        initViews()
        observeData()
    }

    private fun initViews() {
        btnResetFloating = findViewById(R.id.btn_reset_floating)

        pageResults = findViewById(R.id.page_results)
        pageLogs = findViewById(R.id.page_logs)
        tabResults = findViewById(R.id.tab_results)
        tabLogs = findViewById(R.id.tab_logs)
        tvResultHeader = findViewById(R.id.tv_result_header)
        tvResultList = findViewById(R.id.tv_result_list)
        tvMainLogs = findViewById(R.id.tv_main_logs)

        btnExportTxt = findViewById(R.id.btn_export_txt)
        btnExportLogsTxt = findViewById(R.id.btn_export_logs_txt)

        tabResults.setOnClickListener { switchTab(0) }
        tabLogs.setOnClickListener { switchTab(1) }

        findViewById<Button>(R.id.btn_copy_results).setOnClickListener {
            copyToClipboard(tvResultList.text.toString())
        }
        findViewById<Button>(R.id.btn_clear_results).setOnClickListener {
            ScanDataManager.clearMushrooms()
        }

        findViewById<Button>(R.id.btn_copy_logs).setOnClickListener {
            copyToClipboard(tvMainLogs.text.toString())
        }
        findViewById<Button>(R.id.btn_clear_logs).setOnClickListener {
            ScanDataManager.clearLogs()
        }

        // 按钮重置悬浮窗 (点击时强制检查权限)
        btnResetFloating.setOnClickListener {
            if (checkPermissions(autoRedirect = true)) {
                ScanDataManager.triggerResetFloatingWindow()
                Toast.makeText(this, "正在尝试显示 / 重置悬浮球...", Toast.LENGTH_SHORT).show()
            }
        }

        btnExportTxt.setOnClickListener {
            val content = tvResultList.text.toString()
            if (content.isBlank()) {
                Toast.makeText(this, "列表为空，无需导出", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startExportFlow(content, "Mushroom_Results")
        }

        btnExportLogsTxt.setOnClickListener {
            val content = tvMainLogs.text.toString()
            if (content.isBlank()) {
                Toast.makeText(this, "日志为空，无需导出", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startExportFlow(content, "Mushroom_Logs")
        }
    }

    private fun startExportFlow(content: String, filePrefix: String) {
        pendingExportContent = content
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "${filePrefix}_$timestamp.txt"

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createDocumentLauncher.launch(intent)
    }

    private fun saveContentToUri(uri: Uri, content: String) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            Toast.makeText(this, "导出成功！", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeData() {
        ScanDataManager.mushroomList.observe(this) { list ->
            val uniqueList = list.distinct()
            val count = uniqueList.size
            tvResultHeader.text = "已识别列表 (共 $count 个):"
            val content = uniqueList.joinToString("\n")
            tvResultList.text = content
        }

        ScanDataManager.logList.observe(this) { logs ->
            tvMainLogs.text = logs
        }
    }

    private fun switchTab(index: Int) {
        if (index == 0) {
            pageResults.visibility = View.VISIBLE
            pageLogs.visibility = View.GONE
            tabResults.setBackgroundColor(getColor(R.color.purple_500))
            tabLogs.setBackgroundColor(0xFFAAAAAA.toInt())
        } else {
            pageResults.visibility = View.GONE
            pageLogs.visibility = View.VISIBLE
            tabResults.setBackgroundColor(0xFFAAAAAA.toInt())
            tabLogs.setBackgroundColor(getColor(R.color.purple_500))
        }
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("MushroomScanner", text))
        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
    }

    // --- 权限检查逻辑升级版 ---

    /**
     * 检查所有必要权限
     * @param autoRedirect 是否在缺失权限时自动跳转设置页
     */
    private fun checkPermissions(autoRedirect: Boolean = true): Boolean {
        // 1. 基础悬浮窗权限 (Android 标准)
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先授予【悬浮窗】权限", Toast.LENGTH_SHORT).show()
            if (autoRedirect) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "无法打开悬浮窗设置，请手动开启", Toast.LENGTH_LONG).show()
                }
            }
            return false
        }

        // 2. 小米/红米 专属后台弹出权限检查
        if (isXiaomi() && !getMiuiPopupPermission(this)) {
            Toast.makeText(this, "小米手机请务必开启【后台弹出界面】权限", Toast.LENGTH_LONG).show()
            if (autoRedirect) {
                openMiuiPermissionActivity(this)
            }
            return false
        }

        // 3. 无障碍服务权限
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(this, "请开启【菌子扫描仪】无障碍服务", Toast.LENGTH_LONG).show()
            if (autoRedirect) {
                try {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "无法打开无障碍设置，请手动开启", Toast.LENGTH_LONG).show()
                }
            }
            return false
        }

        return true
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(this, AutoScrollService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val stringColonSplitter = TextUtils.SimpleStringSplitter(':')
        stringColonSplitter.setString(enabledServicesSetting)
        while (stringColonSplitter.hasNext()) {
            val componentNameString = stringColonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) return true
        }
        return false
    }

    // --- 小米适配工具区 ---

    private fun isXiaomi(): Boolean {
        return Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)
    }

    /**
     * 反射检查 MIUI 的 "后台弹出界面" 权限 (OpCode 10021)
     */
    private fun getMiuiPopupPermission(context: Context): Boolean {
        return try {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val method: Method = AppOpsManager::class.java.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            // 10021 是 MIUI 后台弹出界面的 OpCode
            val result = method.invoke(
                appOpsManager,
                10021,
                Process.myUid(),
                context.packageName
            ) as Int
            result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            // 如果反射失败（比如非 MIUI 系统或 API 变动），默认返回 true 以免误杀
            true
        }
    }

    /**
     * 跳转到 MIUI 具体的权限管理页
     */
    private fun openMiuiPermissionActivity(context: Context) {
        try {
            // 尝试打开应用详情页 (通常这里可以找到权限管理)
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
            intent.setClassName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity"
            )
            intent.putExtra("extra_pkgname", context.packageName)
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                // 备用方案：打开应用详情页
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "请手动前往设置开启权限", Toast.LENGTH_SHORT).show()
            }
        }
    }
}