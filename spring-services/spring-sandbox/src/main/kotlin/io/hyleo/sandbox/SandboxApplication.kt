package io.hyleo.sandbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.util.*

@SpringBootApplication
@EnableMongoRepositories
@EnableCaching
class SandboxApplication {
    companion object {
        val NETWORK_OWNER: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}

fun main(args: Array<String>) {
    runApplication<SandboxApplication>(*args)
}


