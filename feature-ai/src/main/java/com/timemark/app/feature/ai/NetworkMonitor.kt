package com.timemark.app.feature.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 网络类型（Task 33.3）
 */
enum class NetworkType {
    /** WiFi 连接 */
    WIFI,
    /** 移动数据 */
    CELLULAR,
    /** 以太网 */
    ETHERNET,
    /** 无网络 */
    NONE,
    /** 未知网络 */
    UNKNOWN
}

/**
 * 网络状态监控器（Task 33.3）
 *
 * 通过 ConnectivityManager 监听网络变化，提供当前网络类型的 Flow。
 * 用于 AI 功能在 wifiOnlyMode 下判断是否可发起请求。
 */
class NetworkMonitor(private val context: Context) {

    /**
     * 当前网络类型的 Flow
     *
     * 使用 callbackFlow 包装 NetworkCallback，订阅时注册回调，取消订阅时注销回调。
     */
    fun networkType(): Flow<NetworkType> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager
        if (connectivityManager == null) {
            trySend(NetworkType.UNKNOWN)
            awaitClose { }
            return@callbackFlow
        }

        // 当前网络类型
        fun currentType(): NetworkType {
            val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.NONE
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkType.NONE
            return when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.UNKNOWN
            }
        }

        // 发送当前状态
        trySend(currentType())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(currentType())
            }

            override fun onLost(network: Network) {
                trySend(currentType())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(currentType())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    /**
     * 当前是否为 WiFi 连接
     */
    fun isWifi(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
}
