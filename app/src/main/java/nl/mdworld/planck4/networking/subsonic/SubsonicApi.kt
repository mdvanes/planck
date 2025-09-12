package nl.mdworld.planck4.networking.subsonic

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.networking.ktorHttpClient
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder

class SubsonicApi {

    private fun areCredentialsValid(context: Context): Boolean {
        val apiToken = SettingsManager.getApiToken(context)
        val salt = SettingsManager.getSalt(context)
        val username = SettingsManager.getUsername(context)
        return !(apiToken == SettingsManager.DEFAULT_API_TOKEN || salt == SettingsManager.DEFAULT_SALT || salt.isEmpty() || username.isEmpty())
    }

    suspend fun getPlaylistsKtor(context: Context): SubsonicPlaylistsResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyPlaylistsResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildPlaylistsUrl(context))
            .body()
    }

    suspend fun getPlaylistKtor(
        context: Context,
        id: String
    ): SubsonicPlaylistDetailResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyPlaylistResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildPlaylistUrl(context, id))
            .body()
    }

    suspend fun getArtistsKtor(context: Context): SubsonicArtistsResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyArtistsResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildArtistsUrl(context))
            .body()
    }

    suspend fun getArtistKtor(context: Context, id: String): SubsonicAlbumsResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyAlbumsResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildArtistUrl(context, id))
            .body()
    }

    suspend fun getAlbumKtor(context: Context, id: String): SubsonicAlbumDetailResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyAlbumResponse()
        }

        return ktorHttpClient.get(SubsonicUrlBuilder.buildAlbumUrl(context, id))
            .body()
    }
}
