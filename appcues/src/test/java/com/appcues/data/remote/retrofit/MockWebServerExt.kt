package com.appcues.data.remote.retrofit

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.buffer
import okio.source
import java.nio.charset.StandardCharsets

internal fun MockWebServer.enqueueResponse(fileName: String, code: Int) {
    enqueue(
        MockResponse()
            .setResponseCode(code)
            .setBody(readJsonFromResource(fileName))
    )
}

internal fun MockWebServer.dispatchResponses(responses: HashMap<String, String>) {
    dispatcher = object : Dispatcher() {

        override fun dispatch(request: RecordedRequest): MockResponse {
            responses.forEach {
                if (request.path == it.key) {
                    return MockResponse()
                        .setResponseCode(200)
                        .setBody(readJsonFromResource(it.value))
                }
            }

            return MockResponse().setResponseCode(404)
        }
    }
}

private fun MockWebServer.readJsonFromResource(fileName: String): String {
    val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)

    requireNotNull(inputStream) { "FileName \"$fileName\" was not found in test/resources folder" }

    return inputStream.source().buffer().readString(StandardCharsets.UTF_8)
}
