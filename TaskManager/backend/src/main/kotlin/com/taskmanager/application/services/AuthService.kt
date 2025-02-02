package com.taskmanager.application.services

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.taskmanager.application.config.JwtConfig
import com.taskmanager.application.dtos.requests.LoginRequestDTO
import com.taskmanager.application.dtos.requests.RegistrationRequestDTO
import com.taskmanager.application.dtos.requests.registrationValidator
import com.taskmanager.application.dtos.responses.UserResponseDTO
import com.taskmanager.application.exceptions.*
import com.taskmanager.domain.entities.AppUser
import com.taskmanager.domain.services.IAppUserService
import com.taskmanager.domain.services.IUserService
import io.konform.validation.Invalid
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class AuthService(
    private val userService: IUserService,
    private val appUserService: IAppUserService,
    private val jwtConfig: JwtConfig,
    private val securityService: SecurityService
) {
    private val algorithm = Algorithm.HMAC256(jwtConfig.secret)

    val jwtVerifier: JWTVerifier = JWT
        .require(algorithm)
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .build()

    fun login(loginRequestDTO: LoginRequestDTO): String? {
        val foundUser = appUserService.findByUsername(loginRequestDTO.username)
            ?: throw UserNotFoundException("User not found")

        if (!securityService.validatePassword(loginRequestDTO.password, foundUser.password)) {
            throw InvalidCredentialsException("Invalid credentials")
        }
        return createJwtToken(foundUser)
    }
    fun registerUser(registrationRequestDTO: RegistrationRequestDTO): UserResponseDTO {
        val validationResult = registrationValidator.validate(registrationRequestDTO)
        if (validationResult is Invalid) {
            val errors = validationResult.errors
                .groupBy(
                    { it.dataPath.removePrefix(".") },
                    { it.message },
                )
                .mapValues { (_, messages) -> messages.joinToString(", ") }
            throw ValidationException(errors)
        }

        return transaction {
            if (appUserService.findByUsername(registrationRequestDTO.username) != null) {
                throw UsernameAlreadyExistsException("Username already exists")
            }
            if (userService.findByEmail(registrationRequestDTO.email) != null) {
                throw EmailAlreadyExistsException("Email already exists")
            }
            val hashedPassword = securityService.hashPassword(registrationRequestDTO.password)
            registrationRequestDTO.password = hashedPassword

            val appUser =  appUserService.createAppUser(registrationRequestDTO)
            userService.createUser(registrationRequestDTO,appUser)
        }
    }
    fun generateRefreshToken(username: String): String {
        return JWT.create()
            .withIssuer(jwtConfig.issuer)
            .withSubject(username)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.tokenExpiryMillis))
            .sign(algorithm)
    }

    fun validateRefreshToken(refreshToken: String): String {
        val verifier = JWT.require(algorithm).withIssuer(jwtConfig.issuer).build()
        val decodedJWT = verifier.verify(refreshToken)
        return decodedJWT.subject
    }

    private fun createJwtToken(user: AppUser): String {
        return JWT.create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withClaim("username", user.username)
            .withClaim("role", user.role)
            .withClaim("userId", user.id.value)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtConfig.tokenExpiryMillis))
            .sign(algorithm)
    }
}
