package com.markdowneditor.ui.settings

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.markdowneditor.ui.theme.AppTheme
import com.markdowneditor.viewModel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    themeViewModel: ThemeViewModel
) {
    val darkMode by themeViewModel.darkMode.collectAsState()
    var fontSize by remember { mutableStateOf(16f) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showChangelog by remember { mutableStateOf(false) }

    if (showPrivacyPolicy) {
        WebViewDialog(
            title = "隐私政策",
            url = "file:///android_asset/privacy_policy.html",
            onDismiss = { showPrivacyPolicy = false }
        )
    }

    if (showChangelog) {
        ChangelogDialog(onDismiss = { showChangelog = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
            title = {
                Text(
                    "设置",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppTheme.spacing.lg)
                .padding(bottom = AppTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            SettingsSectionHeader(title = "外观")

            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SettingsIcon(
                        icon = Icons.Default.DarkMode,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "深色模式",
                            style = AppTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "减少眼睛疲劳，适合夜间使用",
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { themeViewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            SettingsSectionHeader(title = "编辑器")

            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SettingsIcon(
                            icon = Icons.Default.FormatSize,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            iconColor = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(AppTheme.spacing.md))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "字体大小",
                                style = AppTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${fontSize.toInt()}sp",
                                style = AppTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(AppTheme.spacing.sm))
                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 12f..24f,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            thumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("12", style = AppTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Text("24", style = AppTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    }
                }
            }

            SettingsSectionHeader(title = "关于")

            SettingsCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Mdown",
                        style = AppTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(AppTheme.spacing.xs))
                    Text(
                        "版本 3.0",
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.History,
                    title = "更新日志",
                    subtitle = "查看版本更新记录",
                    onClick = { showChangelog = true }
                )
            }

            SettingsCard {
                SettingsClickableItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "隐私政策",
                    subtitle = "了解我们如何保护您的数据",
                    onClick = { showPrivacyPolicy = true }
                )
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
private fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsIcon(
                icon = icon,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconColor = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(AppTheme.spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = AppTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun SettingsIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = AppTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            top = AppTheme.spacing.md,
            bottom = AppTheme.spacing.xs,
            start = AppTheme.spacing.sm
        )
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
private fun WebViewDialog(
    title: String,
    url: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                title,
                style = AppTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = false
                        loadUrl(url)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
private fun ChangelogDialog(onDismiss: () -> Unit) {
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
                    Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "更新日志",
                    style = AppTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                ChangelogEntry(
                    version = "3.0",
                    items = listOf(
                        "全新所见即所得编辑模式，在渲染页面上直接编辑",
                        "三种编辑模式：编辑（WYSIWYG）、源代码、分屏",
                        "HTML ↔ Markdown 双向实时同步",
                        "修复图片在各模式下无法显示的问题",
                        "图片自动限制最大高度，防止超大图片溢出",
                        "表格新增列间竖线分隔",
                        "表格支持添加/删除行列（+R/+C/-R/-C）",
                        "离开页面自动保存",
                        "修复 CSP 拦截导致编辑内容无法保存的关键问题",
                        "FileProvider + shouldInterceptRequest 图片加载方案"
                    )
                )
                ChangelogEntry(
                    version = "2.14",
                    items = listOf(
                        "全局状态栏改为主题色垂直渐变，更柔和",
                        "修复 RICH 模式图片无法渲染问题（自定义 scheme）",
                        "修复任务列表复选框保存后重复标记",
                        "链接插入改用弹窗输入 URL，交互更自然",
                        "设置页顶部同步适配渐变条"
                    )
                )
                ChangelogEntry(
                    version = "2.13",
                    items = listOf(
                        "首页顶部添加主题色渐变条，更美观"
                    )
                )
                ChangelogEntry(
                    version = "2.12",
                    items = listOf(
                        "修复 RICH 模式外部变更无法同步渲染的问题",
                        "js 同步防抖降至 100ms 提升响应速度"
                    )
                )
                ChangelogEntry(
                    version = "2.11",
                    items = listOf(
                        "撤销图标改为门形，与返回箭头区分",
                        "撤销按钮紧贴文件名右侧",
                        "编辑器图片改为 file:// URI 修复显示",
                        "编辑区底部永久空白行防卡光标",
                        "限制图片最大高度 70vh"
                    )
                )
                ChangelogEntry(
                    version = "2.10",
                    items = listOf(
                        "新增状态栏绿色背景条",
                        "模式切换按钮去除多余灰色边框",
                        "竖屏模式按钮仅显示图标节省空间",
                        "横屏/平板模式按钮显示图标+文字",
                        "撤销按钮移至文件名右侧防误触"
                    )
                )
                ChangelogEntry(
                    version = "2.9",
                    items = listOf(
                        "Typora 风格 WYSIWYG 渲染编辑模式",
                        "默认进入渲染编辑，源代码模式独立",
                        "TopBar 模式切换按钮居中显示",
                        "横屏/平板模式按钮自适应放大",
                        "撤销按钮移至文件名旁"
                    )
                )
                ChangelogEntry(
                    version = "2.8",
                    items = listOf(
                        "页面切换平移动画，更丝滑",
                        "修复预览和分屏无法滚动",
                        "编辑器支持长文本滚动",
                        "升级 targetSdk 35",
                        "添加隐私政策和更新日志",
                        "签名配置和备份规则优化"
                    )
                )
                ChangelogEntry(
                    version = "2.7",
                    items = listOf(
                        "TopBar 整合编辑/预览/分屏切换",
                        "新增撤销/重做功能",
                        "左右滑动切换视图模式",
                        "帮助指南与快捷键说明",
                        "键盘弹出自适应布局",
                        "导出支持 WebView 截图预览"
                    )
                )
                ChangelogEntry(
                    version = "2.6",
                    items = listOf(
                        "增强导出功能（TXT/Word/图片）",
                        "高级 Markdown 功能",
                        "语音输入功能",
                        "键盘快捷键支持"
                    )
                )
                ChangelogEntry(
                    version = "2.5",
                    items = listOf(
                        "本地文件浏览器增强",
                        "安全防护加固",
                        "文件查看器（PDF/Word/Excel/图片）",
                        "搜索功能优化"
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
private fun ChangelogEntry(version: String, items: List<String>) {
    Column {
        Text(
            "v$version",
            style = AppTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { item ->
            Text(
                "  · $item",
                style = AppTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
