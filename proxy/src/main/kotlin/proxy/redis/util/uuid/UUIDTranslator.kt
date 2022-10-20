package proxy.redis.util.uuid

import com.google.common.base.Charsets
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterables
import com.google.gson.Gson
import proxy.VelocityPlugin
import proxy.contexts.PluginContext
import proxy.redis.tasks.RedisTask
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.exceptions.JedisException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object UUIDTranslator {
    private val nameToUuidMap: MutableMap<String, CachedUUIDEntry> = ConcurrentHashMap(128, 0.5f, 4)
    private val uuidToNameMap: MutableMap<UUID, CachedUUIDEntry> = ConcurrentHashMap(128, 0.5f, 4)
    private fun addToMaps(name: String, uuid: UUID) {
        // This is why I like LocalDate...

        // Cache the entry for three days.
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 3)

        // Create the entry and populate the local maps
        val entry = CachedUUIDEntry(name, uuid, calendar)
        nameToUuidMap[name.lowercase(Locale.getDefault())] = entry
        uuidToNameMap[uuid] = entry
    }

    context(PluginContext)
    fun getTranslatedUuid(player: String, expensiveLookups: Boolean): UUID? {
        // If the player is online, give them their UUID.
        // Remember, local data > remote data.
        if (plugin.getPlayer(player) != null) return plugin.getPlayerUUID(
            player
        )

        // Check if it exists in the map
        val cachedUUIDEntry = nameToUuidMap[player.lowercase(Locale.getDefault())]
        if (cachedUUIDEntry != null) {
            if (!cachedUUIDEntry.expired()) return cachedUUIDEntry.uuid else nameToUuidMap.remove(player)
        }

        // Check if we can exit early
        if (UUID_PATTERN.matcher(player).find()) {
            return UUID.fromString(player)
        }
        if (MOJANGIAN_UUID_PATTERN.matcher(player).find()) {
            // Reconstruct the UUID
            return UUIDFetcher.getUUID(player)
        }

        // If we are in offline mode, UUID generation is simple.
        // We don't even have to cache the UUID, since this is easy to recalculate.
        if (!plugin.isOnlineMode) {
            return UUID.nameUUIDFromBytes("OfflinePlayer:$player".toByteArray(Charsets.UTF_8))
        }
        val redisTask: RedisTask<UUID?> = object : RedisTask<UUID?>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): UUID? {
                val stored = unifiedJedis!!.hget("uuid-cache", player.lowercase(Locale.getDefault()))
                if (stored != null) {
                    // Found an entry value. Deserialize it.
                    val entry = gson.fromJson(stored, CachedUUIDEntry::class.java)

                    // Check for expiry:
                    if (entry.expired()) {
                        unifiedJedis.hdel("uuid-cache", player.lowercase(Locale.getDefault()))
                        // Doesn't hurt to also remove the UUID entry as well.
                        unifiedJedis.hdel("uuid-cache", entry.uuid.toString())
                    } else {
                        nameToUuidMap[player.lowercase(Locale.getDefault())] = entry
                        uuidToNameMap[entry.uuid] = entry
                        return entry.uuid
                    }
                }

                // That didn't work. Let's ask Mojang.
                if (!expensiveLookups || !plugin.isOnlineMode) return null
                val uuidMap1: Map<String?, UUID> = try {
                    UUIDFetcher(listOf(player)).call()
                } catch (e: Exception) {
                  plugin.logFatal("Unable to fetch UUID from Mojang for $player")
                    return null
                }

                for ((key, value) in uuidMap1) {
                    if (key.equals(player, ignoreCase = true)) {
                        persistInfo(key!!, value, unifiedJedis)
                        return value
                    }
                }
                return null
            }
        }
        // Let's try Redis.
        try {
            return redisTask.execute()
        } catch (e: JedisException) {
            plugin.logFatal("Unable to fetch UUID for $player")
        }
        return null // Nope, game over!
    }

    context (PluginContext)
    fun getNameFromUuid(player: UUID, expensiveLookups: Boolean): String? {
        // If the player is online, give them their UUID.
        // Remember, local data > remote data.
        if (plugin.getPlayer(player) != null) return plugin.getPlayerName(player)

        // Check if it exists in the map
        val cachedUUIDEntry = uuidToNameMap[player]
        if (cachedUUIDEntry != null) {
            if (!cachedUUIDEntry.expired()) return cachedUUIDEntry.name else uuidToNameMap.remove(player)
        }
        val redisTask: RedisTask<String?> = object : RedisTask<String?>() {
            override fun unifiedJedisTask(unifiedJedis: UnifiedJedis): String? {
                val stored = unifiedJedis!!.hget("uuid-cache", player.toString())
                if (stored != null) {
                    // Found an entry value. Deserialize it.
                    val entry = gson.fromJson(stored, CachedUUIDEntry::class.java)

                    // Check for expiry:
                    if (entry.expired()) {
                        unifiedJedis.hdel("uuid-cache", player.toString())
                        // Doesn't hurt to also remove the named entry as well.
                        // TODO: Since UUIDs are fixed, we could look up the name and see if the UUID matches.
                        unifiedJedis.hdel("uuid-cache", entry.name)
                    } else {
                        nameToUuidMap[entry.name.lowercase(Locale.getDefault())] = entry
                        uuidToNameMap[player] = entry
                        return entry.name
                    }
                }
                if (!expensiveLookups || !plugin.isOnlineMode) return null

                // That didn't work. Let's ask Mojang. This call may fail, because Mojang is insane.
                val name: String? = try {
                    val nameHist = NameFetcher.nameHistoryFromUuid(player)
                    Iterables.getLast(nameHist, null)
                } catch (e: Exception) {
                    plugin.logFatal("Unable to fetch name from Mojang for $player")
                    return null
                }
                if (name != null) {
                    persistInfo(name, player, unifiedJedis)
                    return name
                }
                return null
            }
        }


        // Okay, it wasn't locally cached. Let's try Redis.
        return try {
            redisTask.execute()
        } catch (e: JedisException) {
            plugin.logFatal("Unable to fetch name for $player")
            null
        }
    }

    fun persistInfo(name: String, uuid: UUID, unifiedJedis: UnifiedJedis) {
        addToMaps(name, uuid)
        val json = gson.toJson(uuidToNameMap[uuid])
        unifiedJedis.hmset(
            "uuid-cache",
            ImmutableMap.of(name.lowercase(Locale.getDefault()), json, uuid.toString(), json)
        )
    }

    private data class CachedUUIDEntry(val name: String, val uuid: UUID, val expiry: Calendar) {
        fun expired() = Calendar.getInstance().after(expiry)
    }

    private val UUID_PATTERN =
        Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")
    private val MOJANGIAN_UUID_PATTERN = Pattern.compile("[a-fA-F0-9]{32}")
    private val gson = Gson()

}