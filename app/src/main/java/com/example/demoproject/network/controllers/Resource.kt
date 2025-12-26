package com.example.demoproject.network.controllers

import com.example.demoproject.network.model.BaseAPIResult

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val statusCode: Int = 200,
    val baseResult: BaseAPIResult? = null
) {

    class Success<T>(data: T? = null, statusCode: Int) : Resource<T>(data, statusCode = statusCode)

    class Error<T>(
        message: String, data: T? = null, errorCode: Int = 400, baseResult: BaseAPIResult? = null
    ) : Resource<T>(data, message, statusCode = errorCode, baseResult = baseResult)

    class Loading<T> : Resource<T>(null)

}
