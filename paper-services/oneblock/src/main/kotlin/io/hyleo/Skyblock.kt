package io.hyleo

import com.google.gson.GsonBuilder
import io.hyleo.api.OneBlock
import io.hyleo.api.season.Season
import io.hyleo.api.Phase
import io.hyleo.gson.LocationGson
import io.hyleo.gson.TextColorGson
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Location
import org.bukkit.event.Listener
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class Skyblock : JavaPlugin() {

    companion object {
        private lateinit var instance: Skyblock
        fun instance() = instance
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(TextColor::class.java, TextColorGson())
            .registerTypeAdapter(Location::class.java, LocationGson())
            .create()
    }

    private val phases: MutableMap<String, Phase> = mutableMapOf()
    private val seasons: MutableList<Season> = mutableListOf()
    private val oneBlocks: MutableList<OneBlock> = mutableListOf()

    fun phaseNames() = phases.keys
    fun phases() = phases.values
    fun phase(name: String) = phases[name]

    private fun registerPhase(phase: Phase) {
        phases[phase.name] = phase
    }

    private fun registerSeason(season: Season, listener: Listener) {
        seasons.add(season)
        server.pluginManager.registerEvents(listener, this)

    }

    fun loadOneBlock(json: String): OneBlock {
        val oneBlock = gson.fromJson(json, OneBlock::class.java)
        oneBlocks.add(oneBlock)
        return oneBlock
    }

    fun unloadOneBlock(oneBlock: OneBlock): String {
        val json = gson.toJson(oneBlock) // If this fails, it should fail before it removes the one block from the list
        oneBlocks.remove(oneBlock)
        return json
    }

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        val pm = server.pluginManager
        registerEvents(pm)
        registerPhases()
        registerSeasons()
    }

    override fun onDisable() {

    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator = object : ChunkGenerator(){}

    private fun registerEvents(pm: PluginManager) {

    }

    private fun registerPhases() {

        registerPhase(Example.ExamplePhase)
    }

    private fun registerSeasons() {

    }


}