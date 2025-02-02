package com.taskmanager.infrastructure.di

import ITaskService
import TaskService
import com.taskmanager.application.config.JwtConfig
import com.taskmanager.application.services.*
import com.taskmanager.domain.repositories.*
import com.taskmanager.domain.services.*
import com.taskmanager.infrastructure.repositories.*
import io.ktor.server.application.*
import org.koin.dsl.module

fun appModule(application: Application) = module {
    single<IUserRepository> { UserRepository() }
    single<IAppUserRepository> { AppUserRepository() }
//    single<ITaskRepository> { TaskRepository() }
//    single<ITeamRepository> {TeamRepository() }


    single<SecurityService> { SecurityService() }
    single<IAppUserService> { AppUserService(get()) }
    single<IUserService> { UserService(get(), get()) }
    single<ITaskService> { TaskService() }
    single<ITeamService> { TeamService() }
    single<IPriorityService> { PriorityService() }
    single<IRoleService> { RoleService() }
    single<IStatusService> { StatusService() }

    single {
        JwtConfig.fromApplicationConfig(application)
    }
    single { AuthService(get(), get(), get(), get()) }
}
