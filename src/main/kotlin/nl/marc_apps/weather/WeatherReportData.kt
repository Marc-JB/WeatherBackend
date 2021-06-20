package nl.marc_apps.weather

import kotlinx.serialization.Serializable
import nl.marc_apps.weather.serialization.DateSerializer
import java.util.*

@Serializable
data class WeatherReportData(
    val files: Collection<String>,
    @Serializable(with = DateSerializer::class)
    val timestamp: Date = Date()
)
