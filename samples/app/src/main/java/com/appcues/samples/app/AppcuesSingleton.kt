package com.appcues.samples.app

import android.annotation.SuppressLint
import com.appcues.Appcues

object AppcuesSingleton {

    @SuppressLint("StaticFieldLeak")
    lateinit var INSTANCE: Appcues

}