package io.hyleo.sandbox.membership

import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document("invitations")
data class Invite(val sandbox: UUID, val invitor: UUID, val invitee: UUID, val created: Date) {
}
