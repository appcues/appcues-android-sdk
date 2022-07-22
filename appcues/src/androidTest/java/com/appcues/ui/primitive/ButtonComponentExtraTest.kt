package com.appcues.ui.primitive

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.karumi.shot.ScreenshotTest
import org.junit.Rule
import org.junit.Test

class ButtonComponentExtraTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testPreviewButtonBorder() {
        composeRule.setContent {
            PreviewButtonBorder()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewButtonComplexContents() {
        composeRule.setContent {
            PreviewButtonComplexContents()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewButtonDefault() {
        composeRule.setContent {
            PreviewButtonDefault()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewButtonGeneral() {
        composeRule.setContent {
            PreviewButtonGeneral()
        }

        compareScreenshot(composeRule.onRoot())
    }
}
