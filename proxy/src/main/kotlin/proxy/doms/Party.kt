package proxy.doms

import java.util.*

data class Party(var leader: UUID, val mods: List<UUID>, val members: List<UUID>)