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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import nl.mdworld.planck4.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current

    // Load current values from settings
    var serverUrl by remember { mutableStateOf(SettingsManager.getServerUrl(context)) }
    var username by remember { mutableStateOf(SettingsManager.getUsername(context)) }
    var salt by remember { mutableStateOf(SettingsManager.getSalt(context)) }
    var apiToken by remember { mutableStateOf(SettingsManager.getApiToken(context)) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    // Track changes to enable save button
    LaunchedEffect(serverUrl, username, salt, apiToken) {
        hasUnsavedChanges = serverUrl != SettingsManager.getServerUrl(context) ||
                username != SettingsManager.getUsername(context) ||
                salt != SettingsManager.getSalt(context) ||
                apiToken != SettingsManager.getApiToken(context)
    }

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

        // Network Settings Section
        SettingsSection(
            title = "Network Settings"
        ) {
            OutlinedTextField(
                value = serverUrl,
                onValueChange = {
                    serverUrl = it
                },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://your-server.com/rest/") }
            )

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("your_username") }
            )

            OutlinedTextField(
                value = salt,
                onValueChange = {
                    salt = it
                },
                label = { Text("Salt") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("random_salt_value") }
            )

            OutlinedTextField(
                value = apiToken,
                onValueChange = {
                    apiToken = it
                },
                label = { Text("API Token") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("your_api_token") }
            )

            var cacheEnabled by remember { mutableStateOf(true) }
            SettingsSwitch(
                label = "Enable Caching",
                checked = cacheEnabled,
                onCheckedChange = { cacheEnabled = it }
            )

            // Save button
            Button(
                onClick = {
                    SettingsManager.saveServerUrl(context, serverUrl)
                    SettingsManager.saveUsername(context, username)
                    SettingsManager.saveSalt(context, salt)
                    SettingsManager.saveApiToken(context, apiToken)
                    hasUnsavedChanges = false
                },
                enabled = hasUnsavedChanges,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
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
