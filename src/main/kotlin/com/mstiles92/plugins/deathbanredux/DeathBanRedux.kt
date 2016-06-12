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

package com.mstiles92.plugins.deathbanredux

import com.mstiles92.plugins.deathbanredux.commands.CreditsCommandHandler
import com.mstiles92.plugins.deathbanredux.commands.DeathbanCommandHandler
import com.mstiles92.plugins.deathbanredux.config.DeathBanConfig
import com.mstiles92.plugins.deathbanredux.data.PlayerData
import com.mstiles92.plugins.deathbanredux.listeners.LoginListener
import com.mstiles92.plugins.stileslib.commands.CommandRegistry
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.mcstats.Metrics
import java.io.File
import java.io.IOException

class DeathBanRedux() : JavaPlugin() {
    private val commandRegistry = CommandRegistry(this)

    override fun onEnable() {
        saveDefaultConfig()
        DeathBanConfig.load(this)

        try {
            val jsonFile = File(dataFolder, "data.json")

            if (!jsonFile.createNewFile()) {
                // Do not load from file if it was just created, as it will be empty.
                PlayerData.load(jsonFile)
            }
        } catch (e: IOException) {
            logger.warning("${ChatColor.RED}Error loading JSON data file!")
        }

        commandRegistry.registerCommands(DeathbanCommandHandler(this))
        commandRegistry.registerCommands(CreditsCommandHandler())

        server.pluginManager.registerEvents(LoginListener, this)

        try {
            val metrics = Metrics(this)
            metrics.start()
        } catch (e: IOException) {
            logger.warning("${ChatColor.RED}Error starting metrics!")
        }
    }

    override fun onDisable() {
        DeathBanConfig.save(this)

        try {
            val jsonFile = File(dataFolder, "data.json")

            PlayerData.save(jsonFile)
        } catch (e: IOException) {
            logger.warning("${ChatColor.RED}Error saving JSON data file!")
        }
    }

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {
        return commandRegistry.handleCommand(sender, command, label, args)
    }
}