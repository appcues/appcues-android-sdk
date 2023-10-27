package com.appcues.data.remote

import okhttp3.MediaType
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal fun MediaType?.charsetOrUTF8(): Charset {
    return this?.charset(null) ?: StandardCharsets.UTF_8
}
