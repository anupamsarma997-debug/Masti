package com.example.util

sealed class SearchIntent {
    data class DoiLookup(val doi: String) : SearchIntent()
    data class DirectUrl(val url: String) : SearchIntent()
    data class TopicSearch(val query: String) : SearchIntent()
}

object IntentRouter {
    private val doiRegex = Regex("""\b(10\.\d{4,9}/[-._;()/:A-Za-z0-9]+)\b""", RegexOption.IGNORE_CASE)

    fun routeInput(input: String): SearchIntent {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return SearchIntent.TopicSearch("")

        // Check if DOI
        val doiMatch = doiRegex.find(trimmed)
        if (doiMatch != null) {
            return SearchIntent.DoiLookup(doiMatch.groupValues[1])
        }

        // Check if URL
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("www.", ignoreCase = true) ||
            (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 3)
        ) {
            val formattedUrl = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else trimmed
            return SearchIntent.DirectUrl(formattedUrl)
        }

        return SearchIntent.TopicSearch(trimmed)
    }
}
