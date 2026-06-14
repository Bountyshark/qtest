package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
  primary = Indigo40,
  onPrimary = Color.White,
  primaryContainer = Indigo80,
  onPrimaryContainer = Indigo10,
  secondary = Teal30,
  onSecondary = Color.White,
  secondaryContainer = Teal90,
  onSecondaryContainer = Teal10,
  tertiary = Amber40,
  onTertiary = Color.White,
  tertiaryContainer = Amber90,
  onTertiaryContainer = Amber10,
  error = IncorrectRed,
  onError = Color.White,
  errorContainer = IncorrectRedLight,
  onErrorContainer = Color(0xFF410002),
  background = Neutral95,
  onBackground = Neutral10,
  surface = Neutral99,
  onSurface = Neutral10,
  surfaceVariant = Neutral90,
  onSurfaceVariant = Color(0xFF49454F),
  outline = Color(0xFFBDBDBD),
  outlineVariant = Color(0xFFE0E0E0),
)

private val DarkColorScheme = darkColorScheme(
  primary = Indigo80,
  onPrimary = Indigo10,
  primaryContainer = Indigo30,
  onPrimaryContainer = Indigo90,
  secondary = Teal80,
  onSecondary = Teal10,
  secondaryContainer = Teal20,
  onSecondaryContainer = Teal90,
  tertiary = Amber80,
  onTertiary = Amber10,
  tertiaryContainer = Amber30,
  onTertiaryContainer = Amber90,
  error = Color(0xFFEF9A9A),
  onError = Color(0xFF601410),
  errorContainer = Color(0xFFC62828),
  onErrorContainer = Color(0xFFFFDAD6),
  background = Neutral10,
  onBackground = Color(0xFFE6E1E5),
  surface = Color(0xFF121212),
  onSurface = Color(0xFFE6E1E5),
  surfaceVariant = Neutral20,
  onSurfaceVariant = Color(0xFFCAC4D0),
  outline = Color(0xFF616161),
  outlineVariant = Color(0xFF3A3A4A),
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
