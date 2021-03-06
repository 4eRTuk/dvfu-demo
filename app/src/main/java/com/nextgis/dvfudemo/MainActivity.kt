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

import android.accounts.AccountManager
import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.nextgis.maplib.api.IGISApplication
import com.nextgis.maplib.api.ILayerView
import com.nextgis.maplib.datasource.GeoEnvelope
import com.nextgis.maplib.datasource.GeoPoint
import com.nextgis.maplib.map.Layer
import com.nextgis.maplib.map.MapDrawable
import com.nextgis.maplib.map.VectorLayer
import com.nextgis.maplib.util.GeoConstants
import com.nextgis.maplibui.api.MapViewEventListener
import com.nextgis.maplibui.api.OverlayItem
import com.nextgis.maplibui.fragment.NGWSettingsFragment
import com.nextgis.maplibui.mapui.MapViewOverlays
import com.nextgis.maplibui.mapui.NGWVectorLayerUI
import com.nextgis.maplibui.util.ConstantsUI
import com.nextgis.maplibui.util.SettingsConstantsUI


class MainActivity : AppCompatActivity(), MapViewEventListener {
    private var map: MapViewOverlays? = null
    private lateinit var selectedOverlay: SelectFeatureOverlay
    private lateinit var busesOverlay: BusesOverlay
    private var preferences: SharedPreferences? = null
    private var authorized = false
    private var receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            map?.map?.let {
                val array = intent.getParcelableArrayListExtra<Bus>("buses")
                val marker = busesOverlay.marker
                busesOverlay.items.clear()
                for (bus in array) {
                    bus.location?.let { coordinates ->
                        busesOverlay.buses.add(bus)
                        busesOverlay.items.add(OverlayItem(it, 0.0, 0.0, marker))
                        busesOverlay.items.last().setCoordinatesFromWGS(coordinates.longitude, coordinates.latitude)
                    }
                }
                map?.postInvalidate()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val signed = preferences!!.getBoolean("signed", false)
        if (!signed) {
            signin()
            return
        }

        authorized = preferences!!.getBoolean("authorized", false)
        val app = application as? IGISApplication
        map = MapViewOverlays(this, app?.map as MapDrawable?)
        map?.id = R.id.map
        selectedOverlay = SelectFeatureOverlay(this, map!!)
        busesOverlay = BusesOverlay(this, map!!)
        map?.addOverlay(selectedOverlay)
        map?.addOverlay(busesOverlay)
        if (!authorized) {
            (map?.map?.getLayerByName(SignInActivity.LAYERS[2].second) as? VectorLayer)?.let {
                it.isVisible = false
            }
            busesOverlay.setVisibility(false)
        }

        val container = findViewById<FrameLayout>(R.id.map)
        container.addView(map)
        setCenter()

        findViewById<ImageButton>(R.id.close).setOnClickListener {
            findViewById<View>(R.id.info).visibility = View.GONE
            selectedOverlay.feature = null
            busesOverlay.defaultColor()
        }

        sync()
    }

