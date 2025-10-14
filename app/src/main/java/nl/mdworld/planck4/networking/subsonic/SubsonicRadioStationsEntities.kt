package nl.mdworld.planck4.networking.subsonic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicInternetRadioStationsResponse(
    @SerialName("subsonic-response") val sr: SubsonicInternetRadioStationsResponse2
)

@Serializable
data class SubsonicInternetRadioStationsResponse2(
    @SerialName("internetRadioStations") val internetRadioStations: SubsonicInternetRadioStationsContainer? = null
)

@Serializable
data class SubsonicInternetRadioStationsContainer(
    @SerialName("internetRadioStation") val station: List<SubsonicRadioStation> = emptyList()
)

@Serializable
data class SubsonicRadioStation(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("streamUrl") val streamUrl: String? = null,
    @SerialName("homepageUrl") val homepageUrl: String? = null,
    @SerialName("bitrate") val bitrate: Int? = null
)

