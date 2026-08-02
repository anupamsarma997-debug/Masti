package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class OpenAccessService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    suspend fun checkUnpaywall(doi: String, email: String = "user@example.com"): UnpaywallResponse? = withContext(Dispatchers.IO) {
        val cleanDoi = doi.trim().removePrefix("https://doi.org/").removePrefix("http://doi.org/").removePrefix("doi:")
        if (cleanDoi.isEmpty()) return@withContext null

        val url = "https://api.unpaywall.org/v2/$cleanDoi?email=$email"
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return@withContext null
                    val adapter = moshi.adapter(UnpaywallResponse::class.java)
                    adapter.fromJson(bodyString)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun checkWaybackMachine(targetUrl: String): WaybackResponse? = withContext(Dispatchers.IO) {
        if (targetUrl.isBlank()) return@withContext null

        val url = "https://archive.org/wayback/available?url=$targetUrl"
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return@withContext null
                    val adapter = moshi.adapter(WaybackResponse::class.java)
                    adapter.fromJson(bodyString)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun checkSemanticScholar(doi: String): SemanticScholarPaper? = withContext(Dispatchers.IO) {
        val cleanDoi = doi.trim().removePrefix("https://doi.org/").removePrefix("http://doi.org/").removePrefix("doi:")
        if (cleanDoi.isEmpty()) return@withContext null

        val fields = "title,year,authors,citationCount,referenceCount,openAccessPdf,citations.title,citations.authors,citations.paperId,references.title,references.authors,references.paperId"
        val url = "https://api.semanticscholar.org/graph/v1/paper/DOI:$cleanDoi?fields=$fields"
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return@withContext null
                    val adapter = moshi.adapter(SemanticScholarPaper::class.java)
                    adapter.fromJson(bodyString)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
