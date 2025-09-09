package nl.mdworld.planck4

import android.content.Context

object SettingsManager {
    private const val PREFS_NAME = "planck_prefs"
    private const val KEY_SERVER_URL = "server_url"
    private const val DEFAULT_SERVER_URL = "https://example.com/rest/"

    fun saveServerUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SERVER_URL, url).apply()
    }

    fun getServerUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    // Convenience method for getting the Jukebox base URL (same as server URL)
    fun getJukeboxBaseUrl(context: Context): String {
        return getServerUrl(context)
    }
}
