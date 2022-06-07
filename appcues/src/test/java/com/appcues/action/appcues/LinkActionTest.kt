package com.appcues.action.appcues

import android.net.Uri
import com.appcues.AppcuesScopeTest
import com.appcues.rules.KoinScopeRule
import com.appcues.util.LinkOpener
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get

@OptIn(ExperimentalCoroutinesApi::class)
internal class LinkActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

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
        mockkStatic(Uri::class)
        every { Uri.parse("https://test/path") } returns mockWebUri
        every { Uri.parse("myapp://test/path") } returns mockAppSchemeUri
    }

    @Test
    fun `link SHOULD have expected type name`() {
        assertThat(LinkAction.TYPE).isEqualTo("@appcues/link")
    }

    @Test
    fun `link SHOULD call LinkOpener startNewIntent for external web link`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("https://test/path")
        val linkOpener: LinkOpener = get()
        val action = LinkAction(mapOf("url" to uri.toString(), "openExternally" to true), linkOpener)

        // WHEN
        action.execute(get())

        // THEN
        verify { linkOpener.startNewIntent(uri) }
    }

    @Test
    fun `link SHOULD call LinkOpener openCustomTabs by default for web link`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("https://test/path")
        val linkOpener: LinkOpener = get()
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener)

        // WHEN
        action.execute(get())

        // THEN
        verify { linkOpener.openCustomTabs(uri) }
    }

    @Test
    fun `link SHOULD call LinkOpener startNewIntent for app scheme link`() = runTest {
        // GIVEN
        val uri: Uri = Uri.parse("myapp://test/path")
        val linkOpener: LinkOpener = get()
        val action = LinkAction(mapOf("url" to uri.toString()), linkOpener)

        // WHEN
        action.execute(get())

        // THEN
        verify { linkOpener.startNewIntent(uri) }
    }
}
