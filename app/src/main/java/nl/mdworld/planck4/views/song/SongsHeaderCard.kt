package nl.mdworld.planck4.views.song

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.mdworld.planck4.views.components.CoverArt

@Composable
fun SongsHeaderCard(
    playlistTitle: String,
    coverArt: String?,
    cachedCount: Int,
    totalCount: Int,
    cacheSizeText: String,
    onRefreshCache: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CoverArt(coverArtId = coverArt)

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
            if (totalCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Cached: $cachedCount / $totalCount" +
                                if (cacheSizeText.isNotBlank()) " ($cacheSizeText)" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onRefreshCache) { Text("Refresh") }
                }
            }
        }
    }
}