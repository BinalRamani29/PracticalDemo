package com.example.demoproject.network.networkUtils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.demoproject.nointernet.NoInternetActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val retryQueue: RetryQueue,
    private val networkHelper: NetworkHelper,
    private val dispatcherIo: CoroutineDispatcher
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    private val mNetworkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            Log.d("NetworkMonitor", "Network available")
            CoroutineScope(dispatcherIo).launch {
                repeat(10) { attempt ->
                    val isConnected = networkHelper.isNetworkConnected()
                    Log.d("NetworkMonitor", "Attempt $attempt")
                    if (isConnected) {
                        Log.d("NetworkMonitor", "isConnected true")
                        withContext(Dispatchers.Main) {
                            sendPopNoInternetBroadcast()
                        }
                        retryQueue.setNetworkStatus(true)
                        return@launch
                    } else {
                        Log.d("NetworkMonitor", "isConnected false")
                        delay(500)
                    }
                }
                Log.d("NetworkMonitor", "3 failed attempts")
                // After 3 failed attempts, still resume with false positive (to unblock UI)
                withContext(Dispatchers.Main) {
                    sendPopNoInternetBroadcast()
                }
                retryQueue.setNetworkStatus(true)
            }
        }

        override fun onLost(network: Network) {
            Log.d("NetworkMonitor", "Network lost")
            CoroutineScope(Dispatchers.Main).launch {
                launchNoInternetActivity()
            }
            CoroutineScope(dispatcherIo).launch {
                retryQueue.setNetworkStatus(false)
            }
        }
    }


    fun startMonitoring() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(
            request,
            mNetworkCallback
        )
    }

    fun performInitialCheck() {
        CoroutineScope(dispatcherIo).launch {
            val isConnected = networkHelper.isNetworkConnected()
            if (isConnected) {
                Log.d("NetworkMonitor", "isConnected true")
                withContext(Dispatchers.Main) {
                    sendPopNoInternetBroadcast()
                }
                retryQueue.setNetworkStatus(true)
                return@launch
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    launchNoInternetActivity()
                }
                CoroutineScope(dispatcherIo).launch {
                    retryQueue.setNetworkStatus(false)
                }
            }
        }
    }

    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(mNetworkCallback)
        } catch (e: Exception) {
            Log.e("NetworkMonitor", "Unregister failed: ${e.localizedMessage}")
        }
    }

    private fun launchNoInternetActivity() {
        val intent = Intent(context, NoInternetActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
    }

    private fun sendPopNoInternetBroadcast() {
        val intent = Intent(ACTION_NETWORK_RESTORED)

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    companion object {
        const val ACTION_NETWORK_RESTORED = "com.demo.NETWORK_RESTORED"
    }
}
