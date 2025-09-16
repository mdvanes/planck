package nl.mdworld.planck4

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Singleton to track key codes pressed for debugging purposes
 * Displays in the Settings screen for easy visibility
 */
object KeyCodeTracker {
    private val _keyCodes = mutableStateListOf<String>()
    val keyCodes: SnapshotStateList<String> = _keyCodes

    fun addKeyCode(keyCode: String, keyName: String) {
        val entry = "[$keyCode] $keyName"
        println("KeyCodeTracker: Adding key code: $entry") // Debug output
        _keyCodes.add(0, entry) // Add to beginning of list (most recent first)

        // Keep only the last 20 entries to prevent memory issues
        if (_keyCodes.size > 20) {
            _keyCodes.removeAt(_keyCodes.size - 1)
        }
        println("KeyCodeTracker: Current list size: ${_keyCodes.size}") // Debug output
    }

    fun clear() {
        println("KeyCodeTracker: Clearing key codes") // Debug output
        _keyCodes.clear()
    }
}
