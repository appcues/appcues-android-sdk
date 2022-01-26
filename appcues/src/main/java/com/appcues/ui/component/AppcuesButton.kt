package com.appcues.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.domain.entity.styling.ComponentColor
import com.appcues.ui.LocalAppcuesActions
import com.appcues.ui.extensions.Compose
import com.appcues.ui.extensions.contentPadding
import com.appcues.ui.extensions.padding

@Composable
internal fun ButtonComponent.Compose() {
    var color: Color? = null
    var gradient: Brush? = null

    backgroundColors.toComposeColors().let {
        if (it.size == 1) {
            color = it.first()
        } else {
            gradient = Brush.horizontalGradient(it)
        }
    }
    val onClick = LocalAppcuesActions.current.onClick

    GradientTextButton(
        modifier = Modifier.padding(style.padding()),
        gradient = gradient,
        color = color,
        contentPadding = style.contentPadding(),
        shape = RoundedCornerShape(style.cornerRadius.dp),
        content = { content.Compose() },
        onClick = { onClick?.invoke(id) },
    )
}

@Composable
private fun List<ComponentColor>.toComposeColors(): List<Color> {
    return if (isEmpty()) {
        arrayListOf(MaterialTheme.colors.primary)
    } else {
        this.map { Color(it.light) }
    }
}

@Composable
private fun GradientTextButton(
    modifier: Modifier = Modifier,
    gradient: Brush? = null,
    color: Color? = null,
    contentPadding: PaddingValues = PaddingValues(),
    onClick: () -> Unit = { },
    shape: Shape = MaterialTheme.shapes.small,
    content: @Composable () -> Unit
) {
    Button(
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .gradientBackground(color, gradient)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

private fun Modifier.gradientBackground(
    color: Color?,
    gradient: Brush?
) = this.then(
    color?.let {
        Modifier.background(it)
    } ?: gradient?.let {
        Modifier.background(it)
    } ?: Modifier
)
