package com.markdowneditor.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FontSettings() {
    var fontSize by remember { mutableStateOf(16) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "字体大小: $fontSize")
            Slider(
                value = fontSize.toFloat(),
                onValueChange = { fontSize = it.toInt() },
                valueRange = 12f..24f,
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "字体类型")
            DropdownMenu(
                expanded = false,
                onDismissRequest = { /* 实际实现中处理 */ }
            ) {
                // 实际实现中添加字体选项
            }
        }
    }
}
