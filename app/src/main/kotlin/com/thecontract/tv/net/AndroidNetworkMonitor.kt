package com.thecontract.tv.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.thecontract.core.net.InterfaceKind
import com.thecontract.core.net.LocalInterface
import com.thecontract.core.net.NetworkScanner
import com.thecontract.tv.ContractLog
import java.net.Inet4Address

/**
 * Watches the television's local addresses (section 6).
 *
 * Uses ConnectivityManager network callbacks so an Ethernet cable being plugged in, a Wi-Fi
 * hand-off or a DHCP lease change is noticed immediately. Every change re-ranks the available
 * interfaces and hands the list to the session manager, which regenerates the join URL and the
 * QR code without disturbing the game in progress.
 *
 * A plain `NetworkInterface` scan is merged in as a fallback, so an interface Android does not
 * report as a "network" (some Shield Ethernet configurations) is still offered.
 */
class AndroidNetworkMonitor(
    context: Context,
    private val onChange: (List<LocalInterface>) -> Unit
) {

    private val connectivity =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val known = LinkedHashMap<Long, List<LocalInterface>>()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            known[network.networkHandle] = describe(network, linkProperties)
            publish()
        }

        override fun onLost(network: Network) {
            known.remove(network.networkHandle)
            publish()
        }

        override fun onAvailable(network: Network) {
            connectivity.getLinkProperties(network)?.let {
                known[network.networkHandle] = describe(network, it)
            }
            publish()
        }
    }

    private fun describe(network: Network, link: LinkProperties): List<LocalInterface> {
        val capabilities = connectivity.getNetworkCapabilities(network)
        val kindHint = when {
            capabilities == null -> null
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> InterfaceKind.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> InterfaceKind.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> InterfaceKind.VPN
            else -> InterfaceKind.OTHER
        }
        val name = link.interfaceName ?: "net"
        return link.linkAddresses
            .map { it.address }
            .filterIsInstance<Inet4Address>()
            .filterNot { it.isLoopbackAddress || it.isAnyLocalAddress }
            .mapNotNull { address ->
                val host = address.hostAddress ?: return@mapNotNull null
                val described = NetworkScanner.describe(name, name, host)
                if (kindHint == null || kindHint == described.kind) {
                    described
                } else {
                    // Trust the transport reported by the platform over the interface name.
                    described.copy(
                        kind = kindHint,
                        displayName = when (kindHint) {
                            InterfaceKind.ETHERNET -> "Ethernet ($name)"
                            InterfaceKind.WIFI -> "Wi-Fi ($name)"
                            InterfaceKind.VPN -> "VPN ($name)"
                            InterfaceKind.OTHER -> name
                        },
                        priority = NetworkScanner.priorityOf(kindHint, described.privateAddress, described.linkLocal)
                    )
                }
            }
    }

    /** Current best-first list, merging callback data with a direct interface scan. */
    fun current(): List<LocalInterface> {
        val fromCallbacks = known.values.flatten()
        val fromScan = NetworkScanner.scan()
        return (fromCallbacks + fromScan)
            .distinctBy { it.id }
            .sortedWith(compareByDescending<LocalInterface> { it.priority }.thenBy { it.address })
    }

    /**
     * Runs on the system's connectivity callback thread, and rebuilds and broadcasts the whole
     * television view. Guarded because a failure there would reach the top of a thread the app
     * does not own, which on Android ends the process — a Wi-Fi blip is not grounds for losing
     * the game in progress.
     */
    private fun publish() {
        runCatching { onChange(current()) }
            .onFailure { ContractLog.w("Network change not applied: ${it.message}") }
    }

    fun start() {
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        runCatching { connectivity.registerNetworkCallback(request, callback) }
        publish()
    }

    fun stop() {
        runCatching { connectivity.unregisterNetworkCallback(callback) }
    }
}
