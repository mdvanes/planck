package nl.mdworld.planck4.views.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import nl.mdworld.planck4.SettingsManager

/**
 * Playback Context Section
 */
@Composable
fun PlaybackContextSection() {
    val context = LocalContext.current
    SettingsSection(title = "Last Played Context") {
        val lastSongId = SettingsManager.getLastSongId(context) ?: "(none)"
        val lastPlaylistId =
            SettingsManager.getLastPlaylistId(context).takeUnless { it.isNullOrBlank() } ?: "(none)"
        val lastFolderId =
            SettingsManager.getLastFolderId(context).takeUnless { it.isNullOrBlank() } ?: "(none)"
        Text("Last Song ID: $lastSongId")
        Text("Last Playlist ID: $lastPlaylistId")
        Text("Last Folder (Album) ID: $lastFolderId")
    }
}

