package nl.mdworld.planck4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

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
    var radioUrl by remember { mutableStateOf(SettingsManager.getRadioUrl(context)) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    // Track changes to enable save button
    LaunchedEffect(serverUrl, username, salt, apiToken, radioUrl) {
        hasUnsavedChanges = serverUrl != SettingsManager.getServerUrl(context) ||
                username != SettingsManager.getUsername(context) ||
                salt != SettingsManager.getSalt(context) ||
                apiToken != SettingsManager.getApiToken(context) ||
                radioUrl != SettingsManager.getRadioUrl(context)
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

        // Subsonic Settings Section
        SettingsSection(
            title = "Subsonic Settings"
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

            var passwordVisibility1 by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = salt,
                onValueChange = {
                    salt = it
                },
                label = { Text("Salt") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("random_salt_value") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (passwordVisibility1) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisibility1 = !passwordVisibility1 }) {
                        Text(
                            text = if (passwordVisibility1) "Hide" else "Show",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            var passwordVisibility by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = apiToken,
                onValueChange = {
                    apiToken = it
                },
                label = { Text("API Token") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("your_api_token") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password
                ),
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    TextButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Text(
                            text = if (passwordVisibility) "Hide" else "Show",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            OutlinedTextField(
                value = radioUrl,
                onValueChange = {
                    radioUrl = it
                },
                label = { Text("Radio URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://your-radio.com/stream/") }
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
                    SettingsManager.saveRadioUrl(context, radioUrl)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
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
