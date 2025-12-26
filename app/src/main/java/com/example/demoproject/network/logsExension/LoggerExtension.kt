package com.example.demoproject.network.logsExension

import android.util.Log
import com.example.demoproject.network.base.ApiManager.Companion.TAG
import com.google.gson.Gson
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.Response
import java.io.IOException


fun buildCurlCommand(
    request: Request?,
    body: RequestBody?,
    response: Response<*>?,
): String {
    val headers =
        request?.headers?.toMultimap()?.map { "-H \"${it.key}: ${it.value.joinToString()}\"" }
            ?.joinToString(" ") ?: ""

    val method = request?.method ?: "GET"
    val url = request?.url?.toUri()?.toString() ?: ""
    val bodyString = body?.let { bodyToString(it) } ?: ""

    val rawResponse = try {
        Gson().toJson(response?.body())
    } catch (e: Exception) {
        "Error reading raw response: ${e.message}"
    }

    return buildString {
        appendLine("========== 🌐 CURL REQUEST ==========")
        appendLine("URL: $url")
        appendLine("Method: $method")
        if (headers.isNotEmpty()) {
            appendLine("Headers:")
            headers.split("-H ").filter { it.isNotBlank() }.forEach {
                appendLine("  -H $it")
            }
        }
        if (bodyString.isNotEmpty()) {
            appendLine("Body:")
            appendLine("$bodyString")
        }
        appendLine()
        appendLine("💡 Full Curl Command:")
        appendLine("curl -X $method $headers ${if (bodyString.isNotEmpty()) "--data '$bodyString'" else ""} \"$url\"")
        appendLine()
        appendLine("========== 📥 RESPONSE ==========")
        appendLine("Code: ${response?.code()}")
        appendLine("Raw: $rawResponse")
        appendLine("=================================")
    }
}

fun printApiDataForNotFound(
    request: Request? = null,
    body: RequestBody? = null,
    response: Response<*>? = null
) {
    val url = request?.url?.toUri()?.toString().orEmpty()
    val headers = Gson().toJson(request?.headers?.toMultimap())
    val bodyString = body?.let { bodyToString(it) }.orEmpty()
    val responseBody = Gson().toJson(response?.body())

    Log.i(TAG, "ApiTrackingModel is null:")
    Log.i(TAG, "API Request: $url")
    Log.i(TAG, "API Header: $headers")
    Log.i(TAG, "API Request Body: $bodyString")
    Log.i(TAG, "API Response: $responseBody")
}

private fun bodyToString(body: RequestBody): String {
    return try {
        val buffer = Buffer()
        body.writeTo(buffer)
        buffer.readUtf8()
    } catch (e: IOException) {
        e.printStackTrace()
        "Unable to read body"
    }
}