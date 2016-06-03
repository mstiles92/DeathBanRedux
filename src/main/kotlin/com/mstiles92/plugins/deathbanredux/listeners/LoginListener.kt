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

import com.mstiles92.plugins.deathbanredux.util.getData
import com.mstiles92.plugins.deathbanredux.util.replaceMessageVariables
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent

object LoginListener : Listener {

    @EventHandler
    fun onPlayerLogin(event: PlayerLoginEvent) {
        val data = event.player.getData()

        if (data.isCurrentlyBanned()) {
            if (data.revivalCredits > 0) {
                data.revivalCredits -= 1
                event.player.sendMessage("You have ${data.revivalCredits} revival credits remaining.")
            } else {
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, "You are currently banned until %unbantime% %unbandate%".replaceMessageVariables(data))
            }
        }
    }
}