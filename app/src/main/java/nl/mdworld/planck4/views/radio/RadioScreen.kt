package nl.mdworld.planck4.views.radio


import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.R
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.ui.theme.PlanckTheme
import nl.mdworld.planck4.views.song.BackgroundCoverArt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//private val hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm")
//
//
//fun formatFromIsoString(iso: String): String {
//    val ldt = LocalDateTime.parse(iso)           // ISO-8601 without zone
//    return ldt.format(hourMinuteFormatter)
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current
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
                painter = painterResource(id = R.drawable.npo_radio_2),
                contentDescription = "Start Radio",
                modifier = Modifier.size(100.dp)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background blurred cover art of the active song (if available)
        BackgroundCoverArt(coverArtUrl = appState?.activeSong?.coverArt)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                startRadioButton()

                startRadioButton()

                startRadioButton()
            }

            Row {
                Text(
                    text = firstTrack?.song?.title ?: "Song Title",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                Text(
                    text = firstTrack?.song?.artist ?: "Artist",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            //Row {
            //    Text(
            //        text = firstTrack?.time?.start ? formatFromIsoString(firstTrack?.time?.start) : "Start Time",
            //        style = MaterialTheme.typography.bodySmall,
            //        color = MaterialTheme.colorScheme.primary
            //    )
            //}

            Row( modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = firstTrack?.broadcast?.title ?: "Broadcast Title",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = firstTrack?.broadcast?.presenters ?: "Broadcast Presenters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }


        }


        //Column(
        //    modifier = Modifier
        //        .fillMaxSize()
        //        .padding(16.dp),
        //    horizontalAlignment = Alignment.CenterHorizontally,
        //    verticalArrangement = Arrangement.Top
        //) {
        //    // Radio control button
        //    Card(
        //        modifier = Modifier
        //            .fillMaxWidth()
        //            .padding(horizontal = 32.dp),
        //        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        //    ) {
        //        Row {
        //            Column(
        //                modifier = Modifier.padding(24.dp),
        //                horizontalAlignment = Alignment.CenterHorizontally
        //            ) {
        //                startRadioButton()
        //
        //                Spacer(modifier = Modifier.height(16.dp))
        //
        //                Text(
        //                    text = if (appState?.isRadioPlaying == true) "Stop Radio" else "Start Radio",
        //                    style = MaterialTheme.typography.titleMedium,
        //                    color = MaterialTheme.colorScheme.onSurface
        //                )
        //
        //                if (appState?.isRadioPlaying == true) {
        //                    Spacer(modifier = Modifier.height(8.dp))
        //                    Text(
        //                        text = "Now Playing",
        //                        style = MaterialTheme.typography.bodySmall,
        //                        color = MaterialTheme.colorScheme.primary
        //                    )
        //                }
        //            }
        //
        //            Column ( modifier = Modifier.padding(24.dp),
        //                horizontalAlignment = Alignment.CenterHorizontally) {
        //
        //                AsyncImage(
        //                    model = appState?.activeSong?.coverArt,
        //                    contentDescription = null,
        //                    modifier = Modifier
        //                        .size(180.dp)
        //                )
        //            }
        //        }
        //    }
        //}
    }
}

@Preview(name = "Radio Screen Preview")
@Composable
fun PreviewRadioScreen() {
    //val mockState = remember {
    //    PlanckAppState(
    //        context = TODO()
    //    ).apply {
    //        activeSong = Song(
    //            id = "preview",
    //            title = "Preview Song",
    //            artist = "Preview Artist",
    //            album = "Preview Album",
    //            duration = 100,
    //            coverArt = "https://picsum.photos/800"
    //        )
    //    }
    //}
    PlanckTheme {
        Surface {
            RadioScreen(
                //appState = mockState
            )
        }
    }
}