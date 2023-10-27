package com.appcues.debugger.ui.shared

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.appcues.R

internal fun copyToClipboardAndToast(context: Context, clipboardManager: ClipboardManager, text: String) {
    clipboardManager.setText(AnnotatedString(text))
    val message = context.getString(R.string.appcues_debugger_clipboard_copy_message)

    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
