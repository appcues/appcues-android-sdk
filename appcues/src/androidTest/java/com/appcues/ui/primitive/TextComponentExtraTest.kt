package com.appcues.ui.primitive

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.karumi.shot.ScreenshotTest
import org.junit.Rule
import org.junit.Test

class TextComponentExtraTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testPreviewTestDefault() {
        composeRule.setContent {
            PreviewTestDefault()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestBorder() {
        composeRule.setContent {
            PreviewTestBorder()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestFixedSize() {
        composeRule.setContent {
            PreviewTestFixedSize()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestLayout() {
        composeRule.setContent {
            PreviewTestLayout()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestSystemFont() {
        composeRule.setContent {
            PreviewTestSystemFont()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestTypography() {
        composeRule.setContent {
            PreviewTestTypography()
        }

        compareScreenshot(composeRule.onRoot())
    }
}
