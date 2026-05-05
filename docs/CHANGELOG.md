# Mdown 更新日志

## v3.0 (2026-05-05) — WYSIWYG 编辑器大版本

### 全新功能：所见即所得（WYSIWYG）编辑模式
- 新增「编辑」模式（RICH），基于 contentEditable WebView 实现真正的所见即所得编辑
- 用户直接在渲染后的页面上编辑，无需学习 Markdown 语法
- 三种模式切换：**编辑**（WYSIWYG，默认）、**源代码**（纯文本）、**分屏**（左右对照）
- 手机默认进入编辑模式，平板默认分屏模式

### 图片功能优化
- 修复图片在编辑模式下无法显示的多个问题（CSP、content:// 协议、FileProvider）
- 通过 shouldInterceptRequest 拦截 content:// 请求，确保图片在所有模式下正常渲染
- 图片自动限制最大高度 480px，防止超大图片溢出

### 表格功能增强
- 表格列之间添加淡色竖线分隔，视觉更清晰
- 新增行列时默认使用 `&nbsp;` 填充，保证正常行高列宽
- 新增按钮：+R（添加行）、+C（添加列）、-R（删除行）、-C（删除列）
- CSS min-width/min-height 确保单元格最小尺寸

### 编辑体验改进
- 修复 CSP 缺少 script-src 导致 JavaScript 被拦截、内容无法同步保存的问题
- 修复 WebView 初始加载时序问题，确保文件内容正确加载
- 内容末尾自动添加空白行，方便在文本下方选中编辑
- 离开页面时自动保存，返回按钮触发保存

### 技术架构
- 重写 RichEditorView：从透明 BasicTextField 叠加方案改为 contentEditable WebView
- 实现 HTML ↔ Markdown 双向转换（Jsoup + flexmark）
- MutationObserver + JavascriptInterface 实现编辑内容实时同步
- FileProvider + shouldInterceptRequest 解决图片跨协议加载
- ViewModel 工具栏方法支持双路径：编辑模式（JavaScript）和源代码模式（文本操作）
