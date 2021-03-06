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

import com.mstiles92.plugins.deathbanredux.DeathBanRedux
import com.mstiles92.plugins.deathbanredux.config.DeathBanConfig
import com.mstiles92.plugins.deathbanredux.data.PlayerData
import com.mstiles92.plugins.deathbanredux.util.*
import com.mstiles92.plugins.stileslib.calendar.CalendarUtils
import com.mstiles92.plugins.stileslib.commands.Arguments
import com.mstiles92.plugins.stileslib.commands.CommandHandler
import com.mstiles92.plugins.stileslib.commands.annotations.Command
import com.mstiles92.plugins.stileslib.commands.annotations.TabCompleter
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import java.util.*

class DeathbanCommandHandler(val plugin: DeathBanRedux) : CommandHandler {
    private val emptyList: List<String> = ArrayList()

    @Command(name = "deathban", aliases = arrayOf("db", "hdb"), permission = "deathban.display")
    fun handleDefault(args: Arguments) {
        args.sender.sendMessage("${ChatColor.GREEN}====[DeathBanRedux Help]====")
        args.sender.sendMessage("${ChatColor.GREEN}<x> ${ChatColor.GREEN}specifies a required parameter, while ${ChatColor.GREEN}[x] ${ChatColor.GREEN}is an optional parameter.")
        args.sender.sendMessage("${ChatColor.GREEN}hdb${ChatColor.GREEN} or ${ChatColor.GREEN}db ${ChatColor.GREEN}may be used in place of ${ChatColor.GREEN}deathban${ChatColor.GREEN} in the commands below.")
        args.sender.sendMessage("${ChatColor.GREEN}/deathban enable ${ChatColor.GREEN}Enable the plugin server-wide.")
        args.sender.sendMessage("${ChatColor.GREEN}/deathban disable ${ChatColor.GREEN}Disable the plugin server-wide.")
        args.sender.sendMessage("${ChatColor.GREEN}/deathban ban <player> [time] ${ChatColor.GREEN}Manually ban a player. Uses default time value if none specified.")
        args.sender.sendMessage("${ChatColor.GREEN}/deathban unban <player> ${ChatColor.GREEN}Manually unban a banned player.")
        args.sender.sendMessage("${ChatColor.GREEN}/deathban status <player> ${ChatColor.GREEN}Check the ban status of a player.")
        args.sender.sendMessage("${ChatColor.GREEN}/credits [player] ${ChatColor.GREEN}Check your own or another player's revival credits.")
        args.sender.sendMessage("${ChatColor.GREEN}/credits send <player> <amount> ${ChatColor.GREEN}Send some of your own revival credits to another player.")
        args.sender.sendMessage("${ChatColor.GREEN}/credits give <player> <amount> ${ChatColor.GREEN}Give a player a certain amount of revival credits.")
        args.sender.sendMessage("${ChatColor.GREEN}/credits take <player> <amount> ${ChatColor.GREEN}Take a certain amount of credits from another player.")

    }

    @TabCompleter(name = "deathban", aliases = arrayOf("db", "hdb"))
    fun completeDefault(args: Arguments) = if (args.args.size == 1) arrayListOf("enable", "disable", "ban", "unban", "status").autocomplete(args.args[0]) else emptyList

    @Command(name = "deathban.enable", aliases = arrayOf("db.enable", "hdb.enable"), permission = "deathban.enable")
    fun handleEnable(args: Arguments) {
        DeathBanConfig.setEnabled(true)
        args.sender.sendMessage("$tag Enabled!")

        Bukkit.getScheduler().runTaskLater(plugin, {
            Bukkit.getOnlinePlayers().filter { it -> it.getData().isCurrentlyBanned() }.forEach { it.kickPlayer("$tag Plugin was enabled while you are banned!") }
        }, DeathBanConfig.getTickDelay().toLong())
    }

    @TabCompleter(name = "deathban.enable", aliases = arrayOf("db.enable", "hdb.enable"))
    fun completeEnable(args: Arguments) = emptyList

