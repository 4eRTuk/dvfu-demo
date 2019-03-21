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
import android.graphics.Canvas
import android.graphics.PointF
import com.nextgis.maplib.datasource.Feature
import com.nextgis.maplib.datasource.GeoGeometry
import com.nextgis.maplib.datasource.GeoMultiPoint
import com.nextgis.maplib.datasource.GeoPoint
import com.nextgis.maplib.map.MapDrawable
import com.nextgis.maplib.util.Constants
import com.nextgis.maplib.util.GeoConstants
import com.nextgis.maplibui.api.DrawItem
import com.nextgis.maplibui.api.Overlay
import com.nextgis.maplibui.api.VertexStyle
import com.nextgis.maplibui.mapui.MapViewOverlays
import com.nextgis.maplibui.util.ControlHelper

class SelectFeatureOverlay(context: Context, map: MapViewOverlays) : Overlay(context, map) {
    private var items: MutableList<DrawItem> = arrayListOf()
    private var selectedItem: DrawItem? = null
    var feature: Feature? = null
        set(value) {
            field = value
            mMapViewOverlays.postInvalidate()
        }

    init {
        val outlineColor = ControlHelper.getColor(mContext, R.attr.colorAccent)
        val fillColor = ControlHelper.getColor(mContext, R.attr.colorPrimary)
        val vertexStyle = VertexStyle(mContext, 255, fillColor, 5f, 2.6f,
            fillColor, 5f, 2.6f, outlineColor, 6f, 3f)
        DrawItem.setVertexStyle(vertexStyle)
    }

    override fun draw(canvas: Canvas?, mapDrawable: MapDrawable?) {
        feature?.let {
            fillDrawItems(it.geometry)

            canvas?.let {
                for (item in items) {
                    val isSelected = selectedItem === item
                    drawItem(item, canvas, isSelected)
                }
            }
        }
    }

    override fun drawOnPanning(canvas: Canvas?, currentMouseOffset: PointF?) {
        canvas?.let {
            for (item in items) {
                val isSelected = selectedItem === item
                val newItem = item.pan(currentMouseOffset)
                if (isSelected) {
                    newItem.setSelectedRing(selectedItem!!.selectedRingId)
                    newItem.setSelectedPoint(selectedItem!!.selectedPointId)
                }

                drawItem(newItem, canvas, isSelected)
            }
        }
    }

    override fun drawOnZooming(canvas: Canvas?, currentFocusLocation: PointF?, scale: Float) {
        canvas?.let {
            for (item in items) {
                val isSelected = selectedItem === item
                val newItem = item.zoom(currentFocusLocation, scale)

                if (isSelected) {
                    newItem.setSelectedRing(selectedItem!!.selectedRingId)
                    newItem.setSelectedPoint(selectedItem!!.selectedPointId)
                }

                drawItem(newItem, canvas, isSelected)
            }
        }
    }

    private fun fillDrawItems(geom: GeoGeometry?) {
        val lastItemsCount = items.size
        val lastSelectedItemPosition = items.indexOf(selectedItem)
        val lastSelectedItem = selectedItem
        items.clear()

        if (null == geom) {
            return
        }

        val geoPoints = arrayOfNulls<GeoPoint>(1)
        when (geom.type) {
            GeoConstants.GTPoint -> {
                geoPoints[0] = geom as GeoPoint?
                selectedItem = DrawItem(DrawItem.TYPE_VERTEX, mMapViewOverlays.map.mapToScreen(geoPoints))
                items.add(selectedItem!!)
            }
            GeoConstants.GTMultiPoint -> {
                val geoMultiPoint = geom as GeoMultiPoint?
                for (i in 0 until geoMultiPoint!!.size()) {
                    geoPoints[0] = geoMultiPoint.get(i)
                    selectedItem = DrawItem(DrawItem.TYPE_VERTEX, mMapViewOverlays.map.mapToScreen(geoPoints))
                    items.add(selectedItem!!)
                }
            }
            GeoConstants.GTLineString -> {
            }
            GeoConstants.GTMultiLineString -> {
            }
            GeoConstants.GTPolygon -> {
            }
            GeoConstants.GTMultiPolygon -> {
            }
            GeoConstants.GTGeometryCollection -> {
            }
            else -> {
            }
        }

        if (items.size == lastItemsCount && lastSelectedItem != null && lastSelectedItemPosition != Constants.NOT_FOUND) {
            selectedItem = items[lastSelectedItemPosition]
            selectedItem!!.setSelectedRing(lastSelectedItem.selectedRingId)
            selectedItem!!.setSelectedPoint(lastSelectedItem.selectedPointId)
        } else {
            selectedItem = items[0]
        }
    }

    private fun drawItem(drawItem: DrawItem, canvas: Canvas, isSelected: Boolean) {
        when (feature?.geometry?.type) {
            GeoConstants.GTPoint, GeoConstants.GTMultiPoint -> drawItem.drawPoints(canvas, isSelected)
            GeoConstants.GTLineString, GeoConstants.GTMultiLineString, GeoConstants.GTPolygon, GeoConstants.GTMultiPolygon -> {
            }
            else -> {
            }
        }
    }

}