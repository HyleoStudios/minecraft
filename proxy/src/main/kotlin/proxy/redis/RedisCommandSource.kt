package proxy.redis

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.permission.Tristate
import net.kyori.adventure.permission.PermissionChecker
import net.kyori.adventure.util.TriState

object RedisCommandSource : CommandSource {
    private val permissionChecker = PermissionChecker.always(TriState.TRUE)

    override fun hasPermission(permission: String) = permissionChecker.test(permission)

    override fun getPermissionValue(s: String) = Tristate.TRUE

    override fun getPermissionChecker() = permissionChecker


}