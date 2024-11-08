package com.appcues.debugger.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector

private var filled: ImageVector? = null

@Suppress("UnusedReceiverParameter", "MagicNumber")
internal val DebuggerIcons.Filled.Close: ImageVector
    get() {
        if (filled != null) {
            return filled!!
        }
        filled = debuggerIcon(name = "Filled.Close") {
            iconPath {
                moveTo(19.0f, 6.41f)
                lineTo(17.59f, 5.0f)
                lineTo(12.0f, 10.59f)
                lineTo(6.41f, 5.0f)
                lineTo(5.0f, 6.41f)
                lineTo(10.59f, 12.0f)
                lineTo(5.0f, 17.59f)
                lineTo(6.41f, 19.0f)
                lineTo(12.0f, 13.41f)
                lineTo(17.59f, 19.0f)
                lineTo(19.0f, 17.59f)
                lineTo(13.41f, 12.0f)
                close()
            }
        }
        return filled!!
    }
