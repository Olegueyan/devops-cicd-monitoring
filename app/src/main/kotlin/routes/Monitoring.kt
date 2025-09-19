package routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import plugins.Database

private val logger = LoggerFactory.getLogger("MonitoringRoutes")

fun Route.monitoringRoutes() {
    get("/health") {
        try {
            Database.getConnection().use { _ ->
                logger.info("[GET /health] Database connection OK")
                call.respondText(
                    """{"status": "UP", "db": "connected"}""",
                    ContentType.Application.Json,
                    HttpStatusCode.OK
                )
            }
        } catch (e: Exception) {
            logger.error("[GET /health] DB connection failed", e)
            call.respondText(
                """{"status": "DOWN", "db": "error", "message": "${e.message}"}""",
                ContentType.Application.Json,
                HttpStatusCode.ServiceUnavailable
            )
        }
    }
}
