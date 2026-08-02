package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnpaywallResponse(
    @Json(name = "doi") val doi: String?,
    @Json(name = "is_oa") val isOa: Boolean = false,
    @Json(name = "title") val title: String?,
    @Json(name = "journal_name") val journalName: String?,
    @Json(name = "published_date") val publishedDate: String?,
    @Json(name = "best_oa_location") val bestOaLocation: OaLocation?,
    @Json(name = "oa_locations") val oaLocations: List<OaLocation>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class OaLocation(
    @Json(name = "url") val url: String?,
    @Json(name = "url_for_pdf") val urlForPdf: String?,
    @Json(name = "version") val version: String?,
    @Json(name = "host_type") val hostType: String?
)

@JsonClass(generateAdapter = true)
data class WaybackResponse(
    @Json(name = "url") val url: String?,
    @Json(name = "archived_snapshots") val archivedSnapshots: ArchivedSnapshots?
)

@JsonClass(generateAdapter = true)
data class ArchivedSnapshots(
    @Json(name = "closest") val closest: Snapshot?
)

@JsonClass(generateAdapter = true)
data class Snapshot(
    @Json(name = "status") val status: String?,
    @Json(name = "available") val available: Boolean = false,
    @Json(name = "url") val url: String?,
    @Json(name = "timestamp") val timestamp: String?
)

@JsonClass(generateAdapter = true)
data class SemanticScholarPaper(
    @Json(name = "paperId") val paperId: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "year") val year: Int?,
    @Json(name = "authors") val authors: List<SemanticAuthor>? = emptyList(),
    @Json(name = "citationCount") val citationCount: Int? = 0,
    @Json(name = "referenceCount") val referenceCount: Int? = 0,
    @Json(name = "openAccessPdf") val openAccessPdf: OpenAccessPdfUrl? = null,
    @Json(name = "citations") val citations: List<PaperInfo>? = emptyList(),
    @Json(name = "references") val references: List<PaperInfo>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class SemanticAuthor(
    @Json(name = "name") val name: String?
)

@JsonClass(generateAdapter = true)
data class OpenAccessPdfUrl(
    @Json(name = "url") val url: String?
)

@JsonClass(generateAdapter = true)
data class PaperInfo(
    @Json(name = "paperId") val paperId: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "year") val year: Int?,
    @Json(name = "authors") val authors: List<SemanticAuthor>? = emptyList()
)

