package nl.mdworld.planck4

import android.Manifest
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*
import nl.mdworld.planck4.networking.subsonic.SubsonicApi
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder
import nl.mdworld.planck4.util.radiometadata.RadioMetadata
import nl.mdworld.planck4.views.library.Album
import nl.mdworld.planck4.views.library.Artist
import nl.mdworld.planck4.views.playlists.Playlist
import nl.mdworld.planck4.views.radio.RadioMetadataManagerFactory
import nl.mdworld.planck4.views.song.Song
import java.lang.ref.WeakReference
import android.support.v4.media.session.PlaybackStateCompat
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

    // Update service communication helpers
    private fun sendMetadataToService(song: Song?) {
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_METADATA
            if (song != null) {
                putExtra(MediaPlaybackService.EXTRA_SONG_ID, song.id)
                putExtra(MediaPlaybackService.EXTRA_SONG_TITLE, song.title)
                song.artist?.let { putExtra(MediaPlaybackService.EXTRA_SONG_ARTIST, it) }
                song.album?.let { putExtra(MediaPlaybackService.EXTRA_SONG_ALBUM, it) }
                song.duration?.let { putExtra(MediaPlaybackService.EXTRA_SONG_DURATION, it) }
                song.coverArt?.let { art ->
                    if (art.startsWith("http", true)) putExtra(MediaPlaybackService.EXTRA_SONG_COVER_ART_URL, art)
                    else putExtra(MediaPlaybackService.EXTRA_SONG_COVER_ART_ID, art)
                }
            }
        }
        try { context.startService(intent) } catch (_: Exception) {}
    }

    private fun sendPlaybackStateToService(state: Int, position: Long = 0L) {
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_PLAYBACK_STATE
            putExtra(MediaPlaybackService.EXTRA_PLAYBACK_STATE, state)
            putExtra(MediaPlaybackService.EXTRA_PLAYBACK_POSITION, position)
        }
        try { context.startService(intent) } catch (_: Exception) {}
    }

    // Renamed semantics: just forward metadata
    private fun updateMediaSessionMetadata(title: String) {
        activeSong?.let { base ->
            val songForService = base.copy(title = title)
            sendMetadataToService(songForService)
        } ?: run {
            // No active song yet; ignore
        }
    }

    private fun updateMediaSessionPlaybackState(state: Int, position: Long = 0L) {
        sendPlaybackStateToService(state, position)
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

    fun playStream(song: Song) {
        try {
            if (song.id != "radio-stream") {
                when (currentScreen) {
                    AppScreen.SONGS -> {
                        selectedPlaylistId?.let {
                            SettingsManager.saveLastPlaylistId(context, it)
                            SettingsManager.saveLastFolderId(context, "")
                        }
                    }
                    AppScreen.ALBUM_SONGS -> {
                        selectedAlbumId?.let {
                            SettingsManager.saveLastFolderId(context, it)
                            SettingsManager.saveLastPlaylistId(context, "")
                        }
                    }
                    else -> { /* no-op for other screens */ }
                }
                SettingsManager.saveLastSongId(context, song.id)
            }
            stopPlayback()
            activeSong = song
            currentSongIndex = songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
            currentPosition = 0; duration = 0
            sendMetadataToService(song)
            updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_BUFFERING, 0L)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                val streamUrl = SubsonicUrlBuilder.buildStreamUrl(context, song.id)
                setDataSource(streamUrl)
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start()
                    this@PlanckAppState.isPlaying = true
                    this@PlanckAppState.duration = mp.duration
                    startProgressUpdates()
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
                }
                setOnErrorListener { _, _, _ -> this@PlanckAppState.isPlaying = false; stopProgressUpdates(); updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L); false }
                setOnCompletionListener { this@PlanckAppState.isPlaying = false; stopProgressUpdates(); updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L); playNextSong() }
            }.also { AppAudioManager.register(it) }
        } catch (e: Exception) {
            e.printStackTrace(); isPlaying = false; stopProgressUpdates(); updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
        }
    }

    fun stopPlayback() {
        stopProgressUpdates()
        mediaPlayer?.let { if (it.isPlaying) it.stop(); it.reset(); it.release() }
        mediaPlayer = null
        isPlaying = false; currentPosition = 0; duration = 0
        updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
    }

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
            radioPlayer?.let { p -> if (!p.isPlaying) { p.start(); isPlaying = true; isRadioPlaying = true; updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, currentPosition.toLong()) } }
        } else {
            mediaPlayer?.let { p -> if (!p.isPlaying) { p.start(); isPlaying = true; startProgressUpdates(); updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, currentPosition.toLong()) } }
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

            val firstTrack = radioMetadata.firstOrNull()
            val title = firstTrack?.song?.title ?: "NPO Radio 2"
            val artist = firstTrack?.song?.artist ?: ""
            // Create a virtual radio song for display in the bottom bar
            val dummySong = Song(
                id = "radio-stream",
                title = title,
                artist = artist,
                album = "Live Stream",
                duration = 0, // Radio has no duration
                coverArt = null
            )

            activeSong = dummySong
            sendMetadataToService(dummySong)
            updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_BUFFERING, 0L)
            radioPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())

                // Use the radio URL from settings
                val audioUrl = SettingsManager.getRadioUrl(context)
                setDataSource(audioUrl)
                prepareAsync()

                setOnPreparedListener { rp ->
                    rp.start()
                    this@PlanckAppState.isRadioPlaying = true; this@PlanckAppState.isPlaying = true
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)

                    // Start metadata monitoring using RadioMetadataManager
                    radioMetadataManager.startMonitoring(audioUrl, onSuccess = { metadata ->
                        val newFirst = metadata.firstOrNull()
                        val prevStart = radioMetadata.firstOrNull()?.time?.start
                        val newStart = newFirst?.time?.start
                        if (prevStart == null || (newStart != null && newStart > prevStart)) {
                            if (this@PlanckAppState.isRadioPlaying) {
                                this@PlanckAppState.activeSong = Song(
                                    id = "radio-stream",
                                    title = newFirst?.song?.title ?: "Unknown Title",
                                    artist = newFirst?.song?.artist ?: "Unknown Artist",
                                    album = "Radio Stream",
                                    duration = 0,
                                    coverArt = newFirst?.song?.imageUrl ?: newFirst?.broadcast?.imageUrl
                                )
                                sendMetadataToService(this@PlanckAppState.activeSong)
                            }
                            radioMetadata = metadata
                        }
                    }, onError = { _ -> this@PlanckAppState.activeSong = dummySong })
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
            e.printStackTrace(); this@PlanckAppState.isRadioPlaying = false; this@PlanckAppState.isPlaying = false; this@PlanckAppState.activeSong = null; updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
        }
    }

    fun stopRadio() {
        radioMetadataManager.stopMonitoring()
        radioSkipMonitorJob?.cancel(); radioSkipMonitorJob = null; isRadioTemporarilyPausedForSkip = false
        radioPlayer?.let { if (it.isPlaying) it.stop(); it.reset(); it.release() }
        radioPlayer = null; isRadioPlaying = false; isPlaying = false; activeSong = null; currentRadioMetadata = emptyMap()
        updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
    }

    // Progress updates (optionally could push position; keep lightweight)
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressUpdateJob = progressUpdateScope.launch {
            while (isPlaying && mediaPlayer != null) {
                mediaPlayer?.let { if (it.isPlaying) { currentPosition = it.currentPosition } }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }

    // Skip-to-last-context state
    private var radioSkipMonitorJob: Job? = null
    private var isRadioTemporarilyPausedForSkip: Boolean = false

    fun cleanup() {
        stopPlayback(); stopRadio(); radioMetadataManager.cleanup(); progressUpdateScope.cancel()
        radioSkipMonitorJob?.cancel(); radioSkipMonitorJob = null; isRadioTemporarilyPausedForSkip = false
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

    fun openLastPlaylistIfAvailable(): Boolean {
        val lastId = SettingsManager.getLastPlaylistId(context)
        if (!lastId.isNullOrBlank()) {
            val name = playlists.firstOrNull { it.id == lastId }?.name ?: "Playlist"
            navigateToSongs(lastId, name)
            return true
        }
        return false
    }

    // Helper to resume radio stream updating activeSong metadata from current radioMetadata
    private fun resumeRadioStreamWithCurrentMetadata() {
        radioPlayer?.let { rp ->
            try {
                rp.start()
                val firstTrack = radioMetadata.firstOrNull()
                activeSong = Song(
                    id = "radio-stream",
                    title = firstTrack?.song?.title ?: firstTrack?.broadcast?.title ?: "Radio Stream",
                    artist = firstTrack?.song?.artist ?: firstTrack?.broadcast?.presenters ?: "",
                    album = "Radio Stream",
                    duration = 0,
                    coverArt = firstTrack?.song?.imageUrl ?: firstTrack?.broadcast?.imageUrl
                )
                sendMetadataToService(activeSong)
                isRadioPlaying = true
                isPlaying = true
                updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    /**
     * Implements the "skip" behavior from the Radio screen:
     * 1. Pause radio stream (but keep metadata monitoring active).
     * 2. Load last playlist or folder context and start playing lastSongId.
     * 3. Monitor radio metadata; when a new first track (time.start) appears, stop song and resume radio.
     */
    fun skipRadioToLastContext() {
        // Preconditions: need a lastSongId AND (playlist OR folder context)
        val lastSongId = SettingsManager.getLastSongId(context)
        val lastPlaylistId = SettingsManager.getLastPlaylistId(context)
        val lastFolderId = SettingsManager.getLastFolderId(context)
        if (lastSongId.isNullOrBlank() || (lastPlaylistId.isNullOrBlank() && lastFolderId.isNullOrBlank())) {
            return // Nothing to do
        }
        if (!isRadioPlaying || radioPlayer == null) return // Skip only meaningful when radio currently playing

        // Pause radio (do not stop monitoring)
        radioPlayer?.let { rp -> if (rp.isPlaying) { rp.pause(); isRadioPlaying = false; isPlaying = false } }
        isRadioTemporarilyPausedForSkip = true

        val initialStart = radioMetadata.firstOrNull()?.time?.start

        // Cancel any previous job
        radioSkipMonitorJob?.cancel()

        // Launch loading + monitoring
        radioSkipMonitorJob = progressUpdateScope.launch {
            // Step 2: load context & play song
            val api = SubsonicApi()
            try {
                val songsList = mutableListOf<Song>()
                if (!lastPlaylistId.isNullOrBlank()) {
                    try {
                        val response = api.getPlaylistKtor(context, lastPlaylistId)
                        songsList += response.sr.playlist.songs?.map { s ->
                            Song(
                                id = s.id,
                                title = s.title,
                                artist = s.artist,
                                album = s.album,
                                duration = s.duration,
                                coverArt = s.coverArt
                            )
                        }.orEmpty()
                    } catch (e: Exception) { /* ignore */ }
                } else if (!lastFolderId.isNullOrBlank()) {
                    try {
                        val mode = SettingsManager.getBrowsingMode(context)
                        if (mode == SettingsManager.BrowsingMode.FILES) {
                            val root = api.getMusicDirectoryKtor(context, lastFolderId)
                            val children = root.sr.directory.child
                            val directSongs = children.filter { !it.isDir }
                            if (directSongs.isNotEmpty()) {
                                songsList += directSongs.map { child ->
                                    Song(
                                        id = child.id,
                                        title = child.title,
                                        artist = child.artist,
                                        album = child.album ?: "",
                                        duration = child.duration,
                                        coverArt = child.coverArt
                                    )
                                }
                            } else {
                                val discDirs = children.filter { it.isDir }
                                for (disc in discDirs) {
                                    try {
                                        val discDir = api.getMusicDirectoryKtor(context, disc.id)
                                        val discSongs = discDir.sr.directory.child.filter { !it.isDir }
                                        songsList += discSongs.map { child ->
                                            Song(
                                                id = child.id,
                                                title = child.title,
                                                artist = child.artist,
                                                album = child.album ?: "",
                                                duration = child.duration,
                                                coverArt = child.coverArt
                                            )
                                        }
                                    } catch (_: Exception) {}
                                }
                            }
                        } else { // TAGS
                            val albumResp = api.getAlbumKtor(context, lastFolderId)
                            songsList += albumResp.sr.album.songs?.map { s ->
                                Song(
                                    id = s.id,
                                    title = s.title,
                                    artist = s.artist,
                                    album = s.album,
                                    duration = s.duration,
                                    coverArt = s.coverArt
                                )
                            }.orEmpty()
                        }
                    } catch (e: Exception) { /* ignore */ }
                }

                val songToPlay = songsList.firstOrNull { it.id == lastSongId }
                if (songToPlay != null) {
                    songs.clear(); songs.addAll(songsList)
                    playStream(songToPlay)
                } else {
                    resumeRadioStreamWithCurrentMetadata(); isRadioTemporarilyPausedForSkip = false; return@launch
                }
            } catch (e: Exception) {
                resumeRadioStreamWithCurrentMetadata(); isRadioTemporarilyPausedForSkip = false; return@launch
            }

            // Step 3 & 4: monitor radio metadata for change
            try {
                while (isActive && isRadioTemporarilyPausedForSkip) {
                    val currentStart = radioMetadata.firstOrNull()?.time?.start
                    val newMetadataDetected = when {
                        initialStart == null && currentStart != null -> true // first timestamp arrived
                        initialStart != null && currentStart != null && currentStart > initialStart -> true
                        else -> false
                    }
                    if (newMetadataDetected) {
                        stopPlayback()
                        resumeRadioStreamWithCurrentMetadata()
                        isRadioTemporarilyPausedForSkip = false
                        break
                    }
                    delay(2000)
                }
            } catch (_: CancellationException) { /* ignore */ }
        }
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
