package com.thecontract.core.net

import kotlinx.serialization.Serializable
import java.net.Inet4Address
import java.net.NetworkInterface

@Serializable
enum class InterfaceKind { ETHERNET, WIFI, VPN, OTHER }

@Serializable
data class LocalInterface(
    val id: String,
    val displayName: String,
    val address: String,
    val kind: InterfaceKind,
    val privateAddress: Boolean,
    val linkLocal: Boolean,
    val priority: Int
)

/**
 * Local IPv4 interface detection (section 6).
 *
 * Loopback, down interfaces, IPv6-only interfaces and unusable addresses are ignored.
 * Link-local (169.254/16) addresses are kept but ranked below any real private address, and VPN
 * interfaces are ranked last so they are never selected automatically — the TV user can still
 * choose one explicitly.
 *
 * On Android the same ranking is applied to the addresses reported by ConnectivityManager's
 * network callbacks, so an address change re-ranks and re-advertises without restarting a game.
 */
object NetworkScanner {

    private val ethernetPrefixes = listOf("eth", "en", "enp", "eno", "ens", "rmnet_eth")
    private val wifiPrefixes = listOf("wlan", "wl", "wifi", "ap")
    private val vpnPrefixes = listOf("tun", "tap", "ppp", "ipsec", "utun", "wg", "nordlynx")

    fun classify(name: String): InterfaceKind {
        val n = name.lowercase()
        return when {
            vpnPrefixes.any { n.startsWith(it) } -> InterfaceKind.VPN
            wifiPrefixes.any { n.startsWith(it) } -> InterfaceKind.WIFI
            ethernetPrefixes.any { n.startsWith(it) } -> InterfaceKind.ETHERNET
            else -> InterfaceKind.OTHER
        }
    }

    fun isPrivateV4(address: String): Boolean {
        val parts = address.split('.').mapNotNull { it.toIntOrNull() }
        if (parts.size != 4) return false
        val (a, b) = parts[0] to parts[1]
        return a == 10 || (a == 172 && b in 16..31) || (a == 192 && b == 168)
    }

    fun isLinkLocalV4(address: String): Boolean = address.startsWith("169.254.")

    fun priorityOf(kind: InterfaceKind, isPrivate: Boolean, isLinkLocal: Boolean): Int = when {
        kind == InterfaceKind.VPN -> 1
        isLinkLocal -> 10
        isPrivate && kind == InterfaceKind.ETHERNET -> 100
        isPrivate && kind == InterfaceKind.WIFI -> 90
        isPrivate -> 60
        else -> 5
    }

    fun describe(name: String, displayName: String, address: String): LocalInterface {
        val kind = classify(name)
        val priv = isPrivateV4(address)
        val link = isLinkLocalV4(address)
        return LocalInterface(
            id = "$name@$address",
            displayName = when (kind) {
                InterfaceKind.ETHERNET -> "Ethernet ($displayName)"
                InterfaceKind.WIFI -> "Wi-Fi ($displayName)"
                InterfaceKind.VPN -> "VPN ($displayName)"
                InterfaceKind.OTHER -> displayName
            },
            address = address,
            kind = kind,
            privateAddress = priv,
            linkLocal = link,
            priority = priorityOf(kind, priv, link)
        )
    }

    /** All usable local IPv4 interfaces, best first. */
    fun scan(): List<LocalInterface> = buildList {
        val interfaces = runCatching { NetworkInterface.getNetworkInterfaces()?.toList() }.getOrNull().orEmpty()
        for (nif in interfaces) {
            val up = runCatching { nif.isUp && !nif.isLoopback }.getOrDefault(false)
            if (!up) continue
            for (addr in nif.inetAddresses) {
                if (addr !is Inet4Address) continue
                if (addr.isLoopbackAddress || addr.isAnyLocalAddress) continue
                val host = addr.hostAddress ?: continue
                add(describe(nif.name, nif.displayName ?: nif.name, host))
            }
        }
    }.sortedWith(compareByDescending<LocalInterface> { it.priority }.thenBy { it.address })

    /** The interface the TV advertises unless the user picks another. */
    fun best(candidates: List<LocalInterface> = scan()): LocalInterface? =
        candidates.maxByOrNull { it.priority }
}
