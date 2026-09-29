package com.tinixmusic.tinixmusic2


import android.text.Html
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SongRepository {
    private val api: TinixApi by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://tinixmusic.ir/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
            .create(TinixApi:: class.java)
    }
    suspend fun getSong(): List<Song>{
        val posts = api.getSongs()
        return posts.map {it.toSong()}
    }
    private fun WpPost.toSong(): Song {
        val imageUrl = embedded?.featuredMedia?.firstOrNull()?.source_url

        // Parse کردن ورژن‌ها
        val versions = mutableListOf<SongVersion>()
        musicVersions?.forEachIndexed { index, url ->
            if (url.isNotBlank()) {
                val typePair = MusicTypes.TYPES.getOrNull(index)
                if (typePair != null) {
                    versions.add(
                        SongVersion(
                            labelEn = typePair.first,
                            labelFa = typePair.second,
                            url = url
                        )
                    )
                }
            }
        }

        return Song(
            id = id.toString(),
            title = Html.fromHtml(title.rendered, Html.FROM_HTML_MODE_LEGACY).toString(),
            artist = artist ?: "نامشخص",
            downloadUrl = music320 ?: "",
            imageUrl = imageUrl,
            versions = versions
        )
    }

}