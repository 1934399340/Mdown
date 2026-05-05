package com.markdowneditor.ui.fileviewer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.markdowneditor.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private enum class ViewerType { IMAGE, PDF, OFFICE, UNKNOWN }

private fun viewerTypeFor(filePath: String): ViewerType {
    val ext = filePath.substringAfterLast('.').lowercase()
    return when (ext) {
        "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg" -> ViewerType.IMAGE
        "pdf" -> ViewerType.PDF
        "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv" -> ViewerType.OFFICE
        else -> ViewerType.UNKNOWN
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileViewerScreen(
    filePath: String,
    onBackClick: () -> Unit
) {
    val file = remember(filePath) { File(filePath) }
    val fileName = remember(file) { file.name }
    val viewerType = remember(filePath) { viewerTypeFor(filePath) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
            title = {
                Text(
                    fileName,
                    style = AppTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
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

        when (viewerType) {
            ViewerType.IMAGE -> ImageViewer(file)
            ViewerType.PDF -> PdfViewer(file)
            ViewerType.OFFICE -> OfficeFileViewer(file)
            ViewerType.UNKNOWN -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                    ) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "不支持的文件格式",
                            style = AppTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun ImageViewer(file: File) {
    var loadError by remember { mutableStateOf(false) }

    if (loadError) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Icon(
                    Icons.Default.BrokenImage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "图片加载失败",
                    style = AppTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    } else {
        AsyncImage(
            model = file,
            contentDescription = file.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.spacing.md),
            contentScale = ContentScale.Fit,
            onError = { loadError = true }
        )
    }
}

@Composable
private fun PdfViewer(file: File) {
    var pages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(file.absolutePath) {
        isLoading = true
        loadError = null
        try {
            val bitmaps = withContext(Dispatchers.IO) {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val result = mutableListOf<Bitmap>()
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val scale = 2f
                    val bitmap = Bitmap.createBitmap(
                        (page.width * scale).toInt(),
                        (page.height * scale).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    result.add(bitmap)
                }
                renderer.close()
                pfd.close()
                result
            }
            pages = bitmaps
        } catch (e: Exception) {
            loadError = "无法加载PDF: ${e.message}"
        }
        isLoading = false
    }

    DisposableEffect(Unit) {
        onDispose {
            pages.forEach { it.recycle() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        "正在加载PDF...",
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            loadError != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.md))
                    Text(
                        loadError!!,
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AppTheme.spacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    pages.forEachIndexed { index, bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "第 ${index + 1} 页",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OfficeFileViewer(file: File) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val ext = file.extension.uppercase()
        val icon = when (ext) {
            "DOC", "DOCX" -> Icons.Default.Description
            "XLS", "XLSX" -> Icons.Default.Description
            "PPT", "PPTX" -> Icons.Default.Description
            else -> Icons.Default.Description
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.lg))

        Text(
            file.name,
            style = AppTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.sm))

        Text(
            "$ext 文件",
            style = AppTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppTheme.spacing.xl))

        var openError by remember { mutableStateOf(false) }

        FilledTonalButton(
            onClick = {
                try {
                    val uri = getFileUri(context, file)
                    val mimeType = when (ext) {
                        "DOC" -> "application/msword"
                        "DOCX" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        "XLS" -> "application/vnd.ms-excel"
                        "XLSX" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        "PPT" -> "application/vnd.ms-powerpoint"
                        "PPTX" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                        "CSV" -> "text/csv"
                        else -> "*/*"
                    }
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    openError = true
                }
            },
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("用其他应用打开")
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.md))

        if (openError) {
            Text(
                "没有找到可以打开 $ext 文件的应用",
                style = AppTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                "请确保手机上已安装可以打开 $ext 文件的应用",
                style = AppTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

private fun getFileUri(context: android.content.Context, file: File): Uri {
    // Try FileProvider first (for app-private files)
    return try {
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: IllegalArgumentException) {
        // File not in FileProvider paths, try MediaStore
        queryMediaStoreUri(context, file)
            ?: copyToTempAndGetUri(context, file)
    }
}

private fun queryMediaStoreUri(context: android.content.Context, file: File): Uri? {
    val projection = arrayOf(MediaStore.MediaColumns._ID)
    val selection = "${MediaStore.MediaColumns.DATA} = ?"
    val selectionArgs = arrayOf(file.absolutePath)

    context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            return Uri.withAppendedPath(
                MediaStore.Files.getContentUri("external"),
                id.toString()
            )
        }
    }
    return null
}

private fun copyToTempAndGetUri(context: android.content.Context, file: File): Uri {
    val tempFile = File(context.cacheDir, "shared_${file.name}")
    file.copyTo(tempFile, overwrite = true)
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
