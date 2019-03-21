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

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.nextgis.maplib.api.IGISApplication
import com.nextgis.maplib.util.Constants

class SignInActivity : AppCompatActivity() {
    private var dialog: ProgressDialog? = null
    private var authorized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        findViewById<Button>(R.id.skip).setOnClickListener { load() }
        findViewById<Button>(R.id.signin).setOnClickListener { load(true) }
    }

    private fun load(authorized: Boolean = false) {
        this.authorized = authorized
        dialog = ProgressDialog(this)
        dialog?.isIndeterminate = true
        dialog?.setCancelable(false)
        dialog?.setMessage(getString(R.string.message_loading))
        dialog?.show()

        val fullUrl = MainActivity.FULL_URL
        val accountName = MainActivity.AUTHORITY
        val app = application as? IGISApplication
        app?.addAccount(accountName, fullUrl, Constants.NGW_ACCOUNT_GUEST, null, "ngw")?.let {
            if (!it) {
                Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show()
                dialog?.dismiss()
                return
            } else {
                signin()
            }
        }
    }

    private fun signin() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.edit().putBoolean("authorized", authorized).apply()
        preferences.edit().putBoolean("signed", true).apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}