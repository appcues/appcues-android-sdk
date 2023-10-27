package com.appcues.debugger.ui.shared

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appcues.R.drawable
import com.appcues.R.string

@Composable
internal fun FloatingBackButton(modifier: Modifier = Modifier, docked: Boolean, onTap: () -> Unit) {
    val elevation = animateDpAsState(if (docked) 0.dp else 12.dp, label = "back icon elevation")

    Box(
        modifier = Modifier
            .then(modifier)
            .size(48.dp)
            .clip(RoundedCornerShape(percent = 100))
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation.value, RoundedCornerShape(percent = 100))
                .clip(RoundedCornerShape(percent = 100))
                .size(32.dp)
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = drawable.appcues_ic_back),
                contentDescription = LocalContext.current.getString(string.appcues_debugger_back_description)
            )
        }
    }
}
