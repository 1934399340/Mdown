package com.markdowneditor.ui.editor

import android.net.Uri
import android.view.KeyEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Brush
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markdowneditor.ui.theme.AppTheme
import com.markdowneditor.viewModel.EditorViewModel

enum class EditorViewMode { RICH, SOURCE, SPLIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    filePath: String,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.smallestScreenWidthDp >= 600
    val view = LocalView.current
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val markdownText by viewModel.markdownText.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    val showShareDialog by viewModel.showShareDialog.collectAsState()
    val showLinkDialog by viewModel.showLinkDialog.collectAsState()

    // AI 聊天状态
    val aiPrompt by viewModel.aiPrompt.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    var viewMode by remember { mutableStateOf(if (isTablet) EditorViewMode.SPLIT else EditorViewMode.RICH) }
    var showSaveToast by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.insertImageFromUri(uri)
        }
        viewModel.onImagePickerHandled()
    }

    val pickImageTrigger by viewModel.pickImageTrigger.collectAsState()
    LaunchedEffect(pickImageTrigger) {
        if (pickImageTrigger) {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    LaunchedEffect(filePath) {
        viewModel.loadFile(filePath)
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.saveFile()
        }
    }

    // Keyboard shortcuts
    DisposableEffect(Unit) {
        val keyEventHandler = android.view.View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && event.isCtrlPressed) {
                when {
                    event.isShiftPressed -> {
                        when (keyCode) {
                            KeyEvent.KEYCODE_K -> { viewModel.insertCodeBlock(); true }
                            KeyEvent.KEYCODE_M -> { viewModel.insertMathBlock(); true }
                            KeyEvent.KEYCODE_I -> { viewModel.insertImage(); true }
                            KeyEvent.KEYCODE_U -> { viewModel.insertStrikethrough(); true }
                            else -> false
                        }
                    }
                    else -> when (keyCode) {
                        KeyEvent.KEYCODE_Z -> { viewModel.undo(); true }
                        KeyEvent.KEYCODE_1 -> { viewModel.insertHeading(1); true }
                        KeyEvent.KEYCODE_2 -> { viewModel.insertHeading(2); true }
                        KeyEvent.KEYCODE_3 -> { viewModel.insertHeading(3); true }
                        KeyEvent.KEYCODE_4 -> { viewModel.insertHeading(4); true }
                        KeyEvent.KEYCODE_5 -> { viewModel.insertHeading(5); true }
                        KeyEvent.KEYCODE_6 -> { viewModel.insertHeading(6); true }
                        KeyEvent.KEYCODE_B -> { viewModel.insertMarkdown("**", "**"); true }
                        KeyEvent.KEYCODE_I -> { viewModel.insertMarkdown("*", "*"); true }
                        KeyEvent.KEYCODE_S -> { viewModel.saveFile(); showSaveToast = true; true }
                        KeyEvent.KEYCODE_K -> { viewModel.insertLink(); true }
                        KeyEvent.KEYCODE_U -> { viewModel.insertMarkdown("<u>", "</u>"); true }
                        KeyEvent.KEYCODE_SLASH -> { viewModel.insertCodeBlock(); true }
                        KeyEvent.KEYCODE_L -> { viewModel.insertUnorderedList(); true }
                        KeyEvent.KEYCODE_O -> { viewModel.insertOrderedList(); true }
                        KeyEvent.KEYCODE_Q -> { viewModel.insertQuote(); true }
                        KeyEvent.KEYCODE_T -> { viewModel.insertTable(); true }
                        KeyEvent.KEYCODE_H -> { viewModel.insertHorizontalRule(); true }
                        KeyEvent.KEYCODE_D -> { viewModel.insertStrikethrough(); true }
                        KeyEvent.KEYCODE_ENTER -> { viewModel.insertNewLine(); true }
                        KeyEvent.KEYCODE_LEFT_BRACKET -> { viewModel.decreaseHeadingLevel(); true }
                        KeyEvent.KEYCODE_RIGHT_BRACKET -> { viewModel.increaseHeadingLevel(); true }
                        else -> false
                    }
                }
            } else false
        }
        view.setOnKeyListener(keyEventHandler)
        onDispose { view.setOnKeyListener(null) }
    }

    LaunchedEffect(showSaveToast) {
        if (showSaveToast) {
            kotlinx.coroutines.delay(1500)
            showSaveToast = false
        }
    }

    if (!isLoaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (showExportDialog) {
        ExportDialog(
            onDismiss = { viewModel.dismissExportDialog() },
            onExportPdf = { viewModel.exportAsPdf() },
            onExportHtml = { viewModel.exportAsHtml() },
            onExportImage = { viewModel.exportAsImage() },
            onExportTxt = { viewModel.exportAsTxt() },
            onExportMd = { viewModel.exportAsMd() }
        )
    }

    if (showShareDialog && exportMessage != null) {
        ShareDialog(
            message = exportMessage!!,
            onShare = { viewModel.shareExportedFile() },
            onDismiss = {
                viewModel.dismissShareDialog()
                viewModel.clearExportMessage()
            }
        )
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    if (showLinkDialog) {
        var linkUrl by remember { mutableStateOf("https://") }
        LinkDialog(
            presetText = viewModel.getLinkPresetText(),
            url = linkUrl,
            onUrlChange = { linkUrl = it },
            onConfirm = { viewModel.insertLinkWithUrl(linkUrl) },
            onDismiss = { viewModel.dismissLinkDialog() }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Status bar colored strip
            val density = LocalDensity.current
            val statusBarHeightDp = with(density) {
                WindowInsets.statusBars.getTop(density).toDp()
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarHeightDp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            )

            // Custom TopBar: left=title+undo, center=mode switch, right=save+export
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
                val modeButtonH = if (isTablet || isLandscape) 40.dp else 32.dp
                val modeIconSize = if (isTablet || isLandscape) 20.dp else 16.dp
                val modeTextStyle = if (isTablet || isLandscape) AppTheme.typography.labelMedium
                    else AppTheme.typography.labelSmall

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: back + filename (weight 1, aligned start)
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = {
                            viewModel.saveFile()
                            backDispatcher?.onBackPressed()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = if (fileName.isBlank()) "未命名" else fileName,
                            style = AppTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Undo (right next to filename, file-withdrawal icon)
                        IconButton(onClick = { viewModel.undo() }) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = com.markdowneditor.R.drawable.ic_undo_file),
                                contentDescription = "撤销",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Center: mode switch buttons (icons only in portrait, icons+text in landscape/tablet)
                    val showLabels = isTablet || isLandscape
                    val modes = listOf(
                        EditorViewMode.RICH to Icons.Default.Edit to "编辑",
                        EditorViewMode.SOURCE to Icons.Default.Code to "源代码",
                        EditorViewMode.SPLIT to Icons.Default.VerticalSplit to "分屏"
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        modes.forEach { (pair, label) ->
                            val (mode, icon) = pair
                            val isSelected = viewMode == mode
                            Surface(
                                onClick = { viewMode = mode },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier
                                        .height(modeButtonH)
                                        .padding(horizontal = if (showLabels) 16.dp else 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(if (showLabels) 6.dp else 4.dp)
                                ) {
                                    Icon(icon, contentDescription = null, modifier = Modifier.size(modeIconSize))
                                    if (showLabels) {
                                        Text(label, style = modeTextStyle)
                                    }
                                }
                            }
                        }
                    }

                    // Right: save + export (weight 1, aligned end)
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = {
                            viewModel.saveFile()
                            showSaveToast = true
                        }) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "保存",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { viewModel.showExportDialog() }) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = "导出",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 编辑器内容区 — weight(1f) 自动填充 TopBar 和 AiChatBar 之间的空间
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = viewMode,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        val slideDir = if (forward) -1 else 1
                        slideInHorizontally(
                            animationSpec = tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                            initialOffsetX = { it / 3 * slideDir }
                        ) + fadeIn(animationSpec = tween(200)) togetherWith
                            slideOutHorizontally(
                                animationSpec = tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                targetOffsetX = { it / 3 * -slideDir }
                            ) + fadeOut(animationSpec = tween(150))
                    },
                    contentKey = { it }
                ) { mode ->
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = true)
                    ) {
                        if (mode == EditorViewMode.RICH || mode == EditorViewMode.SOURCE || mode == EditorViewMode.SPLIT) {
                            FormattingToolbar(
                                viewModel = viewModel,
                                onHelpClick = { showHelpDialog = true }
                            )
                        }

                        when (mode) {
                            EditorViewMode.RICH -> {
                                RichEditorView(
                                    markdownText = markdownText,
                                    onTextChange = { viewModel.updateText(it) },
                                    cursorPosition = viewModel.cursorPosition,
                                    onCursorPositionChange = { viewModel.setCursorPosition(it) },
                                    onSelectionChange = { start, end -> viewModel.setSelection(start, end) },
                                    onWebViewReady = { webView -> viewModel.webViewRef = java.lang.ref.WeakReference(webView) },
                                    onShortcut = { action ->
                                        when (action) {
                                            "save" -> { viewModel.saveFile(); showSaveToast = true }
                                            "undo" -> viewModel.undo()
                                            "insertLink" -> viewModel.insertLink()
                                            "insertImage" -> viewModel.insertImage()
                                            "insertTable" -> viewModel.insertTable()
                                            "mathBlock" -> viewModel.insertMathBlock()
                                            "newLine" -> viewModel.insertNewLine()
                                            "decreaseHeading" -> viewModel.decreaseHeadingLevel()
                                            "increaseHeading" -> viewModel.increaseHeadingLevel()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                            EditorViewMode.SOURCE -> {
                                MarkdownEditor(
                                    text = markdownText,
                                    onTextChange = { viewModel.updateText(it) },
                                    cursorPosition = viewModel.cursorPosition,
                                    onCursorPositionChange = { viewModel.setCursorPosition(it) },
                                    onSelectionChange = { start, end -> viewModel.setSelection(start, end) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                )
                            }
                            EditorViewMode.SPLIT -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    MarkdownEditor(
                                        text = markdownText,
                                        onTextChange = { viewModel.updateText(it) },
                                        cursorPosition = viewModel.cursorPosition,
                                        onCursorPositionChange = { viewModel.setCursorPosition(it) },
                                        onSelectionChange = { start, end -> viewModel.setSelection(start, end) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                    VerticalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        thickness = 1.dp
                                    )
                                    MarkdownPreview(
                                        markdown = markdownText,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI 聊天输入栏 — 在编辑器内容下方，跟随键盘
            AiChatBar(
                prompt = aiPrompt,
                onPromptChange = { viewModel.setAiPrompt(it) },
                onSend = {
                    if (isAiLoading) viewModel.cancelAiTask()
                    else viewModel.sendAiPrompt()
                },
                isLoading = isAiLoading,
                error = aiError,
                onDismissError = { viewModel.clearAiError() }
            )
        }

        if (showSaveToast) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(AppTheme.spacing.lg),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("已保存", style = AppTheme.typography.labelLarge)
            }
        }

        if (exportMessage != null && !showShareDialog) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(AppTheme.spacing.lg),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(12.dp),
                action = {
                    TextButton(onClick = { viewModel.clearExportMessage() }) {
                        Text("关闭", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            ) {
                Text(exportMessage!!, style = AppTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ShareDialog(
    message: String,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "导出成功",
                style = AppTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                message,
                style = AppTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onShare,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("分享", style = AppTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("关闭", style = AppTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportHtml: () -> Unit,
    onExportTxt: () -> Unit,
    onExportMd: () -> Unit,
    onExportImage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                Icon(
                    Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "导出文件",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                Text(
                    "选择导出格式，文件将保存到 Download/Mdown/ 目录",
                    style = AppTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                ExportOptionItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = "PDF 文档",
                    description = "适合打印和分享",
                    onClick = { onExportPdf(); onDismiss() }
                )
                ExportOptionItem(
                    icon = Icons.Default.Image,
                    title = "图片 PNG",
                    description = "长截图保存",
                    onClick = { onExportImage(); onDismiss() }
                )
                ExportOptionItem(
                    icon = Icons.Default.Language,
                    title = "HTML 网页",
                    description = "带样式的网页格式",
                    onClick = { onExportHtml(); onDismiss() }
                )
                ExportOptionItem(
                    icon = Icons.Default.Share,
                    title = "分享源文件",
                    description = "转发 Markdown 文件给其他应用",
                    onClick = { onExportMd(); onDismiss() }
                )
                ExportOptionItem(
                    icon = Icons.AutoMirrored.Filled.Article,
                    title = "纯文本 TXT",
                    description = "去除格式的纯文本",
                    onClick = { onExportTxt(); onDismiss() }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun ExportOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
            Column {
                Text(title, style = AppTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(description, style = AppTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun FormattingToolbar(
    viewModel: EditorViewModel,
    onHelpClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            FormatButton(text = "H1") { viewModel.insertHeading(1) }
            FormatButton(text = "H2") { viewModel.insertHeading(2) }
            FormatButton(text = "H3") { viewModel.insertHeading(3) }
            FormatDivider()
            FormatButton(icon = Icons.Default.FormatBold, text = "B") { viewModel.insertBold() }
            FormatButton(icon = Icons.Default.FormatItalic, text = "I") { viewModel.insertItalic() }
            FormatButton(icon = Icons.Default.FormatStrikethrough, text = "S") { viewModel.insertStrikethrough() }
            FormatDivider()
            FormatButton(icon = Icons.Default.Code, text = "</>") { viewModel.insertCodeBlock() }
            FormatButton(text = ">") { viewModel.insertQuote() }
            FormatDivider()
            FormatButton(icon = Icons.AutoMirrored.Filled.FormatListBulleted, text = "UL") { viewModel.insertUnorderedList() }
            FormatButton(icon = Icons.Default.FormatListNumbered, text = "OL") { viewModel.insertOrderedList() }
            FormatButton(icon = Icons.Default.CheckBox, text = "Task") { viewModel.insertTaskList() }
            FormatDivider()
            FormatButton(icon = Icons.Default.Link, text = "Link") { viewModel.insertLink() }
            FormatButton(icon = Icons.Default.Image, text = "Img") { viewModel.insertImage() }
            FormatDivider()
            FormatButton(text = "---") { viewModel.insertHorizontalRule() }
            FormatButton(icon = Icons.Default.TableChart, text = "Table") { viewModel.insertTable() }
            FormatButton(icon = Icons.Default.Add, text = "+R") { viewModel.insertTableRow() }
            FormatButton(icon = Icons.Default.Add, text = "+C") { viewModel.insertTableColumn() }
            FormatButton(icon = Icons.Default.Remove, text = "-R") { viewModel.deleteTableRow() }
            FormatButton(icon = Icons.Default.Remove, text = "-C") { viewModel.deleteTableColumn() }
            FormatDivider()
            FormatButton(icon = Icons.AutoMirrored.Filled.HelpOutline, text = "?") { onHelpClick() }
        }
    }
}

@Composable
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            }
            Text(
                text = text,
                style = AppTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FormatDivider() {
    VerticalDivider(
        modifier = Modifier.height(20.dp).padding(horizontal = 2.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 1.dp
    )
}

@Composable
private fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "使用指南",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                HelpSection(
                    title = "快捷键（Ctrl +）",
                    items = listOf(
                        "B - 加粗", "I - 斜体", "D - 删除线",
                        "S - 保存", "Z - 撤销", "K - 插入链接",
                        "1~6 - 标题级别", "L - 无序列表", "O - 有序列表",
                        "Q - 引用", "T - 表格", "H - 分割线",
                        "/ - 代码块", "Enter - 换行",
                        "[ - 降低标题级别", "] - 提升标题级别"
                    )
                )
                HelpSection(
                    title = "Ctrl + Shift +",
                    items = listOf(
                        "K - 代码块", "M - 数学公式",
                        "I - 插入图片", "U - 删除线"
                    )
                )
                HelpSection(
                    title = "手势操作",
                    items = listOf(
                        "左滑 - 切换到下一个视图模式",
                        "右滑 - 切换到上一个视图模式"
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("知道了", style = AppTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
private fun HelpSection(title: String, items: List<String>) {
    Column {
        Text(
            title,
            style = AppTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { item ->
            Text(
                "  $item",
                style = AppTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AiChatBar(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    error: String?,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 错误提示
        if (error != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                    Text(
                        error,
                        style = AppTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissError,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // 聊天输入栏 — adjustNothing + imePadding 自行控制位置
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(
                topStart = if (error == null) 16.dp else 0.dp,
                topEnd = if (error == null) 16.dp else 0.dp
            ),
            modifier = Modifier.imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                // AI 图标
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = if (isLoading) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                // 输入框
                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    placeholder = {
                        Text(
                            "告诉 AI 你想写什么...",
                            style = AppTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    enabled = !isLoading,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    textStyle = AppTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )

                // 发送按钮 / 旋转停止按钮
                if (isLoading) {
                    IconButton(
                        onClick = onSend,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = "停止生成",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = onSend,
                        enabled = prompt.isNotBlank(),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "发送",
                            tint = if (prompt.isNotBlank()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkDialog(
    presetText: String,
    url: String,
    onUrlChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "插入链接",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                if (presetText.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "文本: $presetText",
                            style = AppTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AppTheme.spacing.md)
                        )
                    }
                }
                OutlinedTextField(
                    value = url,
                    onValueChange = onUrlChange,
                    label = { Text("链接地址") },
                    placeholder = { Text("https://example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("确定", style = AppTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "取消",
                    style = AppTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
