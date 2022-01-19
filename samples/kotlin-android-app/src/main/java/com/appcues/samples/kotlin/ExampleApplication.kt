package com.appcues.samples.kotlin

import android.app.Application
import com.appcues.Appcues

class ExampleApplication : Application() {

    companion object {
        lateinit var appcues: Appcues
        var currentUserID = "default-0000"
    }

    override fun onCreate() {
        super.onCreate()

        appcues = Appcues.Builder(this, BuildConfig.APPCUES_ACCOUNT_ID, BuildConfig.APPCUES_APPLICATION_ID)
            .logging(Appcues.LoggingLevel.BASIC)
            .build()
    }
}
