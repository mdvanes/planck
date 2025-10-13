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
    private const val KEY_BROWSING_MODE = "browsing_mode"
    private const val KEY_FOLDER_COUNT_ENRICH = "folder_count_enrich"
    // Added: persistent playback context
    private const val KEY_LAST_SONG_ID = "last_song_id"
    private const val KEY_LAST_PLAYLIST_ID = "last_playlist_id"
    private const val KEY_LAST_FOLDER_ID = "last_folder_id"

    enum class BrowsingMode { TAGS, FILES }

    private const val DEFAULT_SERVER_URL = "https://example.com/rest/"
    const val DEFAULT_USERNAME = "demo_user"
    const val DEFAULT_PASSWORD = "demo_password"
    const val DEFAULT_RADIO_URL = "https://icecast.omroep.nl/radio2-bb-mp3"
    // New default overlay opacity (matches old hardcoded 0.8f)
    const val DEFAULT_OVERLAY_OPACITY = 0.8f
    private val DEFAULT_BROWSING_MODE = BrowsingMode.FILES

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

    // New browsing mode preference helpers
    fun saveBrowsingMode(context: Context, mode: BrowsingMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BROWSING_MODE, mode.name).apply()
    }

    fun getBrowsingMode(context: Context): BrowsingMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val value = prefs.getString(KEY_BROWSING_MODE, DEFAULT_BROWSING_MODE.name)
        return try { BrowsingMode.valueOf(value!!) } catch (e: Exception) { DEFAULT_BROWSING_MODE }
    }

    // New folder count enrichment preference helpers
    fun saveFolderCountEnrichmentEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FOLDER_COUNT_ENRICH, enabled).apply()
    }

    fun getFolderCountEnrichmentEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FOLDER_COUNT_ENRICH, false)
    }

    // Persistence for last played song and its context (playlist or folder)
    fun saveLastSongId(context: Context, songId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_SONG_ID, songId).apply()
    }

    fun getLastSongId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_SONG_ID, null)
    }

    fun saveLastPlaylistId(context: Context, playlistId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_PLAYLIST_ID, playlistId).apply()
    }

    fun getLastPlaylistId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_PLAYLIST_ID, null)
    }

    fun saveLastFolderId(context: Context, folderId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_FOLDER_ID, folderId).apply()
    }

    fun getLastFolderId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_FOLDER_ID, null)
    }

    // Convenience method for getting the Jukebox base URL (same as server URL)
    fun getJukeboxBaseUrl(context: Context): String {
        return getServerUrl(context)
    }
}
