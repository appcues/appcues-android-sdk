package com.appcues.data.remote

import com.appcues.data.remote.RemoteError.HttpError
import com.appcues.data.remote.RemoteError.NetworkError
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

internal class NetworkRequestTest {

    @Test
    fun `execute SHOULD return Success`() = runTest {
        // WHEN
        val result = NetworkRequest.execute { true }
        // THEN
        assertThat(result).isInstanceOf(Success::class.java)

        with(result as Success<Boolean>) {
            assertThat(value).isTrue()
        }
    }

    @Test
    fun `execute SHOULD return Failure WHEN call throws Exception`() = runTest {
        // WHEN
        val result = NetworkRequest.execute { throw object : Exception() {} }
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)

        with(result as Failure<RemoteError>) {
            assertThat(reason).isInstanceOf(NetworkError::class.java)
        }
    }

    @Test
    fun `execute SHOULD return Failure HttpError WHEN call throws HttpException`() = runTest {
        // WHEN
        val response = mockk<Response<Boolean>>(relaxed = true) {
            every { errorBody() } returns null
        }
        val result = NetworkRequest.execute { throw HttpException(response) }
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)

        with(result as Failure<RemoteError>) {
            assertThat(reason).isInstanceOf(HttpError::class.java)
        }
    }
}
