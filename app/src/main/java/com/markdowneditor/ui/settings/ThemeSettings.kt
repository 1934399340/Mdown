package com.markdowneditor.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.markdowneditor.utils.ThemeManager

@Composable
fun ThemeSettings() {
    var selectedTheme by remember { mutableStateOf(ThemeManager.ThemeType.LIGHT) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "浅色主题")
            RadioButton(
                selected = selectedTheme == ThemeManager.ThemeType.LIGHT,
                onClick = { selectedTheme = ThemeManager.ThemeType.LIGHT }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "深色主题")
            RadioButton(
                selected = selectedTheme == ThemeManager.ThemeType.DARK,
                onClick = { selectedTheme = ThemeManager.ThemeType.DARK }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "跟随系统")
            RadioButton(
                selected = selectedTheme == ThemeManager.ThemeType.SYSTEM,
                onClick = { selectedTheme = ThemeManager.ThemeType.SYSTEM }
            )
        }
    }
}
