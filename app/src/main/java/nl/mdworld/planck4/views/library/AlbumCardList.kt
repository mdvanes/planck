package nl.mdworld.planck4.views.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.bambooapps.material3.pullrefresh.PullRefreshIndicator
import eu.bambooapps.material3.pullrefresh.pullRefresh
import eu.bambooapps.material3.pullrefresh.rememberPullRefreshState
import nl.mdworld.planck4.PlanckAppState
import nl.mdworld.planck4.views.library.AlbumCard

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
