package cn.ppps.forwarder.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import cn.ppps.forwarder.App
import cn.ppps.forwarder.R
import cn.ppps.forwarder.core.Core
import cn.ppps.forwarder.entity.CloneInfo
import cn.ppps.forwarder.utils.EncryptedBackup
import cn.ppps.forwarder.utils.HttpServerUtils
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.SharedPreference
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigurationBackupActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var exportButton: Button
    private lateinit var importButton: Button
    private val gson = Gson()
    private data class Payload(val schema: Int = 1, val applicationId: String = "com.hwserve.smsforwarder", val configuration: CloneInfo)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "설정 백업 · 복원"
        val pad = (20 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            setBackgroundColor(resources.getColor(R.color.ui_background))
        }
        fun label(text: String, size: Float): TextView = TextView(this).apply {
            this.text = text; textSize = size; setTextColor(resources.getColor(R.color.ui_text))
            setPadding(0, pad / 2, 0, pad / 2)
        }
        content.addView(label("암호화 설정 백업", 24f))
        content.addView(label("규칙·전송 계정·예약 작업을 비밀번호로 암호화합니다. Android 파일 선택기로 저장하며 전체 저장소 권한은 요청하지 않습니다. 비밀번호를 잊으면 복구할 수 없습니다.", 16f))
        exportButton = Button(this).apply { text = "백업 파일 저장"; setOnClickListener { chooseFile(true) } }
        importButton = Button(this).apply { text = "백업 파일 복원"; setOnClickListener { chooseFile(false) } }
        content.addView(exportButton); content.addView(importButton)
        content.addView(label("복원은 현재 설정·규칙·계정·작업을 교체하고 전달 기록을 지웁니다. 통화·연락처·자동 작업은 복원 후 꺼진 상태이며 필요한 기능을 다시 켜세요. 이 화면에서 만든 암호화 백업만 지원합니다.", 14f))
        status = label("", 16f); content.addView(status)
        content.addView(Button(this).apply { text = "돌아가기"; setOnClickListener { finish() } })
        setContentView(android.widget.ScrollView(this).apply { addView(content) })
    }
    private fun chooseFile(export: Boolean) {
        val intent = Intent(if (export) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE).setType("application/octet-stream")
        if (export) intent.putExtra(Intent.EXTRA_TITLE, "SmsKR-settings.smskr")
        try { startActivityForResult(intent, if (export) 101 else 102) }
        catch (e: Exception) { status.text = "파일 선택기를 열 수 없습니다." }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || requestCode !in 101..102) return
        val uri = data?.data ?: return
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "비밀번호 8자 이상"
        }
        val dialog = AlertDialog.Builder(this).setTitle(if (requestCode == 101) "백업 비밀번호" else "복원 비밀번호")
            .setMessage(if (requestCode == 101) "계정 토큰도 포함됩니다. 안전한 비밀번호로 보관하세요." else "현재 설정과 전달 기록을 교체합니다. 본인이 만든 백업인지 확인하세요.")
            .setView(input).setNegativeButton("취소", null).setPositiveButton("확인", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (input.length() < 8) { input.error = "8자 이상 입력하세요."; return@setOnClickListener }
                val password = input.text.toString().toCharArray(); input.text.clear(); dialog.dismiss()
                process(uri, requestCode == 101, password)
            }
        }
        dialog.show()
    }
    private fun process(uri: Uri, export: Boolean, password: CharArray) {
        exportButton.isEnabled = false; importButton.isEnabled = false; status.text = "처리 중…"
        (application as App).applicationScope.launch {
            val message = withContext(Dispatchers.IO) {
                try {
                    if (export) {
                        val configuration = HttpServerUtils.exportSettings().apply { frpcList = emptyList() }
                        val plain = gson.toJson(Payload(configuration = configuration)).toByteArray(Charsets.UTF_8)
                        val encrypted = EncryptedBackup.encrypt(plain, password)
                        contentResolver.openOutputStream(uri, "wt")!!.use { it.write(encrypted) }
                        "암호화 백업을 저장했습니다."
                    } else {
                        val bytes = contentResolver.openInputStream(uri)!!.use { input ->
                            val output = java.io.ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            while (true) {
                                val count = input.read(buffer); if (count < 0) break
                                require(output.size() + count <= EncryptedBackup.MAX_BYTES)
                                output.write(buffer, 0, count)
                            }
                            output.toByteArray()
                        }
                        val payload = gson.fromJson(String(EncryptedBackup.decrypt(bytes, password), Charsets.UTF_8), Payload::class.java)
                        require(payload.schema == 1 && payload.applicationId == packageName)
                        val config = payload.configuration
                        require(config.senderList != null && config.ruleList != null && config.taskList != null && config.settings.isNotEmpty())
                        val previousPreferences = SharedPreference.exportPreference()
                        try {
                            (application as App).database.runInTransaction {
                                check(HttpServerUtils.restoreSettings(config))
                                SettingUtils.enablePhone = false
                                SettingUtils.enableContactNames = false
                                SettingUtils.enableAutomation = false
                            }
                        } catch (e: Exception) {
                            SharedPreference.clearPreference(); SharedPreference.importPreference(previousPreferences)
                            throw e
                        }
                        "복원 완료. 앱을 다시 열고 필요한 권한과 선택 기능을 확인하세요."
                    }
                } catch (e: Exception) {
                    if (export) "백업 실패: 저장 위치와 파일 권한을 확인하세요."
                    else "복원 실패: 비밀번호·파일 형식을 확인하세요. 변경 전 복원에 실패하면 기존 데이터가 유지됩니다."
                } finally { password.fill('\u0000') }
            }
            withContext(Dispatchers.Main) {
                if (!isFinishing && !isDestroyed) {
                    status.text = message; exportButton.isEnabled = true; importButton.isEnabled = true
                }
            }
        }
    }
}
