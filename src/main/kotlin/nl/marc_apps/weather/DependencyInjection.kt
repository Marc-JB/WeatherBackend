package nl.marc_apps.weather

import nl.marc_apps.weather.serialization.JsonSerialization
import nl.marc_apps.weather.serialization.ProtoBufSerialization
import nl.marc_apps.weather.serialization.XmlSerialization
import org.koin.dsl.module

object DependencyInjection {
    val serializationModule = module {
        single {
            JsonSerialization.serializer
        }

        single {
            XmlSerialization.serializer
        }

        single {
            ProtoBufSerialization.serializer
        }
    }
}
