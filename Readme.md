# 🍿 Popcorn Palace Movie Ticket Booking System

Welcome to **Popcorn Palace**—a robust and intuitive backend service designed for efficiently managing movie listings, showtimes scheduling, and ticket bookings. Built using **Java Spring Boot**, this application emphasizes clarity, reliability, and maintainability.

## 🚀 Overview

The Popcorn Palace backend provides clear and consistent RESTful APIs that ensure ease of integration and reliable performance. It includes comprehensive features, validation, detailed testing, and thorough documentation to support a seamless development and deployment experience.

---

## 🌟 Key Features

### 🎬 Movie Management
- **CRUD operations**: Create, Read, Update, and Delete movies easily.
- **Uniqueness Validation**: Prevents duplicate movie titles.
- **Detailed Schema Validation**: Ensures accurate data entry (e.g., rating within 0-10, valid release years).

### 🕒 Showtime Scheduling
- **Non-Overlapping Showtimes**: Robust logic ensures no overlapping of showtimes within the same theater.
- **Validation Checks**: Enforces logical start and end times, valid movie references, and positive pricing.

### 🎟️ Ticket Booking
- **Seat Reservation Checks**: Prevents double booking for the same seat and showtime.
- **User-friendly Errors**: Clear and informative messages for booking conflicts and validation issues.
- **UUID-based User Tracking**: Ensures clear user identification and session management.

### 🛡️ Strong Schema Validation
- Utilizes **Hibernate Validator** and **Jakarta Validation API** for rigorous schema enforcement.
- Ensures that incoming API data adheres strictly to defined formats and constraints.

### 🧹 Clean DTO-based Requests
- **Data Transfer Objects (DTO)**: Clearly separates request bodies from entities, ensuring secure and efficient data handling.
- Simplifies the mapping process between requests and internal models.

---

## 🧪 Comprehensive Testing

- **Integration Tests**: Extensive suite covering all major functionalities and edge cases, ensuring stability and reliability.
- **Validation Scenarios**: Tests specifically target validation rules to ensure strict compliance with business logic.
- **Conflict and Error Scenarios**: Thoroughly tested overlapping showtime and seat-booking scenarios.

---

## 📚 Well-Documented & Maintainable Code

- **Clear Code Comments**: Each component (controllers, entities, repositories) contains detailed comments.
- **Professional Structure**: Easy-to-navigate project architecture.
- **Lombok Integration**: Reduces boilerplate, making the codebase leaner and more readable.

---

## 🗃️ Database Management

- Uses **PostgreSQL** via Docker for reliable and reproducible database environments.
- Initial database schema and data setup are automated (`schema.sql`, `data.sql`).

---

## ⚙️ Technologies Used

- Java 21
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL
- Hibernate Validator
- Docker
- Maven
- JUnit
- Lombok

---

## 🚦 Getting Started

Check out the [Instructions.md](Instructions.md) file for clear, step-by-step instructions to quickly run, build, and test this project on your local environment.

---

## 🛟 Support & Feedback

Should you have any questions or encounter issues, please reach out—we’re here to help ensure your experience with Popcorn Palace is exceptional.


Thank you for yout time! 🍿✨

Yotam Shekrel: +972 50-7701019 | yotamshekrel@gmail.com
