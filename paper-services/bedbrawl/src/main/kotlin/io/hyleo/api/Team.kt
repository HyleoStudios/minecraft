package io.hyleo.api

import org.bukkit.Location
import org.bukkit.block.EnderChest
import org.bukkit.entity.Player

class Team(
    val info: TeamInfo,
    val spawn: Location,
    val generator: Location,
    val itemShop: Location,
    val teamShop: Location,
    val teamChest: Location,
    val enderChest: Location,
    val bed: Location
) {



}