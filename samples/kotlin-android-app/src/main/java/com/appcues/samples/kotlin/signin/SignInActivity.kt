package com.appcues.samples.kotlin.signin

import android.Manifest
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R
import com.appcues.samples.kotlin.R.id
import com.appcues.samples.kotlin.R.string
import com.appcues.samples.kotlin.databinding.ActivitySigninBinding
import com.appcues.samples.kotlin.main.MainActivity

class SignInActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySigninBinding.inflate(layoutInflater) }
    private val appcues = ExampleApplication.appcues

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setTitle(string.title_activity_signin)

        setContentView(binding.root)

        binding.editTextUserName.setText(ExampleApplication.currentUserID)

        binding.buttonSignIn.setOnClickListener {
            val userInput = binding.editTextUserName.text.toString()
            val userID = if (userInput.isNotEmpty()) userInput else ExampleApplication.currentUserID

            val pushStatus = if (NotificationManagerCompat.from(this).areNotificationsEnabled()) "authorized" else "denied"
            val shouldShowRationale = if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)
            } else {
                false
            }

            appcues.identify(
                userID,
                mapOf(
                    "showPermissionRationale" to shouldShowRationale,
                    "pushStatus" to pushStatus,
                )
            )

            ExampleApplication.currentUserID = userID
            completeSignIn()
        }

        binding.buttonAnonymous.setOnClickListener {
            appcues.anonymous()
            completeSignIn()
        }
    }

    override fun onResume() {
        super.onResume()
        appcues.screen("Sign In")
    }

    private fun completeSignIn() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.sign_in_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            id.skip -> {
                completeSignIn()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
