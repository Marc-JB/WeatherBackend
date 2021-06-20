package nl.marc_apps.weather.serialization

import kotlinx.serialization.json.Json

object JsonSerialization {
    val serializer = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
}
