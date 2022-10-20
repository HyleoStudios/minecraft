package proxy.doms

import java.util.UUID

data class FriendshipRequest(val id: UUID, val from: UUID, val to: UUID, val remove: Boolean, val sent: Long)
