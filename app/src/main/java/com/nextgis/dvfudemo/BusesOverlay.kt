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
import android.graphics.*
import com.nextgis.maplib.datasource.GeoEnvelope
import com.nextgis.maplib.map.MapDrawable
import com.nextgis.maplib.util.GeoConstants
import com.nextgis.maplibui.api.Overlay
import com.nextgis.maplibui.api.OverlayItem
import com.nextgis.maplibui.mapui.MapViewOverlays

class BusesOverlay(context: Context, map: MapViewOverlays) : Overlay(context, map) {
    var buses: MutableList<Bus> = arrayListOf()
    var items: MutableList<OverlayItem> = arrayListOf()
    val marker = BitmapFactory.decodeResource(mContext.resources, R.drawable.ic_bus_grey600_18dp)

    override fun draw(canvas: Canvas?, mapDrawable: MapDrawable?) {
        for (i in 0 until items.size) {
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

    fun defaultColor() {
        for (item in items) {
            item.marker = marker
        }
    }

    fun selectBus(envelope: GeoEnvelope): Bus? {
        for (i in 0 until items.size) {
            if (envelope.contains(items[i].getCoordinates(GeoConstants.CRS_WEB_MERCATOR))) {
                val marker = marker.copy(Bitmap.Config.ARGB_8888, true)
                items[i].marker = applyColorFilter(marker)
                mMapViewOverlays.postInvalidate()
                return buses[i]
            }
        }
        return null
    }

    private fun applyColorFilter(marker: Bitmap): Bitmap {
        val canvas = Canvas(marker)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val filter = PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP)
        paint.colorFilter = filter
        canvas.drawBitmap(marker, 0f, 0f, paint)
        return marker
    }

}