/*
 * This document is a part of the source code and related artifacts for
 * DeathBanRedux, an open source Bukkit plugin for hardcore-type servers
 * where players are temporarily banned upon death.
 *
 * http://dev.bukkit.org/bukkit-plugins/deathbanredux/
 * http://github.com/mstiles92/DeathBanRedux
 *
 * Copyright (c) 2015 Matthew Stiles (mstiles92)
 *
 * Licensed under the Common Development and Distribution License Version 1.0
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the CDDL-1.0 License at
 * http://opensource.org/licenses/CDDL-1.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the license.
 */

package com.mstiles92.plugins.deathbanredux.data

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

data class DeathClass(val name: String, val banTime: String, val deathMessage: String) {

    companion object {
        private val classList: MutableList<DeathClass> = arrayListOf()

        operator fun get(name: String) = classList.find { it -> it.name.equals(name) }
        operator fun get(player: Player) = classList.find { it -> player.hasPermission(it.getPermission()) }

        fun loadFromConfig(section: ConfigurationSection?) {
            section?.getKeys(false)?.forEach { it -> DeathClass(it, section.getString("$it.Ban-Time"), section.getString("$it.Death-Message")) }
        }
    }

    init {
        classList.add(this)
    }

    fun getPermission() : Permission {
        val permission = Permission("deathban.class.$name")
        permission.default = PermissionDefault.FALSE
        return permission
    }
}