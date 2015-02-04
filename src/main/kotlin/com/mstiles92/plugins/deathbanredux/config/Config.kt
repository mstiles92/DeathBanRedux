package com.mstiles92.plugins.deathbanredux.config

import com.mstiles92.plugins.deathbanredux.DeathBanRedux
import java.util.HashMap
import com.mstiles92.plugins.deathbanredux.data.DeathClass
import java.util.ArrayList

class Config(val plugin: DeathBanRedux) {
    val config = plugin.getConfig();
    private val settings = HashMap<String, Any>()
    val deathClasses = ArrayList<DeathClass>()

    fun load() {
        settings["enabled"] = config.getBoolean("Enabled", true)
        settings["banTime"] = config.getString("Ban-Time", "12h")
        settings["deathMessage"] = config.getString("Death-Message", "You have died! You are now banned for %bantimeleft%.")
        settings["earlyMessage"] = config.getString("Early-Message", "Your ban is not up for another %bantimeleft%.")
        settings["tickDelay"] = config.getInt("Tick-Delay", 15)
        settings["startingCredits"] = config.getInt("Starting-Credits", 0)
        settings["verboseLogging"] = config.getBoolean("Verbose", false)
        settings["updateChecking"] = config.getBoolean("Check-for-Updates", true)

        val section = config.getConfigurationSection("Death-Classes")
        section?.getKeys(false)?.forEach { it -> deathClasses.add(DeathClass(it, section.getString("${it}.Ban-Time"), section.getString("${it}.Death-Message"))) }
    }

    fun save() {
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