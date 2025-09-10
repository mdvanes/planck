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

    // Current song index in the playlist for auto-progression
    var currentSongIndex by mutableStateOf(0)
        private set

    // MediaPlayer for audio streaming
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying by mutableStateOf(false)

    // Radio state management
    private var radioPlayer: MediaPlayer? = null
    var isRadioPlaying by mutableStateOf(false)
        private set

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

            // Set the active song and find its index in the current playlist
            activeSong = song
            currentSongIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0

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
                    // Automatically play next song in playlist
                    playNextSong()
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

    // Play the next song in the playlist
    fun playNextSong() {
        if (songs.isNotEmpty()) {
            if (currentSongIndex < songs.size - 1) {
                // Move to the next song in the list
                currentSongIndex++
            } else {
                // Loop back to the first song when reaching the end
                currentSongIndex = 0
            }
            val nextSong = songs[currentSongIndex]
            playStream(nextSong)
        }
    }

    // Play the previous song in the playlist
    fun playPreviousSong() {
        if (songs.isNotEmpty()) {
            if (currentSongIndex > 0) {
                // Move to the previous song in the list
                currentSongIndex--
            } else {
                // Loop to the last song when at the beginning
                currentSongIndex = songs.size - 1
            }
            val previousSong = songs[currentSongIndex]
            playStream(previousSong)
        }
    }

    // Manually trigger next song (for skip button)
    fun skipToNext() {
        playNextSong()
    }

    // Radio control methods
    fun startRadio() {
        try {
            // Stop any current radio playback
            stopRadio()

            // Stop regular music playback to avoid conflicts
            stopPlayback()

            radioPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                val audioUrl = "https://icecast.omroep.nl/radio2-bb-mp3"
                setDataSource(audioUrl)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    this@PlanckAppState.isRadioPlaying = true
                }

                setOnErrorListener { _, _, _ ->
                    this@PlanckAppState.isRadioPlaying = false
                    false
                }

                setOnCompletionListener {
                    this@PlanckAppState.isRadioPlaying = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRadioPlaying = false
        }
    }

    fun stopRadio() {
        radioPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            player.release()
        }
        radioPlayer = null
        isRadioPlaying = false
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
        stopRadio()
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
