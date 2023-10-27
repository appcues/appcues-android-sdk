package com.appcues.data.remote

import com.appcues.logging.Logcues
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

internal class DataLogcuesTest {

    private val logcues = mockk<Logcues>(relaxed = true)

    private val dataLogcues = DataLogcues(logcues)

    @Test
    fun `error SHOULD log error properly`() {
        // WHEN
        dataLogcues.error("description", "test exception message")
        // THEN
        verifySequence {
            logcues.error("description.\nReason: test exception message")
        }
    }

    @Test
    fun `error SHOULD log error pretty`() {
        // GIVEN
        val reason =
            """
            HttpError(code=401,    error=ErrorResponse(error="error (1), {2}, [3]", statusCode=401, message=ErrorMessageResponse()))
            """.trimIndent()

        // WHEN
        dataLogcues.error("description", reason)
        // THEN
        verifySequence {
            logcues.error(
                """
                description.
                Reason: HttpError(
                  code=401,
                  error=ErrorResponse(
                    error="error (1), {2}, [3]",
                    statusCode=401,
                    message=ErrorMessageResponse()
                  )
                )
                """.trimIndent()
            )
        }
    }

    @Test
    fun `debug request SHOULD log WHEN null body`() {
        // GIVEN
        val request = mockk<Request> {
            every { method } returns "GET"
            every { url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf()
            every { body } returns null
        }
        // WHEN
        dataLogcues.debug(request)
        // THEN
        verifySequence {
            logcues.debug(
                """
                REQUEST: GET
                URL: http://www.appcues.com/
                
                (no request body)
                """.trimln()
            )
        }
    }

    @Test
    fun `debug request SHOULD log WHEN body isDuplex`() {
        // GIVEN
        val request = mockk<Request> {
            every { method } returns "HEAD"
            every { url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf()
            every { body } returns mockk {
                every { isDuplex() } returns true
            }
        }
        // WHEN
        dataLogcues.debug(request)
        // THEN
        verifySequence {
            logcues.debug(
                """
                REQUEST: HEAD
                URL: http://www.appcues.com/
                
                (duplex request body omitted)
                """.trimln()
            )
        }
    }

    @Test
    fun `debug request SHOULD log WHEN body isOneShot`() {
        // GIVEN
        val request = mockk<Request> {
            every { method } returns "HEAD"
            every { url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf()
            every { body } returns mockk {
                every { isDuplex() } returns false
                every { isOneShot() } returns true
            }
        }
        // WHEN
        dataLogcues.debug(request)
        // THEN
        verifySequence {
            logcues.debug(
                """
                REQUEST: HEAD
                URL: http://www.appcues.com/
                
                (one-shot body omitted)
                """.trimln()
            )
        }
    }

    @Test
    fun `debug request SHOULD log WHEN body is content type image`() {
        // GIVEN
        val request = mockk<Request> {
            every { method } returns "HEAD"
            every { url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf()
            every { body } returns mockk {
                every { isDuplex() } returns false
                every { isOneShot() } returns false
                every { contentType() } returns "image/png".toMediaType()
            }
        }
        // WHEN
        dataLogcues.debug(request)
        // THEN
        verifySequence {
            logcues.debug(
                """
                REQUEST: HEAD
                URL: http://www.appcues.com/
                
                (encoded image)
                """.trimln()
            )
        }
    }

    @Test
    fun `debug request SHOULD log WHEN body is valid`() {
        // GIVEN
        val requestBody = """{"result":true,"empty_array":[],"array":[{"value":1},{"value":2}]}"""
            .toRequestBody("application/json".toMediaType())
        val request = mockk<Request> {
            every { method } returns "GET"
            every { url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf()
            every { body } returns requestBody
        }
        // WHEN
        dataLogcues.debug(request)
        // THEN
        verifySequence {
            logcues.debug(
                """
                REQUEST: GET
                URL: http://www.appcues.com/
                
                Body(${requestBody.contentLength()} bytes): {
                  "result":true,
                  "empty_array":[],
                  "array":[
                    {
                      "value":1
                    },
                    {
                      "value":2
                    }
                  ]
                }
                """.trimln()
            )
        }
    }

    @Test
    fun `debug request SHOULD log WHEN headers and body are valid`() {
        // GIVEN
        val requestBody = """{"result":200}"""
            .toRequestBody("application/json".toMediaType())
        val request = mockk<Request> {
            every { method } returns "GET"
            every { url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf("Content-Test", "test value")
            every { body } returns requestBody
        }
        // WHEN
        dataLogcues.debug(request)
        // THEN
        verifySequence {
            logcues.debug(
                """
                REQUEST: GET
                URL: http://www.appcues.com/
                
                Headers: {
                  [Content-Test] = test value
                }
                Body(${requestBody.contentLength()} bytes): {
                  "result":200
                }
                """.trimln()
            )
        }
    }

    @Test
    fun `debug request SHOULD log WHEN body not pretty AND content type null`() {
        // GIVEN
        val requestBody = """{"result":true[}}}""".toRequestBody(null)
        val request = mockk<Request> {
            every { method } returns "GET"
            every { url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf()
            every { body } returns requestBody
        }
        // WHEN
        dataLogcues.debug(request)
        // THEN
        verifySequence {
            logcues.debug(
                """
                REQUEST: GET
                URL: http://www.appcues.com/
                
                Body(${requestBody.contentLength()} bytes): {"result":true[}}}
                """.trimln()
            )
        }
    }

    @Test
    fun `debug response SHOULD log WHEN null body`() {
        // GIVEN
        val response = mockk<Response> {
            every { receivedResponseAtMillis } returns 100L
            every { sentRequestAtMillis } returns 0L
            every { code } returns 200
            every { request.url } returns "http://www.appcues.com/".toHttpUrl()
            every { headers } returns Headers.headersOf()
            every { body } returns null
        }
        // WHEN
        dataLogcues.debug(response)
        // THEN
        verifySequence {
            logcues.debug(
                """
                RESPONSE: 200 (100ms)
                URL: http://www.appcues.com/
                
                (no response body)
                """.trimln()
            )
        }
    }

    @Test
    fun `debug response SHOULD log WHEN promisesBody is false`() {
        // GIVEN
        val response = mockk<Response> {
            every { receivedResponseAtMillis } returns 100L
            every { sentRequestAtMillis } returns 0L
            every { code } returns 200
            every { request.url } returns "http://www.appcues.com/".toHttpUrl()
            every { request.method } returns "HEAD"
            every { headers } returns Headers.headersOf()
            every { body } returns mockk(relaxed = true)
        }
        // WHEN
        dataLogcues.debug(response)
        // THEN
        verifySequence {
            logcues.debug(
                """
                RESPONSE: 200 (100ms)
                URL: http://www.appcues.com/
                
                (no response body)
                """.trimln()
            )
        }
    }

    @Test
    fun `debug response SHOULD log WHEN headers and body are valid`() {
        // GIVEN
        val responseBody = """{"result":200}"""
            .toResponseBody("application/json".toMediaType())

        // GIVEN
        val response = mockk<Response> {
            every { receivedResponseAtMillis } returns 100L
            every { sentRequestAtMillis } returns 0L
            every { code } returns 200
            every { request.url } returns "http://www.appcues.com/".toHttpUrl()
            every { request.method } returns "GET"
            every { headers } returns Headers.headersOf("Content-Test", "test value")
            every { body } returns responseBody
        }
        // WHEN
        dataLogcues.debug(response)
        // THEN
        verifySequence {
            logcues.debug(
                """
                RESPONSE: 200 (100ms)
                URL: http://www.appcues.com/
                
                Headers: {
                  [Content-Test] = test value
                }
                Body(${responseBody.contentLength()} bytes): {
                  "result":200
                }
                """.trimln()
            )
        }
    }

    @Test
    fun `debug response SHOULD log WHEN body is not pretty AND content type null`() {
        // GIVEN
        val responseBody = """{"result":]]200}""".toResponseBody(null)

        // GIVEN
        val response = mockk<Response> {
            every { receivedResponseAtMillis } returns 100L
            every { sentRequestAtMillis } returns 0L
            every { code } returns 200
            every { request.url } returns "http://www.appcues.com/".toHttpUrl()
            every { request.method } returns "GET"
            every { headers } returns Headers.headersOf()
            every { body } returns responseBody
        }
        // WHEN
        dataLogcues.debug(response)
        // THEN
        verifySequence {
            logcues.debug(
                """
                RESPONSE: 200 (100ms)
                URL: http://www.appcues.com/
                
                Body(${responseBody.contentLength()} bytes): {"result":]]200}
                """.trimln()
            )
        }
    }

    private fun String.trimln(): String = trimIndent() + "\n"
}
