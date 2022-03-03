package com.appcues.trait.appcues

import android.content.Context
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperiencePresentingTrait
import com.appcues.ui.AppcuesActivity
import org.koin.core.scope.Scope

internal class AppcuesModalTrait(
    override val config: AppcuesConfigMap,
    private val scope: Scope,
    private val context: Context,
) : ExperiencePresentingTrait, ContentWrappingTrait {

    companion object {

        private const val SCREEN_PADDING = 0.05
    }

    override fun presentExperience() {
        context.startActivity(AppcuesActivity.getIntent(context, scope.id))
    }

    @Composable
    override fun WrapContent(
        content: @Composable () -> Unit
    ) {
        val configuration = LocalConfiguration.current
        val dialogHorizontalMargin = (configuration.screenWidthDp * SCREEN_PADDING).dp
        val dialogVerticalMargin = (configuration.screenHeightDp * SCREEN_PADDING).dp

        Card(
            modifier = Modifier
                .defaultMinSize(minWidth = 200.dp, minHeight = 100.dp)
                .padding(horizontal = dialogHorizontalMargin, vertical = dialogVerticalMargin),
            contentColor = Color(color = 0xFFFFFFFF),
            elevation = 10.dp,
            shape = RoundedCornerShape(12.dp),
        ) {
            content()
        }
    }
}
