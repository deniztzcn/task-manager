package com.taskmanager

import com.taskmanager.application.config.JwtConfig
import com.taskmanager.infrastructure.di.appModule
import com.taskmanager.infrastructure.plugins.*
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}
fun Application.module() {
    install(Koin){
        modules(appModule(this@module))
        properties(mapOf("application" to this))
    }
    val jwtConfig = JwtConfig.fromApplicationConfig(this)
    configureCORS()
    configureErrorHandling()
    configureAuthentication(jwtConfig)
    configureDatabases()
    configureContentNegotiation()
    configureRouting()
}


