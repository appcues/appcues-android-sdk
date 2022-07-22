package com.appcues.ui.primitive

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.ui.extensions.imageAspectRatio
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun EmbedHtmlPrimitive.Compose(modifier: Modifier) {
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
        modifier = modifier.then(Modifier.imageAspectRatio(intrinsicSize)),
        state = webViewState,
        onCreated = {
            it.isNestedScrollingEnabled = false
            it.isScrollContainer = false

            with(it.settings) {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = false
            }
        }
    )
}
