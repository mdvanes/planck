package nl.mdworld.planck4.imageloading

import android.content.Context
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import nl.mdworld.planck4.PlanckApplication

/**
 * Manages the on-disk cache for album/cover art.
 * A 100MB disk cache is configured in [PlanckApplication]. This helper offers utilities
 * to query the current size and clear the cache. Size calculations run on the caller thread,
 * so prefer calling them from a background dispatcher (IO).
 */
object CoverArtCacheManager {
    internal const val DIR_NAME = "album_art_cache"

    private fun directory(context: Context): File = File(context.cacheDir, DIR_NAME)

    /** Returns the current size of the cache directory in bytes. */
    fun sizeBytes(context: Context): Long {
        val dir = directory(context)
        if (!dir.exists()) return 0L
        return dir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }

    /** Clears the cache directory AND asks Coil's disk cache (if present) to clear. */
    @OptIn(ExperimentalCoilApi::class)
    fun clear(context: Context) {
        // Clear via Coil API first (if configured)
        try {
            Coil.imageLoader(context).diskCache?.clear()
        } catch (_: Throwable) {}
        // Ensure directory removed
        directory(context).deleteRecursively()
        directory(context).mkdirs()
    }

    /** Formats bytes to a human readable MB string with one decimal. */
    fun formatSize(bytes: Long): String = String.format(Locale.US, "%.1f MB", bytes / 1024.0 / 1024.0)

    /** Convenience suspend function to compute size off the main thread. */
    suspend fun sizeBytesAsync(context: Context): Long = withContext(Dispatchers.IO) { sizeBytes(context) }
    /** Convenience suspend function to clear cache off the main thread. */
    suspend fun clearAsync(context: Context) = withContext(Dispatchers.IO) { clear(context) }
}
