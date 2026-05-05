package com.markdowneditor.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.markdowneditor.ui.editor.EditorScreen
import com.markdowneditor.ui.filebrowser.FileBrowserScreen
import com.markdowneditor.ui.fileviewer.FileViewerScreen
import com.markdowneditor.ui.localfiles.LocalFileBrowserScreen
import com.markdowneditor.ui.settings.SettingsScreen
import com.markdowneditor.ui.theme.MarkdownEditorTheme
import com.markdowneditor.utils.SecurityHelper
import com.markdowneditor.viewModel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

sealed class Screen(val route: String) {
    object FileBrowser : Screen("file_browser")
    object Editor : Screen("editor/{filePath}") {
        fun createRoute(filePath: String): String {
            val encoded = URLEncoder.encode(filePath, "UTF-8")
            return "editor/$encoded"
        }
    }
    object Settings : Screen("settings")
    object LocalFileBrowser : Screen("local_file_browser")
    object FileViewer : Screen("file_viewer/{filePath}") {
        fun createRoute(filePath: String): String {
            val encoded = URLEncoder.encode(filePath, "UTF-8")
            return "file_viewer/$encoded"
        }
    }
}

private fun isViewableFileType(filePath: String): Boolean {
    val lower = filePath.lowercase()
    return lower.endsWith(".pdf") ||
        lower.endsWith(".doc") || lower.endsWith(".docx") ||
        lower.endsWith(".xls") || lower.endsWith(".xlsx") ||
        lower.endsWith(".ppt") || lower.endsWith(".pptx") ||
        lower.endsWith(".csv") ||
        lower.endsWith(".png") || lower.endsWith(".jpg") ||
        lower.endsWith(".jpeg") || lower.endsWith(".gif") ||
        lower.endsWith(".webp") || lower.endsWith(".bmp") ||
        lower.endsWith(".svg")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var themeViewModel: ThemeViewModel

    private var hasHandledInitialIntent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isRestoring = savedInstanceState != null
        val initialFilePath = if (!isRestoring && !hasHandledInitialIntent) {
            hasHandledInitialIntent = true
            handleIncomingIntent(intent)
        } else null

        setContent {
            val darkMode by themeViewModel.darkMode.collectAsState()

            MarkdownEditorTheme(darkTheme = darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        initialFilePath = initialFilePath,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val filePath = handleIncomingIntent(intent)
        if (filePath != null) {
            hasHandledInitialIntent = true
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun handleIncomingIntent(intent: Intent?): String? {
        if (intent == null) return null

        val action = intent.action ?: return null
        if (action != Intent.ACTION_VIEW && action != Intent.ACTION_EDIT) return null

        val uri: Uri = intent.data ?: return null

        return try {
            val rawName = getFileNameFromUri(uri)
            val fileName = SecurityHelper.sanitizeFileName(rawName)

            if (!fileName.contains(".")) return null

            val allowedExts = setOf(
                "md", "markdown", "txt",
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg",
                "csv", "json", "xml", "html", "css", "js"
            )
            val ext = fileName.substringAfterLast('.', "").lowercase()
            if (ext !in allowedExts) return null

            val sharedDir = File(getExternalFilesDir(null), "shared")
            sharedDir.mkdirs()
            val destFile = File(sharedDir, fileName)

            if (!SecurityHelper.isPathSafe(destFile.absolutePath, sharedDir)) return null

            contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytes = 0L
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        totalBytes += bytesRead
                        if (totalBytes > SecurityHelper.getMaxFileSize()) {
                            destFile.delete()
                            return null
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName: String? = null

        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            }
        }

        if (fileName.isNullOrBlank()) {
            fileName = uri.pathSegments?.lastOrNull() ?: uri.lastPathSegment
        }

        if (fileName.isNullOrBlank()) {
            fileName = "shared_file.md"
        }

        if (!fileName!!.contains(".")) {
            fileName = "$fileName.md"
        }

        return fileName!!
    }
}

@Composable
fun MainNavigation(
    initialFilePath: String? = null,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()

    LaunchedEffect(initialFilePath) {
        if (initialFilePath != null) {
            val route = if (isViewableFileType(initialFilePath)) {
                Screen.FileViewer.createRoute(initialFilePath)
            } else {
                Screen.Editor.createRoute(initialFilePath)
            }
            navController.navigate(route) {
                popUpTo(Screen.FileBrowser.route) { inclusive = false }
            }
        }
    }

    val slideDuration = 280
    val enterTransition: EnterTransition = slideInHorizontally(
        animationSpec = tween(slideDuration, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        initialOffsetX = { it }
    ) + fadeIn(animationSpec = tween(slideDuration - 80))

    val exitTransition: ExitTransition = slideOutHorizontally(
        animationSpec = tween(slideDuration, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        targetOffsetX = { -it / 4 }
    ) + fadeOut(animationSpec = tween(slideDuration - 120))

    val popEnterTransition: EnterTransition = slideInHorizontally(
        animationSpec = tween(slideDuration, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        initialOffsetX = { -it / 4 }
    ) + fadeIn(animationSpec = tween(slideDuration - 80))

    val popExitTransition: ExitTransition = slideOutHorizontally(
        animationSpec = tween(slideDuration, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        targetOffsetX = { it }
    ) + fadeOut(animationSpec = tween(slideDuration - 120))

    NavHost(
        navController = navController,
        startDestination = Screen.FileBrowser.route,
        enterTransition = { enterTransition },
        exitTransition = { exitTransition },
        popEnterTransition = { popEnterTransition },
        popExitTransition = { popExitTransition }
    ) {
        composable(Screen.FileBrowser.route) {
            FileBrowserScreen(
                onFileClick = { filePath ->
                    if (isViewableFileType(filePath)) {
                        navController.navigate(Screen.FileViewer.createRoute(filePath))
                    } else {
                        navController.navigate(Screen.Editor.createRoute(filePath))
                    }
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onLocalFilesClick = {
                    navController.navigate(Screen.LocalFileBrowser.route)
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
            val filePath = URLDecoder.decode(encodedPath, "UTF-8")
            EditorScreen(filePath = filePath)
        }

        composable(
            route = Screen.FileViewer.route,
            arguments = listOf(
                navArgument("filePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("filePath") ?: ""
            val filePath = URLDecoder.decode(encodedPath, "UTF-8")
            FileViewerScreen(
                filePath = filePath,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                themeViewModel = themeViewModel
            )
        }

        composable(Screen.LocalFileBrowser.route) {
            LocalFileBrowserScreen(
                onFileClick = { filePath ->
                    if (isViewableFileType(filePath)) {
                        navController.navigate(Screen.FileViewer.createRoute(filePath))
                    } else {
                        navController.navigate(Screen.Editor.createRoute(filePath))
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
