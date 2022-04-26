package com.appcues.action.appcues

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault

internal class LinkAction(
    override val config: AppcuesConfigMap,
    private val context: Context,
) : ExperienceAction {

    companion object {
        const val TYPE = "@appcues/link"
    }

    private val url: String? = config.getConfig("url")
    private val openExternally = config.getConfigOrDefault("openExternally", false)

    override suspend fun execute(appcues: Appcues) {
        // start web activity if url is not null
        if (url != null) {
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.lowercase()
            if (scheme != null) {
                // only HTTP or HTTPS URLs are eligible for Chrome Custom Tabs in-app browser
                if (!openExternally && (scheme == "http" || scheme == "https")) {
                    openCustomTabs(uri)
                } else {
                    // this will handle any in-app deep link scheme URLs OR any web urls that were
                    // requested to open into the external browser application
                    startNewIntent(uri)
                }
            }
        }
    }

    private fun startNewIntent(uri: Uri) {
        Intent(Intent.ACTION_VIEW).apply {
            data = uri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            context.startActivity(it)
        }
    }

    private fun openCustomTabs(uri: Uri) {
        CustomTabsIntent.Builder().build().apply {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            it.launchUrl(context, uri)
        }
    }
}
