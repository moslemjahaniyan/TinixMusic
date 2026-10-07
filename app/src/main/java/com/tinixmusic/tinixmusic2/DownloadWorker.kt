package com.tinixmusic.tinixmusic2



import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val songId = inputData.getString("songId") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()
        val artist = inputData.getString("artist")
        val url = inputData.getString("url") ?: return Result.failure()
        val imageUrl = inputData.getString("imageUrl")

        return try {
            val fileName = "song_$songId.mp3"
            val outputFile = File(applicationContext.filesDir, fileName)

            // دانلود فایل
            withContext(Dispatchers.IO) {
                URL(url).openStream().use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            // ذخیره توی دیتابیس
            val dao = AppDatabase.getInstance(applicationContext).downloadDao()
            dao.insert(
                DownloadedSong(
                    songId = songId,
                    title = title,
                    artist = artist,
                    localPath = outputFile.absolutePath,
                    imageUrl = imageUrl
                )
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}