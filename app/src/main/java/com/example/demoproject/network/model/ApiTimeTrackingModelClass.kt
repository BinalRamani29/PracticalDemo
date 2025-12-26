package com.example.demoproject.network.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ApiTimeTrackingModelClass(
    @SerializedName("url")
    var url: String,

    @SerializedName("method")
    var method: String? = null,

    @SerializedName("code")
    var code: String? = null,

    @SerializedName("headers")
    var headers: Map<String, Any>? = null,

    @SerializedName("status")
    var status: String? = null,

    @SerializedName("url_request_id")
    var urlRequestId: String? = null,

    @SerializedName("api_body")
    var apiBody: String? = null,

    @SerializedName("original_api_response")
    var originalApiResponse: Any? = null,

    @SerializedName("api_response")
    var apiResponse: Any? = null,

    @SerializedName("server_execution_time")
    var serverExecutionTime: Long = 0,

    @SerializedName("requestTime")
    var requestTime: Long = 0,

    @SerializedName("responseTime")
    var responseTime: Long = 0,

    @SerializedName("client_execution_time")
    var clientExecutionTime: Long = 0,

    @SerializedName("latency")
    var latency: Long = 0,

    @SerializedName("networkType")
    var networkType: String? = null,

    @SerializedName("errorClass")
    var errorClass: String? = null,

    @SerializedName("errorMessage")
    var errorMessage: String? = null,
) : Serializable