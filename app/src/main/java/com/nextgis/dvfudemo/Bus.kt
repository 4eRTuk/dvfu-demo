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

import android.location.Location
import android.os.Parcel
import android.os.Parcelable

class Bus(val title: String?, val location: Location?, val congestion: Double) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString(), parcel.readParcelable<Location>(Location::class.java.classLoader), parcel.readDouble())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeParcelable(location, flags)
        dest.writeDouble(congestion)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Bus> {
        override fun createFromParcel(parcel: Parcel): Bus{
            return Bus(parcel)
        }

        override fun newArray(size: Int): Array<Bus?> {
            return arrayOfNulls(size)
        }
    }
}