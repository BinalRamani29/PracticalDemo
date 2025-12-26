package com.example.demoproject.network.networkUtils

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryQueue @Inject constructor() {
    private val mutex = Mutex()
    private val queuedRequests = mutableListOf<Pair<Interceptor.Chain, Request>>()
    private val pendingResults = mutableMapOf<String, CompletableDeferred<Response>>()

    @Volatile
    private var isNetworkAvailable: Boolean = true


    private fun requestKey(request: Request): String {
        return "${request.method}:${request.url}"
    }

    // Add failed requests to the queue
    suspend fun addRequest(chain: Interceptor.Chain, request: Request) {
        Log.e("RetryQueue", "addRequest")
        mutex.withLock {
            val key = requestKey(request)
            if (!pendingResults.containsKey(key)) {
                Log.e("RetryQueue", "addRequest : $key")
                queuedRequests.add(chain to request)
                pendingResults[key] = CompletableDeferred()
            }
        }
    }

    suspend fun setNetworkStatus(isAvailable: Boolean) {
        Log.e("RetryQueue", "setNetworkStatus : $isAvailable")
        isNetworkAvailable = isAvailable
        if (isAvailable) {
            resumeAll()
        }
    }

    // Wait for the retry result when the network is restored
    suspend fun waitForRetryResult(request: Request): Response {
        val key = requestKey(request)
        Log.e("RetryQueue", "waitForRetryResult : $key")
        return pendingResults[key]?.await()
            ?: run {
                Log.e("RetryQueue", "waitForRetryResult-CancellationException : $key")
                throw CancellationException("Retry was cancelled")

            }
    }

    // Resume all queued requests when network is restored
    private suspend fun resumeAll() {
        Log.e("RetryQueue", "resumeAll: ${queuedRequests.toList()}")
        mutex.withLock {
            val toRetry = queuedRequests.toList()
            queuedRequests.clear()
            toRetry.forEach { (chain, request) ->
                val key = requestKey(request)
                try {
                    val response = chain.proceed(request)
                    Log.e("RetryQueue", "resumeAll-complete: $key")
                    pendingResults[key]?.complete(response)
                } catch (e: Exception) {
                    Log.e("RetryQueue", "resumeAll-completeExceptionally: $key")
                    pendingResults[key]?.completeExceptionally(e)
                } finally {
                    pendingResults.remove(key)
                }
            }
        }
    }

    // Clear all pending requests
    suspend fun clearAll() {
        Log.e("RetryQueue", "clearAll: ${pendingResults.toList()}")
        mutex.withLock {
            pendingResults.values.forEach { it.completeExceptionally(CancellationException("Network retry cancelled")) }
            queuedRequests.clear()
            pendingResults.clear()
        }
    }
}
