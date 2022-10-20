package io.hyleo.gson

import com.google.gson.*
import net.kyori.adventure.text.format.TextColor
import java.lang.reflect.Type

class TextColorGson: JsonSerializer<TextColor>, JsonDeserializer<TextColor> {
    override fun serialize(src: TextColor?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.asHexString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): TextColor? {
        val hex = json?.asString ?: throw JsonParseException("Text Color cannot be null")
        return TextColor.fromHexString(hex)
    }

}