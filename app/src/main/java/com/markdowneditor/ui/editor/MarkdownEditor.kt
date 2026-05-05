package com.markdowneditor.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.markdowneditor.ui.theme.AppTheme
import kotlinx.coroutines.delay

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MarkdownEditor(
    text: String,
    onTextChange: (String) -> Unit,
    cursorPosition: Int,
    onCursorPositionChange: (Int) -> Unit = {},
    onSelectionChange: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val scrollState = rememberScrollState()

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text, selection = androidx.compose.ui.text.TextRange(text.length)))
    }

    LaunchedEffect(text, cursorPosition) {
        val clampedPos = cursorPosition.coerceIn(0, text.length)
        val newSelection = androidx.compose.ui.text.TextRange(clampedPos)
        if (textFieldValue.text != text || textFieldValue.selection != newSelection) {
            textFieldValue = TextFieldValue(
                text = text,
                selection = newSelection,
                composition = null
            )
        }
    }

    // Auto-scroll to cursor when cursor position changes
    LaunchedEffect(cursorPosition) {
        delay(100)
        bringIntoViewRequester.bringIntoView()
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .verticalScroll(scrollState)
            .bringIntoViewRequester(bringIntoViewRequester)
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                onTextChange(newValue.text)
                onCursorPositionChange(newValue.selection.start)
                onSelectionChange(newValue.selection.start, newValue.selection.end)
            },
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = LocalConfiguration.current.screenHeightDp.dp),
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = AppTheme.typography.editorText.fontSize,
                fontWeight = AppTheme.typography.editorText.fontWeight,
                lineHeight = AppTheme.typography.editorText.lineHeight,
                letterSpacing = AppTheme.typography.editorText.letterSpacing
            ),
            decorationBox = { innerTextField ->
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        text = "开始编写 Markdown ...",
                        style = AppTheme.typography.editorText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                innerTextField()
            }
        )
    }
}
