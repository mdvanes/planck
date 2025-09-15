package nl.mdworld.planck4.util.radiometadata.strategies

import nl.mdworld.planck4.util.radiometadata.RadioMetadata

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



