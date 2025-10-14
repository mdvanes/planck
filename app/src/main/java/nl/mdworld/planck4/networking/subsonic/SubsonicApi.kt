package nl.mdworld.planck4.networking.subsonic

import android.content.Context
import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import nl.mdworld.planck4.SettingsManager
import nl.mdworld.planck4.networking.ktorHttpClient

class SubsonicApi {

    private fun areCredentialsValid(context: Context): Boolean {
        val username = SettingsManager.getUsername(context)
        val password = SettingsManager.getPassword(context)
        return password != SettingsManager.DEFAULT_PASSWORD && username != SettingsManager.DEFAULT_USERNAME && password.isNotEmpty() && username.isNotEmpty()
    }

    suspend fun getPlaylistsKtor(context: Context): SubsonicPlaylistsResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyPlaylistsResponse()
        }
        return ktorHttpClient.get(SubsonicUrlBuilder.buildPlaylistsUrl(context)).body()
    }

    suspend fun getPlaylistKtor(context: Context, id: String): SubsonicPlaylistDetailResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyPlaylistResponse()
        }
        return ktorHttpClient.get(SubsonicUrlBuilder.buildPlaylistUrl(context, id)).body()
    }

    suspend fun getArtistsKtor(context: Context): SubsonicArtistsResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyArtistsResponse()
        }
        return ktorHttpClient.get(SubsonicUrlBuilder.buildArtistsUrl(context)).body()
    }

    suspend fun getArtistKtor(context: Context, id: String): SubsonicAlbumsResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyAlbumsResponse()
        }
        return ktorHttpClient.get(SubsonicUrlBuilder.buildArtistUrl(context, id)).body()
    }

    suspend fun getAlbumKtor(context: Context, id: String): SubsonicAlbumDetailResponse {
        if (!areCredentialsValid(context)) {
            return SubsonicDummyResponses.createDummyAlbumResponse()
        }
        return ktorHttpClient.get(SubsonicUrlBuilder.buildAlbumUrl(context, id)).body()
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

    // New: internet radio stations with enhanced logging
    suspend fun getInternetRadioStationsKtor(context: Context): SubsonicInternetRadioStationsResponse {
        val tag = "SubsonicApi"
        if (!areCredentialsValid(context)) {
            Log.w(tag, "InternetRadio: Credentials invalid or default; returning dummy stations")
            return SubsonicDummyResponses.createDummyInternetRadioStationsResponse()
        }
        val url = SubsonicUrlBuilder.buildInternetRadioStationsUrl(context)
        // Avoid logging sensitive token & salt by trimming query after first '?'
        val base = url.substringBefore('?')
        Log.d(tag, "InternetRadio: Fetching stations from $url")
        return try {
            val httpResponse: HttpResponse = ktorHttpClient.get(url)
            Log.d(tag, "InternetRadio: HTTP status=${httpResponse.status.value}")
            val response: SubsonicInternetRadioStationsResponse = httpResponse.body()
            val count = response.sr.internetRadioStations?.station?.size ?: 0
            Log.d(tag, "InternetRadio: Received $count stations")
            response
        } catch (e: Exception) {
            Log.e(tag, "InternetRadio: Error fetching stations: ${e.message}", e)
            throw e
        }
    }
}
