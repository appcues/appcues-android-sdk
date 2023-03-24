package com.appcues.samples.kotlin.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.databinding.FragmentProfileBinding
import com.appcues.samples.kotlin.signin.SignInActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val appcues = ExampleApplication.appcues

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonSaveProfile.setOnClickListener {
            val properties: HashMap<String, Any> = hashMapOf()

            val givenName = binding.editTextGivenName.text.toString()
            if (givenName.isNotEmpty()) {
                properties["given_name"] = givenName
            }

            val familyName = binding.editTextFamilyName.text.toString()
            if (familyName.isNotEmpty()) {
                properties["family_name"] = familyName
            }

            appcues.identify(ExampleApplication.currentUserID, properties)

            binding.editTextGivenName.text = null
            binding.editTextFamilyName.text = null
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.profile_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.sign_out -> {
                            appcues.reset()
                            ExampleApplication.currentUserID = ""
                            val intent = Intent(activity, SignInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                            startActivity(intent)
                            activity?.finish()
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
        appcues.screen("Update Profile")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
