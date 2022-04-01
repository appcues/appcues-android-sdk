package com.appcues.ui.extensions

import androidx.compose.runtime.Composable
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.ui.component.Compose

@Composable
internal fun ExperiencePrimitive.Compose() = when (this) {
    is ButtonPrimitive -> Compose()
    is ImagePrimitive -> Compose()
    is TextPrimitive -> Compose()
    is HorizontalStackPrimitive -> Compose()
    is VerticalStackPrimitive -> Compose()
    is EmbedHtmlPrimitive -> Compose()
}
