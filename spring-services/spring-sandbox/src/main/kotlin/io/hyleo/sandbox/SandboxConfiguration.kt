package io.hyleo.sandbox

import java.util.*

class SandboxConfiguration {
    companion object {
        val NETWORK_OWNER = UUID.fromString("00000000-0000-0000-0000-000000000000")
        const val INVITE_EXPIRATION = "7d"
        @JvmStatic
        fun sandboxCache(owner: UUID) = "sandbox-cache-$owner"
    }


}