    @Command(name = "deathban.disable", aliases = arrayOf("db.disable", "hdb.disable"), permission = "deathban.enable")
    fun handleDisable(args: Arguments) {
        DeathBanConfig.setEnabled(false)
        args.sender.sendMessage("$tag Disabled!")
    }
    @TabCompleter(name = "deathban.disable", aliases = arrayOf("db.disable", "hdb.disable"))
    fun completeDisable(args: Arguments) = emptyList

    @Command(name = "deathban.ban", aliases = arrayOf("db.ban", "hdb.ban"), permission = "deathban.ban")
    fun handleBan(args: Arguments) {
        if (args.args.size < 1) {
            args.sender.sendMessage("$errorTag You must specify a player!")
            return
        }

        val playerName = args.args[0]
        val player = Bukkit.getPlayer(playerName)
        val playerData = PlayerData[playerName]
        val banTime = if (args.args.size > 1) args.args[1] else DeathBanConfig.getBanTime()

        if (playerData == null) {
            //TODO: lookup player UUID and store ban instead of failing
            args.sender.sendMessage("$errorTag PlayerData not found for $playerName!")
        } else if (player != null && player.hasPermission("deathban.ban.exempt")) {
            args.sender.sendMessage("$errorTag This player can not be banned!")
        } else {
            val banCalendar = CalendarUtils.parseTimeDifference(banTime)

            if (banCalendar == null) {
                args.sender.sendMessage("$errorTag Invalid ban time entered!")
            } else {
                playerData.banTime = banCalendar.timeInMillis
                args.sender.sendMessage("$tag $playerName has been banned for $banTime.")
                Bukkit.getScheduler().runTaskLater(plugin, {
                    val kickMessage = player?.getDeathClass()?.deathMessage ?: DeathBanConfig.getDeathMessage()
                    player?.kickPlayer(kickMessage.replaceMessageVariables(playerData))
                }, DeathBanConfig.getTickDelay().toLong())
            }
        }
    }

    @TabCompleter(name = "deathban.ban", aliases = arrayOf("db.ban", "hdb.ban"))
    fun completeBan(args: Arguments) = if (args.args.size == 1) PlayerData.getAllUsernames().autocomplete(args.args[0]) else emptyList

    @Command(name = "deathban.unban", aliases = arrayOf("db.unban", "hdb.unban"), permission = "deathban.unban")
    fun handleUnban(args: Arguments) {
        if (args.args.size < 1) {
            args.sender.sendMessage("$errorTag You must specify a player.")
            return
        }

        val playerName = args.args[0]
        val playerData = PlayerData[playerName]

        if (playerData == null || !playerData.isCurrentlyBanned()) {
            args.sender.sendMessage("$errorTag $playerName is not currently banned.")
        } else {
            playerData.resetBanTime()
            args.sender.sendMessage("$tag ${playerData.lastSeenName} has been unbanned.")
        }
    }

    @TabCompleter(name = "deathban.unban", aliases = arrayOf("db.unban", "hdb.unban"))
    fun completeUnban(args: Arguments) = if (args.args.size == 1) PlayerData.getAllUsernames().autocomplete(args.args[0]) else emptyList

    @Command(name = "deathban.status", aliases = arrayOf("db.status", "hdb.status"), permission = "deathban.status")
    fun handleStatus(args: Arguments) {
        if (args.args.size < 1) {
            args.sender.sendMessage("$errorTag You must specify a player!")
            return
        }

        val playerName = args.args[0]
        val playerData = PlayerData[playerName]

        if (playerData == null || !playerData.isCurrentlyBanned()) {
            args.sender.sendMessage("$tag $playerName is not currently banned.")
        } else {
            args.sender.sendMessage("$tag %player% is banned until %unbantime% %unbandate%".replaceMessageVariables(playerData))
        }
    }

    @TabCompleter(name = "deathban.status", aliases = arrayOf("db.status", "hdb.status"))
    fun completeStatus(args: Arguments) = if (args.args.size == 1) PlayerData.getAllUsernames().autocomplete(args.args[0]) else emptyList
}