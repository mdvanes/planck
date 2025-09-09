package nl.mdworld.planck4

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck4.ui.theme.PlanckTheme

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
        "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"

    Row(modifier = Modifier.padding(all = 8.dp)) {
        AsyncImage(
            model = if (song.coverArt != null)
                "${SettingsManager.getJukeboxBaseUrl(context)}/getCoverArt${apiConfig}&id=${song.coverArt}"
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

@Composable
fun PlaylistHeaderCard(playlistTitle: String, coverArt: String?) {
    val context = LocalContext.current
    val playerName = context.getString(R.string.subsonic_player_name)
    val apiConfig =
        "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = if (coverArt != null)
                "${SettingsManager.getJukeboxBaseUrl(context)}/getCoverArt${apiConfig}&id=${coverArt}"
            else null,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Text(
                text = playlistTitle,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
    }
}

@Composable
fun SongListItem(song: Song, index: Int, onClick: (Song) -> Unit = {}) {
    val durationText = if (song.duration != null) {
        val minutes = song.duration / 60
        val seconds = song.duration % 60
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    } else {
        ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick(song) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            modifier = Modifier.width(32.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            if (song.artist != null) {
                Text(
                    text = song.artist,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            }
        }

        if (durationText.isNotEmpty()) {
            Text(
                text = durationText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp
            )
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

@Preview(name = "Song List Preview")
@Composable
fun PreviewSongCardList() {
    PlanckTheme {
        Surface {
            SongCardList(
                songs = listOf(
                    Song("1", "Example Song 1", "Artist 1", "Album 1", 180, "cover1"),
                    Song("2", "Example Song 2", "Artist 2", "Album 2", 240, "cover2"),
                    Song("3", "Example Song 3", "Artist 3", "Album 3", 210, "cover3")
                ),
                playlistTitle = "My Playlist"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCardList(
    modifier: Modifier = Modifier,
    songs: List<Song>,
    playlistTitle: String = "Playlist",
    onSongClick: (Song) -> Unit = {}
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = { /* TODO: Implement refresh */ }
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            // First item: Playlist header with cover art
            item {
                PlaylistHeaderCard(
                    playlistTitle = playlistTitle,
                    coverArt = songs.firstOrNull()?.coverArt
                )
            }

            // Remaining items: Song list items with index, title, artist, and duration
            itemsIndexed(songs) { index, song ->
                SongListItem(song = song, index = index, onClick = onSongClick)
            }
        }
        PullRefreshIndicator(
            refreshing = false,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
