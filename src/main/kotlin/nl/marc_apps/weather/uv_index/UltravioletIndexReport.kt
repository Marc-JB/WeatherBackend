package nl.marc_apps.weather.uv_index

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import nl.marc_apps.weather.DataRepresentation
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.div

class UltravioletIndexReport : KoinComponent {
    private val jsonSerialization by inject<Json>()

    private val protoBufSerialization by inject<ProtoBuf>()

    private val DUTCH = Locale.Builder().setLanguage("nl").setRegion("NL").build()

    suspend fun generate(rootDir: Path, fileNames: Collection<String>): Collection<String> {
        val reportPath = rootDir / "ultraviolet_index.json"
        val reportFile = File(reportPath.toUri())
        val reportPathProto = rootDir / "ultraviolet_index.proto.bin"
        val reportFileProto = File(reportPathProto.toUri())
        val data = mutableListOf<UltravioletIndex>()

        if ("zonkracht.txt" in fileNames) {
            val file = File((rootDir / "zonkracht.txt").toUri())
            withContext(Dispatchers.IO) {
                file.bufferedReader().use {
                    val dateLine = it.readLine()
                    val localDate = LocalDate.parse(dateLine, DateTimeFormatter.ofPattern("yyyyMMdd", DUTCH))
                    var currentDate = localDate
                    it.forEachLine {
                        val (indexWhenSunny, indexWhenCloudy) = it.trim().split(' ')
                        data += UltravioletIndex(
                            Date.from(currentDate.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC)),
                            indexWhenCloudy = indexWhenCloudy.toDouble(),
                            indexWhenSunny = indexWhenSunny.toDouble()
                        )
                        currentDate = currentDate.plusDays(1)
                    }
                }
            }
        } else if ("zonkracht_who.txt" in fileNames) {
            val file = File((rootDir / "zonkracht_who.txt").toUri())
            withContext(Dispatchers.IO) {
                file.bufferedReader().use {
                    val dateLine = it.readLine()
                    val localDate = LocalDate.parse(dateLine, DateTimeFormatter.ofPattern("yyyyMMdd", DUTCH))
                    var currentDate = localDate
                    it.forEachLine {
                        val (indexWhenSunny, indexWhenCloudy) = it.trim().split(' ')
                        data += UltravioletIndex(
                            Date.from(currentDate.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC)),
                            indexWhenCloudy = indexWhenCloudy.toInt().toDouble(),
                            indexWhenSunny = indexWhenSunny.toInt().toDouble()
                        )
                        currentDate = currentDate.plusDays(1)
                    }
                }
            }
        }

        withContext(Dispatchers.IO) {
            reportFile.writeText(jsonSerialization.encodeToString(DataRepresentation(data = data)))

            reportFileProto.writeBytes(protoBufSerialization.encodeToByteArray(DataRepresentation(data = data)))

            val filesToRemove = listOf("zonkracht.txt", "zonkracht_who.txt", "zonkrachtverwachting.xml", "zonkrachtverwachting_who.xml")
            for (fileToRemove in filesToRemove) {
                val file = File((rootDir / fileToRemove).toUri())
                file.delete()
            }
        }

        return fileNames - "zonkracht.txt" - "zonkracht_who.txt" - "zonkrachtverwachting.xml" - "zonkrachtverwachting_who.xml" + "ultraviolet_index.json"
    }
}