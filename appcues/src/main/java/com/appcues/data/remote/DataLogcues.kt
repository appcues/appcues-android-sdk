package com.appcues.data.remote

import com.appcues.logging.Logcues
import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer

/**
 * This class wrappers Logcues and provide specific log methods for Http Request, Response
 * and general error logging where the "reason" should be formatted(beautify)
 */
internal class DataLogcues(private val logcues: Logcues) {

    companion object {

        private const val indent = "  "
    }

    fun error(description: String, reason: String) {
        logcues.error("$description.\nReason: ${reason.beautify()}")
    }

    fun debug(request: Request) {
        val message = StringBuffer()

        message.appendLine("REQUEST: ${request.method}")
        message.appendLine("URL: ${request.url}")
        message.appendLine()
        message.appendHeader(request.headers)

        val body = request.body
        if (body == null) {
            message.appendLine("(no request body)")
        } else if (body.isDuplex()) {
            message.appendLine("(duplex request body omitted)")
        } else if (body.isOneShot()) {
            message.appendLine("(one-shot body omitted)")
        } else if (bodyIsImage(body.contentType())) {
            message.appendLine("(encoded image)")
        } else {
            val buffer = Buffer()
            body.writeTo(buffer)
            val decodedBody = buffer.readString(body.contentType().charsetOrUTF8())
            message.appendBody(decodedBody, body.contentLength())
        }
        logcues.debug(message.toString())
    }

    fun debug(response: Response) {
        val message = StringBuffer()
        val responseTime = response.receivedResponseAtMillis - response.sentRequestAtMillis

        message.appendLine("RESPONSE: ${response.code} (${responseTime}ms)")
        message.appendLine("URL: ${response.request.url}")
        message.appendLine()
        message.appendHeader(response.headers)

        val body = response.body
        if (body == null || !response.promisesBody()) {
            message.appendLine("(no response body)")
        } else {
            val source = body.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            val decodedBody = source.buffer.clone().readString(body.contentType().charsetOrUTF8())
            message.appendBody(decodedBody, body.contentLength())
        }
        logcues.debug(message.toString())
    }

    private fun bodyIsImage(mediaType: MediaType?): Boolean {
        return mediaType != null && mediaType.type.contains("image")
    }

    private fun StringBuffer.appendLine(message: String) {
        append("$message\n")
    }

    private fun StringBuffer.appendHeader(headers: Headers) {
        if (headers.size > 0) {
            appendLine("Headers: {")
            headers.forEach { appendLine("$indent[${it.first}] = ${it.second}") }
            appendLine("}")
        }
    }

    private fun StringBuffer.appendBody(decodedBody: String, contentLength: Long) {
        appendLine("Body($contentLength bytes): ${decodedBody.beautify()}")
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    fun String.beautify(): String {
        try {
            val builder = StringBuilder()
            var indentation = 0
            var quoting = false

            forEachIndexed { index, char ->
                if (char == '"') quoting = !quoting

                if (quoting) {
                    builder.append(char)
                } else if (!builder.handle(char, indentation)) {
                    indentation = beautifyAtIndex(builder, char, index, indentation)
                }
            }

            return builder.toString()
        } catch (e: Exception) {
            // in case any parsing goes wrong just return existing text
            return this
        }
    }

    private fun StringBuilder.handle(char: Char, sourceIndentation: Int): Boolean {
        return when {
            // break line and keep indentation
            char == ',' -> {
                appendLine(char)
                append(indent.repeat(sourceIndentation))
                true
            }
            // skip empty spaces when we are indenting
            char == ' ' && sourceIndentation > 0 -> true
            else -> false
        }
    }

    private fun String.beautifyAtIndex(prettyJson: StringBuilder, char: Char, index: Int, sourceIndentation: Int): Int {
        var indentation = sourceIndentation
        when {
            char == '(' && get(index + 1) != ')' -> {
                prettyJson.appendLine(char)
                indentation++
                prettyJson.append(indent.repeat(indentation))
            }
            char == '{' && get(index + 1) != '}' -> {
                prettyJson.appendLine(char)
                indentation++
                prettyJson.append(indent.repeat(indentation))
            }
            char == '[' && get(index + 1) != ']' -> {
                prettyJson.appendLine(char)
                indentation++
                prettyJson.append(indent.repeat(indentation))
            }
            char == ']' && get(index - 1) != '[' -> {
                prettyJson.appendLine()
                indentation--
                prettyJson.append(indent.repeat(indentation))
                prettyJson.append(char)
            }
            char == '}' && get(index - 1) != '{' -> {
                prettyJson.appendLine()
                indentation--
                prettyJson.append(indent.repeat(indentation))
                prettyJson.append(char)
            }
            char == ')' && get(index - 1) != '(' -> {
                prettyJson.appendLine()
                indentation--
                prettyJson.append(indent.repeat(indentation))
                prettyJson.append(char)
            }
            else -> prettyJson.append(char)
        }

        return indentation
    }
}
