// Application.kt

import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import plugins.configureSerialization
import routes.monitoringRoutes
import routes.userRoutes
import services.UserService

val logger: Logger = LoggerFactory.getLogger("Application");

fun main() {
    val appPort = System.getenv("APP_PORT")?.toInt() ?: 8080

    val userService = UserService()

    embeddedServer(Netty, port = appPort, host = "0.0.0.0") {
        configureSerialization()
        routing {
            monitoringRoutes()
            userRoutes(userService)

            // Fallback 404 pour toutes les routes non définies
            route("{...}") {
                handle {
                    val path = call.request.uri
                    logger.warn("[404] Route not found: $path")
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Route not found", "path" to path)
                    )
                }
            }
        }
    }.start(wait = true)
}
