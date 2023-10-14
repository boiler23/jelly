package com.ilyabogdanovich.jelly.ide.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector

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
