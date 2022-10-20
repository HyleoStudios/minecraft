package io.hyleo

import io.hyleo.api.TeamInfo
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin


class BedBrawl : JavaPlugin() {

    companion object {
        private lateinit var instance: BedBrawl
        fun instance() = instance
    }

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        val pm = server.pluginManager
        registerEvents(pm)
    }

    override fun onDisable() {
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator = object : ChunkGenerator(){}

    private fun registerEvents(pm: PluginManager) {

    }

}