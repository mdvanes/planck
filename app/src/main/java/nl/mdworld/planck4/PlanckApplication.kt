package nl.mdworld.planck4

import android.app.Application
import android.util.Log

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
    }
}

