package com.tinixmusic.tinixmusic2


import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcApi {
    @GET("api/search")
    suspend fun search(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String
    ): List<LrcSearchResult>
}

interface TranslateApi {
    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String = "autodetect|fa"
    ): MyMemoryResponse
}

object LyricsRepository {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val lrcApi: LrcApi = Retrofit.Builder()
        .baseUrl("https://lrclib.net/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(LrcApi::class.java)

    private val translateApi: TranslateApi = Retrofit.Builder()
        .baseUrl("https://api.mymemory.translated.net/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(TranslateApi::class.java)

    private val translationCache = mutableMapOf<String, String>()

    suspend fun fetchLyrics(title: String, artist: String): List<LyricLine> {
        return try {
            val results = lrcApi.search(title, artist)
            val lrc = results.firstOrNull { !it.syncedLyrics.isNullOrBlank() }?.syncedLyrics
                ?: results.firstOrNull()?.plainLyrics
                ?: return emptyList()
            parseLrc(lrc)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseLrc(lrc: String): List<LyricLine> {
        val regex = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2,3}))?\\](.*)")
        val lines = mutableListOf<LyricLine>()
        lrc.lines().forEach { line ->
            val match = regex.find(line) ?: return@forEach
            val (min, sec, millis, text) = match.destructured
            val timeMs = min.toLong() * 60_000 +
                    sec.toLong() * 1000 +
                    millis.padEnd(3, '0').toLong()
            if (text.isNotBlank()) {
                lines.add(LyricLine(timeMs, text.trim()))
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    suspend fun translateLine(text: String): String {
        if (text.isBlank()) return ""
        translationCache[text]?.let { return it }
        return try {
            val response = translateApi.translate(text)
            val translated = response.responseData.translatedText
            translationCache[text] = translated
            translated
        } catch (e: Exception) {
            ""
        }
    }
}