package plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }
}
