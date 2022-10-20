package proxy.redis.util.uuid

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.okhttp.Request
import proxy.redis.MojangHttpClient
import java.io.IOException
import java.util.*

object NameFetcher {
    private val gson = Gson()

    @Throws(IOException::class)
    fun nameHistoryFromUuid(uuid: UUID): List<String> {
        val url = "https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names"
        val request = Request.Builder().url(url).get().build()
        val body = MojangHttpClient.newCall(request).execute().body()

        val response = body.string()
        body.close()

        val listType = object : TypeToken<List<Name>?>() {}.type
        val names = gson.fromJson<List<Name>>(response, listType)
        val humanNames: MutableList<String> = ArrayList()

        names.forEach { humanNames.add(it.name) }

        return humanNames
    }

    data class Name(val name: String, val changedToAt: Long)
}