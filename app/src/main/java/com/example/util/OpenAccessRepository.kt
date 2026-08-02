package com.example.util

import com.example.network.OaLocation
import com.example.network.OpenAccessService
import com.example.network.PaperInfo
import com.example.network.SemanticScholarPaper
import com.example.network.UnpaywallResponse
import com.example.network.WaybackResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class LegalFreeAccessResult(
    val doi: String,
    val title: String? = null,
    val bestFreeUrl: String? = null,
    val sourceName: String? = null,
    val isOaAvailable: Boolean = false,
    val unpaywallPdf: String? = null,
    val waybackUrl: String? = null,
    val semanticPdf: String? = null,
    val citationCount: Int = 0,
    val referenceCount: Int = 0,
    val citers: List<PaperInfo> = emptyList(),
    val references: List<PaperInfo> = emptyList(),
    val errorMessage: String? = null
)

class OpenAccessRepository(
    private val openAccessService: OpenAccessService = OpenAccessService()
) {

    suspend fun findLegalFreeAccess(doi: String, pageUrl: String? = null): LegalFreeAccessResult = coroutineScope {
        val cleanDoi = doi.trim().removePrefix("https://doi.org/").removePrefix("http://doi.org/").removePrefix("doi:")
        val targetUrl = pageUrl ?: if (cleanDoi.isNotBlank()) "https://doi.org/$cleanDoi" else ""

        // Parallel lookup: Unpaywall + Wayback + Semantic Scholar
        val unpaywallDeferred = async { openAccessService.checkUnpaywall(cleanDoi) }
        val waybackDeferred = async { if (targetUrl.isNotBlank()) openAccessService.checkWaybackMachine(targetUrl) else null }
        val semanticDeferred = async { openAccessService.checkSemanticScholar(cleanDoi) }

        val unpaywall = unpaywallDeferred.await()
        val wayback = waybackDeferred.await()
        val semantic = semanticDeferred.await()

        val unpaywallPdf = unpaywall?.bestOaLocation?.urlForPdf ?: unpaywall?.bestOaLocation?.url
        val waybackSnapshotUrl = wayback?.archivedSnapshots?.closest?.takeIf { it.available }?.url
        val semanticPdf = semantic?.openAccessPdf?.url

        // Determine best legal free URL
        val (bestUrl, bestSource) = when {
            !unpaywallPdf.isNull_or_empty() -> unpaywallPdf to "Unpaywall Open Access"
            !semanticPdf.isNull_or_empty() -> semanticPdf to "Semantic Scholar Open Access"
            !waybackSnapshotUrl.isNull_or_empty() -> waybackSnapshotUrl to "Wayback Machine Snapshot"
            else -> null to null
        }

        LegalFreeAccessResult(
            doi = cleanDoi,
            title = unpaywall?.title ?: semantic?.title,
            bestFreeUrl = bestUrl,
            sourceName = bestSource,
            isOaAvailable = bestUrl != null,
            unpaywallPdf = unpaywallPdf,
            waybackUrl = waybackSnapshotUrl,
            semanticPdf = semanticPdf,
            citationCount = semantic?.citationCount ?: 0,
            referenceCount = semantic?.referenceCount ?: 0,
            citers = semantic?.citations ?: emptyList(),
            references = semantic?.references ?: emptyList(),
            errorMessage = if (unpaywall == null && wayback == null && semantic == null) "Failed to query open access repositories. Check connection." else null
        )
    }

    private fun String?.isNull_or_empty() = this.isNullOrBlank()
}
