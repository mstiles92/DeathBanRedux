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

package com.mstiles92.plugins.deathbanredux.util

import com.mstiles92.plugins.deathbanredux.data.PlayerData
import com.mstiles92.plugins.deathbanredux.data.PlayerDataStore
import com.mstiles92.plugins.stileslib.calendar.CalendarUtils
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.Calendar


fun Player.getData() : PlayerData {
    return PlayerDataStore[this]
}

fun String.replaceMessageVariables(data: PlayerData) : String {
    val timeFormat = SimpleDateFormat("hh:mm a z")
    val dateFormat = SimpleDateFormat("MM/dd/yyyy")

    val now = Calendar.getInstance()
    val unbanTime = data.getUnbanCalendar()

    var message = this.replaceAll("%player%", data.lastSeenName)

    message = message.replaceAll("%currenttime%", timeFormat.format(now.getTime()))
    message = message.replaceAll("%currentdate%", dateFormat.format(now.getTime()))

    if (data.isCurrentlyBanned()) {
        message = message.replaceAll("%unbantime%", timeFormat.format(unbanTime.getTime()))
        message = message.replaceAll("%unbandate%", dateFormat.format(unbanTime.getTime()))
        message = message.replaceAll("%bantimeleft%", CalendarUtils.buildTimeDifference(now, unbanTime))
    }

    return message
}