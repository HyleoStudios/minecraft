package proxy.redis.util

import com.google.common.annotations.VisibleForTesting

@VisibleForTesting
object RedisUtil {
    @JvmField
    var PROXY_TIMEOUT = 30
    @JvmStatic
    fun isRedisVersionRight(redisVersion: String): Boolean {
        val args = redisVersion.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (args.size < 2) {
            return false
        }
        val major = args[0].toInt()
        val minor = args[1].toInt()
        return major >= 3 && minor >= 0
    }

    // Ham1255: i am keeping this if some plugin uses this *IF*
    @Deprecated("")
    fun canUseLua(redisVersion: String): Boolean {
        // Need to use >=3 to use Lua optimizations.
        return isRedisVersionRight(redisVersion)
    }
}