package io.hyleo.api

import org.bukkit.Location
import java.util.*

data class OneBlock(val location: Location) {
    val id: UUID = UUID.randomUUID()

    private val whitelist: MutableList<UUID> = mutableListOf()
    private val phases: MutableMap<String, PhaseInfo> = mutableMapOf()
    private val removedPhases: MutableMap<String, PhaseInfo> = mutableMapOf()

    fun players() = if(whitelist.isEmpty()) {
        location.world.players
    } else {
        location.world.players.filter { whitelist.contains(it.uniqueId) }
    }

    fun whitelist() = whitelist.toList()

    fun whitelistPlayer(player: UUID) = whitelist.add(player)

    fun unwhitelistPlayer(player: UUID) = whitelist.remove(player)

    fun usingWhiteList() = whitelist.isNotEmpty()


    fun blocksBroken(phase: String) = phases.getOrDefault(phase, 0)

    fun currentPhase() {

    }

}
