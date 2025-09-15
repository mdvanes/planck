package nl.mdworld.planck4.util.radiometadata.strategies

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import nl.mdworld.planck4.util.radiometadata.BroadcastInfo
import nl.mdworld.planck4.util.radiometadata.PickPath
import nl.mdworld.planck4.util.radiometadata.RadioMetadata
import nl.mdworld.planck4.util.radiometadata.RadioSchema
import nl.mdworld.planck4.util.radiometadata.SongInfo
import nl.mdworld.planck4.util.radiometadata.TimeInfo
import nl.mdworld.planck4.util.radiometadata.presets.NPO2_PRESET
import nl.mdworld.planck4.util.radiometadata.presets.SKY_PRESET
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.fold


object ApiMetadataUtil {

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
            is String -> PRESETS[config]
                ?: throw IllegalArgumentException("No schema found for config $config")

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
            val tracks = pickFrom(responses, schema.paths.tracks) as? JsonArray
                ?: return@coroutineScope emptyList()

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
                            presenters = pickFromAsString(
                                responses,
                                schema.paths.broadcast?.presenters
                            ),
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


}