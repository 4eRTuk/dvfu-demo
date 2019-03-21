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
import android.widget.TextView



class CafeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cafe)
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
            dialog.dismiss()
        }
        call.setOnClickListener {
            dialog.dismiss()
        }
        route.setOnClickListener {
            dialog.dismiss()
        }
        order.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}