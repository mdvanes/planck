package nl.mdworld.planck4.views.radio

import kotlinx.coroutines.*
import nl.mdworld.planck4.util.radiometadata.RadioMetadataUtil

/**
 * Manager class for handling radio stream metadata extraction and monitoring.
 * Supports ICY metadata protocol and MediaMetadataRetriever fallback.
 */
class RadioMetadataManager {

    private var metadataJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Start monitoring radio metadata from the given stream URL.
     * Updates will be provided through the callback every 5 seconds.
     *
     * @param streamUrl The radio stream URL to monitor
     * @param onMetadataUpdate Callback invoked when new metadata is available
     */
    fun startMonitoring(
        streamUrl: String,
        onMetadataUpdate: (Map<String, String>) -> Unit
    ) {
        stopMonitoring()

        metadataJob = scope.launch {
            while (isActive) {
                try {
                    // Fetch metadata from the stream
                    val metadata = fetchRadioMetadata(streamUrl)
                    if (metadata.isNotEmpty()) {
                        onMetadataUpdate(metadata)
                        logRadioMetadata(metadata)
                    }
                    delay(5000) // Check for metadata updates every 5 seconds
                } catch (e: Exception) {
                    println("RadioMetadata: Error fetching metadata: ${e.message}")
                    delay(10000) // Wait longer on error
                }
            }
        }
    }

    /**
     * Stop monitoring radio metadata.
     */
    fun stopMonitoring() {
        metadataJob?.cancel()
        metadataJob = null
    }

    /**
     * Clean up resources and stop all monitoring.
     */
    fun cleanup() {
        stopMonitoring()
        scope.cancel()
    }

    /**
     * Fetch metadata from the radio stream using ICY protocol and MediaMetadataRetriever fallback.
     *
     * @param streamUrl The radio stream URL
     * @return Map of metadata key-value pairs
     */
    private suspend fun fetchRadioMetadata(streamUrl: String): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val metadata = mutableMapOf<String, String>()

            try {
                // Try to fetch ICY metadata using HTTP headers first
                val icyMetadata = fetchICYMetadata(streamUrl)
                println("RadioMetadata: Fetched ICY metadata: $icyMetadata")
                metadata.putAll(icyMetadata)

                val tracks = RadioMetadataUtil.getRadioMetaData("npo2")
                println("RadioMetadata: Fetched RadioMetadataUtil metadata: $tracks")

            } catch (e: Exception) {
                println("RadioMetadata: Error with ICY metadata: ${e.message}")

                // Fallback: try MediaMetadataRetriever (may not work well with live streams)
                //try {
                //    val retriever = android.media.MediaMetadataRetriever()
                //    retriever.setDataSource(streamUrl, HashMap<String, String>())
                //
                //    // Extract available metadata
                //    retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                //        metadata["title"] = it
                //    }
                //    retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)?.let {
                //        metadata["artist"] = it
                //    }
                //    retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM)?.let {
                //        metadata["album"] = it
                //    }
                //    retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_GENRE)?.let {
                //        metadata["genre"] = it
                //    }
                //
                //    retriever.release()
                //
                //} catch (e2: Exception) {
                //    println("RadioMetadata: Error with MediaMetadataRetriever: ${e2.message}")
                //}
            }

            metadata
        }
    }

    /**
     * Fetch ICY metadata from radio stream headers.
     *
     * @param streamUrl The radio stream URL
     * @return Map of ICY metadata key-value pairs
     */
    private suspend fun fetchICYMetadata(streamUrl: String): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val metadata = mutableMapOf<String, String>()

            try {
                val url = java.net.URL(streamUrl)
                val connection = url.openConnection() as java.net.HttpURLConnection

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
                println("RadioMetadata: Error fetching ICY metadata: ${e.message}")
            }

            metadata
        }
    }

    /**
     * Log radio metadata to console in a formatted way.
     *
     * @param metadata The metadata map to log
     */
    private fun logRadioMetadata(metadata: Map<String, String>) {
        println("=== Radio Metadata Update ===")
        if (metadata.isEmpty()) {
            println("No metadata available")
        } else {
            metadata.forEach { (key, value) ->
                println("$key: $value")
            }
        }
        println("=============================")
    }
}
