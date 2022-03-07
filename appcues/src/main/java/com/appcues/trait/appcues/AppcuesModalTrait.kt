package com.appcues.trait.appcues

import ExpandedBottomSheetModal
import android.content.Context
import androidx.compose.runtime.Composable
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperiencePresentingTrait
import com.appcues.trait.appcues.AppcuesModalTrait.PresentationStyle.BOTTOM_SHEET
import com.appcues.trait.appcues.AppcuesModalTrait.PresentationStyle.DIALOG
import com.appcues.trait.appcues.AppcuesModalTrait.PresentationStyle.EXPANDED_BOTTOM_SHEET
import com.appcues.trait.appcues.AppcuesModalTrait.PresentationStyle.FULL_SCREEN
import com.appcues.ui.AppcuesActivity
import com.appcues.ui.modal.BottomSheetModal
import com.appcues.ui.modal.DialogModal
import com.appcues.ui.modal.FullScreenModal
import org.koin.core.scope.Scope

internal class AppcuesModalTrait(
    override val config: AppcuesConfigMap,
    private val scope: Scope,
    private val context: Context,
) : ExperiencePresentingTrait, ContentWrappingTrait {

    internal enum class PresentationStyle {
        DIALOG, FULL_SCREEN, BOTTOM_SHEET, EXPANDED_BOTTOM_SHEET
    }

    private val presentationStyle = config
        .getConfigOrDefault("presentationStyle", "dialog")
        .toModalPresentationStyle()

    private val style = config.getConfigStyle()

    override fun presentExperience() {
        context.startActivity(AppcuesActivity.getIntent(context, scope.id))
    }

    @Composable
    override fun WrapContent(content: @Composable () -> Unit) {
        when (presentationStyle) {
            DIALOG -> DialogModal(style, content)
            FULL_SCREEN -> FullScreenModal(style, content)
            BOTTOM_SHEET -> BottomSheetModal(style, content)
            EXPANDED_BOTTOM_SHEET -> ExpandedBottomSheetModal(style, content)
        }
    }

    private fun String.toModalPresentationStyle(): PresentationStyle {
        return when (this) {
            "dialog" -> DIALOG
            "full" -> FULL_SCREEN
            "sheet" -> EXPANDED_BOTTOM_SHEET
            "halfSheet" -> BOTTOM_SHEET
            else -> DIALOG
        }
    }
}
