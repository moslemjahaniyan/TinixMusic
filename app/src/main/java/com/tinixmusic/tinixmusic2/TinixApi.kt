package com.tinixmusic.tinixmusic2

import retrofit2.http.GET
import retrofit2.http.Query

interface TinixApi{
    @GET("wp-json/wp/v2/posts")
    suspend fun getSongs(@Query("per_page") perPage: Int = 20, @Query("_embed") embed: Boolean = true): List<WpPost>
}