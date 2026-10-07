package com.tinixmusic.tinixmusic2



import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow

object DownloadRepository {

    fun getDownloadedSongs(context: Context): Flow<List<DownloadedSong>> {
        return AppDatabase.getInstance(context).downloadDao().getAll()
    }

    suspend fun isDownloaded(context: Context, songId: String): Boolean {
        return AppDatabase.getInstance(context).downloadDao().getById(songId) != null
    }

    suspend fun deleteDownload(context: Context, songId: String) {
        val dao = AppDatabase.getInstance(context).downloadDao()
        val song = dao.getById(songId)
        song?.let {
            java.io.File(it.localPath).delete() // حذف فایل
        }
        dao.delete(songId)
    }

    fun startDownload(
        context: Context,
        song: Song,
        url: String
    ) {
        val data = Data.Builder()
            .putString("songId", song.id)
            .putString("title", song.title)
            .putString("artist", song.artist)
            .putString("url", url)
            .putString("imageUrl", song.imageUrl)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }


    suspend fun getLocalPath(context: Context, songId: String): String? {
        return AppDatabase.getInstance(context).downloadDao().getById(songId)?.localPath
    }

}