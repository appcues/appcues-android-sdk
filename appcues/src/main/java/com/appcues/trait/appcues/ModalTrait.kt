package com.appcues.trait.appcues

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.ModalTrait.PresentationStyle.DIALOG
import com.appcues.trait.appcues.ModalTrait.PresentationStyle.FULL
import com.appcues.trait.appcues.ModalTrait.PresentationStyle.HALF_SHEET
import com.appcues.trait.appcues.ModalTrait.PresentationStyle.SHEET
import com.appcues.ui.modal.BottomSheetModal
import com.appcues.ui.modal.DialogModal
import com.appcues.ui.modal.DialogTransition
import com.appcues.ui.modal.DialogTransition.FADE
import com.appcues.ui.modal.DialogTransition.SLIDE
import com.appcues.ui.modal.ExpandedBottomSheetModal
import com.appcues.ui.modal.FullScreenModal
import com.appcues.ui.presentation.OverlayViewPresenter
import com.appcues.ui.utils.rememberAppcuesWindowInfo
import org.koin.core.scope.Scope

internal class ModalTrait(
    override val config: AppcuesConfigMap,
    renderContext: RenderContext,
    scope: Scope,
) : ContentWrappingTrait, PresentingTrait {

    companion object {

        const val TYPE = "@appcues/modal"
    }

    private enum class PresentationStyle {
        DIALOG, FULL, HALF_SHEET, SHEET
    }

    // this can throw on init, if invalid presentation style - not one of the known types
    private val presentationStyle = config.getConfig<String?>("presentationStyle").toPresentationStyle()

    private val style = config.getConfigStyle("style")

    private val presenter = OverlayViewPresenter(scope, renderContext)

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
            DIALOG -> DialogModal(style, content, windowInfo, getDialogTransition())
            FULL -> FullScreenModal(style, content, windowInfo)
            SHEET -> ExpandedBottomSheetModal(style, content, windowInfo)
            HALF_SHEET -> BottomSheetModal(style, content, windowInfo)
        }
    }

    override fun present() {
        val success = presenter.present()

        if (!success) {
            throw AppcuesTraitException("unable to create modal overlay view")
        }
    }

    override fun remove() {
        presenter.remove()
    }

    private fun getDialogTransition(): DialogTransition {
        return when (config.getConfigOrDefault("transition", "fade")) {
            "slide" -> SLIDE
            else -> FADE
        }
    }

    private fun String?.toPresentationStyle(): PresentationStyle {
        return when (this) {
            "dialog" -> DIALOG
            "full" -> FULL
            "sheet" -> SHEET
            "halfSheet" -> HALF_SHEET
            else -> throw AppcuesTraitException("invalid modal presentation style: $this")
        }
    }
}
