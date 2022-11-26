package com.example.easyparknamur

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import com.example.easyparknamur.databinding.ActivityMainBinding
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.core.permissions.PermissionsListener

class MainActivity : AppCompatActivity(), PermissionsListener {

    private val permissionsManager = PermissionsManager(this)
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.Gps.setOnClickListener{startActivity(Intent(this, GpsActivity::class.java)) }
//        binding.appBarMain.fab.setOnClickListener { startActivity(Intent(this, GpsActivity::class.java)) }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            this,
            "This app needs location and storage permissions in order to show its functionality.",
            Toast.LENGTH_LONG
        ).show()
    }


    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            requestOptionalPermissions()
        } else {
            Toast.makeText(
                this,
                "You didn't grant the permissions required to use the app",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestOptionalPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        // starting from Android R leak canary writes to Download storage without the permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS") !=
            PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add("android.permission.POST_NOTIFICATIONS")
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                10
            )
        }
    }
    private fun isMapboxTokenProvided() =
        getString(R.string.mapbox_access_token) != MAPBOX_ACCESS_TOKEN_PLACEHOLDER

    private fun showNoTokenErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Error: no mapbox access token")
            .setMessage("Please put your mapbox access token to res/values/mapbox_access_token.xml")
            .setCancelable(false)
            .setPositiveButton("Ok") { _, _ ->
                finish()
            }
            .show()
    }
}
private const val MAPBOX_ACCESS_TOKEN_PLACEHOLDER = "YOUR_MAPBOX_ACCESS_TOKEN_GOES_HERE"