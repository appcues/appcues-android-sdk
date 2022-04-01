package com.appcues.ui.component

import android.annotation.SuppressLint
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.ui.LocalAppcuesActionDelegate
import com.appcues.ui.LocalAppcuesActions
import com.appcues.ui.extensions.PrimitiveGestureProperties
import com.appcues.ui.extensions.imageAspectRatio
import com.appcues.ui.extensions.primitiveStyle
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun EmbedHtmlPrimitive.Compose() {
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

    Box(
        modifier = Modifier
            .primitiveStyle(
                component = this,
                gestureProperties = PrimitiveGestureProperties(
                    onAction = LocalAppcuesActionDelegate.current.onAction,
                    actions = LocalAppcuesActions.current,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    enabled = remember { true },
                    role = Role.Image,
                ),
                isDark = isSystemInDarkTheme(),
                noSizeFillMax = true
            )
            .imageAspectRatio(intrinsicSize)
    ) {
        WebView(
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
}
