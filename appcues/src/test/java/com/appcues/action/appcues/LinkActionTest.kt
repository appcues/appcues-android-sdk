package com.appcues.action.appcues

import android.content.ActivityNotFoundException
import android.net.Uri
import com.appcues.Appcues
import com.appcues.AppcuesScopeTest
import com.appcues.NavigationHandler
import com.appcues.di.component.get
import com.appcues.logging.Logcues
import com.appcues.rules.TestScopeRule
import com.appcues.util.LinkOpener
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class LinkActionTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @Before
    fun setUp() {
        // mocking of android.net.Uri is required https://stackoverflow.com/a/63890812
        val mockWebUri = mockk<Uri>(relaxed = true) {
            every { this@mockk.toString() } returns "https://test/path"
            every { this@mockk.scheme } returns "https"
        }
        val mockAppSchemeUri = mockk<Uri>(relaxed = true) {
            every { this@mockk.toString() } returns "myapp://test/path"
            every { this@mockk.scheme } returns "myapp"
        }
        val invalidUri = mockk<Uri>(relaxed = true) {
            every { this@mockk.toString() } returns "invalid:url"
            every { this@mockk.scheme } returns null
        }
        mockkStatic(Uri::class)
        every { Uri.parse("https://test/path") } returns mockWebUri
        every { Uri.parse("myapp://test/path") } returns mockAppSchemeUri
        every { Uri.parse("invalid:url") } returns invalidUri
    }

    @Test
    fun `link SHOULD have expected type name`() {
        assertThat(LinkAction.TYPE).isEqualTo("@appcues/link")
    }

    @Test
    fun `custom constructor SHOULD include url to configMap`() = runTest {
        // GIVEN
        val linkOpener: LinkOpener = get()
        val appcues = mockk<Appcues>(relaxed = true)
        val action = LinkAction(redirectUrl = "custom-url", linkOpener, appcues, mockk(relaxed = true))
        // THEN
        assertThat(action.config).containsEntry("url", "custom-url")
    }

    @Test
    fun `category SHOULD be link`() {
        // GIVEN
        val linkOpener: LinkOpener = get()
        val appcues = mockk<Appcues>(relaxed = true)
        val action = LinkAction(mapOf(), linkOpener, appcues, mockk(relaxed = true))
        // THEN
        assertThat(action.category).isEqualTo("link")
    }

    @Test
    fun `destination SHOULD match url`() {
        // GIVEN
        val linkOpener: LinkOpener = get()
        val appcues = mockk<Appcues>(relaxed = true)
        val action = LinkAction(mapOf("url" to "test-url.com"), linkOpener, appcues, mockk(relaxed = true))
        // THEN
        assertThat(action.destination).isEqualTo("test-url.com")
    }

    @Test
    fun `destination SHOULD be empty if no url is present`() {
        // GIVEN
        val linkOpener: LinkOpener = get()
        val appcues = mockk<Appcues>(relaxed = true)
        val action = LinkAction(mapOf(), linkOpener, appcues, mockk(relaxed = true))
        // THEN
        assertThat(action.destination).isEmpty()
    }

    @Test
    fun `link SHOULD call LinkOpener startNewIntent for external web link WHEN no navigationHandler is set`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("https://test/path")
        val linkOpener: LinkOpener = get()
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns null
        }
        val action = LinkAction(mapOf("url" to uri.toString(), "openExternally" to true), linkOpener, appcues, mockk(relaxed = true))

        // WHEN
        action.execute()

        // THEN
        verify { linkOpener.startNewIntent(uri) }
    }

    @Test
    fun `link SHOULD call navigationHandler navigate for external web link WHEN navigationHandler is set`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("https://test/path")
        val linkOpener: LinkOpener = get()
        val mockkNavigationHandler = mockk<NavigationHandler>(relaxed = true)
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns mockkNavigationHandler
        }
        val action = LinkAction(mapOf("url" to uri.toString(), "openExternally" to true), linkOpener, appcues, mockk(relaxed = true))

        // WHEN
        action.execute()

        // THEN
        coVerify { mockkNavigationHandler.navigateTo(uri) }
        verify { linkOpener wasNot Called }
    }

    @Test
    fun `link SHOULD call LinkOpener openCustomTabs by default for web link`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("https://test/path")
        val linkOpener: LinkOpener = get()
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener, get(), mockk(relaxed = true))

        // WHEN
        action.execute()

        // THEN
        verify { linkOpener.openCustomTabs(uri) }
    }

    @Test
    fun `link SHOULD call LinkOpener startNewIntent for app scheme link WHEN no navigationHandler is set`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("myapp://test/path")
        val linkOpener: LinkOpener = get()
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns null
        }
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener, appcues, mockk(relaxed = true))

        // WHEN
        action.execute()

        // THEN
        verify { linkOpener.startNewIntent(uri) }
    }

    @Test
    fun `link SHOULD call navigationHandler navigate for app scheme link WHEN navigationHandler is set`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("myapp://test/path")
        val linkOpener: LinkOpener = get()
        val mockkNavigationHandler = mockk<NavigationHandler>(relaxed = true)
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns mockkNavigationHandler
        }
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener, appcues, mockk(relaxed = true))

        // WHEN
        action.execute()

        // THEN
        coVerify { mockkNavigationHandler.navigateTo(uri) }
        verify { linkOpener wasNot Called }
    }

    @Test
    fun `execute SHOULD not call anything when url is null`() = runTest {
        // GIVEN
        val linkOpener: LinkOpener = get()
        val mockkNavigationHandler = mockk<NavigationHandler>(relaxed = true)
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns mockkNavigationHandler
        }
        val action = LinkAction(mapOf(), linkOpener, appcues, mockk(relaxed = true))

        // WHEN
        action.execute()

        // THEN
        coVerify { mockkNavigationHandler wasNot Called }
        verify { linkOpener wasNot Called }
    }

    @Test
    fun `execute SHOULD not call anything when url scheme is null`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("invalid:url")
        val linkOpener: LinkOpener = get()
        val mockkNavigationHandler = mockk<NavigationHandler>(relaxed = true)
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns mockkNavigationHandler
        }
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener, appcues, mockk(relaxed = true))

        // WHEN
        action.execute()

        // THEN
        coVerify { mockkNavigationHandler wasNot Called }
        verify { linkOpener wasNot Called }
    }

    @Test
    fun `execute SHOULD call logcues error when ActivityNotFoundException is thrown by linkOpener`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("myapp://test/path")
        val exception = ActivityNotFoundException("Invalid Activity")
        val linkOpener: LinkOpener = mockk(relaxed = true) {
            every { startNewIntent(any()) } throws exception
        }
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns null
        }
        val logcues = mockk<Logcues>(relaxed = true)
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener, appcues, logcues)

        // WHEN
        action.execute()

        // THEN
        verify { logcues.error(message = "Unable to process deep link myapp://test/path\n\n Reason: ${exception.message}") }
    }

    @Test
    fun `execute SHOULD call logcues error when ActivityNotFoundException is thrown by navigationHandler`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("myapp://test/path")
        val linkOpener: LinkOpener = get()
        val exception = ActivityNotFoundException("Invalid Activity")
        val appcues = mockk<Appcues>(relaxed = true) {
            every { this@mockk.navigationHandler } returns mockk(relaxed = true) {
                coEvery { navigateTo(any()) } throws exception
            }
        }
        val logcues = mockk<Logcues>(relaxed = true)
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener, appcues, logcues)

        // WHEN
        action.execute()

        // THEN
        verify { logcues.error(message = "Unable to process deep link myapp://test/path\n\n Reason: ${exception.message}") }
    }
}
