package com.markdowneditor.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("设置") }
        )
        
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "主题设置", style = MaterialTheme.typography.headlineSmall)
            ThemeSettings()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(text = "字体设置", style = MaterialTheme.typography.headlineSmall)
            FontSettings()
        }
    }
}
