package com.example.demoproject.network.base

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlin.apply
import kotlin.text.isNullOrBlank
import kotlin.text.startsWith
import kotlin.text.trim

open class BaseAPIResult {

    @SerializedName("has_next")
    var hasNext: Boolean? = null

    @SerializedName("status")
    var status = 0

    @SerializedName("message")
    var message: String? = null

    @SerializedName("hostname")
    var hostname: String? = null

    @SerializedName("error")
    var baseAPIError: BaseAPIError? = null

    @SerializedName("executionTimeInMS")
    var executionTimeInMS: Long = 0

    @SerializedName("duration")
    var duration: Int? = null

    @SerializedName("code")
    var code: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("success")
    var success: Boolean? = null

    @SerializedName("status_code")
    var statusCode: Int? = null

    @SerializedName("errors")
    var errors: JsonObject? = null

}

class BaseAPIError {
    @SerializedName("errorCode")
    var errorCode = 0

    @SerializedName("status_code")
    var statusCode: Int? = null

    @SerializedName("code")
    var code: String? = null

    @SerializedName("errorMessage")
    var errorMessage: String? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("data")
    var data: Map<String, Any>? = null
}

fun parseError(response: String?): BaseAPIResult? {
    if (response.isNullOrBlank()) return null

    return try {
        if (response.trim().startsWith("{") || response.trim().startsWith("[")) {
            // Try parsing as JSON Object
            val type = object : TypeToken<BaseAPIResult>() {}.type
            Gson().fromJson(response, type)
        } else {
            // Response is a plain String (Not JSON), wrap it manually
            BaseAPIResult().apply {
                message = response
            }
        }
    } catch (e: Exception) {
        e.printStackTrace(System.err)
        null
    }
}