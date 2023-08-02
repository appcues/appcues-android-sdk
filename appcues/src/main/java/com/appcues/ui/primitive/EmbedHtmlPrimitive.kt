package com.appcues.ui.primitive

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.ui.composables.LocalChromeClient
import com.appcues.ui.extensions.aspectRatio
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun EmbedHtmlPrimitive.Compose(modifier: Modifier) {
    val chromeClient = LocalChromeClient.current
    val webViewState = rememberWebViewStateWithHTMLData(
        data = """
        <head>
            <meta 
                name='viewport' 
                content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no'
            />
        </head>
        <body style='margin: 0; padding: 0'>
            $embed
        </body>"""
    )

    WebView(
        modifier = modifier.then(
            Modifier.aspectRatio(FILL, intrinsicSize, style, LocalDensity.current)
        ),
        state = webViewState,
        onCreated = {
            it.isNestedScrollingEnabled = false
            it.isScrollContainer = false

            with(it.settings) {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
            }
        },
        chromeClient = chromeClient
    )
}

// this is used by the WebView in the Embed component above, to support expand to fullscreen video
internal class EmbedChromeClient(private val container: ViewGroup) : AccompanistWebChromeClient() {
    private var customView: View? = null
    private var customCallback: CustomViewCallback? = null

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        // if a view already exists then immediately terminate the new one
        if (customView != null) {
            customCallback?.onCustomViewHidden()
            return
        }
        customView = view
        customCallback = callback
        container.addView(view)
        container.visibility = View.VISIBLE
    }

    override fun onHideCustomView() {
        if (customView == null) {
            return
        }
        customView?.visibility = View.GONE
        container.visibility = View.GONE
        container.removeView(customView)
        customCallback?.onCustomViewHidden()
        customView = null
        customCallback = null
    }
}
