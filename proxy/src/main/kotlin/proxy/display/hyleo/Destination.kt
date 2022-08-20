package io.hyleo.proxy.display.hyleo

import com.velocitypowered.api.proxy.Player

data class Destination(
    val tag: Tag,
    val score: ()-> Int,
    val player: Player? = null,
    val display: String
)
