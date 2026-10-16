
package com.pratik.axon.providers.web

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WikipediaSearchResult(
    val title: String,
    val key: String,
    val description: String?
)

data class WikipediaArticle(
    val title: String,
    val description: String?,
    val extract: String,
    val pageUrl: String?
)

class WikipediaProvider {

    companion object {
        private const val SEARCH_URL =
            "https://en.wikipedia.org/w/rest.php/v1/search/page"

        private const val SUMMARY_URL =
            "https://en.wikipedia.org/api/rest_v1/page/summary/"

        private const val CONNECT_TIMEOUT = 8000
        private const val READ_TIMEOUT = 10000

        private const val USER_AGENT =
            "AXON/1.0 (Android personal assistant)"
    }

    fun search(
        query: String,
        limit: Int = 5
    ): List<WikipediaSearchResult> {

        val cleanedQuery = query.trim()

        if (cleanedQuery.isBlank()) {
            return emptyList()
        }

        val safeLimit = limit.coerceIn(1, 10)

        val encodedQuery =
            URLEncoder.encode(
                cleanedQuery,
                "UTF-8"
            )

        val url =
            "$SEARCH_URL?q=$encodedQuery&limit=$safeLimit"

        val json = requestJson(url)
            ?: return emptyList()

        val pages = json.optJSONArray("pages")
            ?: return emptyList()

        val results =
            mutableListOf<WikipediaSearchResult>()

        for (index in 0 until pages.length()) {

            val page =
                pages.optJSONObject(index)
                    ?: continue

            val title =
                page.optString("title")
                    .trim()

            val key =
                page.optString("key")
                    .ifBlank { title }

            val description =
                page.optString("description")
                    .takeIf { it.isNotBlank() }

            if (title.isNotBlank()) {
                results.add(
                    WikipediaSearchResult(
                        title = title,
                        key = key,
                        description = description
                    )
                )
            }
        }

        return results
    }

    fun getSummary(
        title: String
    ): WikipediaArticle? {

        val cleanedTitle = title.trim()

        if (cleanedTitle.isBlank()) {
            return null
        }

        val encodedTitle =
            URLEncoder.encode(
                cleanedTitle,
                "UTF-8"
            ).replace("+", "%20")

        val json =
            requestJson(
                SUMMARY_URL + encodedTitle
            ) ?: return null

        val articleTitle =
            json.optString("title")
                .ifBlank { cleanedTitle }

        val description =
            json.optString("description")
                .takeIf { it.isNotBlank() }

        val extract =
            json.optString("extract")
                .trim()

        val contentUrls =
            json.optJSONObject("content_urls")

        val desktop =
            contentUrls?.optJSONObject("desktop")

        val pageUrl =
            desktop?.optString("page")
                ?.takeIf { it.isNotBlank() }

        if (extract.isBlank()) {
            return null
        }

        return WikipediaArticle(
            title = articleTitle,
            description = description,
            extract = extract,
            pageUrl = pageUrl
        )
    }

    fun findArticle(
        query: String
    ): WikipediaArticle? {

        val cleanedQuery = query.trim()

        if (cleanedQuery.isBlank()) {
            return null
        }

        // First try the topic as an article title.
        val directArticle =
            getSummary(cleanedQuery)

        if (directArticle != null) {
            return directArticle
        }

        // If that fails, search Wikipedia for the topic.
        val firstResult =
            search(cleanedQuery, limit = 5)
                .firstOrNull()
                ?: return null

        // Try the canonical page title returned by search.
        return getSummary(firstResult.key)
            ?: getSummary(firstResult.title)
    }

    private fun requestJson(
        address: String
    ): JSONObject? {

        var connection: HttpURLConnection? = null

        try {
            connection =
                URL(address)
                    .openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            connection.connectTimeout =
                CONNECT_TIMEOUT

            connection.readTimeout =
                READ_TIMEOUT

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "User-Agent",
                USER_AGENT
            )

            val responseCode =
                connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return null
            }

            if (responseCode !in 200..299) {
                throw IOException(
                    "Wikipedia returned HTTP $responseCode"
                )
            }

            val responseText =
                connection.inputStream
                    .bufferedReader(Charsets.UTF_8)
                    .use { it.readText() }

            return JSONObject(responseText)

        } finally {
            connection?.disconnect()
        }
    }
}
