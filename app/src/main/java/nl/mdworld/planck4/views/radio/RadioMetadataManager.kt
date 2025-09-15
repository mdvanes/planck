package nl.mdworld.planck4.views.radio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.mdworld.planck4.util.radiometadata.RadioMetadata
import nl.mdworld.planck4.util.radiometadata.strategies.IcyMetadataStrategy
import nl.mdworld.planck4.util.radiometadata.strategies.MetadataStrategy

/**
 * Manager class for handling radio stream metadata extraction and monitoring.
 * Supports multiple metadata strategies including API-based and ICY protocol.
 */
class RadioMetadataManager {

    private var metadataJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val strategies = mutableListOf<MetadataStrategy>()

    /**
     * Add a metadata strategy to the manager.
     * Strategies will be tried in the order they were added.
     *
     * @param strategy The metadata strategy to add
     */
    fun addStrategy(strategy: MetadataStrategy) {
        strategies.add(strategy)
    }

    /**
     * Start monitoring radio metadata from the given stream URL.
     * Updates will be provided through the callback every 5 seconds.
     *
     * @param streamUrl The radio stream URL to monitor
     * @param callback Callback for metadata updates and errors
     */
    fun startMonitoring(
        streamUrl: String,
        onSuccess: (RadioMetadata) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        stopMonitoring()

        metadataJob = scope.launch {
            while (isActive) {
                try {
                    val metadata = fetchRadioMetadata(streamUrl)
                    if (metadata != null) {
                        onSuccess(metadata)
                        logRadioMetadata(metadata)
                        delay(5000)
                    } else {
                        delay(10000)
                    }
                } catch (e: Exception) {
                    println("RadioMetadata: Error fetching metadata: ${e.message}")
                    onError(e)
                    delay(10000)
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
     * Fetch metadata using the configured strategies.
     * Tries each strategy in order until one succeeds.
     *
     * @param streamUrl The radio stream URL
     * @return RadioMetadata object or null if no strategy succeeded
     */
    private suspend fun fetchRadioMetadata(streamUrl: String): RadioMetadata? {
        if (strategies.isEmpty()) {
            // Default to ICY strategy if none configured
            strategies.add(IcyMetadataStrategy())
        }

        for (strategy in strategies) {
            try {
                val metadata = strategy.fetchMetadata(streamUrl)
                if (metadata != null) {
                    return metadata
                }
            } catch (e: Exception) {
                println("RadioMetadata: Strategy ${strategy::class.simpleName} failed: ${e.message}")
                continue
            }
        }

        return null
    }

    /**
     * Log radio metadata to console in a formatted way.
     *
     * @param metadata The metadata to log
     */
    private fun logRadioMetadata(metadata: RadioMetadata) {
        println("=== Radio Metadata Update ===")
        println("Song: ${metadata.song.artist ?: "Unknown"} - ${metadata.song.title ?: "Unknown"}")
        metadata.broadcast?.let { broadcast ->
            broadcast.title?.let { println("Show: $it") }
            broadcast.presenters?.let { println("Presenters: $it") }
        }
        metadata.time?.let { time ->
            time.start?.let { println("Started: $it") }
            time.end?.let { println("Ends: $it") }
        }
        metadata.song.imageUrl?.let { println("Image: $it") }
        metadata.song.listenUrl?.let { println("Listen: $it") }
        println("=============================")
    }
}