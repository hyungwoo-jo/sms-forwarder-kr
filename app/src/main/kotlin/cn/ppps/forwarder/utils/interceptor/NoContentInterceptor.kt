package cn.ppps.forwarder.utils.interceptor

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import cn.ppps.forwarder.entity.result.SendResponse
import cn.ppps.forwarder.utils.Log
import cn.ppps.forwarder.utils.Worker
import cn.ppps.forwarder.workers.UpdateLogsWorker
import com.xuexiang.xutil.XUtil
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

@Suppress("PrivatePropertyName")
class NoContentInterceptor(private val logId: Long) : Interceptor {

    private val TAG: String = NoContentInterceptor::class.java.simpleName

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        if (originalResponse.code() in 201..299) {
            val response = "HTTP Status " + originalResponse.code() + " " + originalResponse.message()
            Log.d(TAG, response)
            /*
            val message = "{\"Code\":0, \"Msg\":\"\", \"Data\":{}}"
            val emptyJsonBody = ResponseBody.create(MediaType.parse("application/json"), message)
            return originalResponse.newBuilder()
                .body(emptyJsonBody)
                .header("Content-Length", message.length.toString())
                .build()
            */
            val sendResponse = SendResponse(logId, 2, response)
            val request = OneTimeWorkRequestBuilder<UpdateLogsWorker>()
                .setInitialDelay(200, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        Worker.UPDATE_LOGS to Gson().toJson(sendResponse)
                    )
                ).build()
            WorkManager.getInstance(XUtil.getContext()).enqueue(request)
        }

        return originalResponse
    }
}

