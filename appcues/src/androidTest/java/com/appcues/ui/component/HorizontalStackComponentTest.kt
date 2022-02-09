package com.appcues.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.karumi.shot.ScreenshotTest
import org.junit.Rule
import org.junit.Test

class HorizontalStackComponentTest : ScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun testPreviewTestHorizontalAlignment() {
        composeRule.setContent {
            PreviewTestHorizontalAlignment()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestHorizontalDefault() {
        composeRule.setContent {
            PreviewTestHorizontalDefault()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestHorizontalDistributionEqual() {
        composeRule.setContent {
            PreviewTestHorizontalDistributionEqual()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestHorizontalDistributionCenter() {
        composeRule.setContent {
            PreviewTestHorizontalDistributionCenter()
        }

        compareScreenshot(composeRule.onRoot())
    }

    @Test
    fun testPreviewTestHorizontalLayout() {
        composeRule.setContent {
            PreviewTestHorizontalLayout()
        }

        compareScreenshot(composeRule.onRoot())
    }
}
