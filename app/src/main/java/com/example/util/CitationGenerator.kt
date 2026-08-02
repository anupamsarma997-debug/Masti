package com.example.util

data class PaperMetadata(
    val title: String,
    val authors: List<String> = emptyList(),
    val journal: String? = null,
    val year: String? = null,
    val doi: String? = null,
    val url: String? = null
)

object CitationGenerator {

    fun generateApa(meta: PaperMetadata): String {
        val authorsFormatted = if (meta.authors.isNotEmpty()) {
            meta.authors.joinToString(", ")
        } else {
            "Unknown Author"
        }
        val yearPart = if (!meta.year.isNull_or_empty()) " (${meta.year})." else " (n.d.)."
        val titlePart = " ${meta.title}."
        val journalPart = if (!meta.journal.isNull_or_empty()) " *${meta.journal}*." else ""
        val doiPart = if (!meta.doi.isNull_or_empty()) " https://doi.org/${meta.doi}" else if (!meta.url.isNull_or_empty()) " ${meta.url}" else ""

        return "$authorsFormatted$yearPart$titlePart$journalPart$doiPart".trim()
    }

    fun generateMla(meta: PaperMetadata): String {
        val authorsFormatted = if (meta.authors.isNotEmpty()) {
            meta.authors.joinToString(", ")
        } else {
            "Unknown Author"
        }
        val titlePart = " \"${meta.title}.\""
        val journalPart = if (!meta.journal.isNull_or_empty()) " *${meta.journal}*," else ""
        val yearPart = if (!meta.year.isNull_or_empty()) " ${meta.year}." else ""
        val doiPart = if (!meta.doi.isNull_or_empty()) " doi:${meta.doi}." else ""

        return "$authorsFormatted.$titlePart$journalPart$yearPart$doiPart".trim()
    }

    fun generateBibTex(meta: PaperMetadata): String {
        val citeKey = (meta.authors.firstOrNull()?.split(" ")?.lastOrNull()?.lowercase() ?: "paper") +
                (meta.year ?: "2026")
        val authorsPart = meta.authors.joinToString(" and ")
        
        return """
            @article{$citeKey,
              title     = {${meta.title}},
              author    = {${if (authorsPart.isNotBlank()) authorsPart else "Unknown"}},
              journal   = {${meta.journal ?: ""}},
              year      = {${meta.year ?: ""}},
              doi       = {${meta.doi ?: ""}},
              url       = {${meta.url ?: ""}}
            }
        """.trimIndent()
    }

    private fun String?.isNull_or_empty() = this.isNullOrBlank()
}
