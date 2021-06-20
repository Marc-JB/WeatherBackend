package nl.marc_apps.weather.station_data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import nl.marc_apps.weather.DataRepresentation
import nl.marc_apps.weather.serialization.DateSerializer
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.div

object WeatherStationReport {
    private val DUTCH = Locale.Builder().setLanguage("nl").setRegion("NL").build()

    suspend fun generate(rootDir: Path, fileNames: Collection<String>): Collection<String> {
        val reportPath = rootDir / "weather_stations_data.json"
        val reportFile = File(reportPath.toUri())
        val reportPathProto = rootDir / "weather_stations_data.proto"
        val reportFileProto = File(reportPathProto.toUri())
        val data = mutableListOf<WeatherStationData>()

        if ("tabel_10Min_data.json" in fileNames) {
            val file = File((rootDir / "tabel_10Min_data.json").toUri())
            withContext(Dispatchers.IO) {
                val knmiData = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                }.decodeFromString<WeatherStationDataKnmi>(file.readText())
                for (station in knmiData.stations) {
                    data += WeatherStationData(
                        name = station.name,
                        overcast = station.overcast.ifBlank { null },
                        temperature = station.temperature.ifBlank { null }?.toDoubleOrNull(),
                        humidity = station.humidity.ifBlank { null }?.toIntOrNull(),
                        wind = WeatherStationData.Wind(
                            direction = station.windDirectionDutch?.english,
                            strengthBeaufort = station.windStrengthBeaufort.ifBlank { null }?.toIntOrNull()
                        ),
                        visibility = station.visibility.ifBlank { null }?.toIntOrNull(),
                        airPressure = station.airPressure.ifBlank { null }?.toDoubleOrNull()
                    )
                }
            }
        }

        withContext(Dispatchers.IO) {
            reportFile.writeText(Json {
                prettyPrint = true
            }.encodeToString(DataRepresentation(data = data)))

            reportFileProto.writeBytes(ProtoBuf.encodeToByteArray(DataRepresentation(data = data)))

            val filesToRemove = listOf("tabel_10Min_data.json", "tabel_10min_data.html")
            for (fileToRemove in filesToRemove) {
                val file = File((rootDir / fileToRemove).toUri())
                file.delete()
            }
        }

        return fileNames - "tabel_10Min_data.json" - "tabel_10min_data.html" + "weather_stations_data.json"
    }
}