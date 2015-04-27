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

package com.mstiles92.plugins.deathbanredux.commands

import com.mstiles92.plugins.deathbanredux.config.Config
import com.mstiles92.plugins.deathbanredux.data.PlayerDataStore
import com.mstiles92.plugins.stileslib.calendar.CalendarUtils
import com.mstiles92.plugins.stileslib.commands.Arguments
import com.mstiles92.plugins.stileslib.commands.CommandHandler
import com.mstiles92.plugins.stileslib.commands.annotations.Command
import org.bukkit.Bukkit
import org.bukkit.ChatColor

public class DeathbanCommandHandler : CommandHandler {

    Command(name = "deathban", aliases = array("db", "hdb"), permission = "deathban.display")
    fun handleDeathban(args: Arguments) {

    }

    Command(name = "deathban.ban", aliases = array("db.ban", "hdb.ban"), permission = "deathban.ban")
    fun handleDeathbanBan(args: Arguments) {
        if (args.getArgs().size() < 1) {
            args.getSender().sendMessage("${ChatColor.RED}You must specify a player!")
            return
        }

        val playerName = args.getArgs()[0]
        val player = Bukkit.getPlayer(playerName)
        val playerData = PlayerDataStore.get(playerName)
        val banTime = if (args.getArgs().size() > 1) args.getArgs()[1] else Config.getBanTime()

        if (playerData == null) {
            //TODO: lookup player UUID and store ban instead of failing
            args.getSender().sendMessage("${ChatColor.RED}PlayerData not found for ${playerName}!")
        } else if (player != null && player.hasPermission("deathban.ban.exempt")) {
            args.getSender().sendMessage("${ChatColor.RED}This player can not be banned!")
        } else {
            val banCalendar = CalendarUtils.parseTimeDifference(banTime)
            playerData.banTime = banCalendar.getTimeInMillis()
            args.getSender().sendMessage("${playerName} has been banned for ${banTime}.")
            //TODO: kick player if online
        }
    }

    Command(name = "deathban.unban", aliases = array("db.unban", "hdb.unban"), permission = "deathban.unban")
    fun handleDeathbanUnban(args: Arguments) {
        if (args.getArgs().size() < 1) {
            args.getSender().sendMessage("${ChatColor.RED}You must specify a player.")
            return
        }

        val playerName = args.getArgs()[0]
        val playerData = PlayerDataStore.get(playerName)

        if (playerData == null || !playerData.isCurrentlyBanned()) {
            args.getSender().sendMessage("${playerName} is not currently banned.")
        } else {
            playerData.resetBanTime()
            args.getSender().sendMessage("${playerData.lastSeenName} has been unbanned.")
        }
    }
}