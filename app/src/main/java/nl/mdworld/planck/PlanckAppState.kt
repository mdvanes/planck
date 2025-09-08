package nl.mdworld.planck

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberPlanckAppState(context: Context = LocalContext.current) = remember(context) {
PlanckAppState(context)
}

class PlanckAppState (private val context: Context) {
    val playlists = mutableStateListOf(
        Playlist(
            "Empty",
            "No Playlist"
        )
    )

    //fun getPlaylists(): MutableList<Playlist> {
    //    return playlists
    //}
}