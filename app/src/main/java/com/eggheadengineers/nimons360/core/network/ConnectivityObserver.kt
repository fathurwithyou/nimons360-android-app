package com.eggheadengineers.nimons360.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

enum class NetworkStatus { WIFI, MOBILE, OFFLINE }

class ConnectivityObserver(context: Context) {

    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val activeNetworks = ConcurrentHashMap<Network, NetworkCapabilities>()

    private val _status = MutableStateFlow(currentStatus())
    val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val caps = cm.getNetworkCapabilities(network) ?: return
                activeNetworks[network] = caps
                _status.value = computeStatus()
            }
            override fun onLost(network: Network) {
                activeNetworks.remove(network)
                _status.value = computeStatus()
            }
            override fun onUnavailable() {
                _status.value = NetworkStatus.OFFLINE
            }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                activeNetworks[network] = caps
                _status.value = computeStatus()
            }
        })
    }

    private fun computeStatus(): NetworkStatus {
        val caps = activeNetworks.values
        return when {
            caps.any { it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } -> NetworkStatus.WIFI
            caps.any { it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } -> NetworkStatus.MOBILE
            else -> NetworkStatus.OFFLINE
        }
    }

    private fun currentStatus(): NetworkStatus {
        val net = cm.activeNetwork ?: return NetworkStatus.OFFLINE
        val caps = cm.getNetworkCapabilities(net) ?: return NetworkStatus.OFFLINE
        return resolveTransport(caps)
    }

    private fun resolveTransport(caps: NetworkCapabilities): NetworkStatus = when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.MOBILE
        else -> NetworkStatus.OFFLINE
    }
}
