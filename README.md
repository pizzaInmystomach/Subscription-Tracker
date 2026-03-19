Subscription Tracker Service
A robust, enterprise-ready backend solution designed to manage recurring subscriptions and prevent unwanted charges through automated, schedule-driven email alerts.

📌 Project Overview
As modern services shift towards subscription-based models, users often struggle with "hidden costs" from forgotten trials or recurring bills. This project provides a centralized RESTful API to track subscriptions and an Automated Reminder Engine that proactively notifies users before the next billing cycle.

Key Features
Subscription Lifecycle Management (CRUD): Complete API for creating, reading, updating, and deleting subscription records.

Automated Reminder Engine: Utilizes Spring Scheduling to perform daily database scans for upcoming billing events.

Email Notification Service: Integrated with Spring Mail (SMTP) to deliver personalized "Cancel Reminders."

Containerized Infrastructure: Fully orchestrated with Docker & Docker Compose for seamless deployment and environment consistency.

Secure Configuration: Implements Environment Variable management to protect sensitive credentials (e.g., SMTP keys).

🏗️ System Architecture
The system follows a Layered Architecture to ensure high maintainability and separation of concerns:

Controller Layer: Handles RESTful HTTP requests and response mapping.

Service Layer: Orchestrates business logic, including the calculation of reminder dates.

Repository Layer: Manages persistence via Spring Data JPA and PostgreSQL.

Scheduling Layer: Triggers automated tasks based on configurable Cron expressions.

🛠️ Tech Stack
Language: Java 21 (LTS)

Framework: Spring Boot 3.x

Persistence: Spring Data JPA, Hibernate

Database: PostgreSQL 15

Automation: Spring Scheduling

DevOps: Docker, Docker Compose

Testing Tooling: Mailtrap (SMTP Sandbox)

🚀 Getting Started
Prerequisites
Docker & Docker Compose

Java 21 (for local development)

Maven

1. Configuration
Create a .env file in the root directory and provide your SMTP credentials:
MAILTRAP_USERNAME=your_username
MAILTRAP_PASSWORD=your_password

2. Build the Application
./mvnw clean package -DskipTests

3. Launch with Docker
docker-compose up --build -d
The API will be accessible at http://localhost:8080.

API Documentation (Sample)