package com.mstiles92.plugins.deathbanredux

import org.bukkit.plugin.java.JavaPlugin
import com.mstiles92.plugins.deathbanredux.config.Config

class DeathBanRedux() : JavaPlugin() {
    val config = Config(this)

    override fun onEnable() {
        config.load()
    }

    override fun onDisable() {
        config.save()
    }
}