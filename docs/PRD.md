# Mdown — Android Markdown 编辑器 PRD

> **版本**：v3.0 | **日期**：2026-06-10 | **作者**：Mdown Team  
> **状态**：已发布（Beta）

---

## 1. 产品概述

### 1.1 产品简介

Mdown 是一款面向安卓平台的 Markdown 编辑器，参考 Typora 的设计理念，提供**所见即所得（WYSIWYG）**的编辑体验。用户无需学习 Markdown 语法即可编写格式丰富的文档，同时支持源代码编辑和实时分屏预览。

v3.0 版本新增**AI 写作助手**，集成 DeepSeek-V4-Pro 大模型，可在编辑器内直接通过自然语言指令生成、修改、润色 Markdown 内容。

### 1.2 产品定位

> 安卓端最好用的 Markdown 编辑器——像 Typora 一样优雅，比备忘录更强大。

### 1.3 核心价值主张

- **零学习成本**：默认 WYSIWYG 模式，像 Word 一样编辑 Markdown
- **全格式导出**：PDF / HTML / PNG / TXT / MD 五种导出格式
- **AI 原生集成**：编辑器内嵌 AI 写作助手，支持全文自动修改
- **离线可用**：所有功能本地运行，AI 功能需联网

---

### 1.5 竞品分析

#### 桌面端

| | **Mdown** | Typora | Obsidian |
|--|-----------|--------|----------|
| 平台 | Android | Win/Mac/Linux | 全平台 |
| WYSIWYG | ✅ 默认 | ✅ 核心 | ❌ 需插件 |
| AI 写作 | ✅ 内置 | ❌ | ❌ 需插件 |
| 价格 | 免费 | ¥89 | 免费 |
| 学习成本 | 低 | 低 | 高 |

- **Typora**：WYSIWYG 标杆，但不做移动端、无 AI。Mdown 在移动端复现 Typora 体验 + AI 能力
- **Obsidian**：插件生态强大，但学习曲线陡峭、移动端无 WYSIWYG。Mdown 避开其知识管理赛道，聚焦"快速单篇写作"

#### 安卓端

| | Mdown | Markor | iA Writer | JotterPad | 纯纯写作 |
|--|-------|--------|-----------|-----------|---------|
| WYSIWYG | ✅ | ❌ | ❌ | ❌ | ❌ |
| AI | ✅ | ❌ | ❌ | ❌ | ❌ |
| 导出 | 5 种 | 0 | 2 种 | 4 种 | 1 种 |
| 平板 | ✅ | ❌ | ❌ | ❌ | ❌ |
| 价格 | 免费 | 免费 | ¥198 | 订阅 | ¥98 |

> Mdown 是安卓端唯一同时具备 **WYSIWYG + AI + 免费 + 5 种导出** 的 Markdown 编辑器。

#### 差异化策略
1. **填补空白**：Typora 不做移动端 → Mdown 来做
2. **弯道超车**：2026 年移动端编辑器普遍无 AI → Mdown 内置 AI 写作助手
3. **避其锋芒**：不做知识管理/插件系统，聚焦"移动端单篇写作"场景

---

## 2. 目标用户

### 2.1 用户画像

| 画像 | 描述 | 核心场景 |
|------|------|---------|
| 📝 **学生/学者** | 需要写笔记、论文、实验报告 | 课堂笔记、文献整理 |
| 💻 **技术写作者** | 开发者、技术博主 | API 文档、技术博客、README |
| 📊 **职场人士** | 产品经理、运营、管理者 | 周报、会议纪要、项目文档 |
| 📱 **移动办公族** | 通勤/出差时处理文档 | 随时编辑、快速导出分享 |

### 2.2 用户痛点

| 痛点 | 现有方案的问题 | Mdown 的解决 |
|------|---------------|-------------|
| 不懂 Markdown 语法 | 纯文本编辑器门槛高 | WYSIWYG 模式，直接编辑渲染后的内容 |
| 安卓端选择少 | 现有 App 功能简陋或体验差 | 对标桌面端 Typora 体验 |
| 导出格式受限 | 多数 App 仅支持 1-2 种格式 | 支持 5 种导出格式 |
| 写作效率低 | 需要切换 App 查资料、润色 | AI 写作助手内嵌编辑器 |
| 平板适配差 | 大多仅适配手机 | 自动识别平板/手机，优化布局 |

---

## 3. 功能需求

### 3.1 功能优先级

| 优先级 | 功能模块 | 说明 |
|--------|---------|------|
| P0 | Markdown 编辑与渲染 | 核心功能，不可缺失 |
| P0 | 文件管理 | 创建、保存、删除、搜索 |
| P0 | 所见即所得编辑 | v3.0 核心特性 |
| P1 | 多格式导出 | PDF / HTML / PNG / TXT / MD |
| P1 | AI 写作助手 | v3.0 新增 |
| P1 | 暗黑模式 | 适配系统主题 |
| P2 | 键盘快捷键 | 外接键盘场景 |
| P2 | 平板自适应 | 大屏优化 |
| P3 | 本地文件浏览器 | 浏览设备内文件 |

### 3.2 用户故事

| ID | 用户故事 | 验收标准 |
|----|---------|---------|
| US-01 | 作为学生，我想快速创建一篇课堂笔记，使用标题、列表等格式 | 新建文件→输入内容→自动 Markdown 渲染→保存 |
| US-02 | 作为产品经理，我不想学 Markdown 语法就能写周报 | 在 WYSIWYG 模式下输入→点击工具栏按钮格式化→所见即所得 |
| US-03 | 作为开发者，我想在手机上查看和编辑 README.md | 打开已有 .md 文件→源代码/预览模式切换→语法高亮 |
| US-04 | 作为职场人士，我想把写好的文档导出为 PDF 分享 | 编辑完成→点击导出→选择 PDF→文件保存到 Download/Mdown |
| US-05 | 作为写作者，我想用 AI 帮我润色不满意的段落 | 选中段落→在 AI 输入框输入"润色这段"→AI 返回修改版本→自动替换原文 |
| US-06 | 作为写作者，我想让 AI 帮我修改全文的某个部分 | 在 AI 输入框输入"把所有二级标题改成三级"→AI 自动定位并返回完整修改后文档 |
| US-07 | 作为用户，我想用深色模式在夜间写作 | 进入设置→开启深色模式→全局变暗 |

---

## 4. 功能详述

### 4.1 编辑模式

Mdown 提供三种编辑模式，通过顶栏按钮一键切换：

| 模式 | 图标 | 说明 | 适用场景 |
|------|------|------|---------|
| **编辑（RICH）** | ✏️ | 所见即所得，基于 WebView contentEditable | 默认模式，适合大部分用户 |
| **源代码（SOURCE）** | `</>` | 纯文本 Markdown 编辑，语法高亮 | 熟悉 Markdown 的用户 |
| **分屏（SPLIT）** | ▯▯ | 左编辑右预览，实时同步 | 学习 Markdown / 复杂排版 |

**技术方案（RICH 模式）**：
- WebView + contentEditable 实现所见即所得编辑
- flexmark 库实现 Markdown → HTML 正向渲染
- 自研 Jsoup HTML → Markdown 逆向转换（[HtmlToMarkdown.kt](../app/src/main/java/com/markdowneditor/utils/HtmlToMarkdown.kt)）
- MutationObserver + JavaScriptInterface 实现编辑内容实时同步
- 防抖策略：内容变更 250ms、选区变更 400ms

### 4.2 格式化工具栏

位于编辑区顶部，水平可滚动，提供以下格式化按钮：

| 分组 | 按钮 | 快捷键（Ctrl+） |
|------|------|----------------|
| 标题 | H1, H2, H3 | 1, 2, 3 |
| 文字样式 | **B**, *I*, ~~S~~ | B, I, D |
| 代码/引用 | `</>`, > | /, Q |
| 列表 | UL, OL, ☑Task | L, O |
| 链接/图片 | 🔗Link, 🖼Img | K, Shift+I |
| 分割线/表格 | ---, ▦Table | H, T |
| 表格操作 | +R, +C, -R, -C | — |

### 4.3 文件管理

- **文件操作**：创建、保存、重命名、删除
- **文件夹**：创建文件夹、进入子目录
- **剪贴板**：复制/剪切/粘贴文件（支持跨文件夹）
- **搜索**：按文件名搜索
- **文件格式支持**：.md, .markdown, .txt
- **查看支持**：PDF, Word, Excel, PPT, 图片（内置查看器）
- **存储位置**：`Android/data/com.markdowneditor/files/`
- **外部打开**：支持从其他 App 用 Mdown 打开文件（Intent Filter）

