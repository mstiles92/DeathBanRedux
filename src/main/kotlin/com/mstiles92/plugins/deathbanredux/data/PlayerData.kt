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

package com.mstiles92.plugins.deathbanredux.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.apache.commons.io.IOUtils
import org.bukkit.entity.Player
import java.io.File
import java.util.*

data class PlayerData(val uuid: String, var lastSeenName: String, var revivalCredits: Int = 0, var banTime: Long = 0) {

    companion object {
        private var instances: MutableMap<String, PlayerData> = mutableMapOf()

        operator fun get(player: Player) : PlayerData {
            val data = instances[player.uniqueId.toString()]
            data?.lastSeenName = player.name
            return data ?: PlayerData(player.uniqueId.toString(), player.name)
        }

        operator fun get(uuid: UUID) : PlayerData? = instances[uuid.toString()]

        operator fun get(username: String) : PlayerData? = instances.values.firstOrNull{ it -> it.lastSeenName.equals(username, ignoreCase = true) }

        fun save(file: File, pretty: Boolean = true) {
            val gson = if (pretty) GsonBuilder().setPrettyPrinting().create() else Gson()

            file.writer().use { IOUtils.write(gson.toJson(instances), it) }
        }

        fun load(file: File) {
            val gson = Gson()

            file.reader().use { instances = gson.fromJson(IOUtils.toString(it), object: TypeToken<HashMap<String, PlayerData>>(){}.type) ?: instances }
        }
    }

    init {
        instances.put(this.uuid, this)
    }

    fun getUnbanCalendar() : Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = banTime
        return calendar
    }

    fun isCurrentlyBanned() : Boolean {
        if (banTime == 0L) {
            return false
        } else if (Calendar.getInstance().after(getUnbanCalendar())) {
            resetBanTime()
            return false
        } else {
            return true
        }
    }

    fun resetBanTime() {
        banTime = 0L
    }
}