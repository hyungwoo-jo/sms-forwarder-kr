package cn.ppps.forwarder.fragment.client

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.ppps.forwarder.App
import cn.ppps.forwarder.R
import cn.ppps.forwarder.activity.MainActivity
import cn.ppps.forwarder.core.BaseFragment
import cn.ppps.forwarder.databinding.FragmentClientCloneBinding
import cn.ppps.forwarder.entity.CloneInfo
import cn.ppps.forwarder.server.model.BaseResponse
import cn.ppps.forwarder.utils.AppUtils
import cn.ppps.forwarder.utils.Base64
import cn.ppps.forwarder.utils.CommonUtils
import cn.ppps.forwarder.utils.HttpServerUtils
import cn.ppps.forwarder.utils.KEY_DEFAULT_SELECTION
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.RSACrypt
import cn.ppps.forwarder.utils.SM4Crypt
import cn.ppps.forwarder.utils.SettingUtils
import cn.ppps.forwarder.utils.XToastUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.xuexiang.xaop.annotation.SingleClick
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.cache.model.CacheMode
import com.xuexiang.xhttp2.callback.SimpleCallBack
import com.xuexiang.xhttp2.exception.ApiException
import com.xuexiang.xpage.annotation.Page
import com.xuexiang.xrouter.annotation.AutoWired
import com.xuexiang.xrouter.launcher.XRouter
import com.xuexiang.xrouter.utils.TextUtils
import com.xuexiang.xui.utils.CountDownButtonHelper
import com.xuexiang.xui.widget.actionbar.TitleBar
import com.xuexiang.xui.widget.dialog.materialdialog.DialogAction
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog
import com.xuexiang.xutil.data.ConvertTools
import com.xuexiang.xutil.file.FileIOUtils
import com.xuexiang.xutil.file.FileUtils
import com.xuexiang.xutil.resource.ResUtils.getStringArray
import java.io.File
import java.util.Date

@Suppress("PrivatePropertyName")
@Page(name = "기기 설정 이전")
class CloneFragment : BaseFragment<FragmentClientCloneBinding?>(), View.OnClickListener {

    private val TAG: String = CloneFragment::class.java.simpleName
    private var backupPath: String? = null
    private val backupFile = "SmsForwarder.json"
    private var pushCountDownHelper: CountDownButtonHelper? = null
    private var pullCountDownHelper: CountDownButtonHelper? = null
    private var exportCountDownHelper: CountDownButtonHelper? = null
    private var importCountDownHelper: CountDownButtonHelper? = null

    @JvmField
    @AutoWired(name = KEY_DEFAULT_SELECTION)
    var defaultSelection: Int = 0

    override fun initArgs() {
        XRouter.getInstance().inject(this)
    }

    override fun viewBindingInflate(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): FragmentClientCloneBinding {
        return FragmentClientCloneBinding.inflate(inflater, container, false)
    }

    override fun initTitle(): TitleBar? {
        val titleBar = super.initTitle()!!.setImmersive(false)
        titleBar.setTitle(R.string.api_clone)
        return titleBar
    }

    /**
     */
    override fun initViews() {
        XXPermissions.with(this)
            .permission(PermissionLists.getManageExternalStoragePermission())
            .request(object : OnPermissionCallback {
                @SuppressLint("SetTextI18n")
                override fun onResult(grantedList: MutableList<IPermission>, deniedList: MutableList<IPermission>) {
                    val allGranted = deniedList.isEmpty()
                    if (!allGranted) {
                        val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(requireActivity(), deniedList)
                        if (doNotAskAgain) {
                            XToastUtils.error(R.string.toast_denied_never)
                            XXPermissions.startPermissionActivity(requireContext(), deniedList)
                        }
                        binding!!.tvBackupPath.text = getString(R.string.storage_permission_tips)
                        return
                    }
                    backupPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
                    binding!!.tvBackupPath.text = backupPath + File.separator + backupFile

                }
            })

        binding!!.tabBar.setTabTitles(getStringArray(R.array.clone_type_option))
        binding!!.tabBar.setOnTabClickListener { _, position ->
            if (position == 1) {
                binding!!.layoutNetwork.visibility = View.GONE
                binding!!.layoutOffline.visibility = View.VISIBLE
            } else {
                binding!!.layoutNetwork.visibility = View.VISIBLE
                binding!!.layoutOffline.visibility = View.GONE
            }
        }
        if (defaultSelection == 1) {
            binding!!.tabBar.visibility = View.GONE
            binding!!.layoutNetwork.visibility = View.GONE
            binding!!.layoutOffline.visibility = View.VISIBLE
        }

        pushCountDownHelper = CountDownButtonHelper(binding!!.btnPush, SettingUtils.requestTimeout)
        pushCountDownHelper!!.setOnCountDownListener(object : CountDownButtonHelper.OnCountDownListener {
            override fun onCountDown(time: Int) {
                binding!!.btnPush.text = String.format(getString(R.string.seconds_n), time)
            }

            override fun onFinished() {
                binding!!.btnPush.text = getString(R.string.push)
            }
        })
        pullCountDownHelper = CountDownButtonHelper(binding!!.btnPull, SettingUtils.requestTimeout)
        pullCountDownHelper!!.setOnCountDownListener(object : CountDownButtonHelper.OnCountDownListener {
            override fun onCountDown(time: Int) {
                binding!!.btnPull.text = String.format(getString(R.string.seconds_n), time)
            }

            override fun onFinished() {
                binding!!.btnPull.text = getString(R.string.pull)
            }
        })
        exportCountDownHelper = CountDownButtonHelper(binding!!.btnExport, 3)
        exportCountDownHelper!!.setOnCountDownListener(object : CountDownButtonHelper.OnCountDownListener {
            override fun onCountDown(time: Int) {
                binding!!.btnExport.text = String.format(getString(R.string.seconds_n), time)
            }

            override fun onFinished() {
                binding!!.btnExport.text = getString(R.string.export)
            }
        })
        importCountDownHelper = CountDownButtonHelper(binding!!.btnImport, 3)
        importCountDownHelper!!.setOnCountDownListener(object : CountDownButtonHelper.OnCountDownListener {
            override fun onCountDown(time: Int) {
                binding!!.btnImport.text = String.format(getString(R.string.seconds_n), time)
            }

            override fun onFinished() {
                binding!!.btnImport.text = getString(R.string.imports)
            }
        })
    }

    override fun initListeners() {
        binding!!.btnPush.setOnClickListener(this)
        binding!!.btnPull.setOnClickListener(this)
        binding!!.btnExport.setOnClickListener(this)
        binding!!.btnImport.setOnClickListener(this)
    }

    @SingleClick
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_push -> pushData()
            R.id.btn_pull -> pullData()
            R.id.btn_export -> {
                try {
                    exportCountDownHelper?.start()
                    val file = File(backupPath + File.separator + backupFile)
                    FileUtils.createFileByDeleteOldFile(file)
                    val cloneInfo = HttpServerUtils.exportSettings()
                    val jsonStr = Gson().toJson(cloneInfo)
                    Log.d(TAG, "jsonStr = $jsonStr")
                    if (FileIOUtils.writeFileFromString(file, jsonStr)) {
                        XToastUtils.success(getString(R.string.export_succeeded))
                    } else {
                        binding!!.tvExport.text = getString(R.string.export_failed)
                        XToastUtils.error(getString(R.string.export_failed))
                    }
                } catch (e: Exception) {
                    XToastUtils.error(String.format(getString(R.string.export_failed_tips), e.message))
                }
            }
            R.id.btn_import -> {
                try {
                    importCountDownHelper?.start()
                    val file = File(backupPath + File.separator + backupFile)
                    if (!FileUtils.isFileExists(file)) {
                        XToastUtils.error(getString(R.string.import_failed_file_not_exist))
                        return
                    }

                    val jsonStr = FileIOUtils.readFile2String(file)
                    Log.d(TAG, "jsonStr = $jsonStr")
                    if (TextUtils.isEmpty(jsonStr)) {
                        XToastUtils.error(getString(R.string.import_failed))
                        return
                    }

                    val builder = GsonBuilder()
                    builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Any?> { _, _, _ -> Date() })
                    val gson = builder.create()
                    val cloneInfo = gson.fromJson(jsonStr, CloneInfo::class.java)
                    Log.d(TAG, "cloneInfo = $cloneInfo")

                    HttpServerUtils.compareVersion(cloneInfo)

                    if (HttpServerUtils.restoreSettings(cloneInfo)) {
                        MaterialDialog.Builder(requireContext())
                            .iconRes(R.drawable.icon_api_clone)
                            .title(R.string.clone)
                            .content(R.string.import_succeeded)
                            .cancelable(false)
                            .positiveText(R.string.confirm)
                            .onPositive { _: MaterialDialog?, _: DialogAction? ->
                                val intent = Intent(App.context, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }
                            .show()
                    } else {
                        XToastUtils.error(getString(R.string.import_failed))
                    }
                } catch (e: Exception) {
                    XToastUtils.error(String.format(getString(R.string.import_failed_tips), e.message))
                }
            }
        }
    }

    private fun pushData() {
        if (!CommonUtils.checkUrl(HttpServerUtils.serverAddress)) {
            XToastUtils.error(getString(R.string.invalid_service_address))
            return
        }

        pushCountDownHelper?.start()

        val requestUrl: String = HttpServerUtils.serverAddress + "/clone/push"
        Log.i(TAG, "requestUrl:$requestUrl")

        val msgMap: MutableMap<String, Any> = mutableMapOf()
        val timestamp = System.currentTimeMillis()
        msgMap["timestamp"] = timestamp
        val clientSignKey = HttpServerUtils.clientSignKey
        if (!TextUtils.isEmpty(clientSignKey)) {
            msgMap["sign"] = HttpServerUtils.calcSign(timestamp.toString(), clientSignKey)
        }
        msgMap["data"] = HttpServerUtils.exportSettings()

        var requestMsg: String = Gson().toJson(msgMap)
        Log.i(TAG, "requestMsg:$requestMsg")

        val postRequest = XHttp.post(requestUrl).keepJson(true).timeOut((SettingUtils.requestTimeout * 1000).toLong())
            .cacheMode(CacheMode.NO_CACHE).timeStamp(true)

        when (HttpServerUtils.clientSafetyMeasures) {
            2 -> {
                val publicKey = RSACrypt.getPublicKey(HttpServerUtils.clientSignKey)
                try {
                    requestMsg = Base64.encode(requestMsg.toByteArray())
                    requestMsg = RSACrypt.encryptByPublicKey(requestMsg, publicKey)
                    Log.i(TAG, "requestMsg: $requestMsg")
                } catch (e: Exception) {
                    XToastUtils.error(getString(R.string.request_failed) + e.message)
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                    return
                }
                postRequest.upString(requestMsg)
            }

            3 -> {
                try {
                    val sm4Key = ConvertTools.hexStringToByteArray(HttpServerUtils.clientSignKey)
                    //requestMsg = Base64.encode(requestMsg.toByteArray())
                    val encryptCBC = SM4Crypt.encrypt(requestMsg.toByteArray(), sm4Key)
                    requestMsg = ConvertTools.bytes2HexString(encryptCBC)
                    Log.i(TAG, "requestMsg: $requestMsg")
                } catch (e: Exception) {
                    XToastUtils.error(getString(R.string.request_failed) + e.message)
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                    return
                }
                postRequest.upString(requestMsg)
            }

            else -> {
                postRequest.upJson(requestMsg)
            }
        }

        postRequest.execute(object : SimpleCallBack<String>() {
            override fun onError(e: ApiException) {
                XToastUtils.error(e.displayMessage)
                pushCountDownHelper?.finish()
            }

            override fun onSuccess(response: String) {
                Log.i(TAG, response)
                try {
                    var json = response
                    if (HttpServerUtils.clientSafetyMeasures == 2) {
                        val publicKey = RSACrypt.getPublicKey(HttpServerUtils.clientSignKey)
                        json = RSACrypt.decryptByPublicKey(json, publicKey)
                        json = String(Base64.decode(json))
                    } else if (HttpServerUtils.clientSafetyMeasures == 3) {
                        val sm4Key = ConvertTools.hexStringToByteArray(HttpServerUtils.clientSignKey)
                        val encryptCBC = ConvertTools.hexStringToByteArray(json)
                        val decryptCBC = SM4Crypt.decrypt(encryptCBC, sm4Key)
                        json = String(decryptCBC)
                    }
                    val resp: BaseResponse<String> = Gson().fromJson(json, object : TypeToken<BaseResponse<String>>() {}.type)
                    if (resp.code == 200) {
                        XToastUtils.success(getString(R.string.request_succeeded))
                    } else {
                        XToastUtils.error(getString(R.string.request_failed) + resp.msg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                    XToastUtils.error(getString(R.string.request_failed) + response)
                }
                pushCountDownHelper?.finish()
            }
        })

    }

    private fun pullData() {
        if (!CommonUtils.checkUrl(HttpServerUtils.serverAddress)) {
            XToastUtils.error(getString(R.string.invalid_service_address))
            return
        }

        exportCountDownHelper?.start()

        val requestUrl: String = HttpServerUtils.serverAddress + "/clone/pull"
        Log.i(TAG, "requestUrl:$requestUrl")

        val msgMap: MutableMap<String, Any> = mutableMapOf()
        val timestamp = System.currentTimeMillis()
        msgMap["timestamp"] = timestamp
        val clientSignKey = HttpServerUtils.clientSignKey
        if (!TextUtils.isEmpty(clientSignKey)) {
            msgMap["sign"] = HttpServerUtils.calcSign(timestamp.toString(), clientSignKey)
        }

        val dataMap: MutableMap<String, Any> = mutableMapOf()
        dataMap["version_code"] = AppUtils.getAppVersionCode()
        msgMap["data"] = dataMap

        var requestMsg: String = Gson().toJson(msgMap)
        Log.i(TAG, "requestMsg:$requestMsg")

        val postRequest = XHttp.post(requestUrl).keepJson(true).timeStamp(true)

        when (HttpServerUtils.clientSafetyMeasures) {
            2 -> {
                val publicKey = RSACrypt.getPublicKey(HttpServerUtils.clientSignKey)
                try {
                    requestMsg = Base64.encode(requestMsg.toByteArray())
                    requestMsg = RSACrypt.encryptByPublicKey(requestMsg, publicKey)
                    Log.i(TAG, "requestMsg: $requestMsg")
                } catch (e: Exception) {
                    XToastUtils.error(getString(R.string.request_failed) + e.message)
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                    return
                }
                postRequest.upString(requestMsg)
            }

            3 -> {
                try {
                    val sm4Key = ConvertTools.hexStringToByteArray(HttpServerUtils.clientSignKey)
                    //requestMsg = Base64.encode(requestMsg.toByteArray())
                    val encryptCBC = SM4Crypt.encrypt(requestMsg.toByteArray(), sm4Key)
                    requestMsg = ConvertTools.bytes2HexString(encryptCBC)
                    Log.i(TAG, "requestMsg: $requestMsg")
                } catch (e: Exception) {
                    XToastUtils.error(getString(R.string.request_failed) + e.message)
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                    return
                }
                postRequest.upString(requestMsg)
            }

            else -> {
                postRequest.upJson(requestMsg)
            }
        }

        postRequest.execute(object : SimpleCallBack<String>() {
            override fun onError(e: ApiException) {
                XToastUtils.error(e.displayMessage)
                exportCountDownHelper?.finish()
            }

            override fun onSuccess(response: String) {
                Log.i(TAG, response)
                try {
                    var json = response
                    if (HttpServerUtils.clientSafetyMeasures == 2) {
                        val publicKey = RSACrypt.getPublicKey(HttpServerUtils.clientSignKey)
                        json = RSACrypt.decryptByPublicKey(json, publicKey)
                        json = String(Base64.decode(json))
                    } else if (HttpServerUtils.clientSafetyMeasures == 3) {
                        val sm4Key = ConvertTools.hexStringToByteArray(HttpServerUtils.clientSignKey)
                        val encryptCBC = ConvertTools.hexStringToByteArray(json)
                        val decryptCBC = SM4Crypt.decrypt(encryptCBC, sm4Key)
                        json = String(decryptCBC)
                    }

                    val builder = GsonBuilder()
                    builder.registerTypeAdapter(Date::class.java, JsonDeserializer<Any?> { _, _, _ -> Date() })
                    val gson = builder.create()
                    val resp: BaseResponse<CloneInfo> = gson.fromJson(json, object : TypeToken<BaseResponse<CloneInfo>>() {}.type)
                    if (resp.code == 200) {
                        val cloneInfo = resp.data
                        Log.d(TAG, "cloneInfo = $cloneInfo")

                        if (cloneInfo == null) {
                            XToastUtils.error(getString(R.string.request_failed))
                            return
                        }

                        HttpServerUtils.compareVersion(cloneInfo)

                        if (HttpServerUtils.restoreSettings(cloneInfo)) {
                            XToastUtils.success(getString(R.string.import_succeeded))
                        }
                    } else {
                        XToastUtils.error(getString(R.string.request_failed) + resp.msg)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, e.toString())
                    XToastUtils.error(getString(R.string.request_failed) + response)
                }
                exportCountDownHelper?.finish()
            }
        })

    }

    override fun onDestroyView() {
        if (pushCountDownHelper != null) pushCountDownHelper!!.recycle()
        if (pullCountDownHelper != null) pullCountDownHelper!!.recycle()
        if (exportCountDownHelper != null) exportCountDownHelper!!.recycle()
        if (importCountDownHelper != null) importCountDownHelper!!.recycle()
        super.onDestroyView()
    }
}