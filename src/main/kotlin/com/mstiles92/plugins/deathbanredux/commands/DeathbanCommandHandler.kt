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
import com.mstiles92.plugins.deathbanredux.util.getData
import com.mstiles92.plugins.deathbanredux.util.replaceMessageVariables
import com.mstiles92.plugins.stileslib.calendar.CalendarUtils
import com.mstiles92.plugins.stileslib.commands.Arguments
import com.mstiles92.plugins.stileslib.commands.CommandHandler
import com.mstiles92.plugins.stileslib.commands.annotations.Command
import org.bukkit.Bukkit
import org.bukkit.ChatColor

public class DeathbanCommandHandler : CommandHandler {

    private val tag = "${ChatColor.BLUE}[DeathBanRedux]${ChatColor.RESET}"
    private val errorTag = "${ChatColor.BLUE}[DeathBanRedux]${ChatColor.RED}"

    Command(name = "deathban", aliases = arrayOf("db", "hdb"), permission = "deathban.display")
    fun handleDefault(args: Arguments) {
        args.getSender().sendMessage("${ChatColor.GREEN}====[DeathBanRedux Help]====")
        args.getSender().sendMessage("${ChatColor.GREEN}<x> ${ChatColor.GREEN}specifies a required parameter, while ${ChatColor.GREEN}[x] ${ChatColor.GREEN}is an optional parameter.")
        args.getSender().sendMessage("${ChatColor.GREEN}hdb${ChatColor.GREEN} or ${ChatColor.GREEN}db ${ChatColor.GREEN}may be used in place of ${ChatColor.GREEN}deathban${ChatColor.GREEN} in the commands below.")
        args.getSender().sendMessage("${ChatColor.GREEN}/deathban enable ${ChatColor.GREEN}Enable the plugin server-wide.")
        args.getSender().sendMessage("${ChatColor.GREEN}/deathban disable ${ChatColor.GREEN}Disable the plugin server-wide.")
        args.getSender().sendMessage("${ChatColor.GREEN}/deathban ban <player> [time] ${ChatColor.GREEN}Manually ban a player. Uses default time value if none specified.")
        args.getSender().sendMessage("${ChatColor.GREEN}/deathban unban <player> ${ChatColor.GREEN}Manually unban a banned player.")
        args.getSender().sendMessage("${ChatColor.GREEN}/deathban status <player> ${ChatColor.GREEN}Check the ban status of a player.")
        args.getSender().sendMessage("${ChatColor.GREEN}/credits [player] ${ChatColor.GREEN}Check your own or another player's revival credits.")
        args.getSender().sendMessage("${ChatColor.GREEN}/credits send <player> <amount> ${ChatColor.GREEN}Send some of your own revival credits to another player.")
        args.getSender().sendMessage("${ChatColor.GREEN}/credits give <player> <amount> ${ChatColor.GREEN}Give a player a certain amount of revival credits.")
        args.getSender().sendMessage("${ChatColor.GREEN}/credits take <player> <amount> ${ChatColor.GREEN}Take a certain amount of credits from another player.")

    }

    Command(name = "deathban.enable", aliases = arrayOf("db.enable", "hdb.enable"), permission = "deathban.enable")
    fun handleEnable(args: Arguments) {
        Config.setEnabled(true)
        args.getSender().sendMessage("${tag} Enabled!")

        for (player in Bukkit.getOnlinePlayers()) {
            if (player.getData().isCurrentlyBanned()) {
                //TODO: kick player
            }
        }
    }

    Command(name = "deathban.disable", aliases = arrayOf("db.disable", "hdb.disable"), permission = "deathban.enable")
    fun handleDisable(args: Arguments) {
        Config.setEnabled(false)
        args.getSender().sendMessage("${tag} Disabled!")
    }

    Command(name = "deathban.ban", aliases = arrayOf("db.ban", "hdb.ban"), permission = "deathban.ban")
    fun handleBan(args: Arguments) {
        if (args.getArgs().size() < 1) {
            args.getSender().sendMessage("${errorTag} You must specify a player!")
            return
        }

        val playerName = args.getArgs()[0]
        val player = Bukkit.getPlayer(playerName)
        val playerData = PlayerDataStore[playerName]
        val banTime = if (args.getArgs().size() > 1) args.getArgs()[1] else Config.getBanTime()

        if (playerData == null) {
            //TODO: lookup player UUID and store ban instead of failing
            args.getSender().sendMessage("${errorTag} PlayerData not found for ${playerName}!")
        } else if (player != null && player.hasPermission("deathban.ban.exempt")) {
            args.getSender().sendMessage("${errorTag} This player can not be banned!")
        } else {
            val banCalendar = CalendarUtils.parseTimeDifference(banTime)

            if (banCalendar == null) {
                args.getSender().sendMessage("${errorTag} Invalid ban time entered!")
            } else {
                playerData.banTime = banCalendar.getTimeInMillis()
                args.getSender().sendMessage("${tag} ${playerName} has been banned for ${banTime}.")
                //TODO: kick player if online
            }
        }
    }

    Command(name = "deathban.unban", aliases = arrayOf("db.unban", "hdb.unban"), permission = "deathban.unban")
    fun handleUnban(args: Arguments) {
        if (args.getArgs().size() < 1) {
            args.getSender().sendMessage("${errorTag} You must specify a player.")
            return
        }

        val playerName = args.getArgs()[0]
        val playerData = PlayerDataStore[playerName]

        if (playerData == null || !playerData.isCurrentlyBanned()) {
            args.getSender().sendMessage("${errorTag} ${playerName} is not currently banned.")
        } else {
            playerData.resetBanTime()
            args.getSender().sendMessage("${tag} ${playerData.lastSeenName} has been unbanned.")
        }
    }

    Command(name = "deathban.status", aliases = arrayOf("db.status", "hdb.status"), permission = "deathban.status")
    fun handleStatus(args: Arguments) {
        if (args.getArgs().size() < 1) {
            args.getSender().sendMessage("${errorTag} You must specify a player!")
            return
        }

        val playerName = args.getArgs()[0]
        val playerData = PlayerDataStore[playerName]

        if (playerData == null || !playerData.isCurrentlyBanned()) {
            args.getSender().sendMessage("${tag} ${playerName} is not currently banned.")
        } else {
            args.getSender().sendMessage("${tag} %player% is banned until %unbantime% %unbandate%".replaceMessageVariables(playerData))
        }
    }
}