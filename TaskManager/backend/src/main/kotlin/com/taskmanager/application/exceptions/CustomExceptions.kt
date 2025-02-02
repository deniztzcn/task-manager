package com.taskmanager.application.exceptions

class AuthenticationException(message: String = "Authentication failed") : RuntimeException(message)
class AuthorizationException(message: String = "Access denied") : RuntimeException(message)
class UserNotFoundException(message: String) : Exception(message)
class InvalidCredentialsException(message: String) : Exception(message)
class ValidationException(val errors: Map<String, String>) : Exception()
class UsernameAlreadyExistsException(message: String) : Exception(message)
class AdminDeletionException(message: String): Exception(message)
class EmailAlreadyExistsException(message: String = "Email already exists") : Exception(message)
class TeamRoleNotFoundException(message: String) : Exception(message)
class UserInTeamNotFoundException(message: String) : Exception(message)
class PriorityNotFoundException(message: String) : Exception(message)
class StatusNotFoundException(message: String) : Exception(message)
