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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.view.View


open class ReviewAdapter(private var items: List<Review>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        (holder as ViewHolder).name?.text = item.name
        holder.date?.text = item.date
        holder.review?.text = item.review
    }

    override fun getItemCount(): Int = items.size

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var name: TextView? = null
        var date: TextView? = null
        var review: TextView? = null

        init {
            name = view.findViewById(R.id.name) as TextView
            date = view.findViewById(R.id.date) as TextView
            review = view.findViewById(R.id.review) as TextView
        }
    }
}