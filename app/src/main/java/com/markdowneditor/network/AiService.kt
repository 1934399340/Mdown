package com.markdowneditor.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// DeepSeek API 请求/响应数据类
data class ChatMessage(
    val role: String,    // "system" | "user" | "assistant"
    val content: String
)

data class ChatRequest(
    val model: String = "deepseek-v4-pro",
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096
)

data class ChatResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val error: ApiError? = null
)

data class Choice(
    val message: ChatMessage? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class ApiError(
    val message: String? = null,
    val type: String? = null
)

/**
 * AI 返回结果类型，告知 EditorViewModel 如何处理返回值
 */
enum class AiResultType {
    NEW_CONTENT,      // 新增内容，插入光标处
    REPLACE_SELECTION, // 替换选中文本
    FULL_DOCUMENT     // 替换整个文档
}

data class AiResult(
    val text: String,
    val type: AiResultType
)

@Singleton
class AiService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        const val DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"
        private const val MAX_DOC_CHARS = 12000  // 传给 AI 的文档上限
    }

    /**
     * 调用 DeepSeek API
     *
     * 三种模式：
     * 1. 新建：无文档上下文 → AI 生成新内容 → 插入光标
     * 2. 选中修改：用户选中文本 + 修改指令 → AI 只输出修改后的片段 → 替换选中
     * 3. 全文修改：有文档 + 修改指令（无选中）→ AI 输出完整修改后文档 → 替换全文
     */
    suspend fun generateMarkdown(
        apiKey: String,
        userPrompt: String,
        contextMarkdown: String? = null,
        selectedText: String? = null
    ): Result<AiResult> = withContext(Dispatchers.IO) {
        try {
            val hasDoc = !contextMarkdown.isNullOrBlank()
            val hasSelection = !selectedText.isNullOrBlank()
            val isModify = hasDoc && containsModifyIntent(userPrompt)

            // 确定模式
            val mode = when {
                hasSelection && isModify -> "REPLACE_SELECTION"
                hasDoc && isModify -> "FULL_DOCUMENT"
                else -> "NEW_CONTENT"
            }

            val systemPrompt = """
你是一个专业的 Markdown 写作助手，精通 Markdown 语法。你必须严格遵守以下规则：

【核心规则 — 最重要】
1. 只输出 Markdown 文本，绝对不要输出任何解释、寒暄、开场白、结尾语。
2. 不要输出"好的"、"当然"、"以下是修改后的"之类的话。
3. 不要用代码块（```）包裹你的输出。
4. 直接输出内容本身。

【模式判断】
- 如果用户要求修改/润色/翻译现有文档中的内容，并且你是要修改整个文档，则返回修改后的完整文档。
- 如果用户选中了一段文本让你修改，只返回那段文本修改后的版本。
- 如果用户是让你生成新内容（写一篇文章、写一段话等），直接生成即可。

【格式要求】
- 使用合适的 Markdown 语法：标题(#)、列表(-)、代码块、表格、引用(>)、加粗(**)、斜体(*)等。
- 默认语言为中文。
            """.trimIndent()

            val messages = buildList {
                add(ChatMessage(role = "system", content = systemPrompt))

                when (mode) {
                    "REPLACE_SELECTION" -> {
                        // 提供全文上下文 + 高亮选中部分
                        val truncated = contextMarkdown!!.take(MAX_DOC_CHARS)
                        add(ChatMessage(
                            role = "user",
                            content = "文档全文：\n\n$truncated"
                        ))
                        add(ChatMessage(role = "assistant", content = "已理解。"))
                        add(ChatMessage(
                            role = "user",
                            content = "我选中了文档中的以下内容，请只修改选中的这部分，只输出修改后的结果：\n\n<<<选中开始>>>\n${selectedText!!.take(4000)}\n<<<选中结束>>>\n\n修改要求：$userPrompt"
                        ))
                    }

                    "FULL_DOCUMENT" -> {
                        val doc = contextMarkdown!!.take(MAX_DOC_CHARS)
                        add(ChatMessage(
                            role = "user",
                            content = "以下是我的 Markdown 文档的完整内容：\n\n---文档开始---\n$doc\n---文档结束---\n\n请根据我的要求修改这份文档，输出修改后的完整文档（不要省略任何未修改的部分）。\n\n修改要求：$userPrompt"
                        ))
                    }

                    else -> {
                        // NEW_CONTENT — 可能有文档上下文用于参考
                        if (hasDoc) {
                            add(ChatMessage(
                                role = "user",
                                content = "参考以下文档了解上下文：\n\n${contextMarkdown!!.take(MAX_DOC_CHARS)}"
                            ))
                            add(ChatMessage(role = "assistant", content = "已理解。"))
                        }
                        add(ChatMessage(
                            role = "user",
                            content = userPrompt
                        ))
                    }
                }
            }

            // 全文修改模式需要更大的 maxTokens
            val maxTokens = if (mode == "FULL_DOCUMENT") 16384 else 4096

            val requestBody = ChatRequest(
                messages = messages,
                temperature = 0.7,
                maxTokens = maxTokens
            )

            val httpRequest = Request.Builder()
                .url(DEEPSEEK_API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(gson.toJson(requestBody).toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("API 请求失败 (${response.code}): 请检查 API Key 是否正确")
                )
            }

            val chatResponse = gson.fromJson(responseBody, ChatResponse::class.java)

            if (chatResponse.error != null) {
                return@withContext Result.failure(
                    Exception(chatResponse.error.message ?: "未知 API 错误")
                )
            }

            val content = chatResponse.choices
                ?.firstOrNull()
                ?.message
                ?.content

            if (content.isNullOrBlank()) {
                return@withContext Result.failure(Exception("AI 返回了空内容，请重新描述你的需求"))
            }

            val resultType = when (mode) {
                "REPLACE_SELECTION" -> AiResultType.REPLACE_SELECTION
                "FULL_DOCUMENT" -> AiResultType.FULL_DOCUMENT
                else -> AiResultType.NEW_CONTENT
            }

            Result.success(AiResult(text = content.trim(), type = resultType))
        } catch (e: Exception) {
            Result.failure(Exception("网络请求失败: ${e.message}"))
        }
    }

    /**
     * 检测用户 prompt 中是否包含修改意图
     */
    private fun containsModifyIntent(prompt: String): Boolean {
        val modifyKeywords = listOf(
            "修改", "改成", "改为", "更改", "替换", "换成",
            "润色", "优化", "改进", "改善",
            "改写", "重写", "改一下", "换一种",
            "翻译", "译成", "翻译成",
            "修正", "修复", "纠正", "改正",
            "扩写", "缩写", "精简", "缩短", "展开",
            "调整", "转变", "转为", "变成",
            "删除", "删掉", "去掉", "移除", "去除",
            "添加", "加上", "插入", "增加", "补充",
            "改成", "把", "将", "让"
        )
        return modifyKeywords.any { prompt.contains(it) }
    }
}
