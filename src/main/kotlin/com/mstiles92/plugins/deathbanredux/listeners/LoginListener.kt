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

package com.mstiles92.plugins.deathbanredux.listeners

import com.mstiles92.plugins.deathbanredux.DeathBanRedux
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

public class LoginListener(val plugin: DeathBanRedux) : Listener {

    fun register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin)
    }

    EventHandler fun onPlayerLogin(event: PlayerLoginEvent) {
        val player = event.getPlayer()
        val data = plugin.playerDataStore[player]

        if (data.isCurrentlyBanned()) {
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You are banned!")
        } else {
            player.sendMessage("You have ${data.revivalCredits} revival credits remaining.")
        }
    }
}