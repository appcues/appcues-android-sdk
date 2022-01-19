package com.appcues.samples.kotlin.signin

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.appcues.samples.kotlin.ExampleApplication
import com.appcues.samples.kotlin.R.id
import com.appcues.samples.kotlin.R.string
import com.appcues.samples.kotlin.databinding.ActivitySigninBinding
import com.appcues.samples.kotlin.main.MainActivity

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setTitle(string.title_activity_signin)

        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editTextUserName.setText(ExampleApplication.currentUserID)

        binding.buttonSignIn.setOnClickListener {
            val userID = findViewById<EditText>(id.editTextUserName).text.toString()
            ExampleApplication.currentUserID = userID
            ExampleApplication.appcues.identify(userID)

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
            startActivity(intent)
            finish()
        }
    }
}
