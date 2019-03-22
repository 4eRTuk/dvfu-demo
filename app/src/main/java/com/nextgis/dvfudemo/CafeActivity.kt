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

import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.nextgis.maplib.api.IGISApplication
import com.nextgis.maplib.map.MapDrawable
import com.nextgis.maplib.map.VectorLayer
import com.nextgis.maplibui.util.LayerUtil
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.LinearLayoutManager
import android.view.View


class CafeActivity : AppCompatActivity() {
    private var phone: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cafe)

        val layerId = intent.getIntExtra("layer_id", -1)
        val featureId = intent.getLongExtra("feature_id", -1)
        val app = application as? IGISApplication
        ((app?.map as MapDrawable?)?.getLayerByName(SignInActivity.LAYERS[3].second) as? VectorLayer)?.let {
            try {
                val features = it.query(null, "facility_id = ?", arrayOf("$featureId"), null, null)
                val items = arrayListOf<Review>()
                if (features != null) {
                    if (features.moveToFirst()) {
                        val name = features.getColumnIndex("name")
                        val date = features.getColumnIndex("date")
                        val review = features.getColumnIndex("review")
                        do {
                            items.add(
                                Review(
                                    features.getString(name),
                                    features.getString(date),
                                    features.getString(review)
                                )
                            )
                        } while (features.moveToNext())

                        features.close()
                    }
                }
                findViewById<RecyclerView>(R.id.reviews).adapter = ReviewAdapter(items)
                findViewById<RecyclerView>(R.id.reviews).layoutManager = LinearLayoutManager(this)
            } catch (e: Exception) {
            }
        }

        ((app?.map as MapDrawable?)?.getLayerById(layerId) as? VectorLayer)?.let {
            it.getFeature(featureId)?.let { feature ->
                title = feature.getFieldValueAsString("title")
                phone = feature.getFieldValueAsString("phone")
                supportActionBar?.subtitle = feature.getFieldValueAsString("category")
                findViewById<TextView>(R.id.description).text = feature.getFieldValueAsString("description")
                findViewById<TextView>(R.id.menu).text = feature.getFieldValueAsString("menu")
            }
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { dialog() }
    }

    private fun dialog() {
        val view = layoutInflater.inflate(R.layout.action_sheet, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        val review = view.findViewById<TextView>(R.id.review)
        val call = view.findViewById<TextView>(R.id.call)
        val route = view.findViewById<TextView>(R.id.route)
        val order = view.findViewById<TextView>(R.id.order)
        review.setOnClickListener {
            showForm(3)
            dialog.dismiss()
        }
        if (phone.isBlank())
            call.visibility = View.GONE
        call.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:$phone")
            startActivity(callIntent)
            dialog.dismiss()
        }
        route.setOnClickListener {
            dialog.dismiss()
        }
        order.setOnClickListener {
            showForm(4)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showForm(id: Int) {
        val app = application as? IGISApplication
        val orders = (app?.map as MapDrawable?)?.getLayerByName(SignInActivity.LAYERS[id].second)
        LayerUtil.showEditForm(orders as VectorLayer?, this, -1L, null)
    }
}