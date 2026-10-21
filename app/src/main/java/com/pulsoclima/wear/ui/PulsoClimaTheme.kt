package com.pulsoclima.wear.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val Night = Color(0xFF07131A)
val DeepOcean = Color(0xFF102832)
val Mint = Color(0xFF5DE1C2)
val Ice = Color(0xFFEAFBF7)
val Sky = Color(0xFF80D8FF)
val Amber = Color(0xFFFFC857)

@Composable
fun PulsoClimaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = Colors(
            primary = Mint,
            primaryVariant = Sky,
            secondary = Amber,
            background = Night,
            surface = DeepOcean,
            onPrimary = Night,
            onSecondary = Night,
            onBackground = Ice,
            onSurface = Ice,
            error = Color(0xFFFF6B6B),
            onError = Night,
        ),
        content = content,
    )
}
