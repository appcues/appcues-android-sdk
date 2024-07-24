package com.appcues.samples.kotlin.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.R.id
import com.appcues.samples.kotlin.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

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

        handleLinkIntent(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController(id.nav_host_fragment_activity_example).navigate(id.navigation_events)
            findNavController(id.nav_host_fragment_activity_example).clearBackStack(id.navigation_recycler_view)
            return true
        }

        return super.onOptionsItemSelected(item)
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

                data?.getQueryParameter("experience")?.let {
                    lifecycleScope.launch {
                        ExampleApplication.appcues.show(it)
                    }
                }
            }
        }
    }
}
