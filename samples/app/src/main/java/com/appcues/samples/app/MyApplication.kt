package com.appcues.samples.app

import android.app.Application
import com.appcues.Appcues
import java.lang.ref.WeakReference

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val appcues = Appcues.Builder(
            context = this,
            accountId = "",
            applicationId = ""
        ).logging(Appcues.LoggingLevel.BASIC)
            .build()

        AppcuesSingleton.INSTANCE = appcues
    }
}