package com.appcues.samples.kotlin

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.appcues.Appcues
import com.appcues.LoggingLevel

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
            loggingLevel = if (BuildConfig.DEBUG) LoggingLevel.DEBUG else LoggingLevel.INFO
        }
    }
}
