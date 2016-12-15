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
import com.mstiles92.plugins.deathbanredux.util.errorTag
import com.mstiles92.plugins.deathbanredux.util.tag
import com.mstiles92.plugins.stileslib.commands.Arguments
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.runners.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class CreditsCommandHandlerTest {
    var console = mock(ConsoleCommandSender::class.java)
    var player1 = mock(Player::class.java)
    var player2 = mock(Player::class.java)
    var args = mock(Arguments::class.java)

    @Before
    fun setup() {
        console = mock(ConsoleCommandSender::class.java)
        `when`(console.hasPermission("deathban.credits.check.others")).thenReturn(true)

        player1 = mock(Player::class.java)
        `when`(player1.name).thenReturn("Player1")
        `when`(player1.uniqueId).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        `when`(player1.hasPermission("deathban.credits.check.others")).thenReturn(true)
        PlayerData[player1].banTime = System.currentTimeMillis() + (1000 * 60 * 60 * 24)
        PlayerData[player1].revivalCredits = 5

        player2 = mock(Player::class.java)
        `when`(player2.name).thenReturn("Slayer")
        `when`(player2.uniqueId).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        `when`(player2.hasPermission("deathban.credits.check.others")).thenReturn(false)
        PlayerData[player2].revivalCredits = 1

        args = mock(Arguments::class.java)
    }

    private fun mockArgs(sender: CommandSender, args: Array<String>) : Arguments {
        val mock = mock(Arguments::class.java)
        `when`(mock.sender).thenReturn(sender)
        `when`(mock.args).thenReturn(args)
        `when`(mock.isPlayer).thenReturn(sender is Player)
        `when`(mock.player).thenReturn(if (sender is Player) sender else null)
        return mock
    }

    @Test
    fun handleDefault_checkOwnCredits() {
        CreditsCommandHandler().handleDefault(mockArgs(console, arrayOf()))
        verify(console).sendMessage("$errorTag Only players may check their own credit balance.")

        CreditsCommandHandler().handleDefault(mockArgs(player1, arrayOf()))
        verify(player1).sendMessage("$tag Revival credits: 5")

        CreditsCommandHandler().handleDefault(mockArgs(player2, arrayOf()))
        verify(player2).sendMessage("$tag Revival credits: 1")
    }

    @Test
    fun handleDefault_checkOtherCredits() {
        CreditsCommandHandler().handleDefault(mockArgs(console, arrayOf("Player1")))
        verify(console).sendMessage("$tag Player1's revival credits: 5")

        CreditsCommandHandler().handleDefault(mockArgs(player1, arrayOf("Slayer")))
        verify(player1).sendMessage("$tag Slayer's revival credits: 1")

        CreditsCommandHandler().handleDefault(mockArgs(player2, arrayOf("Player1")))
        verify(player2).sendMessage("$errorTag You do not have permission to perform this command.")
    }

    @Test
    fun handleDefault_checkOtherCreditsCaseInsensitive() {
        CreditsCommandHandler().handleDefault(mockArgs(console, arrayOf("player1")))
        verify(console).sendMessage("$tag Player1's revival credits: 5")

        CreditsCommandHandler().handleDefault(mockArgs(player1, arrayOf("slayer")))
        verify(player1).sendMessage("$tag Slayer's revival credits: 1")

        CreditsCommandHandler().handleDefault(mockArgs(player2, arrayOf("player1")))
        verify(player2).sendMessage("$errorTag You do not have permission to perform this command.")
    }

    @Test
    fun handleDefault_checkOtherCreditsInvalidPlayer() {
        CreditsCommandHandler().handleDefault(mockArgs(console, arrayOf("invalid")))
        verify(console).sendMessage("$errorTag The specified player could not be found.")

        CreditsCommandHandler().handleDefault(mockArgs(player1, arrayOf("invalid")))
        verify(player1).sendMessage("$errorTag The specified player could not be found.")

        CreditsCommandHandler().handleDefault(mockArgs(player2, arrayOf("invalid")))
        verify(player2).sendMessage("$errorTag You do not have permission to perform this command.")
    }

    @Test
    fun completeDefault_noArgs() {
        var result = CreditsCommandHandler().completeDefault(mockArgs(console, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player1, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player2, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun completeDefault_subcommandOnly() {
        var result = CreditsCommandHandler().completeDefault(mockArgs(console, arrayOf("g")))
        Assert.assertEquals(listOf("give"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player1, arrayOf("g")))
        Assert.assertEquals(listOf("give"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player2, arrayOf("g")))
        Assert.assertEquals(listOf("give"), result)
    }

    @Test
    fun completeDefault_playerNameOnly() {
        var result = CreditsCommandHandler().completeDefault(mockArgs(console, arrayOf("P")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player1, arrayOf("P")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player2, arrayOf("P")))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun completeDefault_playerNameOnlyCaseInsensitive() {
        var result = CreditsCommandHandler().completeDefault(mockArgs(console, arrayOf("p")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player1, arrayOf("p")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player2, arrayOf("p")))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun completeDefault_subcommandAndPlayerName() {
        var result = CreditsCommandHandler().completeDefault(mockArgs(console, arrayOf("s")))
        Assert.assertEquals(listOf("send", "Slayer"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player1, arrayOf("s")))
        Assert.assertEquals(listOf("send", "Slayer"), result)

        result = CreditsCommandHandler().completeDefault(mockArgs(player2, arrayOf("s")))
        Assert.assertEquals(listOf("send"), result)
    }

    @Test
    fun handleSend_noArgs() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf()))
        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to send to that player.")

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf()))
        verify(player2).sendMessage("$errorTag You must specify both a player and an amount to send to that player.")
    }

    @Test
    fun handleSend_insufficientArgs() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("Slayer")))
        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to send to that player.")

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("Player1")))
        verify(player2).sendMessage("$errorTag You must specify both a player and an amount to send to that player.")
    }

    @Test
    fun handleSend_stringCreditAmount() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("Slayer", "invalid")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("Player1", "invalid")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleSend_negativeCreditAmount() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("Slayer", "-5")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("Player1", "-5")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleSend_zeroCreditAmount() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("Slayer", "0")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("Player1", "0")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleSend_invalidPlayerName() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("invalid", "1")))
        verify(player1).sendMessage("$errorTag The specified player could not be found.")

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("invalid", "1")))
        verify(player2).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleSend_notEnoughCredits() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("Slayer", "10")))
        verify(player1).sendMessage("$errorTag You do not have enough revival credits.")

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("Player1", "10")))
        verify(player2).sendMessage("$errorTag You do not have enough revival credits.")
    }

    @Test
    fun handleSend_validArgs() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("Slayer", "3")))
        verify(player1).sendMessage("$tag You have successfully sent Slayer 3 revival credits.")
        Assert.assertEquals(2, PlayerData[player1].revivalCredits)
        Assert.assertEquals(4, PlayerData[player2].revivalCredits)

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("Player1", "2")))
        verify(player2).sendMessage("$tag You have successfully sent Player1 2 revival credits.")
        Assert.assertEquals(4, PlayerData[player1].revivalCredits)
        Assert.assertEquals(2, PlayerData[player2].revivalCredits)
    }

    @Test
    fun handleSend_validArgsCaseInsensitive() {
        CreditsCommandHandler().handleSend(mockArgs(player1, arrayOf("slayer", "3")))
        verify(player1).sendMessage("$tag You have successfully sent Slayer 3 revival credits.")
        Assert.assertEquals(2, PlayerData[player1].revivalCredits)
        Assert.assertEquals(4, PlayerData[player2].revivalCredits)

        CreditsCommandHandler().handleSend(mockArgs(player2, arrayOf("player1", "2")))
        verify(player2).sendMessage("$tag You have successfully sent Player1 2 revival credits.")
        Assert.assertEquals(4, PlayerData[player1].revivalCredits)
        Assert.assertEquals(2, PlayerData[player2].revivalCredits)
    }

    @Test
    fun completeSend_noArgs() {
        var result = CreditsCommandHandler().completeSend(mockArgs(player1, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeSend(mockArgs(player2, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun completeSend_singleArg() {
        var result = CreditsCommandHandler().completeSend(mockArgs(player1, arrayOf("S")))
        Assert.assertEquals(listOf("Slayer"), result)

        result = CreditsCommandHandler().completeSend(mockArgs(player2, arrayOf("P")))
        Assert.assertEquals(listOf("Player1"), result)
    }

    @Test
    fun completeSend_singleArgCaseInsensitive() {
        var result = CreditsCommandHandler().completeSend(mockArgs(player1, arrayOf("s")))
        Assert.assertEquals(listOf("Slayer"), result)

        result = CreditsCommandHandler().completeSend(mockArgs(player2, arrayOf("p")))
        Assert.assertEquals(listOf("Player1"), result)
    }

    @Test
    fun completeSend_twoArgs() {
        var result = CreditsCommandHandler().completeSend(mockArgs(player1, arrayOf("Slayer", "2")))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeSend(mockArgs(player2, arrayOf("Player1", "2")))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun handleGive_noArgs() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf()))
        verify(console).sendMessage("$errorTag You must specify both a player and an amount to give that player.")

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf()))
        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to give that player.")

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf()))
        verify(player2).sendMessage("$errorTag You must specify both a player and an amount to give that player.")
    }

    @Test
    fun handleGive_singleArg() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf("Player1")))
        verify(console).sendMessage("$errorTag You must specify both a player and an amount to give that player.")

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf("Slayer")))
        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to give that player.")

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf("Player1")))
        verify(player2).sendMessage("$errorTag You must specify both a player and an amount to give that player.")
    }

    @Test
    fun handleGive_stringCreditAmount() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf("Player1", "invalid")))
        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf("Slayer", "invalid")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf("Player1", "invalid")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_negativeCreditAmount() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf("Player1", "-1")))
        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf("Slayer", "-1")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf("Player1", "-1")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_zeroCreditAmount() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf("Player1", "0")))
        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf("Slayer", "0")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf("Player1", "0")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_invalidPlayerName() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf("invalid", "1")))
        verify(console).sendMessage("$errorTag The specified player could not be found.")

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf("invalid", "1")))
        verify(player1).sendMessage("$errorTag The specified player could not be found.")

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf("invalid", "1")))
        verify(player2).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleGive_validArgs() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf("Player1", "2")))
        verify(console).sendMessage("$tag You have successfully given Player1 2 revival credits.")
        Assert.assertEquals(7, PlayerData[player1].revivalCredits)

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf("Slayer", "2")))
        verify(player1).sendMessage("$tag You have successfully given Slayer 2 revival credits.")
        Assert.assertEquals(3, PlayerData[player2].revivalCredits)

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf("Player1", "2")))
        verify(player2).sendMessage("$tag You have successfully given Player1 2 revival credits.")
        Assert.assertEquals(9, PlayerData[player1].revivalCredits)
    }

    @Test
    fun handleGive_validArgsCaseInsensitive() {
        CreditsCommandHandler().handleGive(mockArgs(console, arrayOf("player1", "2")))
        verify(console).sendMessage("$tag You have successfully given Player1 2 revival credits.")
        Assert.assertEquals(7, PlayerData[player1].revivalCredits)

        CreditsCommandHandler().handleGive(mockArgs(player1, arrayOf("slayer", "2")))
        verify(player1).sendMessage("$tag You have successfully given Slayer 2 revival credits.")
        Assert.assertEquals(3, PlayerData[player2].revivalCredits)

        CreditsCommandHandler().handleGive(mockArgs(player2, arrayOf("player1", "2")))
        verify(player2).sendMessage("$tag You have successfully given Player1 2 revival credits.")
        Assert.assertEquals(9, PlayerData[player1].revivalCredits)
    }

    @Test
    fun completeGive_noArgs() {
        var result = CreditsCommandHandler().completeGive(mockArgs(console, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player1, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player2, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun completeGive_singleArg() {
        var result = CreditsCommandHandler().completeGive(mockArgs(console, arrayOf("P")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player1, arrayOf("S")))
        Assert.assertEquals(listOf("Slayer"), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player2, arrayOf("P")))
        Assert.assertEquals(listOf("Player1"), result)
    }

    @Test
    fun completeGive_singleArgCaseInsensitive() {
        var result = CreditsCommandHandler().completeGive(mockArgs(console, arrayOf("p")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player1, arrayOf("s")))
        Assert.assertEquals(listOf("Slayer"), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player2, arrayOf("p")))
        Assert.assertEquals(listOf("Player1"), result)
    }

    @Test
    fun completeGive_twoArgs() {
        var result = CreditsCommandHandler().completeGive(mockArgs(console, arrayOf("Player1", "2")))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player1, arrayOf("Slayer", "2")))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeGive(mockArgs(player2, arrayOf("Player1", "2")))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun handleTake_noArgs() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf()))
        verify(console).sendMessage("$errorTag You must specify both a player and an amount to take from that player.")

        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf()))
        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to take from that player.")

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf()))
        verify(player2).sendMessage("$errorTag You must specify both a player and an amount to take from that player.")
    }

    @Test
    fun handleTake_singleArg() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("Player1")))
        verify(console).sendMessage("$errorTag You must specify both a player and an amount to take from that player.")

        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("Slayer")))
        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to take from that player.")

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("Player1")))
        verify(player2).sendMessage("$errorTag You must specify both a player and an amount to take from that player.")
    }

    @Test
    fun handleTake_stringCreditAmount() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("Player1", "invalid")))
        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("Slayer", "invalid")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("Player1", "invalid")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleTake_negativeCreditAmount() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("Player1", "-1")))
        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("Slayer", "-1")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("Player1", "-1")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleTake_zeroCreditAmount() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("Player1", "0")))
        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("Slayer", "0")))
        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("Player1", "0")))
        verify(player2).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleTake_invalidPlayerName() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("invalid", "1")))
        verify(console).sendMessage("$errorTag The specified player could not be found.")

        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("invalid", "1")))
        verify(player1).sendMessage("$errorTag The specified player could not be found.")

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("invalid", "1")))
        verify(player2).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleTake_validArgs() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("Player1", "2")))
        verify(console).sendMessage("$tag You have successfully taken 2 revival credits from Player1.")
        Assert.assertEquals(3, PlayerData[player1].revivalCredits)

        PlayerData[player2].revivalCredits = 4
        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("Slayer", "2")))
        verify(player1).sendMessage("$tag You have successfully taken 2 revival credits from Slayer.")
        Assert.assertEquals(2, PlayerData[player2].revivalCredits)

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("Player1", "2")))
        verify(player2).sendMessage("$tag You have successfully taken 2 revival credits from Player1.")
        Assert.assertEquals(1, PlayerData[player1].revivalCredits)
    }

    @Test
    fun handleTake_validArgsCaseInsensitive() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("player1", "2")))
        verify(console).sendMessage("$tag You have successfully taken 2 revival credits from Player1.")
        Assert.assertEquals(3, PlayerData[player1].revivalCredits)

        PlayerData[player2].revivalCredits = 4
        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("slayer", "2")))
        verify(player1).sendMessage("$tag You have successfully taken 2 revival credits from Slayer.")
        Assert.assertEquals(2, PlayerData[player2].revivalCredits)

        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("player1", "2")))
        verify(player2).sendMessage("$tag You have successfully taken 2 revival credits from Player1.")
        Assert.assertEquals(1, PlayerData[player1].revivalCredits)
    }

    @Test
    fun handleTake_validArgsTakeAll() {
        CreditsCommandHandler().handleTake(mockArgs(console, arrayOf("Player1", "10")))
        verify(console).sendMessage("$tag You have successfully taken all of Player1's revival credits.")
        Assert.assertEquals(0, PlayerData[player1].revivalCredits)

        CreditsCommandHandler().handleTake(mockArgs(player1, arrayOf("Slayer", "10")))
        verify(player1).sendMessage("$tag You have successfully taken all of Slayer's revival credits.")
        Assert.assertEquals(0, PlayerData[player2].revivalCredits)

        PlayerData[player1].revivalCredits = 5
        CreditsCommandHandler().handleTake(mockArgs(player2, arrayOf("Player1", "10")))
        verify(player2).sendMessage("$tag You have successfully taken all of Player1's revival credits.")
        Assert.assertEquals(0, PlayerData[player1].revivalCredits)
    }
    
    @Test
    fun completeTake_noArgs() {
        var result = CreditsCommandHandler().completeTake(mockArgs(console, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player1, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player2, arrayOf()))
        Assert.assertEquals(listOf<String>(), result)
    }

    @Test
    fun completeTake_singleArg() {
        var result = CreditsCommandHandler().completeTake(mockArgs(console, arrayOf("P")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player1, arrayOf("S")))
        Assert.assertEquals(listOf("Slayer"), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player2, arrayOf("P")))
        Assert.assertEquals(listOf("Player1"), result)
    }

    @Test
    fun completeTake_singleArgCaseInsensitive() {
        var result = CreditsCommandHandler().completeTake(mockArgs(console, arrayOf("p")))
        Assert.assertEquals(listOf("Player1"), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player1, arrayOf("s")))
        Assert.assertEquals(listOf("Slayer"), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player2, arrayOf("p")))
        Assert.assertEquals(listOf("Player1"), result)
    }

    @Test
    fun completeTake_twoArgs() {
        var result = CreditsCommandHandler().completeTake(mockArgs(console, arrayOf("Player1", "2")))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player1, arrayOf("Slayer", "2")))
        Assert.assertEquals(listOf<String>(), result)

        result = CreditsCommandHandler().completeTake(mockArgs(player2, arrayOf("Player1", "2")))
        Assert.assertEquals(listOf<String>(), result)
    }
}