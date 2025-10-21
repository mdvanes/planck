package nl.mdworld.planck4

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.*
import nl.mdworld.planck4.networking.NetworkMonitor
import nl.mdworld.planck4.networking.subsonic.SubsonicApi
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder
import nl.mdworld.planck4.songcache.SongCacheManager
import nl.mdworld.planck4.util.radiometadata.RadioMetadata
import nl.mdworld.planck4.views.library.Album
import nl.mdworld.planck4.views.library.Artist
import nl.mdworld.planck4.views.playlists.Playlist
import nl.mdworld.planck4.views.radio.RadioMetadataManagerFactory
import nl.mdworld.planck4.views.song.Song
import java.lang.ref.WeakReference

@Composable
fun rememberPlanckAppState(context: Context = LocalContext.current) =
    remember(context) { PlanckAppState(context) }

enum class AppScreen { PLAYLISTS, SONGS, SETTINGS, ARTISTS, ALBUMS, ALBUM_SONGS, RADIO }

class PlanckAppState(private val context: Context) {
    // --- App-wide lists/state ---
    val playlists = mutableStateListOf(
        Playlist(id = "empty", coverArt = "Empty", name = "No Playlist")
    )
    val songs = mutableStateListOf<Song>()
    val artists = mutableStateListOf<Artist>()
    val albums = mutableStateListOf<Album>()

    var currentScreen by mutableStateOf(AppScreen.PLAYLISTS); private set
    var reloadTrigger by mutableStateOf(0); private set

    var selectedPlaylistId by mutableStateOf<String?>(null); private set
    var selectedPlaylistName by mutableStateOf<String?>(null); private set
    var selectedArtistId by mutableStateOf<String?>(null); private set
    var selectedArtistName by mutableStateOf<String?>(null); private set
    var selectedAlbumId by mutableStateOf<String?>(null); private set
    var selectedAlbumName by mutableStateOf<String?>(null); private set

    var activeSong by mutableStateOf<Song?>(null)

    // Playback progress
    var currentSongIndex by mutableStateOf(0); private set
    var currentPosition by mutableStateOf(0); private set
    var duration by mutableStateOf(0); private set

    // Media players
    private var mediaPlayer: MediaPlayer? = null
    private var radioPlayer: MediaPlayer? = null
    var isPlaying by mutableStateOf(false)
    var isRadioPlaying by mutableStateOf(false)

    // Radio metadata
    var radioMetadata by mutableStateOf<List<RadioMetadata>>(emptyList())
    private var radioMetadataManager = createRadioMetadataManager()

    // Radio skip state
    private var radioSkipMonitorJob: Job? = null
    private var isRadioTemporarilyPausedForSkip = false
    var isRadioSkipping by mutableStateOf(false)

    // Offline + caching additions
    var isNetworkAvailable by mutableStateOf(true); private set
    var transientMessage by mutableStateOf<String?>(null); private set
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var networkMonitorJob: Job? = null
    private var prefetchJob: Job? = null

    // Progress update job
    private var progressUpdateJob: Job? = null
    private val progressUpdateScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Refresh triggers
    var isSongsRefreshing by mutableStateOf(false); private set
    private var playlistSongsRefreshTrigger by mutableStateOf(0)
    private var albumSongsRefreshTrigger by mutableStateOf(0)

    init {
        PlanckAppStateHolder.set(this)
        observeNetwork()
    }

    private fun observeNetwork() {
        networkMonitorJob?.cancel()
        networkMonitorJob = appScope.launch {
            NetworkMonitor.isOnline.collect { online ->
                isNetworkAvailable = online
                if (!online) {
                    if (isRadioPlaying) {
                        stopRadio()
                        transientMessage = "No internet connection. Radio stopped."
                    }
                } else {
                    // Back online: resume prefetch for current queue
                    if (isPlaying && activeSong?.id != "radio-stream") {
                        launchPrefetch()
                    }
                }
            }
        }
    }

    private fun createRadioMetadataManager(): nl.mdworld.planck4.views.radio.RadioMetadataManager {
        val url = SettingsManager.getRadioUrl(context)
        return if (url == SettingsManager.DEFAULT_RADIO_URL) RadioMetadataManagerFactory.createForNPORadio2() else RadioMetadataManagerFactory.createGeneric()
    }

    fun updateRadioMetadataManager() {
        radioMetadataManager.stopMonitoring(); radioMetadataManager = createRadioMetadataManager()
    }

    fun consumeTransientMessage() {
        transientMessage = null
    }

    // --- Navigation helpers ---
    fun navigateToSongs(playlistId: String, playlistName: String) {
        selectedPlaylistId = playlistId; selectedPlaylistName = playlistName; currentScreen =
            AppScreen.SONGS; songs.clear()
    }

    fun navigateToPlaylists() {
        currentScreen = AppScreen.PLAYLISTS; selectedPlaylistId = null; selectedPlaylistName = null
    }

    fun navigateToSettings() {
        currentScreen = AppScreen.SETTINGS
    }

    fun navigateToRadio() {
        currentScreen = AppScreen.RADIO
    }

    fun navigateToArtists() {
        currentScreen = AppScreen.ARTISTS; selectedArtistId = null; selectedArtistName =
            null; selectedAlbumId = null; selectedAlbumName = null
    }

    fun navigateToAlbums(artistId: String, artistName: String) {
        selectedArtistId = artistId; selectedArtistName = artistName; currentScreen =
            AppScreen.ALBUMS; albums.clear()
    }

    fun navigateToAlbumSongs(albumId: String, albumName: String) {
        selectedAlbumId = albumId; selectedAlbumName = albumName; currentScreen =
            AppScreen.ALBUM_SONGS; songs.clear()
    }

    fun triggerReload() {
        reloadTrigger++
        playlists.clear()
        playlists.add(Playlist(id = "empty", coverArt = "Empty", name = "No Playlist"))
        songs.clear(); artists.clear(); albums.clear()
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

    // --- Playback control (songs) ---
    fun playStream(song: Song) {
        try {
            if (song.id != "radio-stream") {
                when (currentScreen) {
                    AppScreen.SONGS -> selectedPlaylistId?.let {
                        SettingsManager.saveLastPlaylistId(
                            context,
                            it
                        ); SettingsManager.saveLastFolderId(context, "")
                    }

                    AppScreen.ALBUM_SONGS -> selectedAlbumId?.let {
                        SettingsManager.saveLastFolderId(
                            context,
                            it
                        ); SettingsManager.saveLastPlaylistId(context, "")
                    }

                    else -> {}
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
                setAudioAttributes(
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                // Decide data source (cached vs stream)
                val cached = SongCacheManager.getCachedFile(context, song.id)
                if (!isNetworkAvailable && cached == null) {
                    transientMessage = "Offline: ${song.title} not cached."
                    this@PlanckAppState.isPlaying = false
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
                    return
                }
                when {
                    cached != null -> setDataSource(cached.absolutePath)
                    else -> setDataSource(SubsonicUrlBuilder.buildStreamUrl(context, song.id))
                }
                prepareAsync()
                setOnPreparedListener { mp ->
                    mp.start(); this@PlanckAppState.isPlaying = true; this@PlanckAppState.duration =
                    mp.duration
                    startProgressUpdates(); updateMediaSessionPlaybackState(
                    PlaybackStateCompat.STATE_PLAYING,
                    0L
                )
                    launchPrefetch()
                }
                setOnErrorListener { _, _, _ ->
                    this@PlanckAppState.isPlaying =
                        false; stopProgressUpdates(); updateMediaSessionPlaybackState(
                    PlaybackStateCompat.STATE_ERROR,
                    0L
                ); false
                }
                setOnCompletionListener {
                    this@PlanckAppState.isPlaying =
                        false; stopProgressUpdates(); updateMediaSessionPlaybackState(
                    PlaybackStateCompat.STATE_STOPPED,
                    0L
                ); playNextSong()
                }
            }.also { AppAudioManager.register(it) }
        } catch (e: Exception) {
            e.printStackTrace(); isPlaying =
                false; stopProgressUpdates(); updateMediaSessionPlaybackState(
                PlaybackStateCompat.STATE_ERROR,
                0L
            )
        }
    }

    private fun launchPrefetch() {
        prefetchJob?.cancel()
        if (!isNetworkAvailable || songs.isEmpty()) return
        prefetchJob = appScope.launch(Dispatchers.IO) {
            val ordered = buildList {
                if (songs.size > 1) {
                    val next = (currentSongIndex + 1) % songs.size
                    for (i in 0 until songs.size - 1) {
                        val idx = (next + i) % songs.size
                        if (idx != currentSongIndex) add(songs[idx])
                    }
                }
            }
            for (s in ordered) {
                if (!isActive) break
                if (!SongCacheManager.isCached(context, s.id)) {
                    SongCacheManager.cacheSongIfNeeded(context, s.id)
                } else {
                    SongCacheManager.getCachedFile(context, s.id)
                        ?.setLastModified(System.currentTimeMillis())
                }
                delay(500)
            }
        }
    }

    fun stopPlayback() {
        stopProgressUpdates(); mediaPlayer?.let { if (it.isPlaying) it.stop(); it.reset(); it.release() }; mediaPlayer =
            null; isPlaying = false; currentPosition = 0; duration =
            0; updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
    }

    fun pausePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause(); isPlaying =
                    false; stopProgressUpdates(); updateMediaSessionPlaybackState(
                    PlaybackStateCompat.STATE_PAUSED,
                    currentPosition.toLong()
                )
            }
        }
    }

    fun resumePlayback() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start(); isPlaying =
                    true; startProgressUpdates(); updateMediaSessionPlaybackState(
                    PlaybackStateCompat.STATE_PLAYING,
                    currentPosition.toLong()
                )
            }
        }
    }

    fun playNextSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex =
                if (currentSongIndex < songs.size - 1) currentSongIndex + 1 else 0; playStream(songs[currentSongIndex])
        }
    }

    fun playPreviousSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex =
                if (currentSongIndex > 0) currentSongIndex - 1 else songs.size - 1; playStream(songs[currentSongIndex])
        }
    }

    fun skipToNext() = playNextSong()

    // --- Radio playback ---
    fun startRadio() {
        try {
            stopRadio(); stopPlayback()
            val firstTrack = radioMetadata.firstOrNull()
            val dummySong = Song(
                id = "radio-stream",
                title = firstTrack?.song?.title ?: "NPO Radio 2",
                artist = firstTrack?.song?.artist ?: "",
                album = "Live Stream",
                duration = 0,
                coverArt = null
            )
            activeSong =
                dummySong; sendMetadataToService(dummySong); updateMediaSessionPlaybackState(
                PlaybackStateCompat.STATE_BUFFERING,
                0L
            )
            radioPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                val audioUrl = SettingsManager.getRadioUrl(context)
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener { rp ->
                    rp.start()
                    this@PlanckAppState.isRadioPlaying = true
                    this@PlanckAppState.isPlaying = true
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
                    radioMetadataManager.startMonitoring(audioUrl, onSuccess = { metadata ->
                        val newFirst = metadata.firstOrNull()
                        val prevStart = radioMetadata.firstOrNull()?.time?.start
                        val newStart = newFirst?.time?.start
                        if (prevStart == null || (newStart != null && newStart > prevStart)) {
                            if (this@PlanckAppState.isRadioPlaying) {
                                activeSong = Song(
                                    id = "radio-stream",
                                    title = newFirst?.song?.title ?: newFirst?.broadcast?.title
                                    ?: "Radio Stream",
                                    artist = newFirst?.song?.artist
                                        ?: newFirst?.broadcast?.presenters ?: "",
                                    album = "Radio Stream",
                                    duration = 0,
                                    coverArt = newFirst?.song?.imageUrl
                                        ?: newFirst?.broadcast?.imageUrl
                                )
                                sendMetadataToService(activeSong)
                            }
                            radioMetadata = metadata
                        }
                    }, onError = { _ -> activeSong = dummySong })
                }
                setOnErrorListener { _, _, _ ->
                    this@PlanckAppState.isRadioPlaying = false
                    this@PlanckAppState.isPlaying = false
                    activeSong = null
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
                    false
                }
                setOnCompletionListener {
                    this@PlanckAppState.isRadioPlaying = false
                    this@PlanckAppState.isPlaying = false
                    activeSong = null
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
                }
            }.also { AppAudioManager.register(it) }
        } catch (e: Exception) {
            e.printStackTrace(); isRadioPlaying = false; isPlaying = false; activeSong =
                null; updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
        }
    }

    fun stopRadio() {
        radioMetadataManager.stopMonitoring(); radioSkipMonitorJob?.cancel(); radioSkipMonitorJob =
            null; isRadioTemporarilyPausedForSkip = false; isRadioSkipping =
            false; radioPlayer?.let { if (it.isPlaying) it.stop(); it.reset(); it.release() }; radioPlayer =
            null; isRadioPlaying = false; isPlaying = false; activeSong =
            null; updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_STOPPED, 0L)
    }

    private fun resumeRadioStreamWithCurrentMetadata() {
        try {
            // Reset and re-prepare the radio player to get the current live stream position
            radioPlayer?.let { rp ->
                if (rp.isPlaying) rp.stop()
                rp.reset()
                rp.setAudioAttributes(
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                val audioUrl = SettingsManager.getRadioUrl(context)
                rp.setDataSource(audioUrl)
                rp.prepareAsync()
                rp.setOnPreparedListener { prepared ->
                    prepared.start()
                    val firstTrack = radioMetadata.firstOrNull()
                    activeSong = Song(
                        id = "radio-stream",
                        title = firstTrack?.song?.title ?: firstTrack?.broadcast?.title
                        ?: "Radio Stream",
                        artist = firstTrack?.song?.artist ?: firstTrack?.broadcast?.presenters ?: "",
                        album = "Radio Stream",
                        duration = 0,
                        coverArt = firstTrack?.song?.imageUrl ?: firstTrack?.broadcast?.imageUrl
                    )
                    sendMetadataToService(activeSong)
                    isRadioPlaying = true
                    isPlaying = true
                    isRadioSkipping = false
                    isRadioTemporarilyPausedForSkip = false
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_PLAYING, 0L)
                }
                rp.setOnErrorListener { _, _, _ ->
                    this@PlanckAppState.isRadioPlaying = false
                    this@PlanckAppState.isPlaying = false
                    activeSong = null
                    updateMediaSessionPlaybackState(PlaybackStateCompat.STATE_ERROR, 0L)
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isRadioPlaying = false
            isPlaying = false
            isRadioSkipping = false
            isRadioTemporarilyPausedForSkip = false
        }
    }

    fun skipRadioToLastContext() {
        if (isRadioTemporarilyPausedForSkip) {
            radioSkipMonitorJob?.cancel(); radioSkipMonitorJob =
                null; stopPlayback(); resumeRadioStreamWithCurrentMetadata(); return
        }
        val lastSongId = SettingsManager.getLastSongId(context)
        val lastPlaylistId = SettingsManager.getLastPlaylistId(context)
        val lastFolderId = SettingsManager.getLastFolderId(context)
        if (lastSongId.isNullOrBlank() || (lastPlaylistId.isNullOrBlank() && lastFolderId.isNullOrBlank())) return
        if (!isRadioPlaying || radioPlayer == null) return
        radioPlayer?.let {
            if (it.isPlaying) {
                it.pause(); isRadioPlaying = false; isPlaying = false
            }
        }
        isRadioTemporarilyPausedForSkip = true; isRadioSkipping = true
        val initialStart = radioMetadata.firstOrNull()?.time?.start
        radioSkipMonitorJob?.cancel()
        radioSkipMonitorJob = progressUpdateScope.launch {
            val api = SubsonicApi()
            try {
                val songsList = mutableListOf<Song>()
                if (!lastPlaylistId.isNullOrBlank()) {
                    runCatching {
                        api.getPlaylistKtor(
                            context,
                            lastPlaylistId
                        )
                    }.getOrNull()?.sr?.playlist?.songs?.map { s ->
                        Song(
                            s.id,
                            s.title,
                            s.artist,
                            s.album,
                            s.duration,
                            s.coverArt
                        )
                    }?.let { songsList += it }
                } else if (!lastFolderId.isNullOrBlank()) {
                    try {
                        val mode = SettingsManager.getBrowsingMode(context)
                        if (mode == SettingsManager.BrowsingMode.FILES) {
                            val root = api.getMusicDirectoryKtor(context, lastFolderId)
                            val children = root.sr.directory.child
                            val directSongs = children.filter { !it.isDir }
                            if (directSongs.isNotEmpty()) songsList += directSongs.map { c ->
                                Song(
                                    c.id,
                                    c.title,
                                    c.artist,
                                    c.album ?: "",
                                    c.duration,
                                    c.coverArt
                                )
                            } else {
                                val discDirs = children.filter { it.isDir }
                                for (disc in discDirs) runCatching {
                                    api.getMusicDirectoryKtor(
                                        context,
                                        disc.id
                                    )
                                }.getOrNull()?.sr?.directory?.child?.filter { !it.isDir }
                                    ?.map { ch ->
                                        Song(
                                            ch.id,
                                            ch.title,
                                            ch.artist,
                                            ch.album ?: "",
                                            ch.duration,
                                            ch.coverArt
                                        )
                                    }?.let { songsList += it }
                            }
                        } else {
                            api.getAlbumKtor(context, lastFolderId).sr.album.songs?.map { s ->
                                Song(
                                    s.id,
                                    s.title,
                                    s.artist,
                                    s.album,
                                    s.duration,
                                    s.coverArt
                                )
                            }?.let { songsList += it }
                        }
                    } catch (_: Exception) {
                    }
                }
                val songToPlay = songsList.firstOrNull { it.id == lastSongId }
                if (songToPlay != null) {
                    songs.clear(); songs.addAll(songsList); playStream(songToPlay)
                } else {
                    resumeRadioStreamWithCurrentMetadata(); isRadioTemporarilyPausedForSkip =
                        false; return@launch
                }
            } catch (e: Exception) {
                resumeRadioStreamWithCurrentMetadata(); isRadioTemporarilyPausedForSkip =
                    false; return@launch
            }

            try {
                while (isActive && isRadioTemporarilyPausedForSkip) {
                    val currentStart = radioMetadata.firstOrNull()?.time?.start
                    val newMetadataDetected = when {
                        initialStart == null && currentStart != null -> true
                        initialStart != null && currentStart != null && currentStart > initialStart -> true
                        else -> false
                    }
                    if (newMetadataDetected) {
                        stopPlayback(); resumeRadioStreamWithCurrentMetadata(); isRadioTemporarilyPausedForSkip =
                            false; break
                    }
                    delay(2000)
                }
            } catch (_: CancellationException) {
            }
        }
    }

    // --- Progress tracking ---
    private fun startProgressUpdates() {
        progressUpdateJob?.cancel(); progressUpdateJob = progressUpdateScope.launch {
            while (isPlaying && mediaPlayer != null) {
                mediaPlayer?.let { if (it.isPlaying) currentPosition = it.currentPosition }; delay(
                    500
                )
            }
        }
    }

    private fun stopProgressUpdates() {
        progressUpdateJob?.cancel(); progressUpdateJob = null
    }

    // --- Media session communication ---
    private fun sendMetadataToService(song: Song?) {
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_METADATA
            song?.let {
                putExtra(MediaPlaybackService.EXTRA_SONG_ID, it.id)
                putExtra(MediaPlaybackService.EXTRA_SONG_TITLE, it.title)
                it.artist?.let { a -> putExtra(MediaPlaybackService.EXTRA_SONG_ARTIST, a) }
                it.album?.let { al -> putExtra(MediaPlaybackService.EXTRA_SONG_ALBUM, al) }
                it.duration?.let { d -> putExtra(MediaPlaybackService.EXTRA_SONG_DURATION, d) }
                it.coverArt?.let { art ->
                    if (art.startsWith("http", true)) putExtra(
                        MediaPlaybackService.EXTRA_SONG_COVER_ART_URL,
                        art
                    ) else putExtra(MediaPlaybackService.EXTRA_SONG_COVER_ART_ID, art)
                }
            }
        }
        runCatching { context.startService(intent) }
    }

    private fun updateMediaSessionPlaybackState(state: Int, position: Long = 0L) {
        val intent = Intent(context, MediaPlaybackService::class.java).apply {
            action = MediaPlaybackService.ACTION_UPDATE_PLAYBACK_STATE
            putExtra(MediaPlaybackService.EXTRA_PLAYBACK_STATE, state)
            putExtra(MediaPlaybackService.EXTRA_PLAYBACK_POSITION, position)
        }
        runCatching { context.startService(intent) }
    }

    // --- Refresh helpers ---
    fun refreshCurrentPlaylistSongs() {
        if (selectedPlaylistId != null) { isSongsRefreshing = true; playlistSongsRefreshTrigger++ }
    }
    fun refreshCurrentAlbumSongs() {
        if (selectedAlbumId != null) { isSongsRefreshing = true; albumSongsRefreshTrigger++ }
    }
    internal fun markSongsRefreshComplete() { isSongsRefreshing = false }
    internal fun playlistRefreshKey() = playlistSongsRefreshTrigger
    internal fun albumRefreshKey() = albumSongsRefreshTrigger

    // --- Cleanup ---
    fun cleanup() {
        stopPlayback(); stopRadio(); radioMetadataManager.cleanup(); progressUpdateScope.cancel()
        radioSkipMonitorJob?.cancel(); prefetchJob?.cancel(); networkMonitorJob?.cancel(); appScope.cancel()
        isRadioSkipping = false; isRadioTemporarilyPausedForSkip = false
    }
}

object PlanckAppStateHolder {
    private var ref: WeakReference<PlanckAppState>? = null;
    fun set(state: PlanckAppState) {
        ref = WeakReference(state)
    };
    fun get(): PlanckAppState? = ref?.get()
}

fun setSelectedPlaylist(context: Context, playlistName: String) {
    val prefs = context.getSharedPreferences("planck_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("selected_playlist", playlistName).apply()
    runCatching { ClusterScreen.refreshClusterDisplay() }.onFailure { println("Cluster display not available for refresh: ${it.message}") }
}

fun getSelectedPlaylist(context: Context): String =
    context.getSharedPreferences("planck_prefs", Context.MODE_PRIVATE)
        .getString("selected_playlist", "No Playlist Selected") ?: "No Playlist Selected"
