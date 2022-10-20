package proxy.redis.util.uuid

import com.google.common.collect.ImmutableList
import com.google.gson.Gson
import com.squareup.okhttp.*
import proxy.redis.MojangHttpClient
import java.util.*
import java.util.concurrent.Callable

/* Credits to evilmidget38 for this class. I modified it to use Gson. */
class UUIDFetcher private constructor(names: List<String>, rateLimiting: Boolean) : Callable<Map<String?, UUID>> {
    private val names: List<String>
    private val rateLimiting: Boolean

    init {
        this.names = ImmutableList.copyOf(names)
        this.rateLimiting = rateLimiting
    }

    constructor(names: List<String>) : this(names, true) {}

    @Throws(Exception::class)
    override fun call(): Map<String?, UUID> {
        val uuidMap: MutableMap<String?, UUID> = HashMap()
        val requests = Math.ceil(names.size / PROFILES_PER_REQUEST).toInt()
        for (i in 0 until requests) {
            val body = gson.toJson(names.subList(i * 100, Math.min((i + 1) * 100, names.size)))
            val request = Request.Builder().url(PROFILE_URL).post(RequestBody.create(JSON, body)).build()
            val responseBody = MojangHttpClient.newCall(request).execute().body()
            val response = responseBody.string()
            responseBody.close()
            val array = gson.fromJson(response, Array<Profile>::class.java)
            for (profile in array) {
                val uuid = getUUID(profile.id)
                uuidMap[profile.name] = uuid
            }
            if (rateLimiting && i != requests - 1) {
                Thread.sleep(100L)
            }
        }
        return uuidMap
    }

    private data class Profile(val id: String, val name: String)

    companion object {
        private const val PROFILES_PER_REQUEST = 100.0
        private const val PROFILE_URL = "https://api.mojang.com/profiles/minecraft"
        private val JSON = MediaType.parse("application/json")
        private val gson = Gson()


        fun getUUID(id: String?): UUID {
            return UUID.fromString(
                id!!.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(
                    12,
                    16
                ) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32)
            )
        }
    }
}