package com.appcues.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred

internal class RequestPermissionActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_PERMISSION_KEY = "EXTRA_PERMISSION"

        // since there can only be one review activity running at a time, this companion level
        // deferred can be used to await completion
        var completion: CompletableDeferred<Boolean>? = null

        fun getIntent(context: Context, permission: String): Intent = Intent(context, RequestPermissionActivity::class.java)
            .apply {
                putExtra(EXTRA_PERMISSION_KEY, permission)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    private val permissionKey by lazy { intent.getStringExtra(EXTRA_PERMISSION_KEY)!! }

    private val permissionLauncher = registerForActivityResult(RequestPermission()) {
        completion?.complete(it)
        finish()
    }

    private var settingsLauncher = registerForActivityResult(StartActivityForResult()) {
        // check permission again and return the completion
        val hasPermission = ContextCompat.checkSelfPermission(this, permissionKey) == PackageManager.PERMISSION_GRANTED
        completion?.complete(hasPermission)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // remove enter animation from this activity
        if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
        super.onCreate(savedInstanceState)

        val hasPermission = ContextCompat.checkSelfPermission(this, permissionKey)

        when {
            // first we check if the permission is already granted
            hasPermission == PackageManager.PERMISSION_GRANTED -> {
                completion?.complete(true)
                finish()
            }
            // POST_NOTIFICATION did not exist before API 33, so instead we direct user to open the settings page
            VERSION.SDK_INT < VERSION_CODES.TIRAMISU &&
                permissionKey == Manifest.permission.POST_NOTIFICATIONS &&
                !NotificationManagerCompat.from(this).areNotificationsEnabled() -> {
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", packageName, null)
                }
                settingsLauncher.launch(intent)
            }
            // Permission is denied, we try to launch the permission launcher,
            // if user has already declined too many times it will instantly return false
            hasPermission == PackageManager.PERMISSION_DENIED -> {
                permissionLauncher.launch(permissionKey)
            }
        }
    }

    override fun finish() {
        super.finish()
        // remove exit animation from this activity
        if (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
