package nl.mdworld.planck4

import android.content.Context

object SettingsManager {
    private const val PREFS_NAME = "planck_prefs"
    private const val KEY_SERVER_URL = "server_url"
    private const val KEY_USERNAME = "username"
    private const val KEY_SALT = "salt"
    private const val KEY_API_TOKEN = "api_token"
    private const val KEY_RADIO_URL = "radio_url"

    private const val DEFAULT_SERVER_URL = "https://example.com/rest/"
    const val DEFAULT_USERNAME = "demo_user"
    const val DEFAULT_SALT = "demo_salt_123"
    const val DEFAULT_API_TOKEN = "demo_token_456789"
    const val DEFAULT_RADIO_URL = "https://icecast.omroep.nl/radio2-bb-mp3"

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

    fun saveSalt(context: Context, salt: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SALT, salt).apply()
    }

    fun getSalt(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SALT, DEFAULT_SALT) ?: DEFAULT_SALT
    }

    fun saveApiToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_TOKEN, token).apply()
    }

    fun getApiToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_TOKEN, DEFAULT_API_TOKEN) ?: DEFAULT_API_TOKEN
    }

    fun saveRadioUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_RADIO_URL, url).apply()
    }

    fun getRadioUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_RADIO_URL, DEFAULT_RADIO_URL) ?: DEFAULT_RADIO_URL
    }

    // Convenience method for getting the Jukebox base URL (same as server URL)
    fun getJukeboxBaseUrl(context: Context): String {
        return getServerUrl(context)
    }
}
