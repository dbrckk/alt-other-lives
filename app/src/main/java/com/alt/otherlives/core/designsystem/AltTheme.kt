package com.alt.otherlives.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AltBackground = Color(0xFF08080A)
val AltSurface = Color(0xFF111116)
val AltCard = Color(0xFF15141B)
val AltPrimary = Color(0xFFEDE7FF)
val AltAccent = Color(0xFFB7A7FF)
val AltMuted = Color(0xFFAAA8B3)
val AltDimmed = Color(0xFF777680)

@Composable
fun AltTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = AltBackground,
            surface = AltSurface,
            primary = AltPrimary,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
