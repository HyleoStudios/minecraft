package proxy.doms

import java.util.*

data class Punishment(
    val id: UUID,
    val player: UUID,
    val punisher: UUID?,
    val communitySignatures: List<UUID>,
    val expire: Long,
    val reason: String
)