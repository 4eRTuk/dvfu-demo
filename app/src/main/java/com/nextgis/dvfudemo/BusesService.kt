/*
 * Project:  Demo DVFU
 * Purpose:  NextGIS demo project for DVFU
 * Author:   Stanislav Petriakov, becomeglory@gmail.com
 * ****************************************************************************
 * Copyright (c) 2019 NextGIS, info@nextgis.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nextgis.dvfudemo

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import com.nextgis.maplib.util.NetworkUtil
import org.json.JSONArray

class BusesService : Service() {
    private var thread: Thread? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread(Runnable {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val response = NetworkUtil.get(URL, null, null, false)
                    val data = JSONArray(response.responseBody)
                    val array = arrayListOf<Bus>()
                    for (i in 0 until data.length()) {
                        val item = data.getJSONObject(i)
                        val location = Location(LocationManager.GPS_PROVIDER)
                        location.longitude = item.getDouble("lon")
                        location.latitude = item.getDouble("lat")
                        val title = item.getString("title")
                        val congestion = item.getDouble("congestion")
                        array.add(Bus(title, location, congestion))
                    }
                    val notification = Intent("BUSES_UPDATE")
                    notification.putParcelableArrayListExtra("buses", array)
                    sendBroadcast(notification)
                    Thread.sleep(30000)
                } catch (e: InterruptedException) {
                }
            }
        }).start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        thread?.interrupt()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    companion object {
        const val URL = "http://ms.4ert.com/buses"
    }

}