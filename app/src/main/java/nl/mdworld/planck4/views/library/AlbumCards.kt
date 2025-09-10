package nl.mdworld.planck4.views.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.R
import nl.mdworld.planck4.SettingsManager

@Composable
fun ArtistCard(artist: Artist, appState: PlanckAppState? = null) {
    val context = LocalContext.current
    val playerName = R.string.subsonic_player_name
    val apiConfig =
        "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"

    Row(modifier = Modifier
        .padding(all = 8.dp)
        .clickable {
            println("Clicked on artist ${artist.name}")
            appState?.navigateToAlbums(artist.id, artist.name)
        }
    ) {
        AsyncImage(
            model = if (artist.coverArt != null) {
                "${SettingsManager.getJukeboxBaseUrl(context)}/getCoverArt${apiConfig}&id=${artist.coverArt}"
            } else {
                null
            },
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = artist.name,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (artist.albumCount > 0) {
                Text(
                    text = "${artist.albumCount} albums",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun AlbumCard(album: Album, appState: PlanckAppState? = null) {
    val context = LocalContext.current
    val playerName = R.string.subsonic_player_name
    val apiConfig =
        "?u=${SettingsManager.getUsername(context)}&t=${SettingsManager.getApiToken(context)}&s=${SettingsManager.getSalt(context)}&v=1.16.0&c=${playerName}&f=json"

    Row(modifier = Modifier
        .padding(all = 8.dp)
        .clickable {
            println("Clicked on album ${album.name}")
            appState?.navigateToAlbumSongs(album.id, album.name)
        }
    ) {
        AsyncImage(
            model = if (album.coverArt != null) {
                "${SettingsManager.getJukeboxBaseUrl(context)}/getCoverArt${apiConfig}&id=${album.coverArt}"
            } else {
                null
            },
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = album.name,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = album.artist,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (album.year != null) {
                Text(
                    text = album.year.toString(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistCardList(artists: MutableList<Artist>, appState: PlanckAppState? = null, modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }

    // Filter artists based on search query
    val filteredArtists = remember(artists, searchQuery) {
        if (searchQuery.isEmpty()) {
            artists
        } else {
            artists.filter { artist ->
                artist.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier.padding(0.dp, 0.dp, 0.dp, 80.dp)) {
        val isRefreshing = false
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { /* TODO: Implement refresh */ }
        )

        Column {
            // Show search field when search is active
            if (showSearchField) {
                SearchArtistField(
                    onSearch = { query ->
                        searchQuery = query
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.pullRefresh(pullRefreshState)
            ) {
                items(filteredArtists) { artist ->
                    ArtistCard(artist, appState)
                }
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Search floating action button positioned in top right
        FloatingActionButton(
            onClick = {
                showSearchField = !showSearchField
                if (!showSearchField) {
                    searchQuery = ""
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (showSearchField) Icons.Filled.Clear else Icons.Filled.Search,
                contentDescription = if (showSearchField) "Close search" else "Search artists"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumCardList(albums: MutableList<Album>, appState: PlanckAppState? = null, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(0.dp, 0.dp, 0.dp, 80.dp)) {
        val isRefreshing = false
        val pullRefreshState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = { /* TODO: Implement refresh */ }
        )

        LazyColumn(
            modifier = Modifier.pullRefresh(pullRefreshState)
        ) {
            items(albums) { album ->
                AlbumCard(album, appState)
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun SearchArtistField(
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = text,
        onValueChange = { newText ->
            text = newText
            onSearch(newText)
        },
        placeholder = {
            Text("Search artists...")
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = {
                    text = ""
                    onSearch("")
                }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear search")
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        )
    )
}
