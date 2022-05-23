package com.appcues.trait.appcues

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.ContentWrappingTrait
import com.appcues.ui.extensions.rememberWindowInfo
import com.appcues.ui.modal.BottomSheetModal
import com.appcues.ui.modal.DialogModal
import com.appcues.ui.modal.ExpandedBottomSheetModal
import com.appcues.ui.modal.FullScreenModal

internal class ModalTrait(
    override val config: AppcuesConfigMap,
) : ContentWrappingTrait {

    companion object {

        const val TYPE = "@appcues/modal"
    }

    // should we have a default presentation style?
    private val presentationStyle = config.getConfigOrDefault("presentationStyle", "full")

    private val style = config.getConfigStyle("style")

    @Composable
    override fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit) {
        val windowInfo = rememberWindowInfo()

        when (presentationStyle) {
            "dialog" -> DialogModal(style, content, windowInfo)
            "full" -> FullScreenModal(style, content, windowInfo)
            "sheet" -> ExpandedBottomSheetModal(style, content, windowInfo)
            "halfSheet" -> BottomSheetModal(style, content, windowInfo)
            // what to do if presentationStyle is not valid?
            else -> Unit
        }
    }
}
