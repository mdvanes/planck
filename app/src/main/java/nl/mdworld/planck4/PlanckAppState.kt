package nl.mdworld.planck4

import android.Manifest
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder
import nl.mdworld.planck4.util.radiometadata.RadioMetadata
import nl.mdworld.planck4.views.library.Album
import nl.mdworld.planck4.views.library.Artist
import nl.mdworld.planck4.views.playlists.Playlist
import nl.mdworld.planck4.views.radio.RadioMetadataManagerFactory
import nl.mdworld.planck4.views.song.Song
import java.lang.ref.WeakReference
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresPermission
import android.content.Intent

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
    init {
        PlanckAppStateHolder.set(this)
        // Removed immediate notification posting to avoid crashes during very early app startup.
    }

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

    var radioMetadata by mutableStateOf<List<RadioMetadata>>(listOf())

    // Current song index in the playlist for auto-progression
    var currentSongIndex by mutableStateOf(0)
        private set

    // MediaPlayer for audio streaming
    private var mediaPlayer: MediaPlayer? = null
    var isPlaying by mutableStateOf(false)

    // Radio state management
    private var radioPlayer: MediaPlayer? = null
    var isRadioPlaying by mutableStateOf(false)

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

    // MediaSessionCompat for handling media metadata and playback state
    private val mediaSession: MediaSessionCompat = MediaSessionCompat(context, "PlanckSession").apply {
        setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        isActive = true
        // Set initial static metadata
        val initialMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Planck")
            .build()
        setMetadata(initialMetadata)
        val initialState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_STOP or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(PlaybackStateCompat.STATE_STOPPED, 0L, 1.0f)
            .build()
        setPlaybackState(initialState)
    }

    private val notificationChannelId = "planck_playback"
    private val notificationId = 1001

    // Safe no-op channel creator (kept for potential future dynamic updates)
    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (mgr.getNotificationChannel(notificationChannelId) == null) {
                    val ch = NotificationChannel(notificationChannelId, "Playback", NotificationManager.IMPORTANCE_LOW)
                    mgr.createNotificationChannel(ch)
                }
            } catch (_: Exception) { /* Ignore any failure on automotive / restricted hosts */ }
        }
    }

    private fun sendMetadataToService(song: Song?) {
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_METADATA
            if (song != null) {
                putExtra(MediaPlaybackService.EXTRA_SONG_ID, song.id)
                putExtra(MediaPlaybackService.EXTRA_SONG_TITLE, song.title)
                song.artist?.let { putExtra(MediaPlaybackService.EXTRA_SONG_ARTIST, it) }
                song.album?.let { putExtra(MediaPlaybackService.EXTRA_SONG_ALBUM, it) }
                song.duration?.let { putExtra(MediaPlaybackService.EXTRA_SONG_DURATION, it) }
            }
        }
        try { context.startService(intent) } catch (_: Exception) {}
    }

    private fun sendPlaybackStateToService(state: Int) {
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_PLAYBACK_STATE
            putExtra(MediaPlaybackService.EXTRA_PLAYBACK_STATE, state)
        }
        try { context.startService(intent) } catch (_: Exception) {}
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateMediaSessionMetadata(title: String = "Planck") {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .build()
        mediaSession.setMetadata(metadata)
        // Forward to service as well
        activeSong?.let { sendMetadataToService(it.copy(title = title)) }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun updateMediaSessionPlaybackState(state: Int, position: Long = 0L) {
        val actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, position, 1.0f)
            .build()
        mediaSession.setPlaybackState(playbackState)
        sendPlaybackStateToService(state)
    }

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

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun playStream(song: Song) {
        try {
            stopPlayback()

            // Set the active song and find its index in the current playlist
            activeSong = song
            // Update session + service with real song title
            updateMediaSessionMetadata(song.title)
            currentSongIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
            sendMetadataToService(song)

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
                    start(); this@PlanckAppState.isPlaying = true; this@PlanckAppState.duration = duration; this@PlanckAppState.currentPosition = 0; startProgressUpdates();
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
                }

                setOnErrorListener { _, _, _ -> this@PlanckAppState.isPlaying = false; stopProgressUpdates(); updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L); false }

                setOnCompletionListener {
                    this@PlanckAppState.isPlaying = false
                    stopProgressUpdates()
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
                    // Automatically play next song in playlist
                    playNextSong()
                }
            }.also { AppAudioManager.register(it) }
        } catch (e: Exception) {
            e.printStackTrace(); isPlaying = false; stopProgressUpdates(); updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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
        updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun pausePlayback() {
        if (isRadioPlaying) {
            // Handle radio pause
            radioPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    isPlaying = false
                    isRadioPlaying = false
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED, currentPosition.toLong())
                }
            }
        } else {
            // Handle regular song pause
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    isPlaying = false
                    stopProgressUpdates()
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PAUSED, currentPosition.toLong())
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
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, currentPosition.toLong())
                }
            }
        } else {
            // Handle regular song resume
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPlaying = true
                    startProgressUpdates()
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, currentPosition.toLong())
                }
            }
        }
    }

    // Play the next song in the playlist
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun skipToNext() {
        playNextSong()
    }

    // Radio control methods
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun startRadio() {
        try {
            // Stop any current radio playback
            stopRadio()

            // Stop regular music playback to avoid conflicts
            stopPlayback()

            val firstTrack = radioMetadata.firstOrNull();
            val title = firstTrack?.song?.title ?: "NPO Radio 2"
            val artist = firstTrack?.song?.artist ?: ""
            // Create a virtual radio song for display in bottom bar
            val dummySong = Song(
                id = "radio-stream",
                title = title,
                artist = artist,
                album = "Live Stream",
                duration = 0, // Radio has no duration
                coverArt = null
            )

            activeSong = dummySong
            updateMediaSessionMetadata(dummySong.title)
            sendMetadataToService(dummySong)

            radioPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())

                // Use the radio URL from settings
                val audioUrl = SettingsManager.getRadioUrl(context)
                setDataSource(audioUrl)
                prepareAsync()

                setOnPreparedListener {
                    start()
                    this@PlanckAppState.isRadioPlaying = true
                    this@PlanckAppState.isPlaying = true
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)

                    // Start metadata monitoring using RadioMetadataManager
                    radioMetadataManager.startMonitoring(audioUrl, onSuccess = { metadata ->
                        val firstTrack = metadata.firstOrNull();
                        val artist = firstTrack?.song?.artist ?: "Unknown Artist"
                        val prevStartTime = radioMetadata.firstOrNull()?.time?.start
                        val newStartTime = firstTrack?.time?.start
                        // Prevent stale metadata from updating the display
                        if(prevStartTime == null || (newStartTime != null && newStartTime > prevStartTime)) {
                            if(isRadioPlaying) {
                                activeSong = Song(
                                    id = "radio-stream",
                                    title = firstTrack?.song?.title ?: "Unknown Title",
                                    artist = artist,
                                    album = "Radio Stream",
                                    duration = 0,
                                    coverArt = firstTrack?.song?.imageUrl ?: firstTrack?.broadcast?.imageUrl)
                                sendMetadataToService(activeSong)
                                updateMediaSessionMetadata(activeSong?.title ?: "Radio Stream")
                            }
                            radioMetadata = metadata
                        }
                    }, onError = { _ -> activeSong = dummySong })
                }

                setOnErrorListener { _, _, _ -> this@PlanckAppState.isRadioPlaying = false; this@PlanckAppState.isPlaying = false; this@PlanckAppState.activeSong = null; updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L); false }

                setOnCompletionListener {
                    this@PlanckAppState.isRadioPlaying = false
                    this@PlanckAppState.isPlaying = false
                    this@PlanckAppState.activeSong = null
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
                }
            }.also { AppAudioManager.register(it) }
        } catch (e: Exception) {
            e.printStackTrace(); isRadioPlaying = false; isPlaying = false; activeSong = null; updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
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
        updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
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
                    delay(100)
                } catch (_: Exception) {
                    break
                }
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun cleanup() {
        stopPlayback()
        stopRadio()
        radioMetadataManager.cleanup()
        progressUpdateScope.cancel()
        mediaSession.release()
        // Notification persistence handled by service; nothing to cancel here.
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

object PlanckAppStateHolder {
    private var ref: WeakReference<PlanckAppState>? = null
    fun set(state: PlanckAppState) { ref = WeakReference(state) }
    fun get(): PlanckAppState? = ref?.get()
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
