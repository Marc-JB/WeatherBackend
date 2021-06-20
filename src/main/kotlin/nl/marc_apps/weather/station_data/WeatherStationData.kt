package nl.marc_apps.weather.station_data

import kotlinx.serialization.Serializable

@Serializable
data class WeatherStationData(
    val name: String,
    val overcast: String?,
    val temperature: Double?,
    val humidity: Int?,
    val wind: Wind,
    val visibility: Int?,
    val airPressure: Double?
) {
    @Serializable
    data class Wind(
        val direction: WindDirection?,
        val strengthBeaufort: Int?
    )
}
