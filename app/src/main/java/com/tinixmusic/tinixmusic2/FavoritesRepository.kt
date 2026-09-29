package com.tinixmusic.tinixmusic2

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.favoriteDateStore by preferencesDataStore(name = "favorites")
object FavoritesRepository {
    private val FAVORITES_KEY = stringSetPreferencesKey("favorite_ids")

    fun getFavorite(context: Context): Flow<Set<String>> {
        return context.favoriteDateStore.data.map { prefs ->
            prefs[FAVORITES_KEY] ?: emptySet()
        }
    }

    suspend fun toggleFavorite(context: Context, songId: String){
        context.favoriteDateStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()
            prefs[FAVORITES_KEY] = if (current.contains(songId)){
                current - songId
            }else{
                current + songId
            }
        }
    }

}