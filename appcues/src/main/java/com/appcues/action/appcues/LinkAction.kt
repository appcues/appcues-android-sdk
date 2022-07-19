package com.appcues.action.appcues

import android.net.Uri
import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault
import com.appcues.util.LinkOpener
import org.koin.core.scope.Scope
import kotlin.collections.set

internal class LinkAction(
    scope: Scope,
    override val config: AppcuesConfigMap,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/link"
    }

    constructor(koinScope: Scope, redirectUrl: String) : this(
        koinScope,
        hashMapOf<String, Any>().apply {
            this["url"] = redirectUrl
        }
    )

    private val linkOpener: LinkOpener = scope.get()

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
                    linkOpener.openCustomTabs(uri)
                } else {
                    // this will handle any in-app deep link scheme URLs OR any web urls that were
                    // requested to open into the external browser application
                    linkOpener.startNewIntent(uri)
                }
            }
        }
    }
}
