package com.appcues.samples.kotlin.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.appcues.Appcues
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.databinding.FragmentRecyclerviewBinding

class RecyclerViewFragment : Fragment() {

    companion object {

        private const val ITEMS = 10
    }

    class ViewAdapter(val appcues: Appcues) : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val root: View
            val title: TextView
            val image: ImageView
            val button: Button

            init {
                root = view
                title = view.findViewById(R.id.title)
                image = view.findViewById(R.id.image)
                button = view.findViewById(R.id.button)
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.view_adapter_item, viewGroup, false)

            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.root.contentDescription = "item ${position + 1}"
            viewHolder.title.text = "Row ${position + 1}"
            viewHolder.button.text = "Button ${position + 1}"
            viewHolder.button.contentDescription = "Button ${position + 1}"
            viewHolder.button.setOnClickListener { appcues.track("RecyclerView Item ${position + 1}") }
        }

        override fun getItemCount() = ITEMS
    }

    private var _binding: FragmentRecyclerviewBinding? = null

    private val binding get() = _binding!!

    private val appcues = ExampleApplication.appcues

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerviewBinding.inflate(inflater, container, false)

        binding.recyclerView.adapter = ViewAdapter(appcues)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        appcues.screen("RecyclerView")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
