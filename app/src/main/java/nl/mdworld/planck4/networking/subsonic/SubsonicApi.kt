package nl.mdworld.planck4.networking.subsonic

import android.content.Context
import io.ktor.client.call.body
import io.ktor.client.request.get
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.networking.ktorHttpClient

class SubsonicApi {

    private fun areCredentialsValid(context: Context): Boolean {
        val username = SettingsManager.getUsername(context)
        val password = SettingsManager.getPassword(context)
        return password != SettingsManager.DEFAULT_PASSWORD && !password.isEmpty() && username != SettingsManager.DEFAULT_USERNAME && !username.isEmpty()
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

    suspend fun getIndexesKtor(context: Context): SubsonicIndexesResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyIndexesResponse()
        }
        return ktorHttpClient.get(SubsonicUrlBuilder.buildIndexesUrl(context)).body()
    }

    suspend fun getMusicDirectoryKtor(context: Context, id: String): SubsonicMusicDirectoryResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyMusicDirectoryResponse(id)
        }
        return ktorHttpClient.get(SubsonicUrlBuilder.buildMusicDirectoryUrl(context, id)).body()
    }
}
