package com.example.util

import com.example.data.AiCacheDao
import com.example.data.AiCacheEntity
import com.example.network.GeminiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiResearchResult(
    val summary: String,
    val tldrBullets: String,
    val simplifiedStudentVersion: String,
    val suggestedQuestions: List<String>
)

class GeminiRepository(
    private val geminiService: GeminiService = GeminiService(),
    private val aiCacheDao: AiCacheDao
) {

    suspend fun getPageAiResearch(
        url: String,
        pageTitle: String,
        extractedText: String,
        forceRefresh: Boolean = false
    ): AiResearchResult = withContext(Dispatchers.IO) {
        if (url.isBlank()) {
            return@withContext AiResearchResult(
                summary = "No URL provided.",
                tldrBullets = "• No content to analyze",
                simplifiedStudentVersion = "",
                suggestedQuestions = emptyList()
            )
        }

        if (!forceRefresh) {
            val cached = aiCacheDao.getCache(url)
            if (cached != null) {
                val questions = cached.suggestedQuestionsJson.split("\n").filter { it.isNotBlank() }
                return@withContext AiResearchResult(
                    summary = cached.summaryText,
                    tldrBullets = cached.tldrBullets,
                    simplifiedStudentVersion = cached.simplifiedText,
                    suggestedQuestions = questions
                )
            }
        }

        val truncatedText = if (extractedText.length > 8000) extractedText.substring(0, 8000) else extractedText

        val prompt = """
            You are an AI Academic Research Assistant.
            Analyze the following web page content titled "$pageTitle":
            ---
            $truncatedText
            ---
            Provide a response structured with exact tags as below:

            [SUMMARY]
            Provide a concise, plain-language summary of the page (150-200 words).

            [TLDR]
            Provide exactly 5 key bullet points summarizing the main findings/takeaways.

            [STUDENT_MODE]
            Explain this content like I'm a student: simplify dense jargon, define complex academic terms inline in plain language.

            [QUESTIONS]
            Generate 4 insightful follow-up research questions that a student or researcher might ask based on this page (one per line).
        """.trimIndent()

        val rawAiOutput = geminiService.generateContent(prompt)

        val summary = extractSection(rawAiOutput, "[SUMMARY]", "[TLDR]").ifBlank { rawAiOutput }
        val tldr = extractSection(rawAiOutput, "[TLDR]", "[STUDENT_MODE]").ifBlank { "• Could not generate bullet points" }
        val studentVer = extractSection(rawAiOutput, "[STUDENT_MODE]", "[QUESTIONS]")
        val questionsRaw = extractSection(rawAiOutput, "[QUESTIONS]", "END_OF_TAGS")
            .ifBlank { rawAiOutput.substringAfter("[QUESTIONS]", "") }
        
        val questionList = questionsRaw.lines()
            .map { it.trim().removePrefix("-").removePrefix("•").removePrefix("1.").removePrefix("2.").removePrefix("3.").removePrefix("4.").trim() }
            .filter { it.length > 5 }

        // Cache in Room DB
        aiCacheDao.insertCache(
            AiCacheEntity(
                url = url,
                summaryText = summary,
                tldrBullets = tldr,
                simplifiedText = studentVer,
                suggestedQuestionsJson = questionList.joinToString("\n"),
                timestamp = System.currentTimeMillis()
            )
        )

        AiResearchResult(
            summary = summary,
            tldrBullets = tldr,
            simplifiedStudentVersion = studentVer,
            suggestedQuestions = questionList
        )
    }

    suspend fun askFollowUpQuestion(
        pageTitle: String,
        extractedText: String,
        question: String
    ): String = withContext(Dispatchers.IO) {
        val truncatedText = if (extractedText.length > 6000) extractedText.substring(0, 6000) else extractedText
        val prompt = """
            You are an AI Academic Research Assistant.
            Based strictly on the following page content ("$pageTitle"):
            ---
            $truncatedText
            ---
            User Question: $question

            Answer in a clear, educational tone. Ground your answer in the provided text.
        """.trimIndent()

        geminiService.generateContent(prompt)
    }

    private fun extractSection(text: String, startTag: String, endTag: String): String {
        val start = text.indexOf(startTag)
        if (start == -1) return ""
        val contentStart = start + startTag.length
        val end = text.indexOf(endTag, contentStart)
        return if (end != -1) {
            text.substring(contentStart, end).trim()
        } else {
            text.substring(contentStart).trim()
        }
    }
}
