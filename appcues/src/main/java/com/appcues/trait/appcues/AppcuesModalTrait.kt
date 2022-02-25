package com.appcues.trait.appcues

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.statemachine.StateMachine
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.ExperiencePresentingTrait
import com.appcues.ui.AppcuesActivity

internal class AppcuesModalTrait(
    override val config: AppcuesConfigMap,
    private val stateMachine: StateMachine,
    private val context: Context,
    private val scopeId: String,
) : ExperiencePresentingTrait, ContentWrappingTrait {

    companion object {

        private const val SCREEN_PADDING = 0.05
    }

    override fun presentExperience() {
        context.startActivity(AppcuesActivity.getIntent(context, scopeId))
    }

    @Composable
    override fun WrapContent(content: @Composable () -> Unit) {
        val dismissOverlay = AppcuesSkippableTrait(null, stateMachine)
        val backdrop = AppcuesBackdropTrait(null, stateMachine)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            backdrop.Backdrop(scope = this)
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
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        content()
                    }

                    dismissOverlay.Overlay(scope = this)
                }
            }
        }
    }
}
