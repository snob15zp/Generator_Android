package com.inhealion.compose.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val colors = lightColors(
    primary = Color(0xff197bac),
    primaryVariant = Color(0xff115293),
    onPrimary = Color(0xffffffff),
    secondary = Color(0xff00b0d3),
    secondaryVariant = Color(0xffab003c),
    onSecondary = Color(0xff000000),
)

@Composable
fun AppTheme(content: @Composable() () -> Unit) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(colors.primaryVariant)
    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}
