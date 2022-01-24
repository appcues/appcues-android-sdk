package com.appcues.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.karumi.shot.ScreenshotTest
import org.junit.Rule
import org.junit.Test

class TextComponentTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun validateTextColor() {
        composeRule.setContent {
            PreviewTextComponentColor()
        }

        compareScreenshot(composeRule.onRoot())
    }
}
