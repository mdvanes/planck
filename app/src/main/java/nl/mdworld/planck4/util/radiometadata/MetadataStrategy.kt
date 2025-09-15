package nl.mdworld.planck4.util.radiometadata

/**
 * Strategy interface for fetching radio metadata from different sources.
 */
interface MetadataStrategy {
    /**
     * Fetch metadata using this strategy.
     *
     * @param streamUrl The radio stream URL (may not be used by all strategies)
     * @return RadioMetadata object or null if no metadata available
     */
    suspend fun fetchMetadata(streamUrl: String): RadioMetadata?
}

/**
 * Strategy for fetching metadata using the radio-metadata API (NPO2, Sky, etc.)
 */
class ApiMetadataStrategy(private val presetName: String) : MetadataStrategy {
    override suspend fun fetchMetadata(streamUrl: String): RadioMetadata? {
        return try {
            val tracks = RadioMetadataUtil.getRadioMetaData(presetName)
            tracks.firstOrNull() // Return the currently playing track
        } catch (e: Exception) {
            println("ApiMetadataStrategy: Error fetching metadata for $presetName: ${e.message}")
            null
        }
    }
}

/**
 * Strategy for fetching metadata using ICY protocol headers.
 */
class IcyMetadataStrategy : MetadataStrategy {
    private val icyFetcher = IcyMetadataFetcher()

    override suspend fun fetchMetadata(streamUrl: String): RadioMetadata? {
        return try {
            val icyData = icyFetcher.fetchICYMetadata(streamUrl)
            if (icyData.isNotEmpty()) {
                // Convert ICY metadata to RadioMetadata format
                val title = icyData["title"]
                val artist = icyData["artist"] ?: icyData["name"] // fallback to station name

                // If we have StreamTitle, try to parse it
                val streamTitle = icyData["streamtitle"]
                if (!streamTitle.isNullOrEmpty()) {
                    return RadioMetadataUtil.parseIcyMetadata("StreamTitle='$streamTitle';")
                }

                // Otherwise use individual fields
                RadioMetadata(
                    song = SongInfo(
                        artist = artist,
                        title = title ?: "Unknown"
                    )
                )
            } else {
                null
            }
        } catch (e: Exception) {
            println("IcyMetadataStrategy: Error fetching ICY metadata: ${e.message}")
            null
        }
    }
}
