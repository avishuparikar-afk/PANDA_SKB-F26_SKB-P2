package com.pashuraksha.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * ModelDownloadManager - handles downloading the GGUF model
 * from a remote URL to context.filesDir/model.gguf.
 *
 * Features:
 *   - Progress callback (0-100%)
 *   - Resume-safe (replaces partial downloads)
 *   - Configurable URL
 */
object ModelDownloadManager {

    private const val TAG = "ModelDownloadManager"

    data class DownloadProgress(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val percent: Int
    )

    /**
     * Downloads the model file. Call from a coroutine on Dispatchers.IO.
     *
     * @param context Android context
     * @param url The download URL (defaults to AiEngineManager.MODEL_DOWNLOAD_URL)
     * @param onProgress Called with download progress updates
     * @return true if download succeeded
     */
    suspend fun downloadModel(
        context: Context,
        url: String = AiEngineManager.MODEL_DOWNLOAD_URL,
        onProgress: (DownloadProgress) -> Unit = {}
    ): Boolean = withContext(Dispatchers.IO) {
        val targetFile = File(context.filesDir, AiEngineManager.MODEL_FILENAME)
        val tempFile = File(context.filesDir, "${AiEngineManager.MODEL_FILENAME}.tmp")

        try {
            Log.d(TAG, "Starting download from: $url")

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 30000
                readTimeout = 60000
                setRequestProperty("User-Agent", "PashuRaksha/2.0")
                instanceFollowRedirects = true
            }
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP error: $responseCode")
                return@withContext false
            }

            val totalBytes = connection.contentLengthLong
            Log.d(TAG, "Total download size: ${totalBytes / 1024 / 1024}MB")

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(1024 * 256) // 256KB buffer
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } > 0) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val percent = if (totalBytes > 0) {
                            (totalRead * 100 / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            -1
                        }

                        onProgress(DownloadProgress(totalRead, totalBytes, percent))
                    }
                }
            }

            // Verify download is complete
            if (tempFile.length() < 100_000_000L) {
                Log.e(TAG, "Downloaded file too small: ${tempFile.length()} bytes")
                tempFile.delete()
                return@withContext false
            }

            // Atomic rename: temp -> final
            if (targetFile.exists()) targetFile.delete()
            val renamed = tempFile.renameTo(targetFile)

            if (renamed) {
                Log.d(TAG, "Download complete! File: ${targetFile.absolutePath} (${targetFile.length() / 1024 / 1024}MB)")
            } else {
                Log.e(TAG, "Failed to rename temp file")
                tempFile.delete()
            }

            return@withContext renamed

        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}", e)
            tempFile.delete()
            return@withContext false
        }
    }
}
