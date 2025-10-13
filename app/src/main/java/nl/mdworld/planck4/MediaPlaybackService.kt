package nl.mdworld.planck4

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.os.Bundle
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import nl.mdworld.planck4.views.song.Song
import android.os.Build
import androidx.core.app.NotificationCompat
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.*
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder

class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MEDIA_ID_PLAYLISTS = "playlists"
        private const val MEDIA_ID_SONGS = "songs"
        private const val NOTIFICATION_CHANNEL_ID = "planck_playback"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_UPDATE_METADATA = "nl.mdworld.planck4.action.UPDATE_METADATA"
        const val ACTION_UPDATE_PLAYBACK_STATE = "nl.mdworld.planck4.action.UPDATE_PLAYBACK_STATE"
        const val EXTRA_SONG_ID = "extra_song_id"
        const val EXTRA_SONG_TITLE = "extra_song_title"
        const val EXTRA_SONG_ARTIST = "extra_song_artist"
        const val EXTRA_SONG_ALBUM = "extra_song_album"
        const val EXTRA_SONG_DURATION = "extra_song_duration"
        const val EXTRA_SONG_COVER_ART_ID = "extra_song_cover_art_id"
        const val EXTRA_SONG_COVER_ART_URL = "extra_song_cover_art_url"
        const val EXTRA_PLAYBACK_STATE = "extra_playback_state"
        const val EXTRA_PLAYBACK_POSITION = "extra_playback_position"
    }

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private var currentSong: Song? = null
    private var currentPlaybackState: Int = PlaybackStateCompat.STATE_NONE
    private var isInForeground: Boolean = false
    private var currentLargeIcon: Bitmap? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(baseContext, "MediaPlaybackService").apply {
            setSessionToken(sessionToken)
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            setPlaybackState(stateBuilder.build())
            setCallback(MySessionCallback())
            val activityIntent = Intent(applicationContext, MainActivity::class.java)
            setSessionActivity(
                PendingIntent.getActivity(
                    applicationContext, 0, activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        ensureNotificationChannel()
        try {
            startForeground(NOTIFICATION_ID, buildNotification())
            isInForeground = true
        } catch (_: SecurityException) { }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_METADATA -> handleMetadataIntent(intent)
            ACTION_UPDATE_PLAYBACK_STATE -> handlePlaybackStateIntent(intent)
        }
        return START_STICKY
    }

    private fun handleMetadataIntent(intent: Intent) {
        val title = intent.getStringExtra(EXTRA_SONG_TITLE)
        if (title == null) return
        val id = intent.getStringExtra(EXTRA_SONG_ID) ?: title
        val artist = intent.getStringExtra(EXTRA_SONG_ARTIST)
        val album = intent.getStringExtra(EXTRA_SONG_ALBUM)
        val duration = if (intent.hasExtra(EXTRA_SONG_DURATION)) intent.getIntExtra(EXTRA_SONG_DURATION, 0) else null
        val coverArtId = intent.getStringExtra(EXTRA_SONG_COVER_ART_ID)
        val coverArtUrl = intent.getStringExtra(EXTRA_SONG_COVER_ART_URL)
        updateMetadata(
            Song(
                id = id,
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                coverArt = coverArtId ?: coverArtUrl
            ),
            coverArtId = coverArtId,
            coverArtUrl = coverArtUrl
        )
    }

    private fun handlePlaybackStateIntent(intent: Intent) {
        val state = intent.getIntExtra(EXTRA_PLAYBACK_STATE, PlaybackStateCompat.STATE_NONE)
        val position = intent.getLongExtra(EXTRA_PLAYBACK_POSITION, 0L)
        updatePlaybackState(state, position)
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Playback",
                    NotificationManager.IMPORTANCE_LOW
                ).apply { description = "Planck playback and status" }
                mgr.createNotificationChannel(ch)
            }
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = currentSong?.title ?: "Planck"
        val subtitle = when {
            currentSong == null -> "Ready"
            currentPlaybackState == PlaybackStateCompat.STATE_PLAYING -> currentSong?.artist ?: currentSong?.album ?: "Playing"
            currentPlaybackState == PlaybackStateCompat.STATE_PAUSED -> "Paused"
            else -> currentSong?.artist ?: currentSong?.album ?: "".trim()
        }
        val isPlaying = currentPlaybackState == PlaybackStateCompat.STATE_PLAYING
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setContentIntent(contentPendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .apply { currentLargeIcon?.let { setLargeIcon(it) } }
            .build()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? =
        BrowserRoot(MY_MEDIA_ROOT_ID, null)

    override fun onLoadChildren(parentMediaId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
        if (parentMediaId == MY_MEDIA_ROOT_ID) {
            mediaItems.add(
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(MEDIA_ID_PLAYLISTS)
                        .setTitle("Playlists")
                        .setSubtitle("Browse playlists")
                        .build(),
                    MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
                )
            )
        }
        result.sendResult(mediaItems)
    }

    inner class MySessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() { updatePlaybackState(PlaybackStateCompat.STATE_PLAYING) }
        override fun onPause() { updatePlaybackState(PlaybackStateCompat.STATE_PAUSED) }
        override fun onStop() { updatePlaybackState(PlaybackStateCompat.STATE_STOPPED) }
        override fun onSkipToNext() { updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT) }
        override fun onSkipToPrevious() { updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS) }
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) { updatePlaybackState(PlaybackStateCompat.STATE_PLAYING) }
    }

    private fun updatePlaybackState(state: Int, position: Long = 0L) {
        currentPlaybackState = state
        val playbackState = stateBuilder.setState(state, position, 1.0f).build()
        mediaSession?.setPlaybackState(playbackState)
        refreshNotification()
    }

    fun updateMetadata(song: Song, coverArtId: String? = null, coverArtUrl: String? = null) {
        currentSong = song
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration?.toLong() ?: 0)
            .build()
        mediaSession?.setMetadata(metadata)
        loadCoverArtAsync(coverArtId, coverArtUrl)
        refreshNotification()
    }

    private fun loadCoverArtAsync(coverArtId: String?, coverArtUrl: String?) {
        val targetData = when {
            coverArtUrl != null -> coverArtUrl
            coverArtId != null -> SubsonicUrlBuilder.buildCoverArtUrl(this, coverArtId)
            currentSong?.coverArt != null && currentSong?.coverArt!!.startsWith("http") -> currentSong?.coverArt
            currentSong?.coverArt != null -> SubsonicUrlBuilder.buildCoverArtUrl(this, currentSong!!.coverArt!!)
            else -> null
        }
        if (targetData == null) { currentLargeIcon = null; return }
        serviceScope.launch {
            try {
                val loader = ImageLoader(this@MediaPlaybackService)
                val req = ImageRequest.Builder(this@MediaPlaybackService)
                    .data(targetData)
                    .allowHardware(false)
                    .size(512)
                    .build()
                val result = loader.execute(req)
                val bmp = (result as? SuccessResult)?.drawable.let { dr -> (dr as? BitmapDrawable)?.bitmap }
                if (bmp != null) {
                    currentLargeIcon = bmp
                    withContext(Dispatchers.Main) { refreshNotification() }
                }
            } catch (_: Exception) { }
        }
    }

    private fun refreshNotification() {
        val notification = buildNotification()
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val playing = currentPlaybackState == PlaybackStateCompat.STATE_PLAYING || currentPlaybackState == PlaybackStateCompat.STATE_BUFFERING
        if (playing) {
            try {
                if (!isInForeground) {
                    startForeground(NOTIFICATION_ID, notification)
                    isInForeground = true
                } else {
                    mgr.notify(NOTIFICATION_ID, notification)
                }
            } catch (_: SecurityException) { }
        } else {
            if (isInForeground) {
                stopForeground(false)
                isInForeground = false
            }
            mgr.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        serviceScope.cancel()
        super.onDestroy()
    }
}
