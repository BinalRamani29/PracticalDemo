package com.example.demoproject.network.base

import android.content.Context
import android.util.Log
import com.example.demoproject.network.model.ApiTimeTrackingModelClass
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import java.nio.charset.Charset
import java.util.Date
import java.util.UUID
import kotlin.collections.set


abstract class BaseInterceptor(
    val context: Context
) : Interceptor {


    companion object {
        const val TAG = "BaseInterceptor"
    }

    val apiTimeTrackingMap = HashMap<String, ApiTimeTrackingModelClass>()

    protected fun interceptBase(request: Request, chain: Interceptor.Chain): Pair<Request, String> {
        val requestTime = Date().time
        val requestId = getRequestId(requestTime)

        try {
            ApiTimeTrackingModelClass(request.url.toUri().toString()).apply {
                this.method = request.method
                this.urlRequestId = requestId
                this.requestTime = requestTime
                apiTimeTrackingMap[requestId] =  this
            }
        } catch (_: Exception) {
        }

        val newRequest = request.newBuilder().apply {
            prepareHeaderMap(requestId).forEach {
                try {
                    addHeader(it.key, it.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }.build()

        return newRequest to requestId
    }

    protected fun captureResponseBody(response: Response, requestId: String): Response {
        try {
//            if (BuildConfig.printLogs) {
                val responseBody = response.body ?: return response

                try {
                    val source = responseBody.source()
                    source.request(Long.MAX_VALUE) // Buffer the entire body.
                    var buffer = source.buffer
                    if ("gzip".equals(response.headers["Content-Encoding"], ignoreCase = true)) {
                        buffer?.clone()?.let { GzipSource(it) }.use { gzippedResponseBody ->
                            buffer = Buffer()
                            gzippedResponseBody?.let { buffer?.writeAll(it) }
                        }
                    }
                    apiTimeTrackingMap[requestId]?.originalApiResponse =
                        buffer?.clone()?.readString(
                            responseBody.contentType()?.charset() ?: Charset.forName("UTF-8")
                        )
                } catch (closed: IllegalStateException) {
                    // Body already consumed/closed, just skip logging instead of crashing
                    Log.i(ApiManager.TAG, "Response body already closed, skipping capture...")
                }
//            }
        } catch (e: Exception) {
            Log.i(ApiManager.TAG, "Original Response Not Found...")
            e.printStackTrace()
        }
        return response
    }


    private fun getRequestId(requestTime: Long): String {
        return requestTime.toString() + "_" + getRandomKey()
    }

    private fun getRandomKey(): String {
        return UUID.randomUUID().toString()
    }

    private fun prepareHeaderMap(requestId: String?): MutableMap<String, String> {
        return mutableMapOf<String, String>().apply {
            put("DEVICE-TYPE", "android")
            requestId?.let { put("REQUEST-ID", it) }
            put("TIMESTAMP", System.currentTimeMillis().toString())
        }
    }
}
