package nl.marc_apps.weather

import it.sauronsoftware.ftp4j.FTPClient
import it.sauronsoftware.ftp4j.FTPFile
import kotlinx.coroutines.*
import java.io.File

class SuspendingFtpClient : AutoCloseable {
    private var actualFtpClient: FTPClient? = null

    val isConnected: Boolean
        get() = actualFtpClient?.isConnected == true

    private suspend inline fun <R> runFtpTask(
        client: FTPClient? = actualFtpClient,
        requiresConnection: Boolean = true,
        crossinline block: (FTPClient) -> R
    ): R {
        return if (client != null && (client.isConnected || !requiresConnection)) {
            withContext(Dispatchers.IO) {
                try {
                    Result.success(block(client))
                } catch (e: Throwable) {
                    Result.failure(e)
                }
            }.getOrThrow()
        } else throw NoConnectionError()
    }

    suspend fun connect(host: String, port: UInt? = null) {
        if (!isConnected) {
            val ftpClient = FTPClient()
            runFtpTask(client = ftpClient, requiresConnection = false) {
                if (port == null) {
                    it.connect(host)
                } else {
                    it.connect(host, port.toInt())
                }
                it.login("anonymous", "anonymous")
            }
            ftpClient.type = FTPClient.TYPE_BINARY
            actualFtpClient = ftpClient
        }
    }

    suspend fun getCurrentDirectoryPath(): String {
        return runFtpTask {
            it.currentDirectory()
        }
    }

    suspend fun changeDirectory(newPath: String) {
        runFtpTask {
            it.changeDirectory(newPath)
        }
    }

    suspend fun changeDirectoryUp() {
        runFtpTask {
            it.changeDirectoryUp()
        }
    }

    suspend fun getDirectoryContents(): List<FTPFile> {
        return runFtpTask {
            it.list().filterNotNull()
        }
    }

    suspend fun downloadFile(source: FTPFile, destination: File) {
        runFtpTask {
            it.download(source.name, destination)
        }
    }

    suspend fun disconnect() {
        runFtpTask {
            println("Disconnecting...")
            try {
                actualFtpClient?.disconnect(true)
            } catch (e: Throwable) {
                actualFtpClient?.disconnect(false)
            }
            println("Disconnected.")
        }

        actualFtpClient = null
    }

    override fun close() {
        MainScope().launch {
            disconnect()
        }
    }

    class NoConnectionError : Exception()

    companion object {
        suspend inline fun start(host: String, port: UInt? = null, block: (SuspendingFtpClient) -> Unit) {
            val client = SuspendingFtpClient()
            client.connect(host, port)
            block(client)
            client.disconnect()
        }
    }
}
