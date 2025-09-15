package nl.mdworld.planck4.util.radiometadata

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class for fetching ICY metadata from radio streams.
 */
class IcyMetadataFetcher {

    /**
     * Fetch ICY metadata from radio stream headers.
     *
     * @param streamUrl The radio stream URL
     * @return Map of ICY metadata key-value pairs
     */
    suspend fun fetchICYMetadata(streamUrl: String): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val metadata = mutableMapOf<String, String>()

            try {
                val url = URL(streamUrl)
                val connection = url.openConnection() as HttpURLConnection

                // Request ICY metadata
                connection.setRequestProperty("Icy-MetaData", "1")
                connection.setRequestProperty("User-Agent", "Planck Radio Player")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                connection.connect()

                // Extract ICY headers
                connection.headerFields.forEach { (key, values) ->
                    if (key?.startsWith("icy-", ignoreCase = true) == true && values.isNotEmpty()) {
                        val cleanKey = key.removePrefix("icy-").lowercase()
                        metadata[cleanKey] = values.first()
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {
                println("IcyMetadataFetcher: Error fetching ICY metadata: ${e.message}")
            }

            metadata
        }
    }
}
