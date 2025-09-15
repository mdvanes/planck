package nl.mdworld.planck4.util.radiometadata.presets

import nl.mdworld.planck4.util.radiometadata.RadioSchema
import nl.mdworld.planck4.util.radiometadata.SchemaPaths
import nl.mdworld.planck4.util.radiometadata.UrlConfig

/**
 * NPO Radio 2 preset configuration.
 * Fetches metadata from NPO Radio 2's API endpoints.
 */
val NPO2_PRESET = RadioSchema(
    name = "npo2",
    urls = listOf(
        UrlConfig(
            "tracks_",
            "https://www.nporadio2.nl/api/tracks",
            //"https://jsonplaceholder.typicode.com/users",
            headers = mapOf("User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:142.0) Gecko/20100101 Firefox/142.0")
        ),
        UrlConfig("broadcasts_", "https://www.nporadio2.nl/api/broadcasts")
    ),
    paths = SchemaPaths(
        tracks = listOf("tracks_", "data"),
        broadcast = SchemaPaths.BroadcastInfo(
            title = listOf("broadcasts_", "data", 0, "title"),
            presenters = listOf("broadcasts_", "data", 0, "presenters"),
            imageUrl = listOf("broadcasts_", "data", 0, "image_url_400x400")
        ),
        time = SchemaPaths.TimeInfo(
            start = listOf("startdatetime"),
            end = listOf("enddatetime")
        ),
        song = SchemaPaths.SongInfo(
            artist = listOf("artist"),
            title = listOf("title"),
            imageUrl = listOf("image_url_400x400"),
            listenUrl = listOf("spotify_url")
        )
    )
)
