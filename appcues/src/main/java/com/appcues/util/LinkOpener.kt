package com.appcues.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

internal class LinkOpener(private var context: Context) {

    fun startNewIntent(uri: Uri) {
        Intent(Intent.ACTION_VIEW).apply {
            data = uri
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            context.startActivity(it)
        }
    }

    fun openCustomTabs(uri: Uri) {
        CustomTabsIntent.Builder().build().apply {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }.also {
            it.launchUrl(context, uri)
        }
    }
}
