package com.example.demoproject.network.base

import android.content.Context
import android.util.Log
import com.example.demoproject.R
import com.example.demoproject.network.controllers.Resource
import com.example.demoproject.network.logsExension.buildCurlCommand
import com.example.demoproject.network.logsExension.printApiDataForNotFound
import com.example.demoproject.network.model.parseError
import com.example.demoproject.network.networkExtensions.getNetworkType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Request
import okhttp3.RequestBody
import okio.Buffer
import retrofit2.Response
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.CancellationException

open class ApiManager(
    private val context: Context, private vararg val baseInterceptor: BaseInterceptor
) {

    companion object {
        const val TAG = "ApiManager"
        val excludeLogApis = arrayOf("support/attachment")
    }

    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Resource<T> {
        try {
            val response = apiCall()
            try {
                printApiDataApi(response.raw().request, response.raw().request.body, response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return Resource.Success(body, response.code())
                }
            }

            return error(response)
        } catch (e: Exception) {
            e.printStackTrace(System.err)
            return errorException(e)
        }
    }

    private fun <T> error(response: Response<T>?): Resource<T> {
        Log.i(TAG, "API response: " + response?.code())

        response?.let {
            val errorBody = response.errorBody()
            val errorString = errorBody?.string()

            val error = if (!errorString.isNullOrEmpty()) {
                parseError(errorString)
            } else {
                null
            }
            return when (response.code()) {
                429 -> {
                    Resource.Error(
                        message = context.getString(R.string.error_something_went_wrong),
                        errorCode = response.code()
                    )
                }


                else -> {
                    Resource.Error(
                        message = error?.message
                            ?: context.getString(R.string.error_something_went_wrong),
                        errorCode = response.code()
                    )
                }
            }
        } ?: run {
            val errorMessage = context.getString(R.string.error_something_went_wrong)
            Log.i(TAG, "API Error Response is NULL: $errorMessage")
            return Resource.Error(errorMessage, null)
        }
    }

    private fun <T> errorException(exception: Exception): Resource<T> {
        return when (exception) {
            is CancellationException -> {
                Resource.Error("")
            }

            else -> {
                Resource.Error(context.getString(R.string.error_something_went_wrong))
            }
        }
    }

    private fun printApiDataApi(
        request: Request? = null, body: RequestBody? = null, response: Response<*>? = null
    ) {
        try {
            val requestId = request?.header("REQUEST-ID").orEmpty()
            val apiTrackingModel =
                baseInterceptor.firstOrNull { it.apiTimeTrackingMap.containsKey(requestId) }?.apiTimeTrackingMap?.remove(
                    requestId
                )?.apply {
                    responseTime = System.currentTimeMillis()
                    status = if (response?.isSuccessful == true) "Success" else "Error"
                    headers = request?.headers?.toMultimap()

                    // Body
                    val bodyString = body?.let { bodyToString(it) }
                    apiBody = when {
                        bodyString == null -> null
                        bodyString.toByteArray(StandardCharsets.UTF_8).size < 1_048_576 -> bodyString
                        else -> "Body is greater than 1MB"
                    }

                    // Response
                    apiResponse = response?.body()?.let { Gson().toJson(it) }

                    // Other details
                    networkType = getNetworkType(context)
                    code = response?.code()?.toString()

                    // Time metrics
                    serverExecutionTime = getServerExecutionTime(apiResponse)
                    clientExecutionTime = responseTime - requestTime
                    latency = getLatency(clientExecutionTime, serverExecutionTime)
                }

            buildCurlCommand(request, body, response)

            if (apiTrackingModel == null) {
                printApiDataForNotFound(request, body, response)
                return
            }

            with(apiTrackingModel) {
                Log.i(TAG, "API Header: ${Gson().toJson(headers)}")
                Log.i(TAG, "API Request Body: $apiBody")

                if (!excludeLogApis.any { url.contains(it, ignoreCase = true) }) {
                }
                Log.i(
                    TAG,
                    "Request Id: $urlRequestId : Response URL: $url : $status" + " : serverExecutionTime-${serverExecutionTime}ms" + " : clientExecutionTime-${clientExecutionTime}ms" + " : Latency-${latency}ms" + " : Network Type: $networkType"
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
            printApiDataForNotFound(request, body, response)
        }
    }


    private fun getServerExecutionTime(response: Any?): Long {
        return try {
            val jsonMap: Map<String, Any?> = Gson().fromJson(
                response?.toString().orEmpty(), object : TypeToken<Map<String, Any?>>() {}.type
            )

            when (val duration = jsonMap["duration"]) {
                is Number -> duration.toLong()
                else -> 0L
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i(TAG, "Duration Not Found...")
            0L
        }
    }


    private fun getLatency(clientExecutionTime: Long, serverExecutionTime: Long): Long =
        clientExecutionTime - serverExecutionTime

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
}
