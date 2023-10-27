package com.appcues.data.remote.interceptor

import com.appcues.data.remote.DataLogcues
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import okhttp3.Interceptor.Chain
import okhttp3.Request
import okhttp3.Response
import org.junit.Test

internal class DataLogcuesInterceptorTest {

    private val dataLogcues = mockk<DataLogcues>(relaxed = true)
    private val chain = mockk<Chain>(relaxed = true)

    private val interceptor = HttpLogcuesInterceptor(dataLogcues)

    @Test
    fun `intercept SHOULD call dependencies in sequence`() {
        // GIVEN
        val request = mockk<Request>(relaxed = true)
        val response = mockk<Response>(relaxed = true)
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response
        // WHEN
        val result = interceptor.intercept(chain)
        // THEN
        verifySequence {
            chain.request()
            dataLogcues.debug(request)
            chain.proceed(request)
            dataLogcues.debug(response)
        }

        assertThat(result).isEqualTo(response)
    }

    @Test(expected = RuntimeException::class)
    fun `intercept SHOULD call dependencies in sequence when proceed throws AND throw same exception`() {
        // GIVEN
        val request = mockk<Request>(relaxed = true)
        every { chain.request() } returns request
        every { chain.proceed(request) } throws RuntimeException("test")
        // WHEN
        interceptor.intercept(chain)
        // THEN
        verifySequence {
            chain.request()
            dataLogcues.debug(request)
            chain.proceed(request)
            dataLogcues.error("Failed to send request", "test")
        }
    }
}
