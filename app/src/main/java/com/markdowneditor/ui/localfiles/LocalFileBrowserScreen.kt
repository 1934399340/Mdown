package com.markdowneditor.ui.localfiles

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markdowneditor.ui.theme.AppTheme
import com.markdowneditor.viewModel.LocalFileBrowserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val lastModified: Long,
    val size: Long
)

data class QuickAccessDir(
    val name: String,
    val path: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalFileBrowserScreen(
    onFileClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: LocalFileBrowserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val currentPath by viewModel.currentPath.collectAsState()
    val pathHistory by viewModel.pathHistory.collectAsState()
    var hasStoragePermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            hasStoragePermission = Environment.isExternalStorageManager()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                permissionLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                permissionLauncher.launch(intent)
            }
        }
    }

    val storageRoot = viewModel.getStorageRoot()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FileItem>?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    val quickAccessDirs = remember {
        val root = storageRoot.absolutePath
        listOf(
            QuickAccessDir("下载", "$root/Download"),
            QuickAccessDir("微信", "$root/Download/WeiXin"),
            QuickAccessDir("QQ", "$root/Tencent/QQfile_recv"),
            QuickAccessDir("文档", "$root/Documents"),
            QuickAccessDir("根目录", root)
        )
    }

    val filesInDir = remember(currentPath, hasStoragePermission) {
        if (!hasStoragePermission) return@remember emptyList()
        val dir = File(currentPath)
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()
                ?.sortedWith(compareByDescending<File> { it.isDirectory }.thenBy { it.name.lowercase() })
                ?.map { file ->
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        lastModified = file.lastModified(),
                        size = if (file.isFile) file.length() else 0L
                    )
                } ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun isSupportedFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".md") || lower.endsWith(".markdown") ||
            lower.endsWith(".txt") || lower.endsWith(".pdf") ||
            lower.endsWith(".doc") || lower.endsWith(".docx") ||
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
            lower.endsWith(".png") || lower.endsWith(".gif") ||
            lower.endsWith(".webp") || lower.endsWith(".bmp") ||
            lower.endsWith(".svg") || lower.endsWith(".csv") ||
            lower.endsWith(".xls") || lower.endsWith(".xlsx") ||
            lower.endsWith(".ppt") || lower.endsWith(".pptx") ||
            lower.endsWith(".zip") || lower.endsWith(".rar") ||
            lower.endsWith(".mp3") || lower.endsWith(".mp4") ||
            lower.endsWith(".avi") || lower.endsWith(".mov") ||
            lower.endsWith(".json") || lower.endsWith(".xml") ||
            lower.endsWith(".html") || lower.endsWith(".css") ||
            lower.endsWith(".js") || lower.endsWith(".kt") ||
            lower.endsWith(".java") || lower.endsWith(".py")
    }

    // Recursive search effect
    LaunchedEffect(searchQuery, currentPath, hasStoragePermission) {
        if (searchQuery.isBlank() || !hasStoragePermission) {
            searchResults = null
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        val query = searchQuery
        searchResults = withContext(Dispatchers.IO) {
            val results = mutableListOf<FileItem>()
            fun scanDir(dir: File, depth: Int) {
                if (depth > 10) return // limit recursion depth
                val children = dir.listFiles() ?: return
                for (child in children) {
                    if (child.isDirectory) {
                        scanDir(child, depth + 1)
                    } else if (child.name.contains(query, ignoreCase = true) && isSupportedFile(child.name)) {
                        results.add(
                            FileItem(
                                name = child.name,
                                path = child.absolutePath,
                                isDirectory = false,
                                lastModified = child.lastModified(),
                                size = child.length()
                            )
                        )
                    }
                }
            }
            scanDir(File(currentPath), 0)
            results.sortedByDescending { it.lastModified }
        }
        isSearching = false
    }

    val displayFiles = if (searchQuery.isBlank()) {
        filesInDir
    } else {
        searchResults ?: emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "浏览本地文件",
                        style = AppTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        currentPath,
                        style = AppTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (!viewModel.goBack()) {
                        onBackClick()
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(2f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAccessDirs.forEach { dir ->
                        Surface(
                            onClick = {
                                viewModel.navigateTo(dir.path)
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    when (dir.name) {
                                        "下载" -> Icons.Default.Download
                                        "根目录" -> Icons.Default.Home
                                        else -> Icons.Default.Folder
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    dir.name,
                                    style = AppTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(AppTheme.spacing.sm))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 100.dp)
                        .heightIn(max = 48.dp),
                    placeholder = {
                        Text(
                            "搜索子目录",
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "清除",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = AppTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            }
        }

        if (!hasStoragePermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        "需要文件访问权限",
                        style = AppTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        "请在设置中授予\"所有文件访问\"权限以浏览本地文件",
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    FilledTonalButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                try {
                                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                        data = android.net.Uri.parse("package:${context.packageName}")
                                    }
                                    permissionLauncher.launch(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                    permissionLauncher.launch(intent)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("前往设置")
                    }
                }
            }
        } else if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        "正在搜索...",
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else if (displayFiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.spacing.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        if (searchQuery.isBlank()) "此目录没有可打开的文件" else "未找到匹配的文件",
                        style = AppTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        if (searchQuery.isBlank()) "点击文件夹进入子目录查找" else "试试其他关键词",
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = AppTheme.spacing.lg,
                    end = AppTheme.spacing.lg,
                    top = AppTheme.spacing.sm,
                    bottom = AppTheme.spacing.xxl
                ),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
            ) {
                if (pathHistory.size > 1) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.goBack() }
                                .padding(horizontal = AppTheme.spacing.md, vertical = AppTheme.spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(AppTheme.spacing.sm))
                            Text(
                                "返回上级目录",
                                style = AppTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                items(displayFiles, key = { it.path }) { fileItem ->
                    val subtitle = if (searchQuery.isNotBlank() && !fileItem.isDirectory) {
                        fileItem.path.removePrefix(currentPath).removePrefix("/").let {
                            if (it == fileItem.name) null else File(it).parent ?: null
                        }
                    } else null
                    FileItemRow(
                        fileItem = fileItem,
                        subtitle = subtitle,
                        onClick = {
                            if (fileItem.isDirectory) {
                                viewModel.navigateTo(fileItem.path)
                            } else {
                                onFileClick(fileItem.path)
                            }
                        }
                    )
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

@Composable
private fun FileItemRow(
    fileItem: FileItem,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(fileItem.lastModified) { dateFormat.format(fileItem.lastModified) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (fileItem.isDirectory)
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (fileItem.isDirectory)
                            MaterialTheme.colorScheme.secondaryContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                val fileIcon = when {
                    fileItem.isDirectory -> Icons.Default.Folder
                    fileItem.name.endsWith(".jpg", ignoreCase = true) ||
                    fileItem.name.endsWith(".jpeg", ignoreCase = true) ||
                    fileItem.name.endsWith(".png", ignoreCase = true) ||
                    fileItem.name.endsWith(".gif", ignoreCase = true) ||
                    fileItem.name.endsWith(".webp", ignoreCase = true) ||
                    fileItem.name.endsWith(".bmp", ignoreCase = true) ||
                    fileItem.name.endsWith(".svg", ignoreCase = true) -> Icons.Default.Image
                    fileItem.name.endsWith(".mp3", ignoreCase = true) ||
                    fileItem.name.endsWith(".wav", ignoreCase = true) ||
                    fileItem.name.endsWith(".flac", ignoreCase = true) ||
                    fileItem.name.endsWith(".aac", ignoreCase = true) -> Icons.Default.MusicNote
                    fileItem.name.endsWith(".mp4", ignoreCase = true) ||
                    fileItem.name.endsWith(".avi", ignoreCase = true) ||
                    fileItem.name.endsWith(".mov", ignoreCase = true) ||
                    fileItem.name.endsWith(".mkv", ignoreCase = true) -> Icons.Default.OndemandVideo
                    else -> Icons.Default.InsertDriveFile
                }
                Icon(
                    fileIcon,
                    contentDescription = null,
                    tint = if (fileItem.isDirectory)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(AppTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileItem.name,
                    style = AppTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = formattedDate,
                        style = AppTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (!fileItem.isDirectory) {
                val sizeText = when {
                    fileItem.size < 1024 -> "${fileItem.size} B"
                    fileItem.size < 1024 * 1024 -> "${fileItem.size / 1024} KB"
                    else -> "${fileItem.size / (1024 * 1024)} MB"
                }
                Text(
                    text = sizeText,
                    style = AppTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
