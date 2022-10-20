package sandbox

import org.bukkit.Bukkit
import org.bukkit.Server.Spigot
import org.bukkit.event.server.ServerListPingEvent
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class SandboxPlugin : JavaPlugin() {

    companion object {

        const val ENVIRONMENT_VAR_SERVER_NAME = "SERVER_NAME"
        const val ENVIRONMENT_VAR_SANDBOX_ID = "SANDBOX_ID"
        const val ENVIRONMENT_VAR_WORLD_NAME = "WORLD_NAME"

        private lateinit var instance: SandboxPlugin
        fun instance() = instance

        private lateinit var serverName: String
        fun serverName() = serverName

        private lateinit var sandboxID: UUID
        fun sandboxID() = sandboxID

        private lateinit var worldName: String
        fun worldName() = worldName
    }

    /**
     * Generates a void world
     */
    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator = object : ChunkGenerator(){}

    override fun onLoad() {
        instance = this
        assert(System.getenv("ENVIRONMENT_VAR_SERVER_NAME") != null) { "SERVER_NAME environment variable is not set" }
        serverName = System.getenv(ENVIRONMENT_VAR_SERVER_NAME)

        assert(System.getenv("ENVIRONMENT_VAR_SANDBOX_ID") != null) { "SANDBOX_ID environment variable is not set" }
        sandboxID = UUID.fromString(System.getenv(ENVIRONMENT_VAR_SANDBOX_ID))

        assert(System.getenv("ENVIRONMENT_VAR_WORLD_NAME") != null) { "WORLD_NAME environment variable is not set" }
        worldName = System.getenv(ENVIRONMENT_VAR_WORLD_NAME)

        runApplication<SandboxPlugin>()
    }

    override fun onEnable() {
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}