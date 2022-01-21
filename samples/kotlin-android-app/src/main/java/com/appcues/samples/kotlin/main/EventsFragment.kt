package com.appcues.samples.kotlin.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.databinding.FragmentEventsBinding

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

        binding.buttonSampleModal.setOnClickListener {
            appcues.show("1234")
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        appcues.screen(getString(R.string.fragment_events))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
