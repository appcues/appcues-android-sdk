package com.appcues.ui.primitive

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import coil.compose.AsyncImage
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.composables.LocalAppcuesActionDelegate
import com.appcues.ui.composables.LocalAppcuesActions
import com.appcues.ui.composables.LocalImageLoader
import com.appcues.ui.composables.LocalLogcues
import com.appcues.ui.extensions.PrimitiveGestureProperties
import com.appcues.ui.extensions.blurHashPlaceholder
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getImageLoader
import com.appcues.ui.extensions.getImageRequest
import com.appcues.ui.extensions.innerPrimitiveStyle
import com.appcues.ui.extensions.outerPrimitiveStyle
import com.appcues.ui.extensions.toImageAsyncContentScale
import com.appcues.ui.utils.rememberBlurHashDecoded

@Composable
internal fun ExperiencePrimitive.Compose(matchParentBox: BoxScope? = null) {
    Box(
        modifier = Modifier.outerPrimitiveStyle(
            component = this,
            gestureProperties = PrimitiveGestureProperties(
                onAction = LocalAppcuesActionDelegate.current.onAction,
                actions = LocalAppcuesActions.current,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                enabled = remember { true },
                role = getRole()
            ),
            isDark = isSystemInDarkTheme(),
            matchParentBox = matchParentBox,
        ),
        contentAlignment = Alignment.Center
    ) {
        BackgroundImage(style)

        with(this@Compose) {
            val modifier = Modifier.innerPrimitiveStyle(this)
            when (this) {
                is BoxPrimitive -> Compose(modifier)
                is ButtonPrimitive -> Compose(modifier)
                is EmbedHtmlPrimitive -> Compose(modifier)
                is HorizontalStackPrimitive -> Compose(modifier)
                is ImagePrimitive -> Compose(modifier, matchParentBox)
                is TextPrimitive -> Compose(modifier)
                is VerticalStackPrimitive -> Compose(modifier)
                is TextInputPrimitive -> Compose(modifier)
                is OptionSelectPrimitive -> Compose(modifier)
            }
        }
    }
}

private fun ExperiencePrimitive.getRole(): Role {
    // This is for any tap actions in the experience which currently only
    // apply to Buttons.  There is not really a role for TextInput for instance,
    // but it also never actually gets used since we don't attach actions to those.
    // If they did have tap actions, presumably the Button Role would be appropriate.
    return when (this) {
        is VerticalStackPrimitive -> Role.Button
        is BoxPrimitive -> Role.Button
        is ButtonPrimitive -> Role.Button
        is EmbedHtmlPrimitive -> Role.Image
        is HorizontalStackPrimitive -> Role.Button
        is ImagePrimitive -> Role.Image
        is TextPrimitive -> Role.Button
        is TextInputPrimitive -> Role.Button
        is OptionSelectPrimitive -> Role.Button
    }
}

@Composable
internal fun BoxScope.BackgroundImage(style: ComponentStyle) {
    if (style.backgroundImage != null) {
        with(style.backgroundImage) {
            val context = LocalContext.current
            val logcues = LocalLogcues.current
            val decodedBlurHash = rememberBlurHashDecoded(blurHash = blurHash)

            AsyncImage(
                modifier = Modifier.matchParentSize(),
                model = context.getImageRequest(imageUrl, contentMode),
                contentDescription = null,
                imageLoader = LocalImageLoader.current ?: context.getImageLoader(),
                placeholder = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
                contentScale = contentMode.toImageAsyncContentScale(),
                alignment = getBoxAlignment(horizontalAlignment, verticalAlignment),
                error = context.blurHashPlaceholder(decodedBlurHash, intrinsicSize),
                onError = {
                    logcues.error(it.result.throwable)
                },
            )
        }
    }
}