### 4.4 导出功能

| 格式 | 实现方式 | 输出路径 |
|------|---------|---------|
| **PDF** | WebView 截图→Canvas 绘制→PdfDocument 分页 | Download/Mdown/xxx.pdf |
| **PNG 图片** | WebView 截图→Bitmap→PNG 压缩 | Download/Mdown/xxx.png |
| **HTML 网页** | flexmark 渲染→内置 CSS 样式→HTML 文件 | Download/Mdown/xxx.html |
| **纯文本 TXT** | 直接输出 Markdown 原文 | Download/Mdown/xxx.txt |
| **Markdown MD** | 分享原始 .md 文件（FileProvider） | 系统分享菜单 |

### 4.5 AI 写作助手 (v3.0 新增)

#### 功能概述
编辑器底部固定 AI 聊天输入栏，用户输入自然语言指令，AI 自动生成/修改 Markdown 内容。

#### 三种工作模式

| 模式 | 触发条件 | AI 行为 | 结果处理 |
|------|---------|--------|---------|
| 🆕 **新建** | 空文档或无修改意图 | 生成新内容 | 插入光标位置 |
| ✏️ **选中修改** | 选中文字 + 修改指令 | 只输出修改后的片段 | 替换选中文本 |
| 📝 **全文修改** | 有内容 + 修改指令（无选中） | 返回修改后的完整文档 | 替换全文 |

#### 技术方案
- **大模型**：DeepSeek-V4-Pro（通过 DeepSeek API）
- **System Prompt**：设计为 Markdown 写作专家，支持三种模式自动切换
- **修改意图识别**：关键词匹配（修改/改成/润色/翻译/删除/添加等 30+ 词）
- **上下文窗口**：最大 12000 字符
- **API Key 管理**：DataStore 本地加密存储，设置页可视化配置
- **任务管理**：
  - 后台执行（NonCancellable）：退出页面任务不中断
  - 手动取消：加载中显示旋转停止按钮，点击取消

#### 安全设计
- API Key 仅存储在本地设备（Android DataStore）
- 网络请求仅通过 HTTPS（`api.deepseek.com`）
- 网络安全配置：仅信任系统证书

### 4.6 设置页

| 设置项 | 说明 |
|--------|------|
| 🌙 深色模式 | Material 3 Dark Theme |
| 🔤 字体大小 | 12-24sp 可调滑块 |
| 🔑 DeepSeek API Key | 配置/修改/清除，密码遮蔽 |
| 📋 更新日志 | v1.0 - v3.0 完整迭代记录 |
| 🔒 隐私政策 | 内嵌 WebView 展示 |

---

## 5. 技术架构

### 5.1 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 语言 | Kotlin | 100% Kotlin |
| UI | Jetpack Compose + Material 3 | 声明式 UI |
| 架构 | MVVM + Repository | 分层解耦 |
| DI | Hilt | 依赖注入 |
| 渲染引擎 | WebView + flexmark | Markdown→HTML |
| HTML 逆转换 | Jsoup（自研） | HTML→Markdown |
| 网络 | OkHttp 4.12 | AI API 调用 |
| 存储 | Room + DataStore + FileSystem | 分类存储 |
| 导出 | iText + Android Canvas + Bitmap | 多格式导出 |

### 5.2 项目结构

```
app/src/main/java/com/markdowneditor/
├── data/
│   ├── model/MarkdownFile.kt        # 文件数据模型
│   ├── database/                     # Room 数据库
│   └── repository/                   # 数据仓库
│       ├── FileRepository.kt
│       └── ApiKeyRepository.kt       # API Key 存储
├── network/
│   └── AiService.kt                  # DeepSeek API 服务
├── ui/
│   ├── MainActivity.kt              # 主 Activity + 导航
│   ├── editor/
│   │   ├── EditorScreen.kt          # 编辑器主页
│   │   ├── RichEditorView.kt        # WYSIWYG WebView 编辑
│   │   ├── MarkdownEditor.kt        # 源代码编辑器
│   │   └── MarkdownPreview.kt       # 预览 WebView
│   ├── filebrowser/                  # 文件浏览
│   ├── settings/                     # 设置页
│   └── theme/                        # 主题系统
├── utils/
│   ├── MarkdownRenderer.kt          # Markdown→HTML
│   ├── HtmlToMarkdown.kt            # HTML→Markdown (自研)
│   ├── ExportManager.kt             # 多格式导出
│   ├── FileManager.kt               # 文件操作
│   └── SecurityHelper.kt            # 路径/文件名安全
└── viewModel/
    ├── EditorViewModel.kt           # 编辑器逻辑（含 AI）
    ├── FileBrowserViewModel.kt      # 文件管理逻辑
    └── AiSettingsViewModel.kt       # AI 设置
```

