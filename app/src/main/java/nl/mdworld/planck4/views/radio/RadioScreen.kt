package nl.mdworld.planck4.views.radio

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    appState: PlanckAppState? = null
) {
    val context = LocalContext.current

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
            modifier = Modifier.size(80.dp)
        ) {
            Icon(
                imageVector = if (appState?.isRadioPlaying == true)
                    Icons.Filled.StopCircle else Icons.Filled.Radio,
                contentDescription = if (appState?.isRadioPlaying == true)
                    "Stop Radio" else "Start Radio",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Radio icon and title
        //Icon(
        //    imageVector = Icons.Filled.Radio,
        //    contentDescription = "Radio",
        //    modifier = Modifier.size(100.dp),
        //    tint = MaterialTheme.colorScheme.primary
        //)
        //
        //Spacer(modifier = Modifier.height(32.dp))
        //
        //Text(
        //    text = "Radio",
        //    style = MaterialTheme.typography.headlineMedium,
        //    fontSize = 28.sp,
        //    color = MaterialTheme.colorScheme.onSurface,
        //    textAlign = TextAlign.Center
        //)
        //
        //Spacer(modifier = Modifier.height(16.dp))
        //
        //Text(
        //    text = "Listen to your favorite radio station",
        //    style = MaterialTheme.typography.bodyMedium,
        //    color = MaterialTheme.colorScheme.onSurfaceVariant,
        //    textAlign = TextAlign.Center
        //)
        //
        //Spacer(modifier = Modifier.height(48.dp))

        // Radio control button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                startRadioButton()
                //IconButton(
                //    onClick = {
                //        if (appState != null) {
                //            if (appState.isRadioPlaying) {
                //                appState.stopRadio()
                //            } else {
                //                appState.startRadio()
                //            }
                //        } else {
                //            // Fallback radio control if appState is null
                //            val mediaPlayer = MediaPlayer()
                //            val audioUrl = SettingsManager.getRadioUrl(context)
                //            mediaPlayer.setAudioAttributes(
                //                AudioAttributes.Builder()
                //                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                //                    .build()
                //            )
                //            try {
                //                mediaPlayer.setDataSource(audioUrl)
                //                mediaPlayer.prepare()
                //                mediaPlayer.start()
                //            } catch (e: Exception) {
                //                e.printStackTrace()
                //            }
                //        }
                //    },
                //    modifier = Modifier.size(80.dp)
                //) {
                //    Icon(
                //        imageVector = if (appState?.isRadioPlaying == true)
                //            Icons.Filled.StopCircle else Icons.Filled.Radio,
                //        contentDescription = if (appState?.isRadioPlaying == true)
                //            "Stop Radio" else "Start Radio",
                //        modifier = Modifier.size(48.dp),
                //        tint = MaterialTheme.colorScheme.primary
                //    )
                //}

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (appState?.isRadioPlaying == true) "Stop Radio" else "Start Radio",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (appState?.isRadioPlaying == true) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
