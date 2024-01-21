package nl.mdworld.planck

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import nl.mdworld.planck.networking.SubsonicApi
import nl.mdworld.planck.networking.SubsonicPlaylistResponse
import nl.mdworld.planck.networking.SubsonicTemp
import nl.mdworld.planck.ui.theme.PlanckTheme

data class Playlist(val coverArt: String, val name: String)

// TODO fix this SuppressLint rule
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PlaylistCard(playlist: Playlist) {
    val playerName = R.string.subsonic_player_name
    val apiConfig =
        "?u=${SubsonicTemp.JUKEBOX_USERNAME}&t=${SubsonicTemp.JUKEBOX_API_TOKEN}&s=${SubsonicTemp.JUKEBOX_SALT}&v=1.16.0&c=${playerName}&f=json"

    val coroutineScope = rememberCoroutineScope()
    val (coverArtId, setCoverArtId) = remember { mutableStateOf<String?>(null) }

    coroutineScope.launch {
        val playlistResponse: SubsonicPlaylistResponse = SubsonicApi().getPlaylistKtor("28")
        println(playlistResponse)
    }

    // Add padding around our message
    Row(modifier = Modifier.padding(all = 8.dp)) {
        //Image(
        //    painter = painterResource(R.drawable.ic_launcher_background),
        //    contentDescription = "Contact profile picture",
        //    modifier = Modifier
        //        // Set image size to 40 dp
        //        .size(40.dp)
        //        // Clip image to be shaped as a circle
        //        .clip(CircleShape)
        //        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        //)
        AsyncImage(
            //src={`${
            //    process.env
            //        .NX_BASE_URL
            //}/api/jukebox/coverart/${id}?type=${type}&hash=${encodeURIComponent(
            //    name
            //)}`
            //model = "https://picsum.photos/200",
            model = "${SubsonicTemp.JUKEBOX_BASE_URL}/getCoverArt${apiConfig}&id=${playlist.coverArt}",
            contentDescription = null,
            modifier = Modifier
                // Set image size to 40 dp
                .size(40.dp)
                // Clip image to be shaped as a circle
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        // Add a horizontal space between the image and the column
        Spacer(modifier = Modifier.width(8.dp))

        // We keep track if the message is expanded or not in this variable
        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            label = "surfaceColor",
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = playlist.name,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )
            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    text = playlist.name,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, name = "Dark Mode"
)
@Composable
fun PreviewPlaylistCard() {
    PlanckTheme {
        Surface {
            PlaylistCard(
                playlist = Playlist("cover-art-url", "Playlist 1")
            )
        }
    }
}

@Composable
fun PlaylistCardList(playlists: List<Playlist>) {
    LazyColumn {
        items(playlists) { playlist ->
            PlaylistCard(playlist)
        }
    }
}

@Preview
@Composable
fun PreviewPlaylistCardList() {
    PlanckTheme {
        PlaylistCardList(SampleData.playlistsSample)
    }
}
