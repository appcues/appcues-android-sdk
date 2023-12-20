package com.appcues.samples.kotlin

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.appcues.Appcues
import com.appcues.LoggingLevel
import com.appcues.NavigationHandler

class ExampleApplication : Application() {

    companion object {

        lateinit var appcues: Appcues
        var currentUserID = "default-0000"
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )

            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        appcues = Appcues(this, BuildConfig.APPCUES_ACCOUNT_ID, BuildConfig.APPCUES_APPLICATION_ID) {
            apiBasePath = "https://api-main.staging.us-west-2.aws.appcues.net/"
            loggingLevel = if (BuildConfig.DEBUG) LoggingLevel.DEBUG else LoggingLevel.INFO
            navigationHandler = object : NavigationHandler {
                // This is an example where we're processing navigation requests coming from Appcues experiences,
                // but simply forwarding them on to start an Activity from an Intent with the given Uri. If an application
                // had a more sophisticated deep linking mechanism and needed finer control over reporting the completion
                // of link navigation - this is where the SDK would allow hooking in and supplying that.
                override suspend fun navigateTo(uri: Uri): Boolean {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = uri
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }.also {
                        startActivity(it)
                    }
                    return true // navigation successful
                }
            }
        }
    }
}
