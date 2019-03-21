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

import com.nextgis.maplibui.GISApplication
import com.nextgis.maplib.util.GeoConstants
import com.nextgis.maplib.util.GeoConstants.TMSTYPE_OSM
import com.nextgis.maplibui.mapui.RemoteTMSLayerUI
import com.nextgis.maplibui.util.SettingsConstantsUI


class DVFUApplication : GISApplication() {
    override fun onCreate() {
        super.onCreate()
        initBaseLayers()
    }

    fun initBaseLayers() {
        if (mMap.getLayerByPathName(OSM) == null) {
            // add OpenStreetMap layer
            val layer = RemoteTMSLayerUI(applicationContext, mMap.createLayerStorage(OSM))
            layer.name = "OSM"
            layer.url = SettingsConstantsUI.OSM_URL
            layer.tmsType = TMSTYPE_OSM
            layer.isVisible = true
            layer.minZoom = GeoConstants.DEFAULT_MIN_ZOOM.toFloat()
            layer.maxZoom = 19f

            mMap.addLayer(layer)
            mMap.moveLayer(0, layer)
        }
    }

    override fun getAuthority(): String {
        return AUTHORITY
    }

    override fun showSettings(setting: String?) {

    }

    override fun sendEvent(category: String?, action: String?, label: String?) {

    }

    override fun sendScreen(name: String?) {

    }

    override fun getAccountsType(): String {
        return ACCOUNT_TYPE
    }

    companion object {
        const val ACCOUNT_TYPE = "dvfu_account"
        const val AUTHORITY = "com.nextgis.dvfudemo.provider"
        const val OSM = "osm_layer"
    }
}