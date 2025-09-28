package nl.mdworld.planck4

import android.content.Context

object SettingsManager {
    private const val PREFS_NAME = "planck_prefs"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_RADIO_URL = "radio_url"
    // New: overlay opacity for background cover art
    private const val KEY_OVERLAY_OPACITY = "overlay_opacity"

    private const val DEFAULT_SERVER_URL = "https://example.com/rest/"
    const val DEFAULT_USERNAME = "demo_user"
    const val DEFAULT_PASSWORD = "demo_password"
    const val DEFAULT_RADIO_URL = "https://icecast.omroep.nl/radio2-bb-mp3"
    // New default overlay opacity (matches old hardcoded 0.8f)
    const val DEFAULT_OVERLAY_OPACITY = 0.8f

    fun saveServerUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SERVER_URL, url).apply()
    }

    fun getServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    fun saveUsername(context: Context, username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getUsername(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USERNAME, DEFAULT_USERNAME) ?: DEFAULT_USERNAME
    }

    fun savePassword(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PASSWORD, token).apply()
    }

    fun getPassword(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    fun saveRadioUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_RADIO_URL, url).apply()
    }

    fun getRadioUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_RADIO_URL, DEFAULT_RADIO_URL) ?: DEFAULT_RADIO_URL
    }

    // New overlay opacity preference helpers
    fun saveOverlayOpacity(context: Context, value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_OVERLAY_OPACITY, clamped).apply()
    }

    fun getOverlayOpacity(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_OVERLAY_OPACITY, DEFAULT_OVERLAY_OPACITY)
    }

    // Convenience method for getting the Jukebox base URL (same as server URL)
    fun getJukeboxBaseUrl(context: Context): String {
        return getServerUrl(context)
    }
}
