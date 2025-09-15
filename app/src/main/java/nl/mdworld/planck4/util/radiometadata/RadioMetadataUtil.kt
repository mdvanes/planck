package nl.mdworld.planck4.util.radiometadata

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.*
import nl.mdworld.planck4.util.radiometadata.presets.NPO2_PRESET
import nl.mdworld.planck4.util.radiometadata.presets.SKY_PRESET
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data class for parsed radio metadata - matches the TypeScript RadioMetadata interface
 */
data class RadioMetadata(
    val time: TimeInfo? = null,
    val broadcast: BroadcastInfo? = null,
    val song: SongInfo
)

data class TimeInfo(
    val start: String? = null,
    val end: String? = null
)

data class BroadcastInfo(
    val title: String? = null,
    val presenters: String? = null,
    val imageUrl: String? = null
)

data class SongInfo(
    val artist: String? = null,
    val title: String? = null,
    val imageUrl: String? = null,
    val listenUrl: String? = null
)

/**
 * Data class for URL configuration in radio schemas
 */
data class UrlConfig(
    val name: String,
    val url: String,
    val headers: Map<String, String>? = null
)

/**
 * Type alias for pick paths (array of strings and integers)
 */
typealias PickPath = List<Any>

/**
 * Data class for radio station schema paths
 */
data class SchemaPaths(
    val tracks: PickPath,
    val time: TimeInfo? = null,
    val broadcast: BroadcastInfo? = null,
    val song: SongInfo
) {
    data class TimeInfo(
        val start: PickPath? = null,
        val end: PickPath? = null
    )

    data class BroadcastInfo(
        val title: PickPath? = null,
        val presenters: PickPath? = null,
        val imageUrl: PickPath? = null
    )

    data class SongInfo(
        val artist: PickPath? = null,
        val title: PickPath,
        val imageUrl: PickPath? = null,
        val listenUrl: PickPath? = null
    )
}

/**
 * Data class for radio station schema - matches the TypeScript RadioSchema interface
 */
data class RadioSchema(
    val name: String,
    val urls: List<UrlConfig>,
    val paths: SchemaPaths
)

/**
 * Data class for radio station presets (kept for compatibility)
 */
data class RadioPreset(
    val name: String,
    val url: String,
    val logo: String? = null,
    val homepage: String? = null
)

object RadioMetadataUtil {

    /**
     * Available presets map - matches the TypeScript configMap
     */
    val PRESETS = mapOf(
        "npo2" to NPO2_PRESET,
        "sky" to SKY_PRESET
    )

    /**
     * Main function to get radio metadata - matches the TypeScript getRadioMetaData function
     */
    suspend fun getRadioMetaData(config: Any): List<RadioMetadata> {
        val schema = when (config) {
            is String -> PRESETS[config] ?: throw IllegalArgumentException("No schema found for config $config")
            is RadioSchema -> config
            else -> throw IllegalArgumentException("Config must be a string preset name or RadioSchema object")
        }

        return getRadioMetaDataBySchema(schema)
    }

    /**
     * Get radio metadata by schema - matches the TypeScript getRadioMetaDataBySchema function
     */
    private suspend fun getRadioMetaDataBySchema(schema: RadioSchema): List<RadioMetadata> {
        if (!isValidSchema(schema)) {
            return emptyList()
        }

        return coroutineScope {
            // Fetch all URLs concurrently
            val responses = schema.urls.map { urlConfig ->
                async {
                    urlConfig.name to fetchJson(urlConfig.url, urlConfig.headers)
                }
            }.awaitAll().toMap()

            // Extract tracks using the schema paths
            val tracks = pickFrom(responses, schema.paths.tracks) as? JsonArray ?: return@coroutineScope emptyList()

            // Process each track
            tracks.mapNotNull { trackElement ->
                try {
                    val track = trackElement.jsonObject

                    RadioMetadata(
                        time = TimeInfo(
                            start = pickFromAsString(track, schema.paths.time?.start),
                            end = pickFromAsString(track, schema.paths.time?.end)
                        ),
                        broadcast = BroadcastInfo(
                            title = pickFromAsString(responses, schema.paths.broadcast?.title),
                            presenters = pickFromAsString(responses, schema.paths.broadcast?.presenters),
                            imageUrl = pickFromAsString(responses, schema.paths.broadcast?.imageUrl)
                        ),
                        song = SongInfo(
                            artist = pickFromAsString(track, schema.paths.song.artist),
                            title = pickFromAsString(track, schema.paths.song.title) ?: "",
                            imageUrl = pickFromAsString(track, schema.paths.song.imageUrl),
                            listenUrl = pickFromAsString(track, schema.paths.song.listenUrl)
                        )
                    )
                } catch (_: Exception) {
                    null // Skip invalid tracks
                }
            }
        }
    }

