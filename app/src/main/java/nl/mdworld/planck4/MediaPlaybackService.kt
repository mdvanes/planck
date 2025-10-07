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
    }

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

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
            startForeground(NOTIFICATION_ID, buildBaseNotification())
        } catch (e: SecurityException) {
            // If POST_NOTIFICATIONS not yet granted (API 33+), we'll remain without foreground until granted.
            // You may request permission earlier in the activity; once granted you can restart service.
        }
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

    private fun buildBaseNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Planck")
            .setContentText("Ready")
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
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
                // For now, returning empty list
            }
            MEDIA_ID_SONGS -> {
                // Add song items here - you can integrate with your PlanckAppState
                // For now, returning empty list
            }
        }

        result.sendResult(mediaItems)
    }

    inner class MySessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            // Handle play command
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }

        override fun onPause() {
            // Handle pause command
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }

        override fun onStop() {
            // Handle stop command
            updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSkipToNext() {
            // Handle skip to next
            updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT)
        }

        override fun onSkipToPrevious() {
            // Handle skip to previous
            updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            // Handle play from media ID
            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    private fun updatePlaybackState(state: Int) {
        val playbackState = stateBuilder
            .setState(state, 0, 1.0f)
            .build()
        mediaSession?.setPlaybackState(playbackState)
    }

    fun updateMetadata(song: Song) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.album)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration?.toLong() ?: 0)
            .build()

        mediaSession?.setMetadata(metadata)
    }

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }
}