    private fun sync() {
        AccountManager.get(this)?.let { manager ->
            (application as? IGISApplication)?.let { app ->
                manager.getAccountsByType(app.accountsType).firstOrNull()?.let { account ->
                    val syncEnabled = NGWSettingsFragment.isAccountSyncEnabled(account, app.authority)
                    if (syncEnabled) {
                        val settings = Bundle()
                        settings.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                        settings.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                        ContentResolver.requestSync(account, app.authority, settings)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val container = findViewById<FrameLayout>(R.id.map)
        container.removeView(map)
    }

    private fun setCenter() {
        map?.let {
            val mapZoom = preferences?.getFloat(SettingsConstantsUI.KEY_PREF_ZOOM_LEVEL, it.minZoom)
            var mapScrollX: Double
            var mapScrollY: Double
            try {
                val x = preferences?.getLong(SettingsConstantsUI.KEY_PREF_SCROLL_X, 0)
                val y = preferences?.getLong(SettingsConstantsUI.KEY_PREF_SCROLL_Y, 0)
                mapScrollX = java.lang.Double.longBitsToDouble(x ?: 0)
                mapScrollY = java.lang.Double.longBitsToDouble(y ?: 0)
            } catch (e: ClassCastException) {
                mapScrollX = 0.0
                mapScrollY = 0.0
            }

            it.setZoomAndCenter(mapZoom ?: it.minZoom, GeoPoint(mapScrollX, mapScrollY))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_signout -> {
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                preferences.edit().remove("signed").remove("authorized").apply()
                val app = application as? IGISApplication
                app?.getAccount(AUTHORITY)?.let { app.removeAccount(it) }
                (app?.map as MapDrawable).delete()
                signin()
                true
            }
            R.id.action_layers -> {
                val app = application as? IGISApplication
                val map = app?.map as MapDrawable?
                val layers = arrayOf("Магазины и автоматы", "Кафе и рестораны", "Автобусы")
                val checked = BooleanArray(layers.size)
                (map?.getLayerByName(SignInActivity.LAYERS[0].second) as Layer).let { checked[0] = it.isVisible }
                (map.getLayerByName(SignInActivity.LAYERS[2].second) as Layer).let { checked[1] = it.isVisible }
                checked[2] = busesOverlay.isVisible
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.track_list)
                    .setMultiChoiceItems(layers, checked) { _, which, selected -> checked[which] = selected }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        (map.getLayerByName(SignInActivity.LAYERS[0].second) as Layer).let { it.isVisible = checked[0] }
                        (map.getLayerByName(SignInActivity.LAYERS[1].second) as Layer).let { it.isVisible = checked[0] }
                        (map.getLayerByName(SignInActivity.LAYERS[2].second) as Layer).let { it.isVisible = checked[1] }

                        busesOverlay.setVisibility(checked[2])
                        if (!busesOverlay.isVisible) {
                            stopBusesService()
                        } else {
                            startBusesService()
                        }
                    }
                if (!authorized) {
                    builder.setNegativeButton("Войти", null)
                }
                val dialog = builder.create()
                dialog.show()
                if (!authorized) {
                    dialog.listView.isEnabled = false
                }
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

    override fun onStart() {
        super.onStart()
        map?.addListener(this)

        startBusesService()
    }

    private fun startBusesService() {
        if (busesOverlay.isVisible) {
            val intentFilter = IntentFilter("BUSES_UPDATE")
            registerReceiver(receiver, intentFilter)
            val intent = Intent(this, BusesService::class.java)
            startService(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        map?.let {
            it.removeListener(this)
            val point = it.mapCenter
            preferences?.edit()?.putFloat(SettingsConstantsUI.KEY_PREF_ZOOM_LEVEL, it.zoomLevel)
                ?.putLong(SettingsConstantsUI.KEY_PREF_SCROLL_X, java.lang.Double.doubleToRawLongBits(point.x))
                ?.putLong(SettingsConstantsUI.KEY_PREF_SCROLL_Y, java.lang.Double.doubleToRawLongBits(point.y))
                ?.apply()
        }

        stopBusesService()
    }

    private fun stopBusesService() {
        val intent = Intent(this, BusesService::class.java)
        stopService(intent)

        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
        }
    }

    override fun onLayersReordered() {

    }

    override fun onLayerDrawFinished(id: Int, percent: Float) {

    }

    override fun onSingleTapUp(event: MotionEvent?) {
        event?.let {
            val tolerance = resources.displayMetrics.density * ConstantsUI.TOLERANCE_DP.toDouble()
            val dMinX = event.x - tolerance
            val dMaxX = event.x + tolerance
            val dMinY = event.y - tolerance
            val dMaxY = event.y + tolerance
            val mapEnv = map?.screenToMap(GeoEnvelope(dMinX, dMaxX, dMinY, dMaxY)) ?: return

            if (busesOverlay.isVisible) {
                busesOverlay.selectBus(mapEnv)?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        findViewById<ImageView>(R.id.avatar).imageTintList = ColorStateList.valueOf(BUS_COLOR)
                    }

                    findViewById<View>(R.id.people).visibility = View.VISIBLE
                    findViewById<ProgressBar>(R.id.people).progress = (it.congestion * 100).toInt()
                    findViewById<TextView>(R.id.title).text = it.title
                    findViewById<TextView>(R.id.category).text = ""
                    findViewById<TextView>(R.id.description).text = "Остановки:\n#1\n#2\n...\nЗаполненность:"
                    findViewById<View>(R.id.info).visibility = View.VISIBLE

                    map?.let { map ->
                        it.location?.let { location ->
                            val center = GeoPoint(location.longitude, location.latitude)
                            center.crs = GeoConstants.CRS_WGS84
                            center.project(GeoConstants.CRS_WEB_MERCATOR)
                            center.y -= 1000
                            map.setZoomAndCenter(map.zoomLevel, center)
                        }
                    }
                    return
                }
            }

            val types = GeoConstants.GTPointCheck
            map?.getVectorLayersByType(types)?.let { layers ->
                var items: List<Long>? = null
                var selectedLayer: NGWVectorLayerUI? = null
                for (layer in layers) {
                    if (!layer.isValid || layer is ILayerView && !layer.isVisible)
                        continue

                    items = (layer as NGWVectorLayerUI).query(mapEnv)
                    if (!items.isEmpty()) {
                        selectedLayer = layer
                        break
                    }
                }

                selectedLayer?.let {
                    for (i in items!!.indices) {
                        val feature = selectedLayer.getFeature(items[i])
                        feature?.let {
                            if (selectedLayer.name == SignInActivity.LAYERS[2].second) {
                                openCafe(selectedLayer.id, feature.id)
                                return
                            }
                            it.geometry?.let { selectedOverlay.feature = feature }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                val cat = feature.getFieldValueAsInteger("category_id")
                                when (cat) {
                                    1 -> findViewById<ImageView>(R.id.avatar).imageTintList =
                                        ColorStateList.valueOf(GROCERY_COLOR)
                                    2 -> findViewById<ImageView>(R.id.avatar).imageTintList =
                                        ColorStateList.valueOf(SUPERMARKET_COLOR)
                                    3 -> findViewById<ImageView>(R.id.avatar).imageTintList =
                                        ColorStateList.valueOf(PHARMACY_COLOR)
                                    else -> findViewById<ImageView>(R.id.avatar).imageTintList =
                                        ColorStateList.valueOf(VENDING_COLOR)
                                }
                            }

                            findViewById<View>(R.id.people).visibility = View.GONE
                            findViewById<TextView>(R.id.title).text = feature.getFieldValueAsString("title")
                            findViewById<TextView>(R.id.category).text = feature.getFieldValueAsString("category")
                            findViewById<TextView>(R.id.description).text = feature.getFieldValueAsString("description")
                        }
                    }

                    selectedOverlay.feature?.let {
                        findViewById<View>(R.id.info).visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun openCafe(layerId: Int, featureId: Long) {
        val intent = Intent(this, CafeActivity::class.java)
        intent.putExtra("layer_id", layerId)
        intent.putExtra("feature_id", featureId)
        startActivity(intent)
    }

    override fun onLayerAdded(id: Int) {

    }

    override fun onLayerDeleted(id: Int) {

    }

    override fun onLayerChanged(id: Int) {

    }

    override fun onExtentChanged(zoom: Float, center: GeoPoint?) {

    }

    override fun onLayerDrawStarted() {

    }

    override fun onLongPress(event: MotionEvent?) {

    }

    override fun panStart(e: MotionEvent?) {

    }

    override fun panMoveTo(e: MotionEvent?) {

    }

    override fun panStop() {

    }

    companion object {
        const val AUTHORITY = "dvfu-demo.nextgis.com"
        const val FULL_URL = "http://$AUTHORITY"
        val CAFE_COLOR = Color.parseColor("#76B952")
        val GROCERY_COLOR = Color.parseColor("#9A23BB")
        val SUPERMARKET_COLOR = Color.parseColor("#BA1290")
        val PHARMACY_COLOR = Color.parseColor("#B20023")
        val VENDING_COLOR = Color.parseColor("#542BAD")
        val BUS_COLOR = Color.parseColor("#51D3FF")
    }
}
