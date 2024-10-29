package com.appcues.debugger.ui.ds

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.appcues.debugger.ui.theme.LocalAppcuesTheme

@Composable
internal fun TextHeader(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = LocalAppcuesTheme.current.tertiary,
    )
}

@Composable
internal fun TextPrimaryHighLighted(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = LocalAppcuesTheme.current.primary
    )
}

@Composable
internal fun TextPrimary(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = LocalAppcuesTheme.current.primary
    )
}

@Composable
internal fun TextSecondary(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = LocalAppcuesTheme.current.secondary
    )
}
