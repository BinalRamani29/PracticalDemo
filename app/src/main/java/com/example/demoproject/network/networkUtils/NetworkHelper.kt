@file:Suppress("DEPRECATION")

package com.example.demoproject.network.networkUtils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import javax.inject.Inject


@Suppress("DEPRECATION")
class NetworkHelper @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun isNetworkConnected(): Boolean {
        return withContext(Dispatchers.IO) {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo

            activeNetwork?.let {
                when (it.type) {
                    ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE -> {
                        if ((it.state == NetworkInfo.State.CONNECTED ||
                                    it.state == NetworkInfo.State.CONNECTING) &&
                            isInternet()
                        ) {
                            return@withContext true
                        }
                    }
                }
            }
            return@withContext false
        }
    }

    private suspend fun isInternet(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sock = Socket()
                val socketAddress: SocketAddress = InetSocketAddress("8.8.8.8", 53)
                sock.connect(socketAddress, 1000) // this will block no more than timeoutMs
                sock.close()
                return@withContext true
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return@withContext false
        }
    }
}