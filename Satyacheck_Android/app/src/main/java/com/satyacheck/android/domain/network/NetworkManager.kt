package com.satyacheck.android.domain.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkManager @Inject constructor(
    private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check current network status
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Get network type
     */
    fun getNetworkType(): NetworkType {
        if (!isNetworkAvailable()) return NetworkType.NONE
        
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.OTHER
        }
    }

    /**
     * Observe network status changes
     */
    fun observeNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.AVAILABLE)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.UNAVAILABLE)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                trySend(if (hasInternet) NetworkStatus.AVAILABLE else NetworkStatus.UNAVAILABLE)
            }
        }

        // Send initial status
        trySend(if (isNetworkAvailable()) NetworkStatus.AVAILABLE else NetworkStatus.UNAVAILABLE)

        // Register callback
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()

    /**
     * Check if network is metered (limited data)
     */
    fun isNetworkMetered(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }

    /**
     * Get network quality estimation
     */
    fun getNetworkQuality(): NetworkQuality {
        if (!isNetworkAvailable()) return NetworkQuality.NONE
        
        val network = connectivityManager.activeNetwork ?: return NetworkQuality.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkQuality.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkQuality.EXCELLENT
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Estimate based on cellular capabilities
                when {
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> NetworkQuality.GOOD
                    else -> NetworkQuality.FAIR
                }
            }
            else -> NetworkQuality.FAIR
        }
    }
}

/**
 * Network status enum
 */
enum class NetworkStatus {
    AVAILABLE,
    UNAVAILABLE
}

/**
 * Network type enum
 */
enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER
}

/**
 * Network quality enum
 */
enum class NetworkQuality {
    NONE,
    POOR,
    FAIR,
    GOOD,
    EXCELLENT
}