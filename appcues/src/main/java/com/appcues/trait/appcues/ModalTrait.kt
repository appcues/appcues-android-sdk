package com.appcues.trait.appcues

import ExpandedBottomSheetModal
import android.content.Context
import androidx.compose.runtime.Composable
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperiencePresentingTrait
import com.appcues.ui.AppcuesActivity
import com.appcues.ui.modal.BottomSheetModal
import com.appcues.ui.modal.DialogModal
import com.appcues.ui.modal.FullScreenModal
import org.koin.core.scope.Scope

internal class ModalTrait(
    override val config: AppcuesConfigMap,
    private val scope: Scope,
    private val context: Context,
) : ExperiencePresentingTrait, ContentWrappingTrait {

    companion object {
        const val TYPE = "@appcues/modal"
    }

    // should we have a default presentation style?
    private val presentationStyle = config.getConfigOrDefault("presentationStyle", "full")

    private val style = config.getConfigStyle("style")

    override fun presentExperience() {
        context.startActivity(AppcuesActivity.getIntent(context, scope.id))
    }

    @Composable
    override fun WrapContent(content: @Composable () -> Unit) {
        when (presentationStyle) {
            "dialog" -> DialogModal(style, content)
            "full" -> FullScreenModal(style, content)
            "sheet" -> ExpandedBottomSheetModal(style, content)
            "halfSheet" -> BottomSheetModal(style, content)
            // what to do if presentationStyle is not valid?
            else -> Unit
        }
    }
}
