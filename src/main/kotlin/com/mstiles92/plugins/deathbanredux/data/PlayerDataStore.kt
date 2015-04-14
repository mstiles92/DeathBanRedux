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

import org.bukkit.entity.Player
import java.util.HashMap
import java.util.UUID

public object PlayerDataStore {
    private val instances = HashMap<UUID, PlayerData>();

    fun get(player: Player) : PlayerData {
        if (instances.containsKey(player.getUniqueId())) {
            val data = instances.get(player.getUniqueId())
            data.lastSeenName = player.getName()
            return data
        } else {
            val data = PlayerData(player.getUniqueId(), player.getName())
            instances.put(player.getUniqueId(), data)
            return data
        }
    }

    fun get(uuid: UUID) : PlayerData? {
        return instances.get(uuid)
    }

    fun get(username: String) : PlayerData? {
        return instances.values().firstOrNull { it -> it.lastSeenName.equalsIgnoreCase(username) }
    }
}