package routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import requests.UserCreateRequest
import requests.UserUpdateRequest
import services.UserService
import java.util.*

private val logger = LoggerFactory.getLogger("UserRoutes")

fun Route.userRoutes(service: UserService) {

    route("/api/users") {

        // ============================
        // GET all users
        // ============================
        get {
            try {
                val users = service.listUsers()
                logger.info(
                    "[GET /api/users] Returned {} users",
                    users.size,
                    kv("context", mapOf(
                        "route" to "/api/users",
                        "method" to "GET",
                        "count" to users.size
                    ))
                )
                call.respond(HttpStatusCode.OK, users)
            } catch (e: Exception) {
                logger.error(
                    "[GET /api/users] Failed to list users",
                    kv("context", mapOf(
                        "route" to "/api/users",
                        "method" to "GET",
                        "result" to "error",
                        "errorMessage" to (e.message ?: "unknown")
                    ))
                )
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // ============================
        // GET user by UUID
        // ============================
        get("{uuid}") {
            val uuid = call.parameters["uuid"]?.let { UUID.fromString(it) }
            if (uuid == null) {
                logger.warn(
                    "[GET /api/users/{uuid}] Invalid UUID",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "GET",
                        "reason" to "invalid_uuid"
                    ))
                )
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID"))
                return@get
            }

            try {
                val user = service.getUser(uuid)
                if (user == null) {
                    logger.info(
                        "[GET /api/users/$uuid] User not found",
                        kv("context", mapOf(
                            "route" to "/api/users/{uuid}",
                            "method" to "GET",
                            "userId" to uuid.toString(),
                            "result" to "not_found"
                        ))
                    )
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                } else {
                    logger.info(
                        "[GET /api/users/$uuid] User found",
                        kv("context", mapOf(
                            "route" to "/api/users/{uuid}",
                            "method" to "GET",
                            "userId" to uuid.toString(),
                            "result" to "ok"
                        ))
                    )
                    call.respond(HttpStatusCode.OK, user)
                }
            } catch (e: Exception) {
                logger.error(
                    "[GET /api/users/$uuid] Failed to fetch user",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "GET",
                        "userId" to uuid.toString(),
                        "result" to "error",
                        "errorMessage" to (e.message ?: "unknown")
                    ))
                )
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // ============================
        // POST create new user
        // ============================
        post {
            try {
                val body = call.receiveText()
                val payload = Json.decodeFromString<UserCreateRequest>(body)

                if (payload.fullname.isBlank() || payload.studyLevel.isBlank() || payload.age <= 0) {
                    logger.warn(
                        "[POST /api/users] Invalid input",
                        kv("context", mapOf(
                            "route" to "/api/users",
                            "method" to "POST",
                            "reason" to "validation_failed"
                        ))
                    )
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid input data"))
                    return@post
                }

                val created = service.createUser(payload.fullname, payload.studyLevel, payload.age)
                logger.info(
                    "[POST /api/users] Created user",
                    kv("context", mapOf(
                        "route" to "/api/users",
                        "method" to "POST",
                        "userId" to created.uuid.toString(),
                        "result" to "created"
                    ))
                )
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                logger.error(
                    "[POST /api/users] Failed to create user",
                    kv("context", mapOf(
                        "route" to "/api/users",
                        "method" to "POST",
                        "result" to "error",
                        "errorMessage" to (e.message ?: "unknown")
                    ))
                )
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // ============================
        // PUT partial update
        // ============================
        put("{uuid}") {
            val uuid = call.parameters["uuid"]?.let { UUID.fromString(it) }
            if (uuid == null) {
                logger.warn(
                    "[PUT /api/users/{uuid}] Invalid UUID",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "PUT",
                        "reason" to "invalid_uuid"
                    ))
                )
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID"))
                return@put
            }

            try {
                val body = call.receiveText()
                val payload = Json.decodeFromString<UserUpdateRequest>(body)

                val existingUser = service.getUser(uuid)
                if (existingUser == null) {
                    logger.info(
                        "[PUT /api/users/$uuid] User not found",
                        kv("context", mapOf(
                            "route" to "/api/users/{uuid}",
                            "method" to "PUT",
                            "userId" to uuid.toString(),
                            "result" to "not_found"
                        ))
                    )
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    return@put
                }

                val updatedUser = existingUser.copy(
                    fullname = payload.fullname ?: existingUser.fullname,
                    studyLevel = payload.studyLevel ?: existingUser.studyLevel,
                    age = payload.age ?: existingUser.age
                )

                service.updateUser(updatedUser)
                logger.info(
                    "[PUT /api/users/$uuid] User updated",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "PUT",
                        "userId" to uuid.toString(),
                        "result" to "updated"
                    ))
                )
                call.respond(HttpStatusCode.OK, updatedUser)
            } catch (e: Exception) {
                logger.error(
                    "[PUT /api/users/$uuid] Failed to update user",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "PUT",
                        "userId" to uuid.toString(),
                        "result" to "error",
                        "errorMessage" to (e.message ?: "unknown")
                    ))
                )
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        // ============================
        // DELETE user
        // ============================
        delete("{uuid}") {
            val uuid = call.parameters["uuid"]?.let { UUID.fromString(it) }
            if (uuid == null) {
                logger.warn(
                    "[DELETE /api/users/{uuid}] Invalid UUID",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "DELETE",
                        "reason" to "invalid_uuid"
                    ))
                )
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid UUID"))
                return@delete
            }

            try {
                val existingUser = service.getUser(uuid)
                if (existingUser == null) {
                    logger.info(
                        "[DELETE /api/users/$uuid] User not found",
                        kv("context", mapOf(
                            "route" to "/api/users/{uuid}",
                            "method" to "DELETE",
                            "userId" to uuid.toString(),
                            "result" to "not_found"
                        ))
                    )
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    return@delete
                }

                service.deleteUser(uuid)
                logger.info(
                    "[DELETE /api/users/$uuid] User deleted",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "DELETE",
                        "userId" to uuid.toString(),
                        "result" to "deleted"
                    ))
                )
                call.respond(HttpStatusCode.NoContent)
            } catch (e: Exception) {
                logger.error(
                    "[DELETE /api/users/$uuid] Failed to delete user",
                    kv("context", mapOf(
                        "route" to "/api/users/{uuid}",
                        "method" to "DELETE",
                        "userId" to uuid.toString(),
                        "result" to "error",
                        "errorMessage" to (e.message ?: "unknown")
                    ))
                )
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}
