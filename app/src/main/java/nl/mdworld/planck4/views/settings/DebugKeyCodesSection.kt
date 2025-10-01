package nl.mdworld.planck4.views.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.mdworld.planck4.KeyCodeTracker

/**
 * Displays captured key codes for debugging in a settings section with controls
 * to add a test key code and clear the list.
 */
@Composable
fun DebugKeyCodesSection() {
    SettingsSection(title = "Debug - Key Codes") {
        if (KeyCodeTracker.keyCodes.isEmpty()) {
            SettingsItem(
                label = "No key codes pressed yet",
                value = "Press car buttons to see them here"
            )
        } else {
            KeyCodeTracker.keyCodes.forEachIndexed { index, keyCode ->
                SettingsItem(
                    label = "${index + 1}.",
                    value = keyCode
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(64.dp)
        ) {
            Button(
                onClick = { KeyCodeTracker.addKeyCode("999", "TEST_BUTTON") },
                modifier = Modifier.weight(1f)
            ) { Text("Add Test Key Code") }
            Button(
                onClick = { KeyCodeTracker.clear() },
                modifier = Modifier.weight(1f)
            ) { Text("Clear Key Codes") }
        }
    }
}

