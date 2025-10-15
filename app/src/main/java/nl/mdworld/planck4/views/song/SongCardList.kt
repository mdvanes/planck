package nl.mdworld.planck4.views.song

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import kotlinx.coroutines.launch
import nl.mdworld.planck4.songcache.SongCacheManager
import nl.mdworld.planck4.ui.theme.PlanckTheme
import androidx.compose.ui.platform.LocalContext

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
        modifier = modifier.fillMaxSize().padding(bottom = 80.dp)
    ) {
        // Background cover art image
        BackgroundCoverArt(coverArtId = coverArt)

        // Content layer
        Box(
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var cachedCount by remember(songs) { mutableStateOf(0) }
            var cacheSizeText by remember { mutableStateOf("") }
            fun refreshCacheStats() {
                cachedCount = songs.count { SongCacheManager.isCached(context, it.id) }
                scope.launch { val sz = SongCacheManager.sizeBytesAsync(context); cacheSizeText = SongCacheManager.formatSize(sz) }
            }
            LaunchedEffect(songs) { refreshCacheStats() }

            LazyColumn {
                // First item: Playlist header with cover art + cache stats
                item {
                    SongsHeaderCard(
                        playlistTitle = playlistTitle,
                        coverArt = coverArt,
                        cachedCount = cachedCount,
                        totalCount = songs.size,
                        cacheSizeText = cacheSizeText,
                        onRefreshCache = { refreshCacheStats() }
                    )
                }

                // Remaining items: Song list items
                itemsIndexed(songs) { index, song ->
                    val isCached = SongCacheManager.isCached(context, song.id)
                    SongListItem(
                        song = song,
                        index = index,
                        isCurrentlyPlaying = currentlyPlayingSong?.id == song.id,
                        isCached = isCached,
                        onClick = {
                            onSongClick(it)
                            scope.launch { refreshCacheStats() }
                        }
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
                    Song("3", "Example Song 3", "Artist 3", "Album 3", 210, "cover3"),
                    Song("4", "Example Song 4", "Artist 1", "Album 1", 180, "cover1"),
                    Song("5", "Example Song 5", "Artist 2", "Album 2", 240, "cover2"),
                    Song("6", "Example Song 6", "Artist 3", "Album 3", 210, "cover3"),
                    Song("7", "Example Song 7", "Artist 1", "Album 1", 180, "cover1"),
                    Song("8", "Example Song 8", "Artist 2", "Album 2", 240, "cover2"),
                    Song("9", "Example Song 9", "Artist 3", "Album 3", 210, "cover3"),
                    Song("10", "Example Song 10", "Artist 1", "Album 1", 180, "cover1"),
                    Song("11", "Example Song 11", "Artist 2", "Album 2", 240, "cover2"),
                    Song("12", "Example Song 12", "Artist 3", "Album 3", 210, "cover3"),
                    Song("13", "Example Song 13", "Artist 1", "Album 1", 180, "cover1"),
                    Song("14", "Example Song 14", "Artist 2", "Album 2", 240, "cover2"),
                    Song("15", "Example Song 15", "Artist 3", "Album 3", 210, "cover3")
                ),
                playlistTitle = "My Playlist"
            )
        }
    }
}
