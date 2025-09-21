package nl.mdworld.planck4

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mdworld.planck4.views.components.NavigationButton
import nl.mdworld.planck4.views.components.BottomAppBar
import nl.mdworld.planck4.views.song.Song

fun handlePlayPause(context: Context, appState: PlanckAppState?) {
    val activeSong = appState?.activeSong
    if (appState != null && activeSong != null) {
        if (appState.isPlaying) appState.pausePlayback() else appState.resumePlayback()
    } else {
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            val audioUrl = SettingsManager.getRadioUrl(context)
            try {
                setDataSource(audioUrl)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnCompletionListener { runCatching { reset(); release() } }
                setOnErrorListener { _, _, _ -> runCatching { reset(); release() }; false }
            } catch (e: Exception) { e.printStackTrace() }
        }
        AppAudioManager.register(mediaPlayer)
    }
}

@Composable
fun PlanckBottomAppBar(
    currentScreen: AppScreen = AppScreen.PLAYLISTS,
    onNavigateBack: () -> Unit = {},
    activeSong: Song? = null,
    onNavigateToSettings: () -> Unit = {},
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current

    // Progress bar above the bar when a song is active
    if (activeSong != null && appState != null) {
        val progress = if (appState.duration > 0) appState.currentPosition.toFloat() / appState.duration.toFloat() else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }

    BottomAppBar(
        modifier = if (activeSong != null) Modifier.height(80.dp) else Modifier
    ) {
        // Back / navigation context button
        if (currentScreen == AppScreen.SONGS || currentScreen == AppScreen.ALBUMS || currentScreen == AppScreen.ALBUM_SONGS) {
            IconButton(onClick = {
                when (currentScreen) {
                    AppScreen.SONGS -> onNavigateBack()
                    AppScreen.ALBUMS -> appState?.navigateToArtists()
                    AppScreen.ALBUM_SONGS -> {
                        if (appState?.selectedArtistId != null && appState.selectedArtistName != null) {
                            appState.navigateToAlbums(appState.selectedArtistId!!, appState.selectedArtistName!!)
                        } else appState?.navigateToArtists()
                    }
                    else -> onNavigateBack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        } else {
            Spacer(modifier = Modifier.width(12.dp))
        }

        // Title + artist column (weight to push controls to end)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
        ) {
            if (activeSong != null) {
                Text(
                    text = activeSong.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                activeSong.artist?.let { artist ->
                    Text(
                        text = artist,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Media controls
        IconButton(onClick = { handlePlayPause(context, appState) }) {
            Icon(
                imageVector = if (appState?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (appState?.isPlaying == true) "Pause" else "Play",
                modifier = Modifier.size(BottomAppBar.IconSize)
            )
        }
        IconButton(onClick = { appState?.stopPlayback() }) {
            Icon(Icons.Filled.Stop, contentDescription = "Stop", modifier = Modifier.size(BottomAppBar.IconSize))
        }
        IconButton(onClick = { appState?.playPreviousSong() }) {
            Icon(Icons.Filled.SkipPrevious, contentDescription = "Prev", modifier = Modifier.size(BottomAppBar.IconSize))
        }
        IconButton(onClick = { appState?.playNextSong() }) {
            Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(BottomAppBar.IconSize))
        }

        // Navigation buttons
        NavigationButton(
            icon = Icons.Filled.LibraryMusic,
            contentDescription = "Playlists",
            isSelected = currentScreen == AppScreen.PLAYLISTS || currentScreen == AppScreen.SONGS,
            onClick = { appState?.navigateToPlaylists() }
        )
        NavigationButton(
            icon = Icons.Filled.Folder,
            contentDescription = "Library",
            isSelected = currentScreen == AppScreen.ARTISTS || currentScreen == AppScreen.ALBUMS || currentScreen == AppScreen.ALBUM_SONGS,
            onClick = { appState?.navigateToArtists() }
        )
        NavigationButton(
            icon = Icons.Filled.Radio,
            contentDescription = "Radio",
            isSelected = currentScreen == AppScreen.RADIO,
            onClick = { appState?.navigateToRadio() }
        )
        NavigationButton(
            icon = Icons.Filled.Settings,
            contentDescription = "Settings",
            isSelected = currentScreen == AppScreen.SETTINGS,
            onClick = onNavigateToSettings
        )
    }
}
