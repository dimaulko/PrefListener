package com.uds_improveit.preflistener

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitoringUtil(context: Context) : NetworkCallback() {
    private val mNetworkRequest: NetworkRequest =
        NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()
    private val mConnectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState: MutableStateFlow<Boolean> = MutableStateFlow<Boolean>(false)
    val networkState: StateFlow<Boolean> = _networkState.asStateFlow()

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        Log.d("NetworkMonitoringUtil", "onAvailable() called: Connected to network")
        _networkState.value = true
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        Log.e("NetworkMonitoringUtil", "onLost() called: Lost network connection")
        _networkState.value = false
    }

    /**
     * Registers the Network-Request callback
     * (Note: Register only once to prevent duplicate callbacks)
     */
    fun registerNetworkCallbackEvents() {
        Log.d("NetworkMonitoringUtil", "registerNetworkCallbackEvents() called")
        mConnectivityManager.registerNetworkCallback(mNetworkRequest, this)
    }

    fun checkNetworkState(): Boolean {
        try {
            val networkInfo = mConnectivityManager.activeNetworkInfo
            _networkState.value = networkInfo != null
                    && networkInfo.isConnected

        } catch (exception: Exception) {
            exception.printStackTrace()
            _networkState.value = false
        } finally {
            return networkState.value
        }
    }
}