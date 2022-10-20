package io.hyleo.gson

import com.google.gson.*
import org.bukkit.Bukkit
import org.bukkit.Location
import java.lang.reflect.Type

class LocationGson : JsonSerializer<Location>, JsonDeserializer<Location> {
    override fun serialize(src: Location?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val obj = JsonObject()

        if (src != null) {
            obj.add("world", JsonPrimitive(src.world.name))
            obj.add("x", JsonPrimitive(src.x))
            obj.add("y", JsonPrimitive(src.y))
            obj.add("z", JsonPrimitive(src.z))
        }

        return obj
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Location {
        val obj: JsonObject = json?.asJsonObject ?: throw JsonParseException("Location cannot be null")

        val world = Bukkit.getWorld(obj["world"].asString)
        val x = obj["x"].asDouble
        val y = obj["y"].asDouble
        val z = obj["z"].asDouble

        return Location(world, x, y, z)
    }
}