package io.hyleo.sandbox.membership

import io.hyleo.sandbox.SandboxConfiguration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.MongoId
import java.util.*

@Document(collection = "invitations")
data class Invite(
    @Id
    @MongoId
    @Indexed(expireAfter= SandboxConfiguration.INVITE_EXPIRATION) val sandbox: UUID,
    @Field val invitor: UUID,
    val invitee: UUID,
    val sent: Date
)
