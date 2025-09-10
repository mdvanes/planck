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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
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
                if (currentScreen == AppScreen.SONGS) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to playlists"
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

                // Spacer to push buttons to the right
                Spacer(modifier = Modifier.weight(1f))

                // Play/Pause button - use app state if available, otherwise fallback to example
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
                        val audioUrl = "https://icecast.omroep.nl/radio2-bb-mp3"

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

                // Radio player
                IconButton(onClick = {
                    val mediaPlayer = MediaPlayer()
                    val audioUrl = "https://icecast.omroep.nl/radio2-bb-mp3"

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
                }) {
                    Icon(
                        imageVector = if (appState?.isPlaying == true) Icons.Filled.Pause else Icons.Filled.Radio,
                        contentDescription = if (appState?.isPlaying == true) "Pause" else "Radio"
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToSettings,
                    containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(Icons.Filled.Settings, "Settings")
                }
            }
        )
    }
}
