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

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PointF
import com.nextgis.maplib.datasource.GeoPoint
import com.nextgis.maplib.map.MapDrawable
import com.nextgis.maplibui.api.Overlay
import com.nextgis.maplibui.api.OverlayItem
import com.nextgis.maplibui.mapui.MapViewOverlays

class BusesOverlay(context: Context, map: MapViewOverlays) : Overlay(context, map) {
    private var items: MutableList<OverlayItem> = arrayListOf()
    private val marker = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_bus_grey600_18dp)

    init {
        items.add(OverlayItem(mMapViewOverlays.map, coordinates[0].x, coordinates[0].y, marker))
        items.add(OverlayItem(mMapViewOverlays.map, coordinates[1].x, coordinates[1].y, marker))
    }

    override fun draw(canvas: Canvas?, mapDrawable: MapDrawable?) {
        for (i in 0 until items.size) {
            items[i].setCoordinatesFromWGS(coordinates[i].x, coordinates[i].y)
            drawOverlayItem(canvas, items[i])
        }
    }

    override fun drawOnPanning(canvas: Canvas?, currentMouseOffset: PointF?) {
        for (item in items) {
            drawOnPanning(canvas, currentMouseOffset, item)
        }
    }

    override fun drawOnZooming(canvas: Canvas?, currentFocusLocation: PointF?, scale: Float) {
        for (item in items) {
            drawOnZooming(canvas, currentFocusLocation, scale, item, false)
        }
    }

    companion object {
        val coordinates = arrayListOf(GeoPoint(131.890756, 43.033386), GeoPoint(131.889368, 43.025636))
    }
}