package com.tinixmusic.tinixmusic2

import android.icu.text.CaseMap.Title
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass (generateAdapter = true)
    data class WpPost(
        val id: Int,
        val date: String,
        val title: Rendered,
        val excerpt: Rendered,
        val content: Rendered,
        @Json(name = "_embedded") val embedded: Embedded?,

        val track: String?,

        val artist: String?,
        val music320: String?,

        val online: String?,


    //به خاطر اینکه این فیلد در SongRepository قرمز شد اینجا اضافه کردم
        @Json(name = "music_versions") val musicVersions: List<String>? = null

    )
@JsonClass(generateAdapter = true)
data class Rendered(val rendered: String)

@JsonClass(generateAdapter = true)
data class Embedded(@Json(name = "wp:featuredmedia") val featuredMedia: List<FeaturedMedia>)

@JsonClass(generateAdapter = true)
data class FeaturedMedia(val source_url: String?)
