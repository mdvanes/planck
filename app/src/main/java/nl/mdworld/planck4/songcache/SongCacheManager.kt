package nl.mdworld.planck4.songcache

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale

/**
 * Manages caching of streamed songs for offline playback.
 * Max size: 500MB. Evicts least-recently-modified files (oldest) when space is needed.
 */
object SongCacheManager {
    internal const val DIR_NAME = "song_cache"
    private const val MAX_SIZE_BYTES: Long = 500L * 1024 * 1024 // 500MB

    private fun directory(context: Context): File = File(context.cacheDir, DIR_NAME).apply { mkdirs() }

    private fun fileNameForId(songId: String): String {
        // Hash to keep filename safe & short, keep original tail for debugging
        val md = MessageDigest.getInstance("MD5").digest(songId.toByteArray()).joinToString("") { "%02x".format(it) }
        return "${md}_${songId.take(16).replace(Regex("[^a-zA-Z0-9._-]"), "_")}.bin"
    }

    private fun fileForSong(context: Context, songId: String): File = File(directory(context), fileNameForId(songId))

    fun isCached(context: Context, songId: String): Boolean = fileForSong(context, songId).exists()

    fun getCachedFile(context: Context, songId: String): File? = fileForSong(context, songId).takeIf { it.exists() }

    fun sizeBytes(context: Context): Long {
        val dir = directory(context)
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    fun clear(context: Context) {
        directory(context).deleteRecursively(); directory(context).mkdirs()
    }

    fun formatSize(bytes: Long): String = String.format(Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0)

    suspend fun sizeBytesAsync(context: Context): Long = withContext(Dispatchers.IO) { sizeBytes(context) }
    suspend fun clearAsync(context: Context) = withContext(Dispatchers.IO) { clear(context) }

    private fun evictIfNeeded(context: Context, incomingSize: Long) {
        val dir = directory(context)
        var current = sizeBytes(context)
        if (current + incomingSize <= MAX_SIZE_BYTES) return
        // Sort files by lastModified ascending (oldest first)
        val files = dir.listFiles()?.filter { it.isFile }?.sortedBy { it.lastModified() } ?: return
        val iterator = files.iterator()
        while (current + incomingSize > MAX_SIZE_BYTES && iterator.hasNext()) {
            val f = iterator.next()
            val len = f.length()
            if (f.delete()) current -= len
        }
    }

    /**
     * Downloads the song if not already cached. Returns cached file or null on failure.
     */
    suspend fun cacheSongIfNeeded(context: Context, songId: String): File? = withContext(Dispatchers.IO) {
        val existing = getCachedFile(context, songId)
        if (existing != null) {
            existing.setLastModified(System.currentTimeMillis())
            return@withContext existing
        }
        val url = SubsonicUrlBuilder.buildStreamUrl(context, songId)
        val connection = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 10000; readTimeout = 20000 }
        return@withContext try {
            connection.connect()
            if (connection.responseCode in 200..299) {
                val temp = File.createTempFile("dl_", ".part", directory(context))
                connection.inputStream.use { input ->
                    temp.outputStream().use { output ->
                        copyStream(input, output)
                    }
                }
                val length = temp.length()
                evictIfNeeded(context, length)
                val finalFile = fileForSong(context, songId)
                if (temp.renameTo(finalFile)) {
                    finalFile.setLastModified(System.currentTimeMillis())
                    finalFile
                } else {
                    temp.delete(); null
                }
            } else null
        } catch (_: Exception) { null } finally { connection.disconnect() }
    }

    private fun copyStream(input: InputStream, output: java.io.OutputStream) {
        val buf = ByteArray(32 * 1024)
        while (true) {
            val r = input.read(buf)
            if (r <= 0) break
            output.write(buf, 0, r)
        }
    }
}

