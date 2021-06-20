package nl.marc_apps.weather.uv_index

import kotlinx.serialization.Serializable
import nl.marc_apps.weather.serialization.DateSerializer
import java.util.*

@Serializable
data class UltravioletIndex(
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val indexWhenCloudy: Double,
    val indexWhenSunny: Double
)
