package com.appcues.trait.appcues

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appcues.R
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import com.appcues.di.scope.AppcuesScope
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.ui.presentation.FabViewPresenter

internal class LauncherTrait(
    override val config: AppcuesConfigMap,
    renderContext: RenderContext,
    scope: AppcuesScope,
) : ContentWrappingTrait, PresentingTrait {

    companion object {

        const val TYPE = "@appcues/launcher"
    }

    // private val style = config.getConfigStyle("style")

    private val presenter = FabViewPresenter(scope, renderContext)

    @Composable
    override fun WrapContent(
        content: @Composable (
            modifier: Modifier,
            containerPadding: PaddingValues,
            safeAreaInsets: PaddingValues,
            hasVerticalScroll: Boolean,
        ) -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd,
        ) {

            // Temporary FAB composition
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(size = 48.dp)
                    .clip(RoundedCornerShape(percent = 100))
                    .shadow(30.dp, RoundedCornerShape(percent = 100))
                    .background(brush = LocalAppcuesTheme.current.primaryButton),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(id = R.drawable.appcues_ic_white_logo),
                    contentDescription = LocalContext.current.getString(R.string.appcues_debugger_fab_image_content_description)
                )
            }
        }
    }

    override fun present() {
        val success = presenter.present()

        if (!success) {
            throw AppcuesTraitException("unable to create launcher overlay view")
        }
    }

    override fun remove() {
        presenter.remove()
    }
}
