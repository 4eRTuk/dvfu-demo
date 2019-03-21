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

import android.Manifest
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.nextgis.maplib.api.IGISApplication
import com.nextgis.maplib.map.MapDrawable
import com.nextgis.maplib.util.Constants
import com.nextgis.maplib.util.PermissionUtil
import com.nextgis.maplibui.service.LayerFillService

class SignInActivity : AppCompatActivity() {
    private var receiver: BroadcastReceiver? = null
    private var dialog: ProgressDialog? = null
    private var authorized = false
    private var total = LAYERS.size

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        findViewById<Button>(R.id.skip).setOnClickListener { load() }
        findViewById<Button>(R.id.signin).setOnClickListener { load(true) }
    }

    private fun requestPermission() {
        val permissions = arrayOf(Manifest.permission.GET_ACCOUNTS, Manifest.permission.WRITE_SYNC_SETTINGS)
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var granted = requestCode == PERMISSIONS_CODE
        for (result in grantResults)
            if (result != PackageManager.PERMISSION_GRANTED)
                granted = false

        if (granted)
            load(authorized)
        else
            Toast.makeText(this, R.string.error_auth, Toast.LENGTH_SHORT).show()
    }

    private fun load(authorized: Boolean = false) {
        this.authorized = authorized
        if (!PermissionUtil.hasPermission(this, Manifest.permission.WRITE_SYNC_SETTINGS)
            || !PermissionUtil.hasPermission(this, Manifest.permission.GET_ACCOUNTS)) {
            requestPermission()
            return
        }

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
                app.getAccount(accountName)?.let { app.removeAccount(it) }
                dialog?.dismiss()
                return
            } else {
                layers()
            }
        }
    }

    private fun clear() {
        val app = application as? DVFUApplication
        (app?.map as MapDrawable).delete()
        app.initBaseLayers()
    }

    private fun layers() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action?.equals(LayerFillService.ACTION_STOP) == true) {
                    Toast.makeText(this@SignInActivity, R.string.canceled, Toast.LENGTH_SHORT).show()
                    clear()
                    return
                }

                val serviceStatus = intent.getShortExtra(LayerFillService.KEY_STATUS, 0)
                when (serviceStatus) {
                    LayerFillService.STATUS_STOP -> {
                        total--
                        if (total <= 0)
                            signin()
                    }
                }
            }
        }

        val intentFilter = IntentFilter(LayerFillService.ACTION_UPDATE)
        intentFilter.addAction(LayerFillService.ACTION_STOP)
        registerReceiver(receiver, intentFilter)

        val intent = Intent(this, LayerFillService::class.java)
        intent.action = LayerFillService.ACTION_ADD_TASK

        val app = application as? IGISApplication
        val map = app?.map as MapDrawable?
        val accountName = MainActivity.AUTHORITY

        for (layer in LAYERS) {
            val uri = Uri.parse(Uri.decode(layer.first))
            val id = uri.lastPathSegment?.toLongOrNull()
            intent.putExtra(LayerFillService.KEY_REMOTE_ID, id)
            intent.putExtra(LayerFillService.KEY_ACCOUNT, accountName)
            intent.putExtra(LayerFillService.KEY_NAME, layer.second)
            intent.putExtra(LayerFillService.KEY_LAYER_GROUP_ID, map?.id)
            intent.putExtra(LayerFillService.KEY_INPUT_TYPE, LayerFillService.NGW_LAYER)
            intent.putExtra(LayerFillService.KEY_URI, uri)
            ContextCompat.startForegroundService(this, intent)
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

    companion object {
        val LAYERS = arrayListOf(
            Pair("http://dvfu-demo.nextgis.com/resource/4", "Магазины"),
            Pair("http://dvfu-demo.nextgis.com/resource/5", "Вендинговые автоматы"),
            Pair("http://dvfu-demo.nextgis.com/resource/6", "Кафе и рестораны")
        )
        const val PERMISSIONS_CODE = 47
    }
}