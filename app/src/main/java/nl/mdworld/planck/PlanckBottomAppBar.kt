package nl.mdworld.planck

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable


@Composable
fun PlanckBottomAppBar() {
    val mediaPlayer = MediaPlayer()

    BottomAppBar(
        actions = {
            IconButton(onClick = { /* do something */ }) {
                Icon(Icons.Filled.Check, contentDescription = "Localized description")
            }
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Localized description",
                )
            }
            IconButton(onClick = {
                // Source: https://www.geeksforgeeks.org/how-to-play-audio-from-url-in-android-using-jetpack-compose/
                // on below line we are creating a variable for our audio url
                val audioUrl = "https://icecast.omroep.nl/radio2-bb-mp3"

                // on below line we are setting audio stream type as
                // stream music on below line.
                mediaPlayer.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                // on below line we are running a try and catch block
                // for our media player.
                try {
                    // on below line we are setting audio source
                    // as audio url on below line.
                    mediaPlayer.setDataSource(audioUrl)

                    // on below line we are preparing
                    // our media player.
                    mediaPlayer.prepare()

                    // on below line we are starting
                    // our media player.
                    mediaPlayer.start()

                } catch (e: Exception) {

                    // on below line we are
                    // handling our exception.
                    e.printStackTrace()
                }

                // on below line we are displaying a toast message as audio player.
                // TODO Toast.makeText(ctx, "Audio started playing..", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Play Example",
                )
            }
            IconButton(onClick = {
                // on below line we are checking
                // if media player is playing.
                if (mediaPlayer.isPlaying) {
                    // if media player is playing
                    // we are stopping it on below line.
                    mediaPlayer.stop()

                    // on below line we are resetting
                    // our media player.
                    mediaPlayer.reset()

                    // on below line we are calling release
                    // to release our media player.
                    mediaPlayer.release()

                    // on below line we are displaying a toast message to pause audio
                    // TODO Toast.makeText(ctx, "Audio has been  paused..", Toast.LENGTH_SHORT).show()
                } else {
                    // if audio player is not displaying we are displaying
                    // below toast message
                    // TODO Toast.makeText(ctx, "Audio not played..", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Pause Example",
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* do something */ },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(Icons.Filled.Add, "Localized description")
            }
        }
    )
}