package nl.mdworld.planck4.songcache

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.mdworld.planck4.views.song.Song
import java.io.File

/**
 * Caches ONLY ONE playlist song list and ONLY ONE album song list (metadata only).
 * When a new playlist (or album) is cached, the previous one is removed.
 * Falls back to cache only when offline or network fetch fails.
 */
object SongListCacheManager {
    private const val DIR_NAME = "song_list_cache"
    private const val PLAYLIST_FILE = "playlist_cache.json"
    private const val ALBUM_FILE = "album_cache.json"
    private const val EXPIRATION_MS: Long = 14L * 24 * 60 * 60 * 1000 // 14 days

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class CachedSongList(val id: String, val songs: List<Song>)

    private fun baseDir(context: Context): File = File(context.cacheDir, DIR_NAME).apply { mkdirs() }
    private fun playlistFile(context: Context) = File(baseDir(context), PLAYLIST_FILE)
    private fun albumFile(context: Context) = File(baseDir(context), ALBUM_FILE)

    // --- Playlist cache ---
    fun savePlaylistSongs(context: Context, playlistId: String, songs: List<Song>) {
        runCatching {
            val f = playlistFile(context)
            // Overwrite file (one cache only). Old album file untouched.
            f.writeText(json.encodeToString(CachedSongList(playlistId, songs)))
        }
    }

    fun loadPlaylistSongs(context: Context, playlistId: String): List<Song>? = runCatching {
        val f = playlistFile(context)
        if (!f.exists()) {
            // Backward compatibility: look for legacy playlist_<id>.json file and migrate
            val legacy = baseDir(context).listFiles()?.firstOrNull { it.name.startsWith("playlist_") && it.name.contains(playlistId) }
            if (legacy != null) {
                return@runCatching migrateLegacyPlaylistFile(f, legacy, playlistId)
            }
            return@runCatching null
        }
        if (isExpired(f)) { f.delete(); return@runCatching null }
        val cached = json.decodeFromString<CachedSongList>(f.readText())
        if (cached.id == playlistId) cached.songs else null
    }.getOrNull()

    private fun migrateLegacyPlaylistFile(target: File, legacy: File, requestedId: String): List<Song>? {
        return runCatching {
            val songs = json.decodeFromString<List<Song>>(legacy.readText())
            target.writeText(json.encodeToString(CachedSongList(requestedId, songs)))
            legacy.delete()
            songs
        }.getOrNull()
    }

    // --- Album cache ---
    fun saveAlbumSongs(context: Context, albumId: String, songs: List<Song>) {
        runCatching {
            val f = albumFile(context)
            f.writeText(json.encodeToString(CachedSongList(albumId, songs)))
        }
    }

    fun loadAlbumSongs(context: Context, albumId: String): List<Song>? = runCatching {
        val f = albumFile(context)
        if (!f.exists()) {
            // Backward compatibility: legacy album_<id>.json
            val legacy = baseDir(context).listFiles()?.firstOrNull { it.name.startsWith("album_") && it.name.contains(albumId) }
            if (legacy != null) return@runCatching migrateLegacyAlbumFile(f, legacy, albumId)
            return@runCatching null
        }
        if (isExpired(f)) { f.delete(); return@runCatching null }
        val cached = json.decodeFromString<CachedSongList>(f.readText())
        if (cached.id == albumId) cached.songs else null
    }.getOrNull()

    private fun migrateLegacyAlbumFile(target: File, legacy: File, requestedId: String): List<Song>? {
        return runCatching {
            val songs = json.decodeFromString<List<Song>>(legacy.readText())
            target.writeText(json.encodeToString(CachedSongList(requestedId, songs)))
            legacy.delete(); songs
        }.getOrNull()
    }

    private fun isExpired(f: File): Boolean = (System.currentTimeMillis() - f.lastModified()) > EXPIRATION_MS

    fun clearAll(context: Context) {
        playlistFile(context).delete(); albumFile(context).delete()
    }
}
