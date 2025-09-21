package nl.mdworld.planck4

import android.media.MediaPlayer
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Central registry for MediaPlayer instances so we can reliably stop audio
 * when the app crashes, is backgrounded, or terminated.
 */
object AppAudioManager {
    private val players = CopyOnWriteArrayList<WeakReference<MediaPlayer>>()

    fun register(player: MediaPlayer) {
        players.add(WeakReference(player))
    }

    fun releaseAll() {
        players.forEach { ref ->
            ref.get()?.let { p ->
                runCatching {
                    if (p.isPlaying) p.stop()
                    p.reset()
                    p.release()
                }
            }
        }
        players.clear()
    }

    fun cleanupAllState() {
        PlanckAppStateHolder.get()?.cleanup()
        releaseAll()
    }
}
