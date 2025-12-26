package com.example.demoproject.network.interceptor

import android.content.Context
import com.example.demoproject.network.base.BaseInterceptor
import com.example.demoproject.network.networkUtils.RetryQueue
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import javax.inject.Inject

class NoAuthInterceptor @Inject constructor(
    private val retryQueue: RetryQueue, @ApplicationContext private val appContext: Context
) : BaseInterceptor(appContext) {

    override fun intercept(chain: Interceptor.Chain): Response {
        val (modifiedRequest, requestId) = interceptBase(chain.request(), chain)

        return try {
            val response = chain.proceed(modifiedRequest)
            captureResponseBody(response, requestId)
        } catch (e: IOException) {
            if (e is UnknownHostException || e is ConnectException) {
                val result = runBlocking(Dispatchers.IO) {
                    retryQueue.addRequest(chain, modifiedRequest)
                    retryQueue.waitForRetryResult(modifiedRequest)
                }
                captureResponseBody(result, requestId)
            } else {
                throw e
            }
        }
    }
}
