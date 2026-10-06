package com.tinixmusic.tinixmusic2


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LrcSearchResult(
    val id: Long?,
    @Json(name = "trackName") val trackName: String?,
    @Json(name = "artistName") val artistName: String?,
    @Json(name = "syncedLyrics") val syncedLyrics: String?,
    @Json(name = "plainLyrics") val plainLyrics: String?
)

data class LyricLine(
    val timeMs: Long,
    val text: String,
    var translation: String = ""
)

@JsonClass(generateAdapter = true)
data class MyMemoryResponse(
    val responseData: MyMemoryData
)

@JsonClass(generateAdapter = true)
data class MyMemoryData(
    val translatedText: String
)