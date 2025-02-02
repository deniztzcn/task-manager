package com.taskmanager.infrastructure.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.taskmanager.application.config.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            ).also { this@configureAuthentication.log.debug("JWT Verifier configured successfully!") }
            validate { credential ->
                val audienceValid = credential.payload.audience.contains(jwtConfig.audience)
                val username = credential.payload.getClaim("username").asString()
                val role = credential.payload.getClaim("role").asString()
                val userId = credential.payload.getClaim("userId").asInt()
                if (audienceValid && !username.isNullOrEmpty() && !role.isNullOrEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
