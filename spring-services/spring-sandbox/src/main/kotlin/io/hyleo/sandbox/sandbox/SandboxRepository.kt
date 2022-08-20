package io.hyleo.sandbox.sandbox

import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*


interface SandboxRepository : MongoRepository<Sandbox, UUID> {

    fun findAllByOwner(owner: UUID): List<Sandbox>
}