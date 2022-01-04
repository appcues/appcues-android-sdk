package com.appcues.samples.kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.appcues.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MyApplication.appcues.show("1234")
    }
}