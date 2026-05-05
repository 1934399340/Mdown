package com.markdowneditor.ui.filebrowser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.markdowneditor.data.model.MarkdownFile
import com.markdowneditor.ui.theme.AppTheme
import com.markdowneditor.viewModel.ClipboardAction
import com.markdowneditor.viewModel.ClipboardState
import com.markdowneditor.viewModel.FileBrowserViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel = hiltViewModel(),
    onFileClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLocalFilesClick: () -> Unit = {}
) {
    val files by viewModel.files.collectAsState()
    val clipboard by viewModel.clipboard.collectAsState()
    val pasteMessage by viewModel.pasteMessage.collectAsState()
    val currentFolder by viewModel.currentFolder.collectAsState()
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<MarkdownFile?>(null) }
    var contextMenuFile by remember { mutableStateOf<MarkdownFile?>(null) }
    var exportMenuFile by remember { mutableStateOf<MarkdownFile?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    var newFileName by remember { mutableStateOf("") }
    var newFolderName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val displayFiles = remember(files, currentFolder, searchQuery) {
        val filtered = if (searchQuery.isBlank()) files
        else files.filter { it.fileName.contains(searchQuery, ignoreCase = true) }

        if (currentFolder == null) {
            filtered
        } else {
            val folderPath = currentFolder!!
            val folder = File(folderPath)
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()
                    ?.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
                    ?.map { file ->
                        MarkdownFile(
                            fileName = if (file.isDirectory) file.name else file.nameWithoutExtension,
                            filePath = file.absolutePath,
                            lastModified = java.util.Date(file.lastModified()),
                            size = if (file.isFile) file.length() else 0L,
                            isDirectory = file.isDirectory
                        )
                    }
                    ?.filter { file ->
                        file.isDirectory ||
                        file.filePath.endsWith(".md", ignoreCase = true) ||
                        file.filePath.endsWith(".markdown", ignoreCase = true) ||
                        file.filePath.endsWith(".txt", ignoreCase = true) ||
                        file.filePath.endsWith(".pdf", ignoreCase = true) ||
                        file.filePath.endsWith(".doc", ignoreCase = true) ||
                        file.filePath.endsWith(".docx", ignoreCase = true) ||
                        file.filePath.endsWith(".xls", ignoreCase = true) ||
                        file.filePath.endsWith(".xlsx", ignoreCase = true) ||
                        file.filePath.endsWith(".ppt", ignoreCase = true) ||
                        file.filePath.endsWith(".pptx", ignoreCase = true) ||
                        file.filePath.endsWith(".png", ignoreCase = true) ||
                        file.filePath.endsWith(".jpg", ignoreCase = true) ||
                        file.filePath.endsWith(".jpeg", ignoreCase = true) ||
                        file.filePath.endsWith(".gif", ignoreCase = true) ||
                        file.filePath.endsWith(".webp", ignoreCase = true) ||
                        file.filePath.endsWith(".bmp", ignoreCase = true) ||
                        file.filePath.endsWith(".svg", ignoreCase = true)
                    }
                    ?: emptyList()
            } else {
                emptyList()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFilesFromDisk()
    }

    LaunchedEffect(pasteMessage) {
        if (pasteMessage != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearPasteMessage()
        }
    }

    if (showCreateFileDialog) {
        CreateFileDialog(
            fileName = newFileName,
            onFileNameChange = { newFileName = it },
            onConfirm = {
                if (newFileName.isNotBlank()) {
                    viewModel.createFile(newFileName, currentFolder)
                    newFileName = ""
                    showCreateFileDialog = false
                }
            },
            onDismiss = {
                newFileName = ""
                showCreateFileDialog = false
            }
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            folderName = newFolderName,
            onFolderNameChange = { newFolderName = it },
            onConfirm = {
                if (newFolderName.isNotBlank()) {
                    if (currentFolder != null) {
                        val folder = File(currentFolder!!, newFolderName)
                        folder.mkdirs()
                    } else {
                        viewModel.createFolder(newFolderName)
                    }
                    newFolderName = ""
                    showCreateFolderDialog = false
                    viewModel.loadFilesFromDisk()
                }
            },
            onDismiss = {
                newFolderName = ""
                showCreateFolderDialog = false
            }
        )
    }

    showDeleteDialog?.let { file ->
        DeleteConfirmDialog(
            fileName = file.fileName,
            isDirectory = file.isDirectory,
            onConfirm = {
                viewModel.deleteFile(file)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }

    contextMenuFile?.let { file ->
        FileContextMenuDialog(
            file = file,
            clipboard = clipboard,
            onCopy = {
                viewModel.copyFile(file)
                contextMenuFile = null
            },
            onCut = {
                viewModel.cutFile(file)
                contextMenuFile = null
            },
            onPaste = {
                viewModel.pasteToFolder(if (file.isDirectory) file.filePath else currentFolder)
                contextMenuFile = null
            },
            onExport = {
                if (!file.isDirectory) {
                    exportMenuFile = file
                }
                contextMenuFile = null
            },
            onDelete = {
                showDeleteDialog = file
                contextMenuFile = null
            },
            onDismiss = { contextMenuFile = null }
        )
    }

    exportMenuFile?.let { file ->
        FileExportDialog(
            file = file,
            onExport = { format ->
                viewModel.exportFile(file, format) { msg ->
                    exportMessage = msg
                }
                exportMenuFile = null
            },
            onDismiss = { exportMenuFile = null }
        )
    }

    if (exportMessage != null) {
        AlertDialog(
            onDismissRequest = { exportMessage = null },
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
                Text("导出结果", style = AppTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Text(exportMessage!!, style = AppTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                TextButton(onClick = { exportMessage = null }) {
                    Text("确定", style = AppTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    Text(
                        "Mdown",
                        style = AppTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(AppTheme.spacing.xl))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(22.dp))
                    },
                    label = {
                        Text("我的文档", style = AppTheme.typography.titleMedium)
                    },
                    selected = true,
                    onClick = {
                        viewModel.setCurrentFolder(null)
                        viewModel.loadFilesFromDisk()
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.md),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(22.dp))
                    },
                    label = {
                        Text("浏览本地文件", style = AppTheme.typography.titleMedium)
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLocalFilesClick()
                    },
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.md),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(AppTheme.spacing.xs))

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(22.dp))
                    },
                    label = {
                        Text("设置", style = AppTheme.typography.titleMedium)
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSettingsClick()
                    },
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.md),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                Text(
                    "版本 1.7",
                    style = AppTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = AppTheme.spacing.xl)
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    if (isSearching) {
                        SearchTopBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClose = {
                            isSearching = false
                            searchQuery = ""
                        }
                    )
                } else {
                    MainTopBar(
                        currentFolder = currentFolder,
                        clipboard = clipboard,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchClick = { isSearching = true },
                        onLocalFilesClick = onLocalFilesClick,
                        onBackToRoot = {
                            viewModel.setCurrentFolder(null)
                            viewModel.loadFilesFromDisk()
                        },
                        onPasteClick = {
                            viewModel.pasteToFolder(currentFolder)
                        },
                        onClearClipboard = {
                            viewModel.clearClipboard()
                        }
                    )
                }
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { showCreateFolderDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = AppTheme.elevation.level2
                        )
                    ) {
                        Icon(
                            Icons.Default.CreateNewFolder,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "新建文件夹",
                            style = AppTheme.typography.labelLarge
                        )
                    }
                    ExtendedFloatingActionButton(
                        onClick = { showCreateFileDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = AppTheme.elevation.level3
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "新建文件",
                            style = AppTheme.typography.labelLarge
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (displayFiles.isEmpty()) {
                    EmptyStateContent(isInFolder = currentFolder != null)
                } else {
                    FileListContent(
                        files = displayFiles,
                        onFileClick = onFileClick,
                        onFolderClick = { folderPath ->
                            viewModel.setCurrentFolder(folderPath)
                        },
                        onLongPress = { file ->
                            contextMenuFile = file
                        },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }

                AnimatedVisibility(
                    visible = pasteMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 160.dp)
                ) {
                    pasteMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            tonalElevation = AppTheme.elevation.level3,
                            modifier = Modifier.padding(horizontal = AppTheme.spacing.lg)
                        ) {
                            Text(
                                text = msg,
                                style = AppTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)
                            )
                        }
                    }
                }
            }
        }

        // Gradient status bar overlay
        val density = LocalDensity.current
        val statusBarHeightDp = with(density) {
            WindowInsets.statusBars.getTop(density).toDp()
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(statusBarHeightDp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(
    currentFolder: String?,
    clipboard: ClipboardState?,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLocalFilesClick: () -> Unit,
    onBackToRoot: () -> Unit,
    onPasteClick: () -> Unit,
    onClearClipboard: () -> Unit
) {
    val folderName = remember(currentFolder) {
        if (currentFolder != null) File(currentFolder).name else null
    }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentFolder != null) {
                    IconButton(onClick = onBackToRoot) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "返回根目录",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        folderName ?: "我的文档",
                        style = AppTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "我的文档",
                        style = AppTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "菜单",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            if (clipboard != null) {
                IconButton(onClick = onPasteClick) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "粘贴",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onClearClipboard) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "取消剪贴板",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onLocalFilesClick) {
                Icon(
                    Icons.Default.FolderOpen,
                    contentDescription = "本地文件",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "搜索文件...",
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = AppTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        navigationIcon = {
            TextButton(onClick = onClose) {
                Text(
                    "取消",
                    style = AppTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun FileListContent(
    files: List<MarkdownFile>,
    onFileClick: (String) -> Unit,
    onFolderClick: (String) -> Unit,
    onLongPress: (MarkdownFile) -> Unit,
    onDeleteClick: (MarkdownFile) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = AppTheme.spacing.lg,
            end = AppTheme.spacing.lg,
            top = AppTheme.spacing.sm,
            bottom = 160.dp
        ),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
    ) {
        items(files, key = { it.filePath }) { file ->
            FileCard(
                file = file,
                onClick = {
                    if (file.isDirectory) {
                        onFolderClick(file.filePath)
                    } else {
                        onFileClick(file.filePath)
                    }
                },
                onLongPress = { onLongPress(file) },
                onDelete = { onDeleteClick(file) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileCard(
    file: MarkdownFile,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val formattedDate = remember(file.lastModified) { dateFormat.format(file.lastModified) }
    val sizeText = remember(file.size) {
        when {
            file.size < 1024 -> "${file.size} B"
            file.size < 1024 * 1024 -> "${file.size / 1024} KB"
            else -> "${file.size / (1024 * 1024)} MB"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (file.isDirectory)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (file.isDirectory)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when {
                        file.isDirectory -> Icons.Default.Folder
                        file.filePath.endsWith(".pdf", ignoreCase = true) -> Icons.Default.PictureAsPdf
                        file.filePath.endsWith(".png", ignoreCase = true) ||
                        file.filePath.endsWith(".jpg", ignoreCase = true) ||
                        file.filePath.endsWith(".jpeg", ignoreCase = true) ||
                        file.filePath.endsWith(".gif", ignoreCase = true) ||
                        file.filePath.endsWith(".webp", ignoreCase = true) ||
                        file.filePath.endsWith(".bmp", ignoreCase = true) ||
                        file.filePath.endsWith(".svg", ignoreCase = true) -> Icons.Default.Image
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = if (file.isDirectory)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fileName,
                    style = AppTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    Text(
                        text = formattedDate,
                        style = AppTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    if (!file.isDirectory) {
                        Text(
                            text = "·",
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = sizeText,
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    } else {
                        val subCount = remember(file.filePath) {
                            val dir = File(file.filePath)
                            if (dir.exists()) {
                                val mdFiles = dir.listFiles()?.filter {
                                    it.isFile && it.name.endsWith(".md", ignoreCase = true)
                                }?.size ?: 0
                                val subDirs = dir.listFiles()?.filter { it.isDirectory }?.size ?: 0
                                when {
                                    subDirs > 0 && mdFiles > 0 -> "$subDirs 个文件夹, $mdFiles 个文件"
                                    subDirs > 0 -> "$subDirs 个文件夹"
                                    mdFiles > 0 -> "$mdFiles 个文件"
                                    else -> "空文件夹"
                                }
                            } else "空文件夹"
                        }
                        Text(
                            text = subCount,
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FileContextMenuDialog(
    file: MarkdownFile,
    clipboard: ClipboardState?,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onPaste: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
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
                    if (file.isDirectory) Icons.Default.Folder else Icons.Default.Description,
                    contentDescription = null,
                    tint = if (file.isDirectory) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
                Text(
                    file.fileName,
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                ContextMenuItem(
                    icon = Icons.Default.ContentCopy,
                    label = "复制",
                    description = "复制到其他文件夹",
                    onClick = onCopy
                )
                ContextMenuItem(
                    icon = Icons.Default.ContentCut,
                    label = "剪切",
                    description = "移动到其他文件夹",
                    onClick = onCut
                )
                if (clipboard != null && file.isDirectory) {
                    ContextMenuItem(
                        icon = Icons.Default.ContentCopy,
                        label = "粘贴到此文件夹",
                        description = if (clipboard.action == ClipboardAction.CUT) "移动" else "复制" + "「${clipboard.file.fileName}」",
                        onClick = onPaste
                    )
                }
                if (!file.isDirectory) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ContextMenuItem(
                        icon = Icons.Default.FileDownload,
                        label = "导出",
                        description = "导出为 PDF、图片等格式",
                        onClick = onExport
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ContextMenuItem(
                    icon = Icons.Default.DeleteOutline,
                    label = "删除",
                    description = if (file.isDirectory) "删除文件夹及所有内容" else "删除此文件",
                    onClick = onDelete,
                    isDestructive = true
                )
            }
        },
        confirmButton = {},
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

@Composable
private fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
            Column {
                Text(
                    label,
                    style = AppTheme.typography.titleMedium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    description,
                    style = AppTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateContent(isInFolder: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isInFolder) Icons.Default.FolderOpen else Icons.Outlined.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                if (isInFolder) "此文件夹为空" else "暂无文档",
                style = AppTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                if (isInFolder) "点击右下角按钮添加文件" else "点击右下角按钮创建你的第一个文档",
                style = AppTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun CreateFileDialog(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "新建文档",
                style = AppTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    "输入文件名，将自动创建 .md 文件",
                    style = AppTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                OutlinedTextField(
                    value = fileName,
                    onValueChange = onFileNameChange,
                    label = {
                        Text(
                            "文件名",
                            style = AppTheme.typography.bodySmall
                        )
                    },
                    placeholder = {
                        Text(
                            "未命名文档",
                            style = AppTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = AppTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                enabled = fileName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                Text("创建", style = AppTheme.typography.labelLarge)
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

@Composable
private fun CreateFolderDialog(
    folderName: String,
    onFolderNameChange: (String) -> Unit,
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
                    Icons.Default.CreateNewFolder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "新建文件夹",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column {
                Text(
                    "创建文件夹来分类管理你的文档",
                    style = AppTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.lg))
                OutlinedTextField(
                    value = folderName,
                    onValueChange = onFolderNameChange,
                    label = {
                        Text(
                            "文件夹名",
                            style = AppTheme.typography.bodySmall
                        )
                    },
                    placeholder = {
                        Text(
                            "新建文件夹",
                            style = AppTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = AppTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                enabled = folderName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                )
            ) {
                Text("创建", style = AppTheme.typography.labelLarge)
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

@Composable
private fun DeleteConfirmDialog(
    fileName: String,
    isDirectory: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                if (isDirectory) "删除文件夹" else "删除文档",
                style = AppTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                if (isDirectory) "确定要删除文件夹 \"$fileName\" 及其所有内容吗？此操作不可恢复。"
                else "确定要删除 \"$fileName\" 吗？此操作不可恢复。",
                style = AppTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("删除", style = AppTheme.typography.labelLarge)
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

private val Color = androidx.compose.ui.graphics.Color

@Composable
private fun FileExportDialog(
    file: MarkdownFile,
    onExport: (String) -> Unit,
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
                    Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "导出「${file.fileName}」",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                Text(
                    "选择导出格式，文件将保存到 Download/Mdown/ 目录",
                    style = AppTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                ContextMenuItem(
                    icon = Icons.Default.PictureAsPdf,
                    label = "PDF 文档",
                    description = "适合打印和分享",
                    onClick = { onExport("pdf") }
                )
                ContextMenuItem(
                    icon = Icons.Default.Image,
                    label = "图片 PNG",
                    description = "长截图保存",
                    onClick = { onExport("image") }
                )
                ContextMenuItem(
                    icon = Icons.Default.Language,
                    label = "HTML 网页",
                    description = "带样式的网页格式",
                    onClick = { onExport("html") }
                )
                ContextMenuItem(
                    icon = Icons.Default.Share,
                    label = "分享源文件",
                    description = "转发 Markdown 文件给其他应用",
                    onClick = { onExport("md") }
                )
                ContextMenuItem(
                    icon = Icons.Default.Article,
                    label = "纯文本 TXT",
                    description = "去除格式的纯文本",
                    onClick = { onExport("txt") }
                )
            }
        },
        confirmButton = {},
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
