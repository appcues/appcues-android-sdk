package com.appcues.trait.appcues

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.ui.modal.BottomSheetModal
import com.appcues.ui.modal.DialogModal
import com.appcues.ui.modal.ExpandedBottomSheetModal
import com.appcues.ui.modal.FullScreenModal
import com.appcues.ui.presentation.OverlayViewPresenter
import com.appcues.ui.utils.rememberAppcuesWindowInfo
import org.koin.core.scope.Scope

internal class ModalTrait(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val scope: Scope,
) : ContentWrappingTrait, PresentingTrait {

    companion object {

        const val TYPE = "@appcues/modal"
    }

    // should we have a default presentation style?
    private val presentationStyle = config.getConfigOrDefault("presentationStyle", "full")

    private val style = config.getConfigStyle("style")

    @Composable
    override fun WrapContent(
        content: @Composable (
            modifier: Modifier,
            containerPadding: PaddingValues,
            safeAreaInsets: PaddingValues
        ) -> Unit
    ) {
        val windowInfo = rememberAppcuesWindowInfo()

        when (presentationStyle) {
            "dialog" -> DialogModal(style, content, windowInfo)
            "full" -> FullScreenModal(style, content, windowInfo)
            "sheet" -> ExpandedBottomSheetModal(style, content, windowInfo)
            "halfSheet" -> BottomSheetModal(style, content, windowInfo)
            // what to do if presentationStyle is not valid?
            else -> Unit
        }
    }

    override fun present() {
        val success = OverlayViewPresenter(scope, renderContext).present()

        if (!success) {
            throw AppcuesTraitException("unable to create modal overlay view")
        }
    }
}
