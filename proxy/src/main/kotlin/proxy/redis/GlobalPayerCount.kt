package proxy.redis

import java.util.concurrent.atomic.AtomicInteger

object GlobalPayerCount : AtomicInteger() {
    override fun toByte(): Byte = TODO("Not yet implemented")

    override fun toChar(): Char = TODO("Not yet implemented")

    override fun toShort(): Short = TODO("Not yet implemented")

}