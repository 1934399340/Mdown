package com.markdowneditor.ui.editor

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun MarkdownEditor(
    text: String,
    onTextChange: (String) -> Unit,
    onCursorPositionChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf(TextFieldValue(text)) 
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            textFieldValue = newValue
            onTextChange(newValue.text)
            onCursorPositionChange(newValue.selection.start)
        },
        modifier = modifier,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { innerTextField ->
            if (text.isEmpty()) {
                Text(
                    text = "开始编写Markdown...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            innerTextField()
        }
    )
}
