package com.appcues.debugger.ui.logs

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun Date.toLogFormat(): String {
    return SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault()).format(this)
}
