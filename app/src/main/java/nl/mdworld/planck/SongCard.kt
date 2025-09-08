package nl.mdworld.planck

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck.networking.SubsonicTemp
import nl.mdworld.planck.ui.theme.PlanckTheme

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Int?,
    val coverArt: String?
)

@Composable
fun SongCard(song: Song) {
    val context = LocalContext.current
    val playerName = context.getString(R.string.subsonic_player_name)
    val apiConfig =
        "?u=${SubsonicTemp.JUKEBOX_USERNAME}&t=${SubsonicTemp.JUKEBOX_API_TOKEN}&s=${SubsonicTemp.JUKEBOX_SALT}&v=1.16.0&c=${playerName}&f=json"

    Row(modifier = Modifier.padding(all = 8.dp)) {
        AsyncImage(
            model = if (song.coverArt != null)
                "${SubsonicTemp.JUKEBOX_BASE_URL}/getCoverArt${apiConfig}&id=${song.coverArt}"
            else null,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.height(80.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Text(
                text = song.title,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp
            )

            if (song.artist != null) {
                Text(
                    text = song.artist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp
                )
            }

            if (song.album != null) {
                Text(
                    text = song.album,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 14.sp
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
fun PreviewSongCard() {
    PlanckTheme {
        Surface {
            SongCard(
                song = Song(
                    id = "1",
                    title = "Example Song",
                    artist = "Example Artist",
                    album = "Example Album",
                    duration = 240,
                    coverArt = "cover-art-id"
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCardList(songs: List<Song>, modifier: Modifier = Modifier) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = { /* TODO: Implement refresh */ }
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            items(songs) { song ->
                SongCard(song = song)
            }
        }
        PullRefreshIndicator(
            refreshing = false,
            state = pullRefreshState,
            modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter)
        )
    }
}
