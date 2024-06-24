package com.appcues.samples.kotlin

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import com.appcues.Appcues
import com.appcues.AppcuesCustomComponentView
import com.appcues.AppcuesExperienceActions
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

        Appcues.registerCustomComponent("setTheme", SetThemeView(this@ExampleApplication))
    }

    private class SetThemeView(private val context: Context) : AppcuesCustomComponentView {
        companion object {

            const val SET_NEW_VALUE_DELAY = 300L
        }
        
        @SuppressLint("SetTextI18n")
        override fun getView(actionsController: AppcuesExperienceActions, config: Map<String, Any>?): ViewGroup {
            return LinearLayout(context).apply {
                addView(
                    Button(context).apply {
                        text = "Dark Mode"

                        setOnClickListener {
                            actionsController.close()

                            val mainHandler = Handler(Looper.getMainLooper())

                            mainHandler.postDelayed({
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            }, SET_NEW_VALUE_DELAY)
                        }
                    }
                )

                addView(
                    Button(context).apply {
                        text = "Light Mode"

                        setOnClickListener {
                            actionsController.close()

                            val mainHandler = Handler(Looper.getMainLooper())

                            mainHandler.postDelayed({
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            }, SET_NEW_VALUE_DELAY)
                        }
                    }
                )
            }
        }
    }
}
