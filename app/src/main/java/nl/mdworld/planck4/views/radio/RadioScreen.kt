package nl.mdworld.planck4.views.radio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.R
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.ui.theme.PlanckTheme
import nl.mdworld.planck4.util.radiometadata.BroadcastInfo
import nl.mdworld.planck4.util.radiometadata.RadioMetadata
import nl.mdworld.planck4.util.radiometadata.SongInfo
import nl.mdworld.planck4.util.radiometadata.TimeInfo
import nl.mdworld.planck4.views.components.BottomAppBar
import nl.mdworld.planck4.views.song.BackgroundCoverArt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun formatFromIsoString(iso: String): String {
    val ldt = LocalDateTime.parse(iso)           // ISO-8601 without zone
    return ldt.format(hourMinuteFormatter)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreenContent(
    appState: PlanckAppState? = null,
    context: Context = LocalContext.current
) {
    val firstTrack = appState?.radioMetadata?.firstOrNull()

    val startRadioButton = @Composable {
        IconButton(
            onClick = {
                if (appState != null) {
                    if (appState.isRadioPlaying) {
                        appState.stopRadio()
                    } else {
                        appState.startRadio()
                    }
                } else {
                    // Fallback radio control if appState is null
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
            },
            modifier = Modifier.size(100.dp)
        ) {
            Image(
                painter = painterResource(
                    id = R.drawable.npo_radio_2
                ),
                contentDescription = "Start Radio",
                modifier = Modifier.size(100.dp).alpha(if (appState?.isRadioPlaying == true) 1f else 0.2f)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background cover art of the active song (if available)
        BackgroundCoverArt(
            coverArtUrl = firstTrack?.song?.imageUrl ?: firstTrack?.broadcast?.imageUrl
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 84.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                startRadioButton()

                Image(
                    painter = painterResource(id = R.drawable.sky_radio),
                    contentDescription = "Dummy Radio",
                    modifier = Modifier.size(100.dp).alpha(0.2f)
                )

                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "SkipNext",
                    modifier = Modifier.size(100.dp).alpha(0.2f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (firstTrack?.song?.title != null) {
                // Previous tracks list (excluding the first/current track)
                val previousTracks = appState?.radioMetadata?.drop(1).orEmpty()
                if (previousTracks.isNotEmpty()) {
                    Text(
                        text = "Previous tracks",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(previousTracks) { track ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val start = track.time?.start?.let { runCatching { formatFromIsoString(it) }.getOrElse { "--:--" } } ?: "--:--"
                                Text(
                                    text = start,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,fontSize = 28.sp
                                )
                                Text(
                                    text = "${track.song.title ?: "Title"} - ${track.song.artist ?: "Artist"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp),
                ) {
                    Text(
                        text = "${firstTrack.broadcast?.title ?: "Broadcast Title"} - ${firstTrack.broadcast?.presenters ?: "Broadcast Presenters"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current
    RadioScreenContent(appState, context)
}

@Preview(name = "Radio Screen Preview")
@Composable
fun PreviewRadioScreen() {
    val context = LocalContext.current
    val mockState = remember {
        PlanckAppState(context = context).apply {
            isRadioPlaying = true
            radioMetadata = listOf(
                RadioMetadata(
                    song = SongInfo(
                        title = "Song Title 1",
                        artist = "Artist 1",
                        imageUrl = "https://picsum.photos/200"
                    ),
                    broadcast = BroadcastInfo(
                        title = "Morning Show",
                        presenters = "DJ Mike",
                        imageUrl = "https://picsum.photos/200"
                    ),
                    time = TimeInfo(
                        start = "2023-10-01T08:00:00",
                        end = "2023-10-01T09:00:00"
                    )
                ),
                RadioMetadata(
                    song = SongInfo(
                        title = "Song Title 2",
                        artist = "Artist 2",
                        imageUrl = "https://picsum.photos/200"
                    ),
                    broadcast = BroadcastInfo(
                        title = "Morning Show",
                        presenters = "DJ Mike",
                        imageUrl = "https://picsum.photos/200"
                    ),
                    time = TimeInfo(
                        start = "2023-10-01T08:00:00",
                        end = "2023-10-01T09:00:00"
                    )
                ),
                RadioMetadata(
                    song = SongInfo(
                        title = "Song Title 3",
                        artist = "Artist 3",
                        imageUrl = "https://fastly.picsum.photos/id/307/200/200.jpg"
                    ),
                    broadcast = BroadcastInfo(
                        title = "Morning Show",
                        presenters = "DJ Mike",
                        imageUrl = "https://picsum.photos/200"
                    ),
                    time = TimeInfo(
                        start = "2023-10-01T08:00:00",
                        end = "2023-10-01T09:00:00"
                    )
                ),
            )
        }
    }

    PlanckTheme {
        Surface {
            RadioScreenContent(
                appState = mockState,
                context = context
            )
        }
    }
}