### 5.3 关键技术决策

| 决策 | 选择 | 原因 |
|------|------|------|
| WYSIWYG 实现 | WebView contentEditable | 渲染一致性、扩展性、成本 |
| 反向转换 | 自研 Jsoup 方案 | flexmark 反向不支持 Task List/Table |
| 键盘适配 | `adjustNothing` + `imePadding()` | 跨 ROM 一致性 |
| API Key 存储 | DataStore | 更安全的 SharedPreferences 替代 |
| AI 任务管理 | NonCancellable + Job | 后台执行不中断 + 可手动取消 |

---

## 6. 版本迭代记录

| 版本 | 日期 | 关键变更 |
|------|------|---------|
| v3.0 | 2026-05-05 | WYSIWYG 编辑模式、HTML↔Markdown 双向转换、AI 写作助手、表格行列增删 |
| v2.14 | 2026-04 | 状态栏渐变、图片渲染修复、链接弹窗 |
| v2.10 | 2026-04 | 模式切换按钮优化、撤销按钮位置调整 |
| v2.8 | 2026-03 | 页面切换动画、targetSdk 35、隐私政策 |
| v1.0 | 2026-02 | 基础编辑、文件管理、导出 PDF/HTML |

完整更新日志见 [CHANGELOG.md](CHANGELOG.md)。

---

## 7. 非功能需求

| 类别 | 要求 |
|------|------|
| 性能 | 10000+ 字文档渲染 < 1s，WebView 加载 < 500ms |
| 兼容性 | Android 8.0+ (API 26)，手机/平板自适应 |
| 安全性 | API Key 本地加密存储，文件操作路径防遍历，URL 白名单验证 |
| 离线 | 除 AI 功能外，所有功能完全离线可用 |
| APK 大小 | < 30MB |

---

## 8. 未来规划

| 优先级 | 功能 | 说明 |
|--------|------|------|
| P1 | 云端同步 | Google Drive / WebDAV |
| P1 | AI 流式输出 | SSE Streaming，边生成边显示 |
| P2 | AI 多模型 | 支持更多模型（OpenAI、Anthropic 等） |
| P2 | 模板系统 | 预设文档模板（周报、会议纪要等） |
| P2 | 数学公式 | LaTeX/MathJax 渲染 |
| P3 | 协作编辑 | 实时多人协作 |
| P3 | Google Play 上架 | — |

---

## 附录

###  术语表

| 术语 | 说明 |
|------|------|
| WYSIWYG | What You See Is What You Get，所见即所得 |
| contentEditable | HTML5 属性，使元素可编辑 |
| flexmark | Java Markdown 解析库 |
| Jsoup | Java HTML 解析库 |
| DataStore | Android Jetpack 持久化存储 |
| IME | Input Method Editor，输入法 |

---

### A. 截图

| 编号 | 页面 | 说明 |
|------|------|------|
| 05 | [AI 聊天栏](screenshots/05_ai_chat_bar.png) | 底部 AI 输入框 |
| 06 | [导出弹窗](screenshots/06_export_dialog.png) | 多格式导出选项 |
| 07 | [文件列表](screenshots/07_file_list.png) | 带文件卡片的列表 |
| 08 | [侧边栏](screenshots/08_drawer_menu.png) | 导航抽屉 |
| 09 | [设置页](screenshots/09_settings_page.png) | 深色模式、字体等 |
| 10 | [AI 设置](screenshots/10_ai_settings.png) | DeepSeek API Key 配置 |
| 11 | [首页带文件](screenshots/11_home_with_files.jpg) | 文件列表主页面 |
| 12 | [编辑器+AI](screenshots/12_editor_with_ai.jpg) | WYSIWYG 编辑 + AI 写作 |
| 13 | [导出结果](screenshots/13_export_result.jpg) | 文件导出成功 |

---

> 📅 最后更新：2026-06-10  
> 📧 联系方式：通过项目 Issue 反馈
