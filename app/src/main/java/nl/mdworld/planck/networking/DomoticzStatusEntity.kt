package nl.mdworld.planck.networking

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DomoticzStatusEntity(
    @SerialName("version")
    val id: String,
    @SerialName("SystemName")
    val name: String,
    //@SerialName("currentRank")
    //val currentRank: Int,
    //@SerialName("totalStars")
    //val totalStars: Int,
    //@SerialName("totalWordsMastered")
    //val totalWordsMastered: Int,
)


