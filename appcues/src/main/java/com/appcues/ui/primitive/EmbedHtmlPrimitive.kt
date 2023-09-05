package com.appcues.ui.primitive

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.ui.composables.LocalChromeClient
import com.appcues.ui.extensions.aspectRatio

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun EmbedHtmlPrimitive.Compose(modifier: Modifier) {
    val chromeClient = LocalChromeClient.current
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    webViewState.value?.let { webView ->
        LaunchedEffect(webView) {
            webView.loadData(
                """
                    <head>
                        <meta
                            name='viewport'
                            content='width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no'
                        />
                    </head>
                    <body style='margin: 0; padding: 0'>
                        $embed
                    </body>""",
                null,
                "utf-8",
            )
        }
    }
    BoxWithConstraints(
        modifier = modifier.then(
            Modifier.aspectRatio(FILL, intrinsicSize, style, LocalDensity.current)
        )
    ) {
        // WebView changes it's layout strategy based on
        // it's layoutParams. We convert from Compose Modifier to
        // layout params here.
        val width =
            if (constraints.hasFixedWidth)
                LayoutParams.MATCH_PARENT
            else
                LayoutParams.WRAP_CONTENT
        val height =
            if (constraints.hasFixedHeight)
                LayoutParams.MATCH_PARENT
            else
                LayoutParams.WRAP_CONTENT

        val layoutParams = FrameLayout.LayoutParams(
            width,
            height
        )

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    isNestedScrollingEnabled = false
                    isScrollContainer = false
                    with(settings) {
                        javaScriptEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                    }
                    this.layoutParams = layoutParams
                    webChromeClient = chromeClient
                }.also { webViewState.value = it }
            }
        )
    }
}

// this is used by the WebView in the Embed component above, to support expand to fullscreen video
internal class EmbedChromeClient(private val container: ViewGroup) : WebChromeClient() {
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
    }

    override fun onHideCustomView() {
        if (customView == null) {
            return
        }
        customView?.visibility = View.GONE
        container.removeView(customView)
        customCallback?.onCustomViewHidden()
        customView = null
        customCallback = null
    }
}
