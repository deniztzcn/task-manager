package com.taskmanager.infrastructure.plugins

import at.favre.lib.crypto.bcrypt.BCrypt
import com.taskmanager.domain.entities.*
import com.taskmanager.domain.enums.AuthRole
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.h2.tools.Server
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException

fun startH2Console() {
    try {
        val server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082")
        server.start()
        println("H2 Console started at: http://localhost:8082")
    } catch (e: SQLException) {
        println("Error starting H2 Console: ${e.message}")
    }
}

fun initializeMigrations() {
    val flyway = Flyway.configure()
        .dataSource("jdbc:h2:file:./data/task_manager", "root", "")
        .baselineOnMigrate(true) // Enable baseline for existing schema
        .baselineVersion("1")
        .locations("classpath:db/migration") // Ensure this matches your migration files path
        .load()
    flyway.migrate()
}

fun Application.configureDatabases() {
    startH2Console()
    initializeMigrations()
    val database = Database.connect(
        url = "jdbc:h2:file:./data/task_manager",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
}
