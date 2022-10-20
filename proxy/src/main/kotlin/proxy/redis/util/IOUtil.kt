package proxy.redis.util

import com.google.common.io.ByteStreams
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

object IOUtil {
    fun readInputStreamAsString(`is`: InputStream?): String {
        val string: String
        string = try {
            String(ByteStreams.toByteArray(`is`!!), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            throw AssertionError(e)
        }
        return string
    }
}