package nl.mdworld.planck4.songcache

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.mdworld.planck4.views.song.Song
import java.io.File

/**
 * Caches playlist + album song lists (metadata only) for offline browsing.
 * Lightweight JSON files stored under cacheDir/song_list_cache.
 * Very small eviction: keep max 100 lists or 5MB total; remove oldest first.
 */
object SongListCacheManager {
    private const val DIR_NAME = "song_list_cache"
    private const val MAX_ENTRIES = 100
    private const val MAX_SIZE_BYTES: Long = 5L * 1024 * 1024 // 5MB
    private const val EXPIRATION_MS: Long = 14L * 24 * 60 * 60 * 1000 // 14 days

    private val json = Json { ignoreUnknownKeys = true }

    private fun baseDir(context: Context): File = File(context.cacheDir, DIR_NAME).apply { mkdirs() }

    private fun playlistFile(context: Context, playlistId: String): File =
        File(baseDir(context), "playlist_${safe(playlistId)}.json")

    private fun albumFile(context: Context, albumId: String): File =
        File(baseDir(context), "album_${safe(albumId)}.json")

    private fun safe(id: String): String = id.replace(Regex("[^a-zA-Z0-9._-]"), "_").take(60)

    private fun evictIfNeeded(context: Context) {
        val dir = baseDir(context)
        val files = dir.listFiles()?.filter { it.isFile } ?: return
        // Remove expired first
        val now = System.currentTimeMillis()
        files.filter { now - it.lastModified() > EXPIRATION_MS }.forEach { it.delete() }
        val remaining = dir.listFiles()?.filter { it.isFile } ?: return
        var totalSize = remaining.sumOf { it.length() }
        var list = remaining.sortedBy { it.lastModified() }.toMutableList()
        while (list.size > MAX_ENTRIES || totalSize > MAX_SIZE_BYTES) {
            val f = list.removeFirstOrNull() ?: break
            val len = f.length()
            if (f.delete()) totalSize -= len
        }
    }

    fun savePlaylistSongs(context: Context, playlistId: String, songs: List<Song>) {
        runCatching {
            evictIfNeeded(context)
            val f = playlistFile(context, playlistId)
            f.writeText(json.encodeToString(songs))
        }
    }

    fun loadPlaylistSongs(context: Context, playlistId: String): List<Song>? = runCatching {
        val f = playlistFile(context, playlistId)
        if (!f.exists()) return null
        if (System.currentTimeMillis() - f.lastModified() > EXPIRATION_MS) { f.delete(); return null }
        json.decodeFromString<List<Song>>(f.readText())
    }.getOrNull()

    fun saveAlbumSongs(context: Context, albumId: String, songs: List<Song>) {
        runCatching {
            evictIfNeeded(context)
            val f = albumFile(context, albumId)
            f.writeText(json.encodeToString(songs))
        }
    }

    fun loadAlbumSongs(context: Context, albumId: String): List<Song>? = runCatching {
        val f = albumFile(context, albumId)
        if (!f.exists()) return null
        if (System.currentTimeMillis() - f.lastModified() > EXPIRATION_MS) { f.delete(); return null }
        json.decodeFromString<List<Song>>(f.readText())
    }.getOrNull()
}

