package com.synthetic.linklog.util

import android.util.Log
import com.synthetic.linklog.domain.model.ScrapedMetadata
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request

object LinkScraper {

    private const val TAG = "LinkScraper"
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private val okHttpClient = OkHttpClient()

    suspend fun scrape(url: String, youtubeApiKey: String? = null): ScrapedMetadata {
        return withContext(Dispatchers.IO) {
            var metadata = ScrapedMetadata(url = url)

            try {
                // Tier 4: Check if it's a media URL
                if (isMediaUrl(url)) {
                    Log.d(TAG, "Media URL detected for $url")
                    
                    // Attempt YouTube Data API if key is present and it's a YouTube URL
                    if (youtubeApiKey != null && isYoutubeUrl(url)) {
                        val videoId = extractYoutubeVideoId(url)
                        if (videoId != null) {
                            val apiMetadata = fetchFromYoutubeApi(videoId, youtubeApiKey)
                            if (apiMetadata != null) {
                                return@withContext apiMetadata.copy(url = url)
                            }
                        }
                    }

                    // Fallback to yt-dlp
                    Log.d(TAG, "Attempting Tier 4 (yt-dlp) for $url")
                    val ytDlMetadata = extractWithYtDlp(url)
                    if (ytDlMetadata != null && (ytDlMetadata.title != null || ytDlMetadata.description != null)) {
                        return@withContext ytDlMetadata
                    }
                    Log.d(TAG, "Tier 4 failed or returned empty, falling back to Jsoup")
                }

                // General HTML Scraping (Tiers 1-3)
                Log.d(TAG, "Attempting Jsoup extraction for $url")
                val document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(5000)
                    .get()

                // Tier 1: OpenGraph
                var title = document.select("meta[property=og:title]").attr("content")
                var description = document.select("meta[property=og:description]").attr("content")
                var image = document.select("meta[property=og:image]").attr("content")

                // Tier 2: Twitter Cards Fallback
                if (title.isBlank()) title = document.select("meta[name=twitter:title]").attr("content")
                if (description.isBlank()) description = document.select("meta[name=twitter:description]").attr("content")
                if (image.isBlank()) image = document.select("meta[name=twitter:image]").attr("content")

                // Tier 3: Basic Tags Fallback
                if (title.isBlank()) title = document.title()
                if (description.isBlank()) description = document.select("meta[name=description]").attr("content")
                if (image.isBlank()) {
                    val icon = document.select("link[rel~=(?i)^(shortcut )?icon]").attr("href")
                    if (icon.isNotBlank()) {
                        // resolve relative urls
                        image = try {
                            URL(URL(url), icon).toString()
                        } catch (e: Exception) {
                            icon
                        }
                    }
                }

                metadata = metadata.copy(
                    title = title.takeIf { it.isNotBlank() },
                    description = description.takeIf { it.isNotBlank() },
                    imageUrl = image.takeIf { it.isNotBlank() }
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error scraping URL: $url", e)
            }

            metadata
        }
    }

    private fun isYoutubeUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains("youtube.com") || lowerUrl.contains("youtu.be")
    }

    private fun extractYoutubeVideoId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\\u200C\\u200B2F|youtu.be%\\u200C\\u200B2F|%2Fv%2F|shorts\\/)[^#\\&\\?\\n]*"
        val regex = Regex(pattern)
        return regex.find(url)?.value
    }

    private fun fetchFromYoutubeApi(videoId: String, apiKey: String): ScrapedMetadata? {
        return try {
            val apiUrl = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id=$videoId&key=$apiKey"
            val request = Request.Builder().url(apiUrl).build()
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val items = json.optJSONArray("items")
                if (items != null && items.length() > 0) {
                    val snippet = items.getJSONObject(0).optJSONObject("snippet")
                    if (snippet != null) {
                        val title = snippet.optString("title")
                        val description = snippet.optString("description")
                        val thumbnails = snippet.optJSONObject("thumbnails")
                        val imageUrl = thumbnails?.optJSONObject("high")?.optString("url")
                            ?: thumbnails?.optJSONObject("default")?.optString("url")
                            
                        return ScrapedMetadata(
                            title = title.takeIf { it.isNotBlank() },
                            description = description.takeIf { it.isNotBlank() },
                            imageUrl = imageUrl?.takeIf { it.isNotBlank() },
                            url = "" // Will be populated in caller
                        )
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "YouTube API fetch failed", e)
            null
        }
    }

    private fun isMediaUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return lowerUrl.contains("youtube.com") ||
                lowerUrl.contains("youtu.be") ||
                lowerUrl.contains("instagram.com/reel") ||
                lowerUrl.contains("instagram.com/p/") ||
                lowerUrl.contains("twitter.com") ||
                lowerUrl.contains("x.com") ||
                lowerUrl.contains("tiktok.com")
    }

    private fun extractWithYtDlp(url: String): ScrapedMetadata? {
        return try {
            val request = YoutubeDLRequest(url)
            request.addOption("--dump-json")
            request.addOption("--skip-download") // crucial for metadata only

            val response = YoutubeDL.getInstance().execute(request, TAG, null)
            val output = response.out

            if (output.isNotBlank()) {
                val json = JSONObject(output)
                
                val title = json.optString("title").takeIf { it.isNotBlank() }
                val description = json.optString("description").takeIf { it.isNotBlank() }
                val thumbnail = json.optString("thumbnail").takeIf { it.isNotBlank() }

                ScrapedMetadata(
                    title = title,
                    description = description,
                    imageUrl = thumbnail,
                    url = url
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "yt-dlp extraction failed for $url", e)
            null
        }
    }

    suspend fun extractPlaylistUrls(playlistUrl: String): List<String> {
        return withContext(Dispatchers.IO) {
            val urls = mutableListOf<String>()
            try {
                val request = YoutubeDLRequest(playlistUrl)
                request.addOption("--dump-json")
                request.addOption("--flat-playlist")

                val response = YoutubeDL.getInstance().execute(request, TAG, null)
                val lines = response.out.split("\n")
                
                for (line in lines) {
                    if (line.isNotBlank()) {
                        try {
                            val json = JSONObject(line)
                            val videoUrl = json.optString("url").takeIf { it.isNotBlank() } ?: json.optString("webpage_url")
                            if (videoUrl.isNotBlank()) {
                                // sometimes it returns just the ID for youtube
                                if (videoUrl.length == 11 && !videoUrl.contains("http")) {
                                    urls.add("https://www.youtube.com/watch?v=$videoUrl")
                                } else {
                                    urls.add(videoUrl)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse playlist item: $line", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to extract playlist URLs for $playlistUrl", e)
            }
            urls
        }
    }
}
