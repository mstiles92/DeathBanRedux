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

    @Test
    fun handleDefault_playerWithNoArgs_displaysOwnCredits() {
        `when`(args.sender).thenReturn(player1)
        `when`(args.args).thenReturn(arrayOf())
        `when`(args.isPlayer).thenReturn(true)
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleDefault(args)

        verify(player1).sendMessage("$tag Revival credits: 5")
    }

    @Test
    fun handleDefault_consoleWithNoArgs_displaysErrorMessage() {
        `when`(args.sender).thenReturn(console)
        `when`(args.args).thenReturn(arrayOf())
        `when`(args.isPlayer).thenReturn(false)
        `when`(args.player).thenReturn(null)

        CreditsCommandHandler().handleDefault(args)

        verify(console).sendMessage("$errorTag Only players may check their own credit balance.")
    }

    @Test
    fun handleDefault_playerWithoutCheckOthersPerms_displaysErrorMessage() {
        `when`(args.sender).thenReturn(player2)
        `when`(args.args).thenReturn(arrayOf("Player1"))
        `when`(args.isPlayer).thenReturn(true)
        `when`(args.player).thenReturn(player2)

        CreditsCommandHandler().handleDefault(args)

        verify(player2).sendMessage("$errorTag You do not have permission to perform this command.")
    }

    @Test
    fun handleDefault_consoleWithoutCheckOthersPerms_displaysErrorMessage() {
        `when`(args.sender).thenReturn(console)
        `when`(args.args).thenReturn(arrayOf("Slayer"))
        `when`(args.isPlayer).thenReturn(false)
        `when`(args.player).thenReturn(null)
        `when`(console.hasPermission("deathban.credits.check.others")).thenReturn(false)

        CreditsCommandHandler().handleDefault(args)

        verify(console).sendMessage("$errorTag You do not have permission to perform this command.")
    }

    @Test
    fun handleDefault_playerCheckOtherInvalidPlayer_displaysErrorMessage() {
        `when`(args.sender).thenReturn(player1)
        `when`(args.args).thenReturn(arrayOf("Player3"))
        `when`(args.isPlayer).thenReturn(true)
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleDefault(args)

        verify(player1).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleDefault_playerCheckOtherValidPlayer_displaysCredits() {
        `when`(args.sender).thenReturn(player1)
        `when`(args.args).thenReturn(arrayOf("Slayer"))
        `when`(args.isPlayer).thenReturn(true)
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleDefault(args)

        verify(player1).sendMessage("$tag Slayer's revivial credits: 1")
    }

    @Test
    fun handleDefault_consoleCheckInvalidPlayer_displaysErrorMessage() {
        `when`(args.sender).thenReturn(console)
        `when`(args.args).thenReturn(arrayOf("Player3"))
        `when`(args.isPlayer).thenReturn(false)
        `when`(args.player).thenReturn(null)

        CreditsCommandHandler().handleDefault(args)

        verify(console).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleDefault_consoleCheckValidPlayer_displaysCredits() {
        `when`(args.sender).thenReturn(console)
        `when`(args.args).thenReturn(arrayOf("Player1"))
        `when`(args.isPlayer).thenReturn(false)
        `when`(args.player).thenReturn(null)

        CreditsCommandHandler().handleDefault(args)

        verify(console).sendMessage("$tag Player1's revivial credits: 5")
    }

    @Test
    fun completeDefault_noArgsProvidedNoPerms_returnsEmptyList() {
        `when`(args.sender).thenReturn(player2)
        `when`(args.args).thenReturn(arrayOf())

        val retVal = CreditsCommandHandler().completeDefault(args)

        Assert.assertEquals(listOf<String>(), retVal)
    }

    @Test
    fun completeDefault_noPerms_returnsSubcommand() {
        `when`(args.sender).thenReturn(player2)
        `when`(args.args).thenReturn(arrayOf("g"))

        val retVal = CreditsCommandHandler().completeDefault(args)

        Assert.assertEquals(listOf("give"), retVal)
    }

    @Test
    fun completeDefault_withPerms_returnsPlayerNames() {
        `when`(args.sender).thenReturn(console)
        `when`(args.args).thenReturn(arrayOf("P"))

        val retVal = CreditsCommandHandler().completeDefault(args)

        Assert.assertEquals(listOf("Player1"), retVal)
    }

    @Test
    fun completeDefault_withPerms_returnsUserAndSubcommand() {
        `when`(args.sender).thenReturn(console)
        `when`(args.args).thenReturn(arrayOf("s"))

        val retVal = CreditsCommandHandler().completeDefault(args)

        Assert.assertEquals(listOf("send", "Slayer"), retVal)
    }

    @Test
    fun handleSend_noArgsProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf())
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to send to that player.")
    }

    @Test
    fun handleSend_insufficientArgsProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer"))
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to send to that player.")
    }

    @Test
    fun handleSend_stringCreditAmount_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer2", "invalid"))
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleSend_negativeCreditAmount_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer2", "-5"))
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleSend_invalidPlayerSpecified_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Player3", "1"))
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleSend_notEnoughCredits_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer", "10"))
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$errorTag You do not have enough revival credits.")
    }

    @Test
    fun handleSend_validArgsProvided_sendsCredits() {
        `when`(args.args).thenReturn(arrayOf("Slayer", "3"))
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$tag You have successfully sent Slayer 3 revival credits.")
        Assert.assertEquals(2, PlayerData[player1].revivalCredits)
        Assert.assertEquals(4, PlayerData[player2].revivalCredits)
    }

    @Test
    fun handleSend_validArgsProvidedCaseInsensitive_sendsCredits() {
        `when`(args.args).thenReturn(arrayOf("slayer", "2"))
        `when`(args.player).thenReturn(player1)

        CreditsCommandHandler().handleSend(args)

        verify(player1).sendMessage("$tag You have successfully sent Slayer 2 revival credits.")
        Assert.assertEquals(3, PlayerData[player1].revivalCredits)
        Assert.assertEquals(3, PlayerData[player2].revivalCredits)
    }

    @Test
    fun completeSend_noArgsProvided_returnsEmptyList() {
        `when`(args.args).thenReturn(arrayOf())

        val retVal = CreditsCommandHandler().completeSend(args)

        Assert.assertEquals(listOf<String>(), retVal)
    }

    @Test
    fun completeSend_singleArgProvided_returnsCorrectUsername() {
        `when`(args.args).thenReturn(arrayOf("P"))

        val retVal = CreditsCommandHandler().completeSend(args)

        Assert.assertEquals(listOf("Player1"), retVal)
    }

    @Test
    fun completeSend_singleArgProvidedCaseInsensitive_returnsCorrectUsername() {
        `when`(args.args).thenReturn(arrayOf("play"))

        val retVal = CreditsCommandHandler().completeSend(args)

        Assert.assertEquals(listOf("Player1"), retVal)
    }

    @Test
    fun completeSend_twoArgsProvided_returnsEmptyList() {
        `when`(args.args).thenReturn(arrayOf("Player1", "2"))

        val retVal = CreditsCommandHandler().completeSend(args)

        Assert.assertEquals(listOf<String>(), retVal)
    }

    @Test
    fun handleGive_consoleNoArgsProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf<String>())
        `when`(args.sender).thenReturn(console)

        CreditsCommandHandler().handleGive(args)

        verify(console).sendMessage("$errorTag You must specify both a player and an amount to give that player.")
    }

    @Test
    fun handleGive_playerNoArgsProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf<String>())
        `when`(args.sender).thenReturn(player1)

        CreditsCommandHandler().handleGive(args)

        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to give that player.")
    }

    @Test
    fun handleGive_consoleSingleArgProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Player1"))
        `when`(args.sender).thenReturn(console)

        CreditsCommandHandler().handleGive(args)

        verify(console).sendMessage("$errorTag You must specify both a player and an amount to give that player.")
    }

    @Test
    fun handleGive_playerSingleArgProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer"))
        `when`(args.sender).thenReturn(player1)

        CreditsCommandHandler().handleGive(args)

        verify(player1).sendMessage("$errorTag You must specify both a player and an amount to give that player.")
    }

    @Test
    fun handleGive_consoleStringAmountProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Player1", "test"))
        `when`(args.sender).thenReturn(console)

        CreditsCommandHandler().handleGive(args)

        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_playerStringAmountProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer", "test"))
        `when`(args.sender).thenReturn(player1)

        CreditsCommandHandler().handleGive(args)

        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_consoleNegativeAmountProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Player1", "-2"))
        `when`(args.sender).thenReturn(console)

        CreditsCommandHandler().handleGive(args)

        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_playerNegativeAmountProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer", "-5"))
        `when`(args.sender).thenReturn(player1)

        CreditsCommandHandler().handleGive(args)

        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_consoleZeroAmountProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Player1", "0"))
        `when`(args.sender).thenReturn(console)

        CreditsCommandHandler().handleGive(args)

        verify(console).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_playerZeroAmountProvided_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Slayer", "0"))
        `when`(args.sender).thenReturn(player1)

        CreditsCommandHandler().handleGive(args)

        verify(player1).sendMessage("$errorTag The amount of credits specified must be a positive integer value.")
    }

    @Test
    fun handleGive_consoleInvalidPlayerSpecified_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Player3", "2"))
        `when`(args.sender).thenReturn(console)

        CreditsCommandHandler().handleGive(args)

        verify(console).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleGive_playerInvalidPlayerSpecified_displaysError() {
        `when`(args.args).thenReturn(arrayOf("Player3", "5"))
        `when`(args.sender).thenReturn(player1)

        CreditsCommandHandler().handleGive(args)

        verify(player1).sendMessage("$errorTag The specified player could not be found.")
    }

    @Test
    fun handleGive_consoleValidArgsProvided_givesCredits() {
        `when`(args.args).thenReturn(arrayOf("Player1", "2"))
        `when`(args.sender).thenReturn(console)

        CreditsCommandHandler().handleGive(args)

        Assert.assertEquals(7, PlayerData[player1].revivalCredits)
        verify(console).sendMessage("$tag You have successfully given Player1 2 revival credits.")
    }

    @Test
    fun handleGive_playerValidArgsProvided_givesCredits() {
        `when`(args.args).thenReturn(arrayOf("Slayer", "5"))
        `when`(args.sender).thenReturn(player1)

        CreditsCommandHandler().handleGive(args)

        Assert.assertEquals(6, PlayerData[player2].revivalCredits)
        verify(player1).sendMessage("$tag You have successfully given Slayer 5 revival credits.")
    }
}