package com.appcues.debugger.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.debugger.ui.icons.DebuggerIcons
import com.appcues.debugger.ui.icons.Info
import com.appcues.debugger.ui.theme.LocalAppcuesTheme

@Composable
internal fun InfoBox(modifier: Modifier = Modifier, text: String) {
    val theme = LocalAppcuesTheme.current
    Row(
        modifier = Modifier
            .then(modifier)
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(3.dp, theme.info.copy(alpha = 0.5f))
            .background(theme.info),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(start = 8.dp),
            imageVector = DebuggerIcons.Outlined.Info,
            contentDescription = "Info Box icon",
            tint = theme.background
        )
        Text(
            modifier = Modifier.padding(8.dp),
            text = text,
            color = theme.background,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
