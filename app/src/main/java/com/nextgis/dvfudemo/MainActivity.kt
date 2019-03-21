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

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import com.nextgis.maplib.api.IGISApplication
import com.nextgis.maplib.map.MapDrawable
import com.nextgis.maplibui.mapui.MapViewOverlays



class MainActivity : AppCompatActivity() {
    private var map: MapViewOverlays? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val signed = preferences.getBoolean("signed", false)
        if (!signed) {
            signin()
            return
        }

        val authorized = preferences.getBoolean("authorized", false)
        val app = application as? IGISApplication
        map = MapViewOverlays(this, app?.map as MapDrawable?)
        map?.id = R.id.map

        val container = findViewById<FrameLayout>(R.id.map)
        container.addView(map)
    }

    override fun onDestroy() {
        super.onDestroy()
        val container = findViewById<FrameLayout>(R.id.map)
        container.removeView(map)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.action_signout -> {
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                preferences.edit().remove("signed").remove("authorized").apply()
                val app = application as? IGISApplication
                app?.getAccount(AUTHORITY)?.let { app.removeAccount(it) }
                signin()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signin() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val AUTHORITY = "dvfu-demo.nextgis.com"
        const val FULL_URL = "http://$AUTHORITY"
    }
}
