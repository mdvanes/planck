package nl.mdworld.planck4.views.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.mdworld.planck4.networking.subsonic.SubsonicRadioStation

/**
 * Internet Radio Stations Section
 */
@Composable
fun InternetRadioSection(
    radioStations: List<SubsonicRadioStation>,
    radioStationsLoading: Boolean,
    radioStationsError: String?,
    loadRadioStations: () -> Unit
) {
    SettingsSection(title = "Internet Radio Stations") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Stations (${radioStations.size})")
            TextButton(
                onClick = { loadRadioStations() },
                enabled = !radioStationsLoading
            ) { Text(if (radioStationsLoading) "Loading…" else "Refresh") }
        }
        when {
            radioStationsLoading -> Text("Loading stations…")
            radioStationsError != null -> Text(
                "Error: ${radioStationsError}",
                color = MaterialTheme.colorScheme.error
            )

            radioStations.isEmpty() -> Text("No stations found")
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    radioStations.forEach { st ->
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)) {
                            Text(
                                st.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            st.streamUrl?.let {
                                Text(
                                    "Stream: $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            st.homepageUrl?.let {
                                Text(
                                    "Home: $it",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            st.bitrate?.let {
                                Text(
                                    "Bitrate: ${it} kbps",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

