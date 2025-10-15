package nl.mdworld.planck4

import android.app.Application
import android.util.Log
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import nl.mdworld.planck4.imageloading.CoverArtCacheManager
import java.io.File
import nl.mdworld.planck4.networking.NetworkMonitor

class PlanckApplication : Application() {
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    override fun onCreate() {
        super.onCreate()
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                Log.e("PlanckApplication", "Uncaught exception: ${e.message}")
                AppAudioManager.cleanupAllState()
            } catch (ex: Exception) {
                Log.e("PlanckApplication", "Cleanup failed", ex)
            } finally {
                previousHandler?.uncaughtException(t, e)
            }
        }

        // Initialize a global Coil ImageLoader with a 100MB disk cache dedicated to album art
        val diskCacheDir = File(cacheDir, CoverArtCacheManager.DIR_NAME).apply { mkdirs() }
        val imageLoader = ImageLoader.Builder(this)
            .diskCache(
                DiskCache.Builder()
                    .directory(diskCacheDir)
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB
                    .build()
            )
            // We generate auth tokens per request; still allow caching via explicit keys
            .respectCacheHeaders(false)
            .build()
        Coil.setImageLoader(imageLoader)

        // Start network monitoring for offline behavior
        try { NetworkMonitor.start(this) } catch (_: Exception) {}
    }
}
