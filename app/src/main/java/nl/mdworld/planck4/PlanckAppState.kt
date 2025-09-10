package nl.mdworld.planck4

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*

@Composable
fun rememberPlanckAppState(context: Context = LocalContext.current) = remember(context) {
    PlanckAppState(context)
}

enum class AppScreen {
    PLAYLISTS,
    SONGS,
    SETTINGS
}

class PlanckAppState (private val context: Context) {
    val playlists = mutableStateListOf(
        Playlist(
            id = "empty",
            coverArt = "Empty",
            name = "No Playlist"
        )
    )

    val songs = mutableStateListOf<Song>()

    var currentScreen by mutableStateOf(AppScreen.PLAYLISTS)
        private set

    var selectedPlaylistId by mutableStateOf<String?>(null)
        private set

    var selectedPlaylistName by mutableStateOf<String?>(null)
        private set

    var activeSong by mutableStateOf<Song?>(null)

    // MediaPlayer for audio streaming
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying by mutableStateOf(false)

    // Progress tracking
    var currentPosition by mutableStateOf(0)
        private set
    var duration by mutableStateOf(0)
        private set

    // Coroutine for progress updates
    private var progressUpdateJob: Job? = null
    private val progressUpdateScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun navigateToSongs(playlistId: String, playlistName: String) {
        selectedPlaylistId = playlistId
        selectedPlaylistName = playlistName
        currentScreen = AppScreen.SONGS
        songs.clear()
    }

    fun navigateToPlaylists() {
        currentScreen = AppScreen.PLAYLISTS
        selectedPlaylistId = null
        selectedPlaylistName = null
    }

    fun navigateToSettings() {
        currentScreen = AppScreen.SETTINGS
    }

    fun playStream(song: Song) {
        try {
            // Stop current playback if any
            stopPlayback()

            // Set the active song
            activeSong = song

            // Create new MediaPlayer instance
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                // Build the stream URL
                val playerName = context.getString(R.string.subsonic_player_name)
                val apiConfig = "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"
                val streamUrl = "${SettingsManager.getJukeboxBaseUrl(context)}stream${apiConfig}&id=${song.id}"

                setDataSource(streamUrl)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    this@PlanckAppState.isPlaying = true
                    this@PlanckAppState.duration = duration
                    startProgressUpdates()
                }

                setOnErrorListener { _, _, _ ->
                    this@PlanckAppState.isPlaying = false
                    stopProgressUpdates()
                    false
                }

                setOnCompletionListener {
                    this@PlanckAppState.isPlaying = false
                    stopProgressUpdates()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
            stopProgressUpdates()
        }
    }

    fun stopPlayback() {
        stopProgressUpdates()
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            player.release()
        }
        mediaPlayer = null
        isPlaying = false
        currentPosition = 0
        duration = 0
    }

    fun pausePlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                isPlaying = false
                stopProgressUpdates()
            }
        }
    }

    fun resumePlayback() {
        mediaPlayer?.let { player ->
            if (!player.isPlaying) {
                player.start()
                isPlaying = true
                startProgressUpdates()
            }
        }
    }

    private fun startProgressUpdates() {
        stopProgressUpdates() // Stop any existing updates
        progressUpdateJob = progressUpdateScope.launch {
            while (isPlaying && mediaPlayer != null) {
                try {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            currentPosition = player.currentPosition
                        }
                    }
                    delay(100) // Update every 100ms for smooth progress
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    // Clean up resources when the state is destroyed
    fun cleanup() {
        stopPlayback()
        progressUpdateScope.cancel()
    }
}

fun setSelectedPlaylist(context: Context, playlistName: String) {
    val prefs = context.getSharedPreferences("planck_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("selected_playlist", playlistName).apply()

    try {
        ClusterScreen.refreshClusterDisplay()
    } catch (e: Exception) {
        println("Cluster display not available for refresh: ${e.message}")
    }
}

fun getSelectedPlaylist(context: Context): String {
    val prefs = context.getSharedPreferences("planck_prefs", Context.MODE_PRIVATE)
    return prefs.getString("selected_playlist", "No Playlist Selected") ?: "No Playlist Selected"
}
