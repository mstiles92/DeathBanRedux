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

import com.mstiles92.plugins.deathbanredux.data.PlayerData
import com.mstiles92.plugins.deathbanredux.util.autocomplete
import com.mstiles92.plugins.deathbanredux.util.errorTag
import com.mstiles92.plugins.deathbanredux.util.tag
import com.mstiles92.plugins.stileslib.commands.Arguments
import com.mstiles92.plugins.stileslib.commands.CommandHandler
import com.mstiles92.plugins.stileslib.commands.annotations.Command
import com.mstiles92.plugins.stileslib.commands.annotations.TabCompleter
import java.util.*

class CreditsCommandHandler() : CommandHandler {
    private val emptyList: List<String> = ArrayList()

    @Command(name = "credits", aliases = arrayOf("cr"), permission = "deathban.credits.check")
    fun handleDefault(args: Arguments) {
        if (args.args.size < 1) {
            if (args.isPlayer) {
                args.sender.sendMessage("$tag Revival credits: ${PlayerData[args.player].revivalCredits}")
            } else {
                args.sender.sendMessage("$errorTag Only players may check their own credit balance.")
            }
        } else {
            if (args.sender.hasPermission("deathban.credits.check.others")) {
                val otherPlayerData = PlayerData[args.args[0]]

                if (otherPlayerData == null) {
                    args.sender.sendMessage("$errorTag The specified player could not be found.")
                } else {
                    args.sender.sendMessage("$tag ${otherPlayerData.lastSeenName}'s revivial credits: ${otherPlayerData.revivalCredits}")
                }
            } else {
                args.sender.sendMessage("$errorTag You do not have permission to perform this command.")
            }
        }
    }

    @TabCompleter(name = "credits", aliases = arrayOf("cr"))
    fun completeDefault(args: Arguments) : List<String> {
        if (args.args.size == 1) {
            var retVal = listOf("send", "give", "take")

            if (args.sender.hasPermission("deathban.credits.check.others")) {
                retVal += PlayerData.getAllUsernames()
            }

            return retVal.autocomplete(args.args[0])
        } else {
            return emptyList
        }
    }

    @Command(name = "credits.send", aliases = arrayOf("cr.send"), permission = "deathban.credits.send", playerOnly = true)
    fun handleSend(args: Arguments) {
        if (args.args.size < 2) {
            args.player.sendMessage("$errorTag You must specify both a player and an amount to send to that player.")
            return
        }

        val creditsArg = tryParseInt(args.args[1])

        if (creditsArg < 1) {
            args.player.sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
            return
        }

        val playerData = PlayerData[args.player]
        val otherPlayerData = PlayerData[args.args[0]]

        if (otherPlayerData == null) {
            args.player.sendMessage("$errorTag The specified player could not be found.")
        } else {
            if (playerData.revivalCredits >= creditsArg) {
                otherPlayerData.revivalCredits += creditsArg
                playerData.revivalCredits -= creditsArg
                args.player.sendMessage("$tag You have successfully sent ${otherPlayerData.lastSeenName} $creditsArg revival credits.")
            } else {
                args.player.sendMessage("$errorTag You do not have enough revival credits.")
            }
        }
    }

    @TabCompleter(name = "credits.send", aliases = arrayOf("cr.send"))
    fun completeSend(args: Arguments) = if (args.args.size == 1) PlayerData.getAllUsernames().autocomplete(args.args[0]) else emptyList

    @Command(name = "credits.give", aliases = arrayOf("cr.give"), permission = "deathban.credits.give")
    fun handleGive(args: Arguments) {
        if (args.args.size < 2) {
            args.sender.sendMessage("$errorTag You must specify both a player and an amount to give that player.")
            return
        }

        val creditsArg = tryParseInt(args.args[1])

        if (creditsArg < 1) {
            args.sender.sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
            return
        }

        val otherPlayerData = PlayerData[args.args[0]]

        if (otherPlayerData == null) {
            args.sender.sendMessage("$errorTag The specified player could not be found.")
        } else {
            otherPlayerData.revivalCredits += creditsArg
            args.sender.sendMessage("$tag You have successfully given ${otherPlayerData.lastSeenName} $creditsArg revival credits.")
        }
    }

    @TabCompleter(name = "credits.give", aliases = arrayOf("cr.give"))
    fun completeGive(args: Arguments) = if (args.args.size == 1) PlayerData.getAllUsernames().autocomplete(args.args[0]) else emptyList

    @Command(name = "credits.take", aliases = arrayOf("cr.take"), permission = "deathban.credits.take")
    fun handleTake(args: Arguments) {
        if (args.args.size < 2) {
            args.sender.sendMessage("$errorTag You must specify both a player and an amount to take from that player.")
            return
        }

        val creditsArg = tryParseInt(args.args[1])

        if (creditsArg < 1) {
            args.sender.sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
            return
        }

        val otherPlayerData = PlayerData[args.args[0]]

        if (otherPlayerData == null) {
            args.sender.sendMessage("$errorTag The specified player could not be found.")
        } else {
            otherPlayerData.revivalCredits -= creditsArg

            if (otherPlayerData.revivalCredits < 0) {
                otherPlayerData.revivalCredits = 0
                args.sender.sendMessage("$tag You have successfully taken all of ${otherPlayerData.lastSeenName}'s revival credits.")
            } else {
                args.sender.sendMessage("$tag You have successfully taken $creditsArg revival credits from ${otherPlayerData.lastSeenName}.")
            }
        }
    }

    @TabCompleter(name = "credits.take", aliases = arrayOf("cr.take"))
    fun completeTake(args: Arguments) = if (args.args.size == 1) PlayerData.getAllUsernames().autocomplete(args.args[0]) else emptyList

    private fun tryParseInt(toParse: String) : Int {
        try {
            return toParse.toInt()
        } catch (e: NumberFormatException) {
            return -1
        }
    }
}