package nl.mdworld.planck4.views.song

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck4.ui.theme.PlanckTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongCardList(
    modifier: Modifier = Modifier,
    songs: List<Song>,
    playlistTitle: String = "Playlist",
    currentlyPlayingSong: Song? = null,
    onSongClick: (Song) -> Unit = {}
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = false,
        onRefresh = { /* TODO: Implement refresh */ }
    )

    val coverArt = songs.firstOrNull()?.coverArt

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background cover art image
        BackgroundCoverArt(coverArtId = coverArt)

        // Content layer
        Box(
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            LazyColumn {
                // First item: Playlist header with cover art
                item {
                    SongsHeaderCard(
                        playlistTitle = playlistTitle,
                        coverArt = coverArt
                    )
                }

                // Remaining items: Song list items with index, title, artist, and duration
                itemsIndexed(songs) { index, song ->
                    SongListItem(
                        song = song,
                        index = index,
                        isCurrentlyPlaying = currentlyPlayingSong?.id == song.id,
                        onClick = onSongClick
                    )
                }
            }
            PullRefreshIndicator(
                refreshing = false,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
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
