package nl.mdworld.planck4.views.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck4.PlanckAppState

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
