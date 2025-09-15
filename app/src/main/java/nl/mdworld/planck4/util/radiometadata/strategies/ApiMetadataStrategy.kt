package nl.mdworld.planck4.util.radiometadata.strategies

import nl.mdworld.planck4.util.radiometadata.RadioMetadata

/**
 * Strategy for fetching metadata using the radio-metadata API (NPO2, Sky, etc.)
 */
class ApiMetadataStrategy(private val presetName: String) : MetadataStrategy {
    override suspend fun fetchMetadata(streamUrl: String): RadioMetadata? {
        return try {
            val tracks = ApiMetadataUtil.getRadioMetaData(presetName)
            tracks.firstOrNull() // Return the currently playing track
        } catch (e: Exception) {
            println("ApiMetadataStrategy: Error fetching metadata for $presetName: ${e.message}")
            null
        }
    }
}