    /**
     * Check if schema is valid - matches the TypeScript isValidSchema function
     */
    private fun isValidSchema(schema: RadioSchema): Boolean {
        return schema.name.isNotEmpty() &&
                schema.urls.isNotEmpty() &&
                schema.urls.all { it.url.isNotEmpty() && it.name.isNotEmpty() } &&
                schema.paths.tracks.isNotEmpty()
    }

    /**
     * Fetch JSON from URL with optional headers
     */
    private fun fetchJson(url: String, headers: Map<String, String>?): JsonElement {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            headers?.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Json.parseToJsonElement(response)
        } catch (_: Exception) {
            JsonObject(emptyMap())
        }
    }

    /**
     * Pick value from JSON using path - matches the TypeScript pickFrom function
     */
    private fun pickFrom(data: Any?, path: PickPath?): Any? {
        if (path == null || data == null) return null

        return path.fold(data as Any?) { acc, next ->
            when {
                acc == null -> null
                acc is Map<*, *> && next is String -> acc[next]
                acc is JsonObject && next is String -> acc[next]
                acc is List<*> && next is Int && next < acc.size -> acc[next]
                acc is JsonArray && next is Int && next < acc.size -> acc[next]
                else -> null
            }
        }
    }

    /**
     * Pick value from JSON and convert to string
     */
    private fun pickFromAsString(data: Any?, path: PickPath?): String? {
        return when (val result = pickFrom(data, path)) {
            is JsonPrimitive -> result.contentOrNull
            is String -> result
            else -> result?.toString()
        }
    }

    /**
     * Legacy function for ICY metadata parsing (kept for backward compatibility)
     */
    @Deprecated("Use getRadioMetaData instead for better metadata support")
    fun parseIcyMetadata(icyString: String): RadioMetadata {
        val regex = Regex("StreamTitle='(.*?)';")
        val match = regex.find(icyString)
        val streamTitle = match?.groups?.get(1)?.value ?: icyString
        val split = streamTitle.split(" - ", limit = 2)
        val artist = if (split.size == 2) split[0].trim().ifEmpty { null } else null
        val title = if (split.size == 2) split[1].trim().ifEmpty { null } else streamTitle.trim().ifEmpty { null }

        return RadioMetadata(
            song = SongInfo(artist = artist, title = title)
        )
    }

    /**
     * List of radio station presets (kept for compatibility with existing code)
     */
    val RADIO_PRESETS = listOf(
        RadioPreset(
            name = "NPO Radio 2",
            url = "https://icecast.omroep.nl/radio2-bb-mp3",
            logo = "npo_radio_2",
            homepage = "https://www.nporadio2.nl"
        ),
        RadioPreset(
            name = "NPO Radio 1",
            url = "https://icecast.omroep.nl/radio1-bb-mp3",
            logo = "npo_radio_1",
            homepage = "https://www.nporadio1.nl"
        ),
        RadioPreset(
            name = "NPO 3FM",
            url = "https://icecast.omroep.nl/3fm-bb-mp3",
            logo = "npo_3fm",
            homepage = "https://www.npo3fm.nl"
        ),
        RadioPreset(
            name = "NPO Radio 4",
            url = "https://icecast.omroep.nl/radio4-bb-mp3",
            logo = "npo_radio_4"
        ),
        RadioPreset(
            name = "NPO Radio 5",
            url = "https://icecast.omroep.nl/radio5-bb-mp3",
            logo = "npo_radio_5"
        ),
        RadioPreset(
            name = "NPO FunX",
            url = "https://icecast.omroep.nl/funx-bb-mp3",
            logo = "npo_funx",
            homepage = "https://www.funx.nl"
        )
    )
}
