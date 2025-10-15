package nl.mdworld.planck4.views.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SongCacheSection(
    cacheSizeText: String,
    onRefresh: () -> Unit,
    onClear: () -> Unit
) {
    SettingsSection(title = "Song Cache") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(64.dp)
        ) {
            SettingsItem(
                label = "Cache Size",
                value = cacheSizeText,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onRefresh, modifier = Modifier.weight(1f)) { Text("Refresh Size") }
            Button(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Clear Song Cache") }
        }
    }
}

