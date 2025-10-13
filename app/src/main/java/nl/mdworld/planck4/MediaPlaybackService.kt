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

class MediaPlaybackService : MediaBrowserServiceCompat() {

    companion object {
        private const val MY_MEDIA_ROOT_ID = "media_root_id"
        private const val MEDIA_ID_PLAYLISTS = "playlists"
        private const val MEDIA_ID_SONGS = "songs"
        // Notification constants
        private const val NOTIFICATION_CHANNEL_ID = "planck_playback"
        private const val NOTIFICATION_ID = 1001

        // Intent actions / extras for external updates
        const val ACTION_UPDATE_METADATA = "nl.mdworld.planck4.action.UPDATE_METADATA"
        const val ACTION_UPDATE_PLAYBACK_STATE = "nl.mdworld.planck4.action.UPDATE_PLAYBACK_STATE"
        const val EXTRA_SONG_ID = "extra_song_id"
        const val EXTRA_SONG_TITLE = "extra_song_title"
        const val EXTRA_SONG_ARTIST = "extra_song_artist"
        const val EXTRA_SONG_ALBUM = "extra_song_album"
        const val EXTRA_SONG_DURATION = "extra_song_duration"
        const val EXTRA_PLAYBACK_STATE = "extra_playback_state"
    }

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    // Track current song & playback state so we can build a dynamic notification
    private var currentSong: Song? = null
    private var currentPlaybackState: Int = PlaybackStateCompat.STATE_NONE
    private var isInForeground: Boolean = false

    override fun onCreate() {
        super.onCreate()

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "MediaPlaybackService").apply {

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
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

            // MySessionCallback has methods that handle callbacks from a media controller
            setCallback(MySessionCallback())

            // Set the session activity
            val activityIntent = Intent(applicationContext, MainActivity::class.java)
            setSessionActivity(
                PendingIntent.getActivity(
                    applicationContext, 0, activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        // Ensure a persistent foreground notification with the word "Planck"
        ensureNotificationChannel()
        try {
            // Initial placeholder notification before a song is set
            startForeground(NOTIFICATION_ID, buildNotification())
            isInForeground = true
        } catch (e: SecurityException) {
            // If POST_NOTIFICATIONS not yet granted (API 33+), we'll remain without foreground until granted.
            // You may request permission earlier in the activity; once granted you can restart service.
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_METADATA -> {
                val title = intent.getStringExtra(EXTRA_SONG_TITLE)
                val id = intent.getStringExtra(EXTRA_SONG_ID) ?: title ?: "" // fallback
                val artist = intent.getStringExtra(EXTRA_SONG_ARTIST)
                val album = intent.getStringExtra(EXTRA_SONG_ALBUM)
                val duration = if (intent.hasExtra(EXTRA_SONG_DURATION)) intent.getIntExtra(EXTRA_SONG_DURATION, 0) else null
                if (title != null) {
                    updateMetadata(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = duration,
                            coverArt = null // Not yet wired
                        )
                    )
                }
            }
            ACTION_UPDATE_PLAYBACK_STATE -> {
                val state = intent.getIntExtra(EXTRA_PLAYBACK_STATE, PlaybackStateCompat.STATE_NONE)
                updatePlaybackState(state)
            }
        }
        return START_STICKY
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

    // Build a dynamic notification using current song & state
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
            .setOngoing(isPlaying) // Ongoing when playing
            .setOnlyAlertOnce(true)
            // Could add media style & actions later
            .build()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        // Return a tree of media items that can be browsed
        return BrowserRoot(MY_MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        when (parentMediaId) {
            MY_MEDIA_ROOT_ID -> {
                // Add browseable categories
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
            MEDIA_ID_PLAYLISTS -> {
                // Add playlist items here - you can integrate with your PlanckAppState
            }
            MEDIA_ID_SONGS -> {
                // Add song items here - you can integrate with your PlanckAppState
            }
        }

        result.sendResult(mediaItems)
    }

    inner class MySessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }

        override fun onPause() {
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }

        override fun onStop() {
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSkipToNext() {
            updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
        }

        override fun onSkipToPrevious() {
            updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    private fun updatePlaybackState(state: Int) {
        currentPlaybackState = state
        val playbackState = stateBuilder
            .setState(state, 0, 1.0f)
            .build()
        mediaSession?.setPlaybackState(playbackState)
        refreshNotification()
    }

    fun updateMetadata(song: Song) {
        currentSong = song
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration?.toLong() ?: 0)
            .build()

        mediaSession?.setMetadata(metadata)
        refreshNotification()
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
                    // Update ongoing foreground notification
                    mgr.notify(NOTIFICATION_ID, notification)
                }
            } catch (e: SecurityException) {
                // Notification permission missing (API 33+). We silently ignore; user must grant.
            }
        } else {
            // Not actively playing: keep a non-foreground notification (unless stopped)
            if (isInForeground) {
                stopForeground(false) // Keep shown
                isInForeground = false
            }
            mgr.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }
}
