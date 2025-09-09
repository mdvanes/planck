package nl.mdworld.planck4

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.mdworld.planck4.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider()

        // Audio Settings Section
        SettingsSection(
            title = "Audio Settings"
        ) {
            var audioQuality by remember { mutableStateOf("High") }

            SettingsDropdown(
                label = "Audio Quality",
                value = audioQuality,
                options = listOf("Low", "Medium", "High", "Lossless"),
                onValueChange = { audioQuality = it }
            )

            var enableEqualizer by remember { mutableStateOf(false) }
            SettingsSwitch(
                label = "Enable Equalizer",
                checked = enableEqualizer,
                onCheckedChange = { enableEqualizer = it }
            )
        }

        // Network Settings Section
        SettingsSection(
            title = "Network Settings"
        ) {
            var serverUrl by remember { mutableStateOf("") }

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://your-server.com") }
            )

            var username by remember { mutableStateOf("") }
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            var cacheEnabled by remember { mutableStateOf(true) }
            SettingsSwitch(
                label = "Enable Caching",
                checked = cacheEnabled,
                onCheckedChange = { cacheEnabled = it }
            )
        }

        // Appearance Settings Section
        SettingsSection(
            title = "Appearance"
        ) {
            var darkMode by remember { mutableStateOf(false) }
            SettingsSwitch(
                label = "Dark Mode",
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )

            var showAlbumArt by remember { mutableStateOf(true) }
            SettingsSwitch(
                label = "Show Album Art",
                checked = showAlbumArt,
                onCheckedChange = { showAlbumArt = it }
            )
        }

        // About Section
        SettingsSection(
            title = "About"
        ) {
            SettingsItem(
                label = "Version",
                value = BuildConfig.VERSION_NAME
            )

            SettingsItem(
                label = "Build",
                value = if (BuildConfig.DEBUG) "Debug" else "Release"
            )

            SettingsItem(
                label = "Padding",
                value = "Padding",
            )

            SettingsItem(
                label = "Padding",
                value = "Padding",
            )

        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
