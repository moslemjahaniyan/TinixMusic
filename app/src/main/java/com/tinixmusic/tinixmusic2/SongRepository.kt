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

        val rawTitle = Html.fromHtml(title.rendered, Html.FROM_HTML_MODE_LEGACY).toString()

        return Song(
            id = id.toString(),
            title = cleanTitle(rawTitle),        // ✅ اینجا پاک‌سازی می‌شه
            artist = artist ?: "نامشخص",
            downloadUrl = music320 ?: "",
            imageUrl = imageUrl,
            versions = versions
        )
    }

    /**
     * تمیز کردن عنوان آهنگ:
     *  ۱. حذف عبارت‌های «دانلود آهنگ»، «دانلود»، «آهنگ»
     *  ۲. حذف هر چیزی که بعد از کلمه‌ی «از» میاد
     *  ۳. حذف فاصله‌های اضافی و کاراکترهای جداکننده‌ی ته‌مانده
     */
    private fun cleanTitle(raw: String): String {
        var t = raw

        // ۱. حذف عبارت‌های رایج ابتدای عنوان
        //    (ترتیب مهمه: اول ترکیب‌های بلندتر، بعد کوتاه‌ترها)
        val phrasesToRemove = listOf(
            "دانلود آهنگ جدید",
            "دانلود آهنگ",
            "دانلود موزیک",
            "دانلود"
        )
        for (phrase in phrasesToRemove) {
            t = t.replace(phrase, "", ignoreCase = true)
        }

        // ۲. حذف هر چیزی که بعد از « از » (با فاصله قبل و بعد) میاد
        //    از indexOf استفاده می‌کنیم که اولین occurrence رو بگیره
        val azIndex = t.indexOf(" از ")
        if (azIndex >= 0) {
            t = t.substring(0, azIndex)
        }

        // ۳. تمیزکاری نهایی
        t = t
            .replace(Regex("\\s+"), " ")           // فاصله‌های پشت‌سرهم → یک فاصله
            .trim()
            .trim('-', '|', '،', ',', ':', '؛', ';', '–', '—')  // کاراکترهای جداکننده‌ی اضافی
            .trim()

        return t
    }

    suspend fun getSongByArtist(artist: String): List<Song>{
        return getSong().filter { song ->
            song.artist?.contains(artist,ignoreCase = true) == true
        }
    }
}