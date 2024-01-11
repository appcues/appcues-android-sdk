package com.appcues.samples.kotlin.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.databinding.FragmentEventsBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val appcues = ExampleApplication.appcues

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)

        binding.buttonEvent1.setOnClickListener {
            appcues.track("event1")
        }

        binding.buttonEvent2.setOnClickListener {
            appcues.track("event2")
        }

        binding.buttonEvent3.setOnClickListener {
            appcues.track("event3")
        }

        binding.recyclerViewButton.setOnClickListener {
            requireActivity()
                .findNavController(R.id.nav_host_fragment_activity_example)
                .navigate(R.id.action_from_events_to_recycler_view)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.events_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.events_menu_debug -> {
                            appcues.debug(requireActivity())
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onResume() {
        super.onResume()
        binding.buttonEvent3.isVisible = false
        appcues.screen("Trigger Events")

        lifecycleScope.launch {
            @Suppress("MagicNumber")
            delay(2_000)
            // the view may have been destroyed during the 2 seconds
            // so do a null safe check on the view binding
            _binding?.let { it.buttonEvent3.isVisible = true }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
