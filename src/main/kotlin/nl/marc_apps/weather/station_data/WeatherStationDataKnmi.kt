package nl.marc_apps.weather.station_data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherStationDataKnmi(
    val date: String,
    @SerialName("elementen")
    val elements: String,
    val stations: Collection<Station>
) {
    @Serializable
    data class Station(
        @SerialName("station")
        val name: String,
        val overcast: String,
        val temperature: String,
        val humidity: String,
        @SerialName("wind_direction")
        val windDirectionDutch: WindDirectionDutch? = null,
        @SerialName("wind_strength")
        val windStrengthBeaufort: String,
        val visibility: String,
        @SerialName("air_pressure")
        val airPressure: String
    )
}
