package com.appcues.samples.kotlin

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            MyApplication.appcues.show("1234")
        }

        findViewById<Button>(R.id.testButton).setOnClickListener {
            Log.i("Appcues", "Test Button Click")
        }
    }
}
