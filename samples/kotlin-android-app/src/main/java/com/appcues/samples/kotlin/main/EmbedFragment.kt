package com.appcues.samples.kotlin.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.databinding.FragmentEmbedBinding

class EmbedFragment : Fragment() {

    private var _binding: FragmentEmbedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val appcues = ExampleApplication.appcues

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmbedBinding.inflate(inflater, container, false)

        appcues.registerEmbed("frame1", binding.embedFrame1)
        appcues.registerEmbed("frame2", binding.embedFrame2)
        appcues.registerEmbed("frame3", binding.embedFrame3)
        appcues.registerEmbed("frame4", binding.embedFrame4)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        appcues.screen("Embed Harness")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
