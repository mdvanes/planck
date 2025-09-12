package nl.mdworld.planck4

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
                    currentScreen == AppScreen.ALBUM_SONGS
                ) {
                    IconButton(onClick = {
                        when (currentScreen) {
                            AppScreen.SONGS -> onNavigateBack() // Back to playlists
                            AppScreen.ALBUMS -> appState?.navigateToArtists() // Back to artists
                            AppScreen.ALBUM_SONGS -> {
                                // Back to albums of the current artist
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
                    }, modifier = Modifier.padding(horizontal = BottomAppBar.IconSpacing)) {
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
                            lineHeight = 40.sp,
                            fontSize = 28.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (activeSong.artist != null) {
                            Text(
                                text = activeSong.artist,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 32.sp,
                                fontSize = 24.sp,
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
                }, modifier = Modifier.padding(horizontal = BottomAppBar.IconSpacing)) {
                    Icon(
                        imageVector = if (appState?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (appState?.isPlaying == true) "Pause" else "Play",
                        modifier = Modifier.size(BottomAppBar.IconSize),
                    )
                }

                // Stop button
                IconButton(onClick = {
                    appState?.stopPlayback()
                }, modifier = Modifier.padding(horizontal = BottomAppBar.IconSpacing)) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(BottomAppBar.IconSize),
                    )
                }

                // Previous song button
                IconButton(onClick = {
                    appState?.playPreviousSong()
                }, modifier = Modifier.padding(horizontal = BottomAppBar.IconSpacing)) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Song",
                        modifier = Modifier.size(BottomAppBar.IconSize),
                    )
                }

                // Next song button
                IconButton(onClick = {
                    appState?.playNextSong()
                }, modifier = Modifier.padding(horizontal = BottomAppBar.IconSpacing)) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Song",
                        modifier = Modifier.size(BottomAppBar.IconSize),
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
                }, modifier = Modifier.padding(horizontal = BottomAppBar.IconSpacing)) {
                    Icon(
                        imageVector = if (appState?.isRadioPlaying == true) Icons.Filled.StopCircle else Icons.Filled.Radio,
                        contentDescription = if (appState?.isRadioPlaying == true) "Stop Radio" else "Start Radio",
                        modifier = Modifier.size(BottomAppBar.IconSize),
                    )
                }

                // Main navigation buttons - Playlists, Library, Settings
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
                    icon = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    isSelected = currentScreen == AppScreen.SETTINGS,
                    onClick = onNavigateToSettings
                )
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
