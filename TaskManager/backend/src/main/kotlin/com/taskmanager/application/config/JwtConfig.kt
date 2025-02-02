package com.taskmanager.application.config

import io.ktor.server.application.*

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val tokenExpiryMillis: Long
) {
    companion object {
        fun fromApplicationConfig(application: Application): JwtConfig {
            val config = application.environment.config.config("jwt")
            return try {
                JwtConfig(
                    secret = config.property("secret").getString(),
                    issuer = config.property("issuer").getString(),
                    audience = config.property("audience").getString(),
                    realm = config.property("realm").getString(),
                    tokenExpiryMillis = config.property("tokenExpiryMillis").getString().toLong()
                )
            } catch (e: Exception) {
                throw IllegalArgumentException("JWT configuration is missing or invalid: ${e.message}")
            }
        }
    }
}
