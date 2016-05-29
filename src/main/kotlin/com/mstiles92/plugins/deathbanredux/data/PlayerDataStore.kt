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
import java.io.FileReader
import java.io.FileWriter
import java.util.HashMap
import java.util.UUID

class PlayerDataStore() {
    private var instances = HashMap<String, PlayerData>()

    operator fun get(player: Player) : PlayerData {
        var data = instances[player.uniqueId.toString()]

        if (data != null) {
            data.lastSeenName = player.name
            return data
        } else {
            data = PlayerData(player.name)
            instances.put(player.uniqueId.toString(), data)
            return data
        }
    }

    operator fun get(uuid: UUID) : PlayerData? {
        return instances[uuid.toString()]
    }

    operator fun get(username: String) : PlayerData? {
        return instances.values.firstOrNull { it -> it.lastSeenName.equals(username, ignoreCase = true) }
    }

    fun save(file: File, pretty: Boolean = true) {
        val gson = if (pretty) GsonBuilder().setPrettyPrinting().create() else Gson()

        val writer = FileWriter(file)
        IOUtils.write(gson.toJson(instances), writer)
        writer.close()
    }

    fun load(file: File) {
        val gson = Gson()

        val reader = FileReader(file)
        val jsonString = IOUtils.toString(reader)
        reader.close()

        instances = gson.fromJson(jsonString, object : TypeToken<HashMap<String, PlayerData>>(){}.type) ?: instances
    }
}