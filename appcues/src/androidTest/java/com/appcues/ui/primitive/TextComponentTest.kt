package com.appcues.ui.primitive

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.karumi.shot.ScreenshotTest
import org.junit.Rule
import org.junit.Test

class TextComponentTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testPreviewTextComponentColor() {
        composeRule.setContent {
            PreviewTextComponentColor()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTextComponentLineHeight() {
        composeRule.setContent {
            PreviewTextComponentLineHeight()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTextComponentLetterSpacing() {
        composeRule.setContent {
            PreviewTextComponentLetterSpacing()
        }

        compareScreenshot(composeRule.onRoot())
    }
}
