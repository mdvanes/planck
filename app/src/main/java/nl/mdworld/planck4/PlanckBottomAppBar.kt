package nl.mdworld.planck4

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mdworld.planck4.views.components.BottomAppBar
import nl.mdworld.planck4.views.components.NavigationButton
import nl.mdworld.planck4.views.song.Song


@Composable
fun PlanckBottomAppBar(
    currentScreen: AppScreen = AppScreen.PLAYLISTS,
    onNavigateBack: () -> Unit = {},
    activeSong: Song? = null,
    onNavigateToSettings: () -> Unit = {},
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current

    val navigateBackButton = @Composable {
        IconButton(
            onClick = {
                when (currentScreen) {
                    AppScreen.SONGS -> onNavigateBack()
                    AppScreen.ALBUMS -> appState?.navigateToArtists()
                    AppScreen.ALBUM_SONGS -> {
                        if (appState?.selectedArtistId != null && appState.selectedArtistName != null) {
                            appState.navigateToAlbums(
                                appState.selectedArtistId!!,
                                appState.selectedArtistName!!
                            )
                        } else {
                            appState?.navigateToArtists()
                        }
                    }

                    else -> onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = when (currentScreen) {
                    AppScreen.SONGS -> "Back to playlists"
                    AppScreen.ALBUMS -> "Back to artists"
                    AppScreen.ALBUM_SONGS -> "Back to albums"
                    else -> "Back"
                }
            )
        }
    }

    val songTitleRow = @Composable {
        if (activeSong != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = activeSong.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }


    val playPauseButton = @Composable {
        IconButton(onClick = {
            if (appState != null && activeSong != null) {
                if (appState.isPlaying) {
                    appState.pausePlayback()
                } else {
                    appState.resumePlayback()
                }
            } else {
                val mediaPlayer = MediaPlayer()
                val audioUrl = SettingsManager.getRadioUrl(context)
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                try {
                    mediaPlayer.setDataSource(audioUrl)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, modifier = Modifier.padding(horizontal = 2.dp)) {
            Icon(
                imageVector = if (appState?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (appState?.isPlaying == true) "Pause" else "Play",
                modifier = Modifier.size(BottomAppBar.IconSize),
            )
        }
    }




    Column {
        // Progress bar at the top
        if (activeSong != null && appState != null) {
            val progress = if (appState.duration > 0) {
                appState.currentPosition.toFloat() / appState.duration.toFloat()
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
            )
        }


        // Main bottom app bar with controls and artist name
        BottomAppBar(
            modifier = if (activeSong != null) Modifier.height(80.dp) else Modifier,
            actions = {

                Row {
                    // Back button - spans full height when needed
                    if (currentScreen == AppScreen.SONGS ||
                        currentScreen == AppScreen.ALBUMS ||
                        currentScreen == AppScreen.ALBUM_SONGS
                    ) {
                        navigateBackButton()
                    }

                    Column {
                        songTitleRow()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Artist name display area
                            if (activeSong?.artist != null) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = activeSong.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            // Media control buttons
                            Row {
                                playPauseButton()

                                IconButton(onClick = {
                                    appState?.stopPlayback()
                                }, modifier = Modifier.padding(horizontal = 2.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.Stop,
                                        contentDescription = "Stop",
                                        modifier = Modifier.size(BottomAppBar.IconSize),
                                    )
                                }

                                IconButton(onClick = {
                                    appState?.playPreviousSong()
                                }, modifier = Modifier.padding(horizontal = 2.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipPrevious,
                                        contentDescription = "Previous Song",
                                        modifier = Modifier.size(BottomAppBar.IconSize),
                                    )
                                }

                                IconButton(onClick = {
                                    appState?.playNextSong()
                                }, modifier = Modifier.padding(horizontal = 2.dp)) {
                                    Icon(
                                        imageVector = Icons.Filled.SkipNext,
                                        contentDescription = "Next Song",
                                        modifier = Modifier.size(BottomAppBar.IconSize),
                                    )
                                }


                            }

                            // Main navigation buttons
                            Row {
                                NavigationButton(
                                    icon = Icons.Filled.LibraryMusic,
                                    contentDescription = "Playlists",
                                    isSelected = currentScreen == AppScreen.PLAYLISTS || currentScreen == AppScreen.SONGS,
                                    onClick = { appState?.navigateToPlaylists() }
                                )

                                NavigationButton(
                                    icon = Icons.Filled.Folder,
                                    contentDescription = "Library",
                                    isSelected = currentScreen == AppScreen.ARTISTS ||
                                            currentScreen == AppScreen.ALBUMS ||
                                            currentScreen == AppScreen.ALBUM_SONGS,
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
                    }
                }
            }
        )
    }
}
