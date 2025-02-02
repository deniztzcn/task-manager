# Task Manager

A full-stack task management application built with Ktor (Kotlin) for the backend and Angular for the frontend. This project allows users to manage tasks and teams in a collaborative environment.

This application serves as a Single Page Application (SPA) and implements validation on both the front end and backend.

## Features

### User Authentication
- Register, log in, and validate users with JWT-based authentication.

### Task Management
- Create, update, and delete tasks.
- Assign tasks to users or teams with due dates, priorities, and statuses.

### Team Collaboration
- Create teams, add members, and assign roles.

### Admin Features
- Manage priorities, roles, and statuses.
- Update user roles and delete users.

### Pagination & Filtering
- Efficiently browse tasks, teams, and users with pagination and filtering support.

### Database
- Uses H2 for development and Flyway for database migrations.

## Technologies

### Backend
- Ktor
- Kotlin
- Exposed (ORM)
- H2 Database
- Flyway
- Koin (Dependency Injection)
- JWT Authentication

### Frontend
- Angular
- Angular Material
- TypeScript

### Tools
- Gradle
- Kotlinx Serialization
- Logback

## API Documentation

The backend exposes RESTful APIs for all features, documented with detailed request/response examples and error handling.

## Development Status

The front-end part of the application is not finished yet.
