package io.hyleo.sandbox.sandbox

import io.hyleo.sandbox.SandboxApplication
import io.hyleo.sandbox.SandboxConfiguration
import io.hyleo.sandbox.membership.Membership
import lombok.ToString
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@ToString
@Document(collection = "sandboxes")
data class Sandbox(
    @Id
    val id: UUID,
    val owner: UUID = SandboxConfiguration.NETWORK_OWNER,
    val mode: String,
    val name: String
) {

    private var lastLoaded: Long = System.currentTimeMillis()
    private val members: MutableMap<UUID, Membership> = mutableMapOf()

    fun lastLoaded(): Long = lastLoaded

    fun justLoaded(): Long {
        val previousLastLoaded = lastLoaded
        lastLoaded = System.currentTimeMillis()
        return previousLastLoaded
    }

    fun addMember(member: UUID): Membership {
        members[member] = Membership(joined = Date(), lastPlayed = Date(-1))
        return members[member]!!
    }

    fun removeMember(member: UUID): Membership? = members.remove(member)

    fun members(): List<UUID> = members.keys.toList()

    fun membership(member: UUID): Membership? = members[member]

    fun isMember(member: UUID): Boolean = members.contains(member)

}
