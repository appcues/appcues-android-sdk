package com.appcues.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.karumi.shot.ScreenshotTest
import org.junit.Rule
import org.junit.Test

class VerticalStackComponentTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testPreviewTestVerticalAlignment() {
        composeRule.setContent {
            PreviewTestVerticalAlignment()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestVerticalDefault() {
        composeRule.setContent {
            PreviewTestVerticalDefault()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestVerticalLayout() {
        composeRule.setContent {
            PreviewTestVerticalLayout()
        }

        compareScreenshot(composeRule.onRoot())
    }
}
