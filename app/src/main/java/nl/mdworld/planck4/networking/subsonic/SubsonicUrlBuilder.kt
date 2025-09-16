package nl.mdworld.planck4.networking.subsonic

import android.content.Context
import nl.mdworld.planck4.SettingsManager
import java.security.MessageDigest
import java.util.UUID

object SubsonicUrlBuilder {
    private const val API_VERSION = "1.16.0"
    private const val FORMAT = "json"
    private const val PLAYER_NAME = "PlanckApp"

    /**
     * Builds the API configuration string with authentication parameters
     */
    fun buildApiConfig(context: Context): String {
        val salt = UUID.randomUUID().toString().replace("-", "").take(12)
        val passAndSalt = "${SettingsManager.getPassword(context)}$salt"
        val md = MessageDigest.getInstance("MD5")
        val hashBytes = md.digest(passAndSalt.toByteArray())
        val token = hashBytes.joinToString("") { "%02x".format(it) }
        return "?u=${SettingsManager.getUsername(context)}&t=${token}&s=${salt}&v=$API_VERSION&c=$PLAYER_NAME&f=$FORMAT"
    }

    /**
     * Builds a complete API URL for the given endpoint
     */
    fun buildApiUrl(context: Context, endpoint: String, additionalParams: String = ""): String {
        val baseUrl = SettingsManager.getJukeboxBaseUrl(context)
        val apiConfig = buildApiConfig(context)
        return "$baseUrl$endpoint$apiConfig$additionalParams"
    }

    /**
     * Builds a cover art URL for the given cover art ID
     */
    fun buildCoverArtUrl(context: Context, coverArtId: String): String {
        return buildApiUrl(context, "getCoverArt", "&id=$coverArtId")
    }

    /**
     * Builds a stream URL for the given song ID
     */
    fun buildStreamUrl(context: Context, songId: String): String {
        return buildApiUrl(context, "stream", "&id=$songId")
    }

    /**
     * Builds a playlists URL
     */
    fun buildPlaylistsUrl(context: Context): String {
        return buildApiUrl(context, "getPlaylists")
    }

    /**
     * Builds a playlist detail URL for the given playlist ID
     */
    fun buildPlaylistUrl(context: Context, playlistId: String): String {
        return buildApiUrl(context, "getPlaylist", "&id=$playlistId")
    }

    /**
     * Builds an artists URL
     */
    fun buildArtistsUrl(context: Context): String {
        return buildApiUrl(context, "getArtists")
    }

    /**
     * Builds an artist detail URL for the given artist ID
     */
    fun buildArtistUrl(context: Context, artistId: String): String {
        return buildApiUrl(context, "getArtist", "&id=$artistId")
    }

    /**
     * Builds an album detail URL for the given album ID
     */
    fun buildAlbumUrl(context: Context, albumId: String): String {
        return buildApiUrl(context, "getAlbum", "&id=$albumId")
    }
}