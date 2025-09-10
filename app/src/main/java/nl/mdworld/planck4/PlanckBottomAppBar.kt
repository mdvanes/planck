package nl.mdworld.planck4

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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

@Composable
fun PlanckBottomAppBar(
    currentScreen: AppScreen = AppScreen.PLAYLISTS,
    onNavigateBack: () -> Unit = {},
    activeSong: Song? = null,
    onNavigateToSettings: () -> Unit = {},
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current

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

        BottomAppBar(
            actions = {
                // Back button - handles different navigation flows
                if (currentScreen == AppScreen.SONGS ||
                    currentScreen == AppScreen.ALBUMS ||
                    currentScreen == AppScreen.ALBUM_SONGS) {
                    IconButton(onClick = {
                        when (currentScreen) {
                            AppScreen.SONGS -> onNavigateBack() // Back to playlists
                            AppScreen.ALBUMS -> appState?.navigateToArtists() // Back to artists
                            AppScreen.ALBUM_SONGS -> {
                                // Back to albums of the current artist
                                if (appState?.selectedArtistId != null && appState.selectedArtistName != null) {
                                    appState.navigateToAlbums(appState.selectedArtistId!!, appState.selectedArtistName!!)
                                } else {
                                    appState?.navigateToArtists()
                                }
                            }
                            else -> onNavigateBack()
                        }
                    }) {
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

                // Display active song title if available
                if (activeSong != null) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activeSong.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activeSong.artist != null) {
                            Text(
                                text = activeSong.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Spacer to push navigation buttons to the right
                Spacer(modifier = Modifier.weight(1f))



                // Media control buttons
                IconButton(onClick = {
                    if (appState != null && activeSong != null) {
                        if (appState.isPlaying) {
                            appState.pausePlayback()
                        } else {
                            appState.resumePlayback()
                        }
                    } else {
                        // Fallback to the original example implementation
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
                }) {
                    Icon(
                        imageVector = if (appState?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (appState?.isPlaying == true) "Pause" else "Play"
                    )
                }

                // Stop button
                IconButton(onClick = {
                    appState?.stopPlayback()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop"
                    )
                }

                // Previous song button
                IconButton(onClick = {
                    appState?.playPreviousSong()
                }) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Song"
                    )
                }

                // Next song button
                IconButton(onClick = {
                    appState?.playNextSong()
                }) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Song"
                    )
                }

                // Radio player
                IconButton(onClick = {
                    if (appState != null) {
                        if (appState.isRadioPlaying) {
                            appState.stopRadio()
                        } else {
                            appState.startRadio()
                        }
                    } else {
                        // Fallback to the original example implementation
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
                }) {
                    Icon(
                        imageVector = if (appState?.isRadioPlaying == true) Icons.Filled.StopCircle else Icons.Filled.Radio,
                        contentDescription = if (appState?.isRadioPlaying == true) "Stop Radio" else "Start Radio"
                    )
                }

                // Main navigation buttons - Playlists, Library, Settings
                IconButton(
                    onClick = {
                        appState?.navigateToPlaylists()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.LibraryMusic,
                        contentDescription = "Playlists",
                        tint = if (currentScreen == AppScreen.PLAYLISTS || currentScreen == AppScreen.SONGS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                IconButton(
                    onClick = {
                        appState?.navigateToArtists()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = "Library",
                        tint = if (currentScreen == AppScreen.ARTISTS ||
                            currentScreen == AppScreen.ALBUMS ||
                            currentScreen == AppScreen.ALBUM_SONGS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = if (currentScreen == AppScreen.SETTINGS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            },
            //floatingActionButton = {
            //    FloatingActionButton(
            //        onClick = onNavigateToSettings,
            //        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
            //        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            //    ) {
            //        Icon(Icons.Filled.Settings, "Settings")
            //    }
            //}
        )
    }
}
