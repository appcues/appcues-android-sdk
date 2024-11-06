package com.appcues.samples.kotlin.custom

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.appcues.AppcuesCustomComponentView
import com.appcues.AppcuesExperienceActions

internal class SimpleButtonView(private val context: Context) : AppcuesCustomComponentView {

    override fun getDebugConfig(): Map<String, Any> = mapOf("event_name" to "customEvent1")

    companion object {

        const val PADDING_SIZE = 100
    }

    private var count = 0

    @SuppressLint("SetTextI18n")
    override fun getView(actionsController: AppcuesExperienceActions, config: Map<String, Any>?): ViewGroup {
        return LinearLayout(context).apply {

            setPadding(PADDING_SIZE)
            orientation = LinearLayout.VERTICAL

            addView(
                TextView(context).apply {
                    text = "Custom View that should track event from config map and close the experience"
                }
            )

            addView(
                Button(context).apply {
                    text = "Close"

                    setOnClickListener {
                        val eventName = config?.get("event_name") as String?
                        eventName?.let { actionsController.track(eventName, mapOf("count" to count++)) }
                        actionsController.updateProfile(mapOf("custom_component" to "Button Closed Clicked"))

                        actionsController.close(markComplete = true)
                    }
                }
            )
        }
    }
}
