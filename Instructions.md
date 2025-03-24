# Instructions for Running, Building, and Testing Popcorn Palace Backend

This guide will walk you through the steps required to set up, build, run, and test the **Popcorn Palace Movie Ticket Booking System** backend.

## Project Overview

This project provides RESTful APIs for managing movies, showtimes, and ticket bookings. Key features include:

- **Movies API**: Add, update, delete, and list movies.
- **Showtimes API**: Schedule movie showtimes, ensuring no overlaps in the same theater.
- **Bookings API**: Book tickets, preventing double booking of seats for the same showtime.
- **Validation and Error Handling**: Comprehensive input validation and informative error messages.
- **Database Persistence**: Managed via PostgreSQL running inside Docker.
- **Testing**: Thorough integration tests covering API endpoints and key business logic.

---

## Prerequisites

Ensure you have installed:

- **Java 21 SDK**
  - [Download Java SDK](https://www.oracle.com/java/technologies/downloads/#java21)

- **Docker Desktop**
  - [Download Docker Desktop](https://www.docker.com/products/docker-desktop/)

- **An IDE**
  - [Download IntelliJ IDEA](https://www.jetbrains.com/idea/download)

---

## Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Yotamshekrel/popcorn-palace
cd popcorn-palace
```

### 2. Start PostgreSQL using Docker

Navigate to the project's root directory containing `compose.yml`, then run:

```bash
docker compose up -d
```

This will spin up a local PostgreSQL instance configured for the project.

---

## Running the Application

The database schema and initial data are automatically created when you run the application.

### For Linux/macOS:

Use Maven wrapper:

```bash
./mvnw spring-boot:run
```

### For Windows:

Use Maven wrapper script:

```cmd
mvnw.cmd spring-boot:run
```

The application should now be running on `http://localhost:8080`.

---

## Testing the Application

Integration tests are provided to verify the functionality and correctness of the APIs.

### For Linux/macOS:

```bash
./mvnw test
```

### For Windows:

```cmd
mvnw.cmd test
```

All tests should pass, indicating correct setup and functionality.

---

## API Endpoints

Once the application is running, you can interact with the provided APIs:

- Movies: `GET`, `POST`, `DELETE`, and update via `POST`.
- Showtimes: `GET`, `POST`, `DELETE`, and update via `POST`.
- Bookings: `POST` to book tickets.

Detailed API documentation and examples can be found in `Readme.md`.

---

## Shutting Down

When you're done, stop the application by pressing `Ctrl+C` in the terminal, and stop the Docker containers by running:

```bash
docker compose down
```

---

You are now set up to fully explore and use the Popcorn Palace backend! If you encounter any issues or have any questions, please reach out!

Yotam Shekrel: +972 50-7701019 | yotamshekrel@gmail.com

