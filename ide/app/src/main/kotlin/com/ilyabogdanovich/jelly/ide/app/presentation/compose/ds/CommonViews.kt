package com.ilyabogdanovich.jelly.ide.app.presentation.compose.ds

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Collection of composables, used across the app (will evolve into design system later).
 *
 * @author Ilya Bogdanovich on 14.10.2023
 */
@Composable
fun RotatingIcon(
    icon: ImageVector,
    modifier: Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 360F,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        )
    )
    Icon(
        icon,
        contentDescription = "",
        modifier = modifier.rotate(angle)
    )
}

@Composable
private fun EditTextStyle(
    textColor: Color = MaterialTheme.colors.onSurface,
) = MaterialTheme.typography.body1.copy(
    color = textColor,
    fontSize = 14.sp,
    fontFamily = FontFamily.Monospace
)

@Composable
fun EditTextField(
    value: TextFieldValue,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onSurface,
    onValueChange: (TextFieldValue) -> Unit = {},
) {
    val state = rememberScrollState()
    BasicTextField(
        modifier = modifier.horizontalScroll(state),
        value = value,
        textStyle = EditTextStyle(textColor),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        onValueChange = onValueChange,
    )
}

@Composable
fun ReadOnlyEditTextField(
    value: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onSurface,
) {
    BasicTextField(
        modifier = modifier,
        value = value,
        textStyle = EditTextStyle(textColor),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        onValueChange = {},
        readOnly = true,
    )
}

@Composable
fun TitleText(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f))
            .padding(10.dp),
    )
}
