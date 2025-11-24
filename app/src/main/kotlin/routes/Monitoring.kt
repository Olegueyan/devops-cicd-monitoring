package routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import plugins.Database

private val logger = LoggerFactory.getLogger("MonitoringRoutes")

fun Route.monitoringRoutes() {
    fun Route.monitoringRoutes() {
        get("/health") {
            val route = "/health"
            val method = "GET"

            try {
                Database.getConnection().use { _ ->
                    logger.info(
                        "[GET /health] Database connection OK",
                        kv("context", mapOf(
                            "route" to route,
                            "method" to method,
                            "db" to "connected",
                            "status" to "UP"
                        ))
                    )
                    call.respondText(
                        """{"status": "UP", "db": "connected"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.OK
                    )
                }
            } catch (e: Exception) {
                logger.error(
                    "[GET /health] DB connection failed",
                    kv("context", mapOf(
                        "route" to route,
                        "method" to method,
                        "db" to "error",
                        "status" to "DOWN",
                        "errorMessage" to (e.message ?: "unknown")
                    ))
                )
                call.respondText(
                    """{"status": "DOWN", "db": "error", "message": "${e.message}"}""",
                    ContentType.Application.Json,
                    HttpStatusCode.ServiceUnavailable
                )
            }
        }
    }
}
