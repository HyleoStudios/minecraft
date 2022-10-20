package proxy.doms

import java.util.*

data class Server(val ip: String, val port: Int, val podName: String, val mode: String, val map: String) {

    val players: MutableList<UUID> = mutableListOf()

    fun players() = players.toList()

}
