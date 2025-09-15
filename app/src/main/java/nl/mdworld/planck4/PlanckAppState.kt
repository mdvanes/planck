package nl.mdworld.planck4

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.mdworld.planck4.SettingsManager.DEFAULT_RADIO_URL
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder
import nl.mdworld.planck4.views.library.Album
import nl.mdworld.planck4.views.library.Artist
import nl.mdworld.planck4.views.playlists.Playlist
import nl.mdworld.planck4.views.radio.RadioMetadataManagerFactory
import nl.mdworld.planck4.views.song.Song
import kotlin.text.get

@Composable
fun rememberPlanckAppState(context: Context = LocalContext.current) = remember(context) {
    PlanckAppState(context)
}

enum class AppScreen {
    PLAYLISTS,
    SONGS,
    SETTINGS,
    ARTISTS,
    ALBUMS,
    ALBUM_SONGS,
    RADIO
}

class PlanckAppState(private val context: Context) {
    val playlists = mutableStateListOf(
        Playlist(
            id = "empty",
            coverArt = "Empty",
            name = "No Playlist"
        )
    )

    val songs = mutableStateListOf<Song>()

    // Album navigation state
    val artists = mutableStateListOf<Artist>()
    val albums = mutableStateListOf<Album>()

    var currentScreen by mutableStateOf(AppScreen.PLAYLISTS)
        private set

    // Reload trigger - increment this to force data reloading
    var reloadTrigger by mutableStateOf(0)
        private set

    var selectedPlaylistId by mutableStateOf<String?>(null)
        private set

    var selectedPlaylistName by mutableStateOf<String?>(null)
        private set

    // Album navigation state
    var selectedArtistId by mutableStateOf<String?>(null)
        private set

    var selectedArtistName by mutableStateOf<String?>(null)
        private set

    var selectedAlbumId by mutableStateOf<String?>(null)
        private set

    var selectedAlbumName by mutableStateOf<String?>(null)
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

    // Radio metadata tracking using RadioMetadataManager
    private var radioMetadataManager: nl.mdworld.planck4.views.radio.RadioMetadataManager =
        createRadioMetadataManager()

    private fun createRadioMetadataManager(): nl.mdworld.planck4.views.radio.RadioMetadataManager {
        val url = SettingsManager.getRadioUrl(context)
        return if (url == SettingsManager.DEFAULT_RADIO_URL) {
            RadioMetadataManagerFactory.createForNPORadio2()
        } else {
            RadioMetadataManagerFactory.createGeneric()
        }
    }

    fun updateRadioMetadataManager() {
        radioMetadataManager.stopMonitoring()
        radioMetadataManager = createRadioMetadataManager()
    }

    var currentRadioMetadata by mutableStateOf<Map<String, String>>(emptyMap())
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

    // Library navigation methods
    fun navigateToRadio() {
        currentScreen = AppScreen.RADIO
    }

    fun navigateToArtists() {
        currentScreen = AppScreen.ARTISTS
        // Clear any previous album selection
        selectedArtistId = null
        selectedArtistName = null
        selectedAlbumId = null
        selectedAlbumName = null
    }

    fun navigateToAlbums(artistId: String, artistName: String) {
        selectedArtistId = artistId
        selectedArtistName = artistName
        currentScreen = AppScreen.ALBUMS
        albums.clear()
    }

    fun navigateToAlbumSongs(albumId: String, albumName: String) {
        selectedAlbumId = albumId
        selectedAlbumName = albumName
        currentScreen = AppScreen.ALBUM_SONGS
        songs.clear()
    }

    fun playStream(song: Song) {
        try {
            // Stop current playback if any
            stopPlayback()

            // Set the active song and find its index in the current playlist
            activeSong = song
            currentSongIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0

            // Reset progress for new song
            currentPosition = 0
            duration = 0

            // Create new MediaPlayer instance
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                // Build the stream URL
                val streamUrl = SubsonicUrlBuilder.buildStreamUrl(context, song.id)

                setDataSource(streamUrl)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    this@PlanckAppState.isPlaying = true
                    this@PlanckAppState.duration = duration
                    this@PlanckAppState.currentPosition = 0
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
        if (isRadioPlaying) {
            // Handle radio pause
            radioPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    isPlaying = false
                    isRadioPlaying = false
                }
            }
        } else {
            // Handle regular song pause
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    isPlaying = false
                    stopProgressUpdates()
                }
            }
        }
    }

    fun resumePlayback() {
        if (activeSong?.id == "radio-stream") {
            // Handle radio resume
            radioPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlaying = true
                    isRadioPlaying = true
                }
            }
        } else {
            // Handle regular song resume
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlaying = true
                    startProgressUpdates()
                }
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

            // Create a virtual radio song for display in bottom bar
            val dummySong = Song(
                id = "radio-stream",
                title = "Radio 2",
                artist = "NPO Radio",
                album = "Live Stream",
                duration = 0, // Radio has no duration
                coverArt = null
            )

            activeSong = dummySong

            radioPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                // Use the radio URL from settings
                val audioUrl = SettingsManager.getRadioUrl(context)
                setDataSource(audioUrl)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    this@PlanckAppState.isRadioPlaying = true
                    this@PlanckAppState.isPlaying = true // Set main playing state for bottom bar

                    // Start metadata monitoring using RadioMetadataManager
                    radioMetadataManager.startMonitoring(audioUrl, onSuccess = { metadata ->
                        //println("Radio Metadata: $metadata")
                        val artist = metadata.song.artist ?: "Unknown Artist"
                        val broadcast = metadata.broadcast?.title ?: "Live Stream"

                        activeSong = Song(
                            id = "radio-stream",
                            title = metadata.song.title ?: "Unknown Title",
                            artist = "$artist - $broadcast",
                            album = "Radio Stream",
                            duration = 0,
                            coverArt = metadata.song.imageUrl ?: metadata.broadcast?.imageUrl
                        )
                    }, onError = { metadata ->
                        activeSong = dummySong
                    })
                }

                setOnErrorListener { _, _, _ ->
                    this@PlanckAppState.isRadioPlaying = false
                    this@PlanckAppState.isPlaying = false
                    this@PlanckAppState.activeSong = null
                    false
                }

                setOnCompletionListener {
                    this@PlanckAppState.isRadioPlaying = false
                    this@PlanckAppState.isPlaying = false
                    this@PlanckAppState.activeSong = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRadioPlaying = false
            isPlaying = false
            activeSong = null
        }
    }

    fun stopRadio() {
        radioMetadataManager.stopMonitoring()
        radioPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.reset()
            player.release()
        }
        radioPlayer = null
        isRadioPlaying = false
        isPlaying = false
        activeSong = null
        currentRadioMetadata = emptyMap()
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
        radioMetadataManager.cleanup()
        progressUpdateScope.cancel()
    }

    fun triggerReload() {
        reloadTrigger++
        // Clear all data to force fresh loading
        playlists.clear()
        playlists.add(
            Playlist(
                id = "empty",
                coverArt = "Empty",
                name = "No Playlist"
            )
        )
        songs.clear()
        artists.clear()
        albums.clear()
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
