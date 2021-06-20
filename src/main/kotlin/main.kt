import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import nl.marc_apps.weather.*
import nl.marc_apps.weather.station_data.WeatherStationReport
import nl.marc_apps.weather.uv_index.UltravioletIndexReport
import org.koin.core.context.startKoin
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.name

const val KNMI_FTP_HOST = "ftp.knmi.nl"

const val DEFAULT_OUTPUT_DIRECTORY = "out"

const val WEATHER_REPORT_FILE = "report"

suspend fun main(args: Array<String> = emptyArray()) {
    startKoin {
        modules(DependencyInjection.serializationModule)
    }

    val path = constructRootOutputPath(args.firstOrNull()?.removeSurrounding("\"") ?: DEFAULT_OUTPUT_DIRECTORY)

    if (canWriteToDirectory(path)) {
        SuspendingFtpClient.start(KNMI_FTP_HOST) {
            try {
                var fileNames = WeatherReportDownloader.getAndDownloadWeatherReport(it, path)
                fileNames = UltravioletIndexReport().generate(path, fileNames)
                fileNames = WeatherStationReport().generate(path, fileNames)
                createReport(fileNames, path)
            } catch (error: Throwable) {
                error.printStackTrace()
            }
        }
    } else {
        println("Can't write to directory \"${path.name}\"")
    }
}

private suspend fun createReport(fileNames: Collection<String>, outDir: Path) {
    val reportFile = File((outDir / "$WEATHER_REPORT_FILE.json").toUri())
    val reportFileProto = File((outDir / "$WEATHER_REPORT_FILE.proto.bin").toUri())

    withContext(Dispatchers.IO) {
        reportFile.writeText(Json {
            prettyPrint = true
        }.encodeToString(WeatherReportData(fileNames)))

        reportFileProto.writeBytes(ProtoBuf.encodeToByteArray(WeatherReportData(fileNames)))
    }
}

private suspend fun canWriteToDirectory(path: Path): Boolean {
    val rootDir = File(path.toUri())
    return withContext(Dispatchers.IO) {
        rootDir.exists() || rootDir.mkdirs()
    }
}

private fun constructRootOutputPath(outDir: String? = DEFAULT_OUTPUT_DIRECTORY): Path {
    val projectRootPath = Path(System.getProperty("user.dir"))
    return if (outDir == null) projectRootPath else projectRootPath / outDir
}
