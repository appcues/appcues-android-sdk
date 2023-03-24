package com.appcues.samples.kotlin.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.R.id
import com.appcues.samples.kotlin.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val menu = binding.navView.menu
        val eventsTab = menu.findItem(id.navigation_events)
        val profileTab = menu.findItem(id.navigation_profile)
        val groupTab = menu.findItem(id.navigation_group)
        MenuItemCompat.setContentDescription(eventsTab, resources.getString(R.string.content_description_tab_events))
        MenuItemCompat.setContentDescription(profileTab, resources.getString(R.string.content_description_tab_profile))
        MenuItemCompat.setContentDescription(groupTab, resources.getString(R.string.content_description_tab_group))

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
                }
            }
        }
    }
}
