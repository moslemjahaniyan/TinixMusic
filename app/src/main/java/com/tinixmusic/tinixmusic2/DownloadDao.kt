package com.tinixmusic.tinixmusic2


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAll(): Flow<List<DownloadedSong>>

    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId")
    suspend fun getById(songId: String): DownloadedSong?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: DownloadedSong)

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun delete(songId: String)
}
