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

package com.mstiles92.plugins.deathbanredux.config

import com.mstiles92.plugins.deathbanredux.data.DeathClass
import org.bukkit.plugin.java.JavaPlugin
import java.util.ArrayList
import java.util.HashMap

public class Config() {
    private val settings = HashMap<String, Any>()
    private val deathClasses = ArrayList<DeathClass>()

    fun load(plugin: JavaPlugin) {
        val config = plugin.config
        settings["enabled"] = config.getBoolean("Enabled", true)
        settings["banTime"] = config.getString("Ban-Time", "12h")
        settings["deathMessage"] = config.getString("Death-Message", "You have died! You are now banned for %bantimeleft%.")
        settings["earlyMessage"] = config.getString("Early-Message", "Your ban is not up for another %bantimeleft%.")
        settings["tickDelay"] = config.getInt("Tick-Delay", 15)
        settings["startingCredits"] = config.getInt("Starting-Credits", 0)
        settings["verboseLogging"] = config.getBoolean("Verbose", false)
        settings["updateChecking"] = config.getBoolean("Check-for-Updates", true)

        val section = config.getConfigurationSection("Death-Classes")
        section?.getKeys(false)?.forEach { it ->
            deathClasses.add(DeathClass(it, section.getString("$it.Ban-Time"), section.getString("$it.Death-Message"))) }
    }

    fun save(plugin: JavaPlugin) {
        val config = plugin.config
        settings.forEach { it -> config.set(it.getKey(), it.getValue()) }
        plugin.saveConfig()
    }

    fun isEnabled() = settings["enabled"] as Boolean

    fun setEnabled(enabled: Boolean) {
        settings["enabled"] = enabled
    }

    fun getBanTime() = settings["banTime"] as String

    fun getDeathMessage() = settings["deathMessage"] as String

    fun getEarlyMessage() = settings["earlyMessage"] as String

    fun getTickDelay() = settings["tickDelay"] as Int

    fun getStartingCredits() = settings["startingCredits"] as Int

    fun shouldLogVerbose() = settings["verboseLogging"] as Boolean

    fun shouldCheckForUpdates() = settings["updateChecking"] as Boolean
}