package nl.marc_apps.weather

import it.sauronsoftware.ftp4j.FTPFile
import java.io.File
import java.nio.file.Path
import kotlin.io.path.div

object WeatherReportDownloader {
    private const val WEATHER_REPORTS_DIRECTORY_NAME = "pub_weerberichten"

    private const val PDF_FILE_EXTENSION = ".pdf"

    private const val SVG_FILE_EXTENSION = ".svg"

    suspend fun getAndDownloadWeatherReport(client: SuspendingFtpClient, outDir: Path): Collection<String> {
        val hasWeatherReportsDir = client.getDirectoryContents().any {
            it.name == WEATHER_REPORTS_DIRECTORY_NAME && it.type == FTPFile.TYPE_DIRECTORY
        }

        if (hasWeatherReportsDir) {
            client.changeDirectory(WEATHER_REPORTS_DIRECTORY_NAME)
            return downloadWeatherReport(client, outDir)
        }

        return emptyList()
    }

    private suspend fun downloadWeatherReport(client: SuspendingFtpClient, outDir: Path): Collection<String> {
        val downloadedFiles = mutableListOf<String>()
        val weatherReports = client.getDirectoryContents()

        var downloadedFileCount = 0u
        var errorCount = 0u
        var skippedFilesCount = 0u

        for (file in weatherReports) {
            if (shouldDownloadFile(file)) {
                val output = File((outDir / file.name).toUri())
                try {
                    client.downloadFile(file, output)
                    downloadedFiles += file.name
                    downloadedFileCount++
                } catch (error: Throwable) {
                    error.printStackTrace()
                    errorCount++
                }
            } else {
                skippedFilesCount++
            }
        }

        println("Downloaded $downloadedFileCount files, skipped $skippedFilesCount files, $errorCount errors")

        return downloadedFiles
    }

    private fun shouldDownloadFile(file: FTPFile): Boolean {
        return file.type == FTPFile.TYPE_FILE &&
                !file.name.startsWith("KNMI_expertpluim_") &&
                !file.name.endsWith(PDF_FILE_EXTENSION) &&
                !file.name.endsWith(SVG_FILE_EXTENSION) &&
                "scheepvaart" !in file.name &&
                "guidance" !in file.name &&
                "K13" !in file.name
    }
}
