package com.taskmanager.infrastructure.plugins

import com.taskmanager.application.exceptions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = createErrorResponse("InvalidRequest", cause.localizedMessage)
            )
        }

        exception<ValidationException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = createErrorResponse("ValidationError", cause.errors)
            )
        }

        exception<InvalidCredentialsException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = createErrorResponse("InvalidCredentials", "Password is wrong!")
            )
        }

        exception<AuthenticationException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = createErrorResponse("AuthenticationFailed", cause.localizedMessage)
            )
        }

        exception<UserNotFoundException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = createErrorResponse("UserNotFound", cause.localizedMessage)
            )
        }

        exception<AuthorizationException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Forbidden,
                message = createErrorResponse("AccessDenied", cause.localizedMessage)
            )
        }

        exception<UsernameAlreadyExistsException> { call, cause ->
            val response = createErrorResponse("Conflict", cause.localizedMessage)
            println("Responding with: $response")
            call.respond(
                status = HttpStatusCode.Conflict,
                message = response
            )
        }

        exception<EmailAlreadyExistsException> { call, cause ->
            val response = createErrorResponse("Conflict", cause.localizedMessage)
            println("Responding with: $response")
            call.respond(
                status = HttpStatusCode.Conflict,
                message = response
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = createErrorResponse("InternalError", "Something went wrong")
            )
        }

        exception<AdminDeletionException> { call, cause ->
            call.respond(
                status = HttpStatusCode.Forbidden,
                message = createErrorResponse("AdminDeletionError", cause.localizedMessage)
            )
        }
    }
}

private fun createErrorResponse(type: String, message: Any): Map<String, Any> {
    return mapOf(
        "type" to type,
        "message" to message
    )
}
