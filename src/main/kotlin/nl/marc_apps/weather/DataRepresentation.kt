package nl.marc_apps.weather

import kotlinx.serialization.Serializable
import nl.marc_apps.weather.serialization.DateSerializer
import java.util.*

@Serializable
data class DataRepresentation<T>(
    @Serializable(with = DateSerializer::class)
    val generatedAt: Date = Date(),
    val data: Collection<T>
)
