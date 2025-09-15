package nl.mdworld.planck4.views.radio

import nl.mdworld.planck4.util.radiometadata.strategies.ApiMetadataStrategy
import nl.mdworld.planck4.util.radiometadata.strategies.IcyMetadataStrategy

/**
 * Factory for creating configured RadioMetadataManager instances.
 */
object RadioMetadataManagerFactory {

    /**
     * Create a RadioMetadataManager configured for NPO Radio stations.
     * Uses API-based metadata with ICY fallback.
     */
    fun createForNPORadio2(): RadioMetadataManager {
        return RadioMetadataManager().apply {
            addStrategy(ApiMetadataStrategy("npo2"))
            addStrategy(IcyMetadataStrategy())
        }
    }

    /**
     * Create a RadioMetadataManager configured for Sky Radio.
     * Uses API-based metadata with ICY fallback.
     */
    fun createForSkyRadio(): RadioMetadataManager {
        return RadioMetadataManager().apply {
            addStrategy(ApiMetadataStrategy("sky"))
            addStrategy(IcyMetadataStrategy())
        }
    }

    /**
     * Create a RadioMetadataManager for generic radio stations.
     * Uses only ICY metadata.
     */
    fun createGeneric(): RadioMetadataManager {
        return RadioMetadataManager().apply {
            addStrategy(IcyMetadataStrategy())
        }
    }

    /**
     * Create a RadioMetadataManager with a custom preset.
     * Uses API-based metadata with ICY fallback.
     *
     * @param presetName The preset name (e.g., "npo2", "sky")
     */
    fun createWithPreset(presetName: String): RadioMetadataManager {
        return RadioMetadataManager().apply {
            addStrategy(ApiMetadataStrategy(presetName))
            addStrategy(IcyMetadataStrategy())
        }
    }
}