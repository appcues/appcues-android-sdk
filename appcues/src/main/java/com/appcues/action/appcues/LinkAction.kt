package com.appcues.action.appcues

import android.content.ActivityNotFoundException
import android.net.Uri
import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.action.MetadataSettingsAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault
import com.appcues.logging.Logcues
import com.appcues.util.LinkOpener

internal class LinkAction(
    override val config: AppcuesConfigMap,
    private val linkOpener: LinkOpener,
    private val appcues: Appcues,
    private val logcues: Logcues,
) : ExperienceAction, MetadataSettingsAction {

    companion object {

        const val TYPE = "@appcues/link"
    }

    constructor(redirectUrl: String, linkOpener: LinkOpener, appcues: Appcues, logcues: Logcues) : this(
        config = hashMapOf<String, Any>("url" to redirectUrl),
        linkOpener = linkOpener,
        appcues = appcues,
        logcues = logcues
    )

    private val url: String? = config.getConfig("url")
    private val openExternally = config.getConfigOrDefault("openExternally", false)

    override val category = "link"

    override val destination: String = url ?: String()

    override suspend fun execute() {
        val uri = url?.let { Uri.parse(it) } ?: return
        val scheme = uri.scheme?.lowercase() ?: return

        // only HTTP or HTTPS URLs are eligible for Chrome Custom Tabs in-app browser
        val isEligibleCustomTab = !openExternally && (scheme == "http" || scheme == "https")

        if (isEligibleCustomTab) {
            linkOpener.openCustomTabs(uri)
        } else try {
            // this will handle any in-app deep link scheme URLs OR any web urls that were
            // requested to open into the external browser application
            appcues.navigationHandler?.navigateTo(uri) ?: linkOpener.startNewIntent(uri)
        } catch (exception: ActivityNotFoundException) {
            logcues.error("Unable to process deep link $destination\n\n Reason: ${exception.message}")
        }
    }
}
