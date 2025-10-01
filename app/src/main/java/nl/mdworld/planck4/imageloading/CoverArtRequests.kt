package nl.mdworld.planck4.imageloading

import android.content.Context
import coil.request.ImageRequest
import nl.mdworld.planck4.networking.subsonic.SubsonicUrlBuilder

/**
 * Utilities to build stable cover art requests whose cache keys ignore volatile auth tokens.
 */
object CoverArtRequests {
    fun buildCoverArtRequest(context: Context, coverArtId: String): ImageRequest {
        val url = SubsonicUrlBuilder.buildCoverArtUrl(context, coverArtId)
        val key = "coverArt:$coverArtId"
        return ImageRequest.Builder(context)
            .data(url)
            .diskCacheKey(key)
            .memoryCacheKey(key)
            .build()
    }
}

