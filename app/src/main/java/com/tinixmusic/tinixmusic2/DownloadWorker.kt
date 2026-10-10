package com.tinixmusic.tinixmusic2

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLDecoder

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
            // ✅ استخراج نام اصلی فایل از URL
            val fileName = extractFileName(url)

            val localPath = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveViaMediaStore(fileName, url)
                } else {
                    saveToPublicDownloads(fileName, url)
                }
            }

            val dao = AppDatabase.getInstance(applicationContext).downloadDao()
            dao.insert(
                DownloadedSong(
                    songId = songId,
                    title = title,
                    artist = artist,
                    localPath = localPath,
                    imageUrl = imageUrl
                )
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * استخراج نام فایل از URL.
     * مثال:
     *   https://tinixmusic.ir/wp-content/uploads/2024/05/Ariana-Grande-Song.mp3
     *   → Ariana-Grande-Song.mp3
     *
     * اگر URL پارامتر داشت (مثل ?v=2) حذفش می‌کنه.
     * اگر پسوند نداشت، .mp3 اضافه می‌کنه.
     */
    private fun extractFileName(url: String): String {
        // پاک کردن query string و fragment
        val cleanUrl = url.substringBefore('?').substringBefore('#')

        // گرفتن آخرین بخش بعد از /
        val rawName = cleanUrl.substringAfterLast('/').ifBlank {
            "audio_${System.currentTimeMillis()}.mp3"
        }

        // دیکد کردن کاراکترهای URL-encoded (مثل %20 → فاصله، %D8%A2 → آ)
        val decoded = try {
            URLDecoder.decode(rawName, "UTF-8")
        } catch (e: Exception) {
            rawName
        }

        // اگه پسوند نداره، .mp3 اضافه کن
        val hasExtension = decoded.substringAfterLast('.', "").length in 2..5
        return if (hasExtension) decoded else "$decoded.mp3"
    }

    /** Android 10+ : ذخیره در Downloads/tinixmusic با MediaStore */
    private fun saveViaMediaStore(fileName: String, url: String): String {
        val resolver = applicationContext.contentResolver

        // اگه فایل با همین نام قبلاً وجود داره، پاکش کن تا جایگزین بشه
        deleteExistingFile(fileName)

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/tinixmusic"
            )
        }

        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri: Uri = resolver.insert(collection, values)
            ?: throw IOException("MediaStore insert failed")

        try {
            resolver.openOutputStream(uri)?.use { output ->
                URL(url).openStream().use { input ->
                    input.copyTo(output)
                }
            } ?: throw IOException("Failed to open output stream")
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }

        return uri.toString()
    }

    /** حذف فایل قبلی با همون نام در MediaStore تا دانلود دوباره جایگزین بشه */
    private fun deleteExistingFile(fileName: String) {
        val resolver = applicationContext.contentResolver
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND " +
                "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
        val selectionArgs = arrayOf(
            fileName,
            Environment.DIRECTORY_DOWNLOADS + "/tinixmusic/"
        )
        try {
            resolver.delete(collection, selection, selectionArgs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Android 9 و پایین‌تر */
    private fun saveToPublicDownloads(fileName: String, url: String): String {
        @Suppress("DEPRECATION")
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        val targetDir = File(downloadsDir, "tinixmusic")
        if (!targetDir.exists()) targetDir.mkdirs()

        val outputFile = File(targetDir, fileName)
        // اگه فایل قبلی هست، پاکش کن
        if (outputFile.exists()) outputFile.delete()

        URL(url).openStream().use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return outputFile.absolutePath
    }
}