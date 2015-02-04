package com.mstiles92.plugins.deathbanredux.data

import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

data class DeathClass(val name: String, val banTime: String, val deathMessage: String) {

    fun getPermission() : Permission {
        val permission = Permission("deathban.class.${name}")
        permission.setDefault(PermissionDefault.FALSE)
        return permission
    }
}