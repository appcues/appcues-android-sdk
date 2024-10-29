package com.appcues.samples.kotlin.custom

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.setPadding
import com.appcues.AppcuesCustomComponentView
import com.appcues.AppcuesExperienceActions

internal class SetThemeView(private val context: Context) : AppcuesCustomComponentView {

    override val debugConfig: Map<String, Any>
        get() = mapOf()

    companion object {

        const val SET_NEW_VALUE_DELAY = 300L
        const val PADDING_SIZE = 100
    }

    @SuppressLint("SetTextI18n")
    override fun getView(actionsController: AppcuesExperienceActions, config: Map<String, Any>?): ViewGroup {
        return LinearLayout(context).apply {

            setPadding(PADDING_SIZE)

            addView(
                Button(context).apply {
                    text = "Dark Mode"

                    setOnClickListener {
                        actionsController.triggerBlockActions()
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
