package com.appcues.samples.kotlin.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.MenuItemCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.appcues.AnalyticType
import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticsListener
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.R.id
import com.appcues.samples.kotlin.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    private val requestPermissionLauncher =
        registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->

            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                ExampleApplication.appcues.identify(
                    ExampleApplication.currentUserID,
                    mapOf(
                        "pushStatus" to "authorized",
                        "showPermissionRationale" to false
                    )
                )
            } else {
                val shouldShowRationale = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    false
                }
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                ExampleApplication.appcues.identify(
                    ExampleApplication.currentUserID,
                    mapOf(
                        "pushStatus" to "denied",
                        "showPermissionRationale" to false // don't show until at least next activity launch
                    )
                )
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(id.nav_host_fragment_activity_example)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                id.navigation_events,
                id.navigation_profile,
                id.navigation_group,
                id.navigation_embed
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val menu = binding.navView.menu
        val eventsTab = menu.findItem(id.navigation_events)
        val profileTab = menu.findItem(id.navigation_profile)
        val groupTab = menu.findItem(id.navigation_group)
        val embedTab = menu.findItem(id.navigation_embed)
        MenuItemCompat.setContentDescription(eventsTab, resources.getString(R.string.content_description_tab_events))
        MenuItemCompat.setContentDescription(profileTab, resources.getString(R.string.content_description_tab_profile))
        MenuItemCompat.setContentDescription(groupTab, resources.getString(R.string.content_description_tab_group))
        MenuItemCompat.setContentDescription(embedTab, resources.getString(R.string.content_description_tab_group))

        ExampleApplication.appcues.analyticsListener = object: AnalyticsListener {
            override fun trackedAnalytic(type: AnalyticType, value: String?, properties: Map<String, Any>?, isInternal: Boolean) {
                if (type == EVENT && value == "request_push") {
                    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }

        val notificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        val shouldShowRationale = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            false
        }

        if (!notificationsEnabled && !shouldShowRationale) {
            // ask immediately
            if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        // otherwise, if shouldShowRational, a flow will handle it

        handleLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleLinkIntent(intent)
    }

    private fun handleLinkIntent(intent: Intent?) {
        val appcuesHandled = ExampleApplication.appcues.onNewIntent(this, intent)

        if (!appcuesHandled) {
            val action: String? = intent?.action
            val data: Uri? = intent?.data

            if (action == Intent.ACTION_VIEW) {
                when (data?.host) {
                    "events" -> binding.navView.selectedItemId = id.navigation_events
                    "profile" -> binding.navView.selectedItemId = id.navigation_profile
                    "group" -> binding.navView.selectedItemId = id.navigation_group
                    "embed" -> binding.navView.selectedItemId = id.navigation_embed
                }
            }
        }
    }
}
