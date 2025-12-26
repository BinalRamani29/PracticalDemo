package com.example.demoproject.network.interceptor

import android.content.Context
import com.example.demoproject.network.base.BaseInterceptor
import com.example.demoproject.network.networkUtils.RetryQueue
import com.example.demoproject.utils.nullSafeExtensions.orDefault
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(
    @ApplicationContext val appContext: Context,
    private val retryQueue: RetryQueue
) : BaseInterceptor(appContext) {

    override fun intercept(chain: Interceptor.Chain): Response {

        val accessToken = ""

        val requestBuilder = chain.request().newBuilder()

        requestBuilder.addHeader(
            "Authorization", "Bearer ${accessToken.orDefault()}"
        )
        val request = requestBuilder.build()

        return try {
            val response = chain.proceed(request)
            captureResponseBody(response)
        } catch (e: IOException) {
            if (e is UnknownHostException || e is ConnectException) {
                val result = runBlocking(Dispatchers.IO) {
                    retryQueue.addRequest(chain, request)
                    retryQueue.waitForRetryResult(request)
                }
                captureResponseBody(result)
            } else {
                throw e
            }
        }
    }

    private fun captureResponseBody(response: Response): Response {
        return response
    }
}