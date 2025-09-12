package nl.mdworld.planck4.views.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

object BottomAppBar {
    val IconSpacing = 8.dp
    val IconSize = 32.dp
}

@Composable
fun NavigationButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.padding(horizontal = BottomAppBar.IconSpacing)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(BottomAppBar.IconSize),
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationButtonPreview() {
    NavigationButton(
        icon = Icons.Default.Home, // Replace with an appropriate icon
        contentDescription = "Home",
        isSelected = true,
        onClick = {}
    )
}