# Sunrise Dental Clinic — Appointment & Patient Management System

A Spring Boot web application built for the CIS6003 Advanced Programming coursework
(Cardiff Metropolitan University). Replaces Sunrise Dental Clinic's paper-based
appointment booking with a validated, database-backed system.

## Features

- **User Authentication** — session-based staff login
- **Register New Appointment** — validated patient/appointment intake, auto-generated
  appointment numbers (`APT-0001`, ...), rejects double-bookings and out-of-hours slots
- **Display Appointment Details** — search by appointment number
- **Calculate and Print Bill** — treatment fee + clinic consultation fee, printable receipt
- **Help Section** — in-app step-by-step guidance for staff
- **REST API** (`/api/*`) — the same business logic exposed as JSON endpoints

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 17 |
| Framework | Spring Boot 3.3.4 (Web, Data JPA, Validation, Thymeleaf) |
| Database | H2 (file-based, `./data/clinicdb`) |
| View | Thymeleaf + Bootstrap 5 |
| Testing | JUnit 5, Spring Boot Test, MockMvc |
| Build | Maven |
| CI | GitHub Actions (`.github/workflows/ci.yml`) — runs the test suite on every push |

## Design Patterns

- **Facade** — `AppointmentService` is the single entry point for the web UI and REST API
- **Strategy** — `BillingStrategy` / `StandardBillingStrategy` decouples the billing rule
  from the service that uses it
- **Repository / DAO** — `AppointmentRepository`, `StaffUserRepository` (Spring Data JPA)
- **Singleton** — `TreatmentPricingConfig`, a Spring-managed bean holding the shared price list

See [`docs/uml-design.html`](docs/uml-design.html) for the full Use Case, Class, and
Sequence diagrams with design rationale.

## Running Locally

Requires JDK 17+ and Maven (or use the included `mvnw` wrapper).

```bash
mvn spring-boot:run
```

Then open http://localhost:8080/login — default credentials: `admin` / `admin123`.

The H2 console is available at http://localhost:8080/h2-console
(JDBC URL: `jdbc:h2:file:./data/clinicdb`, user: `sa`, no password).

## Running Tests

```bash
mvn test
```

8 tests cover appointment registration, double-booking rejection, operating-hours
validation, bill calculation, and the REST API. See
[`docs/test-plan.html`](docs/test-plan.html) for the full test plan, test data, and a
worked TDD (red-green) example.

## Project Structure

```
src/main/java/com/sunrise/dental/
├── model/        Appointment, StaffUser (JPA entities)
├── repository/   Spring Data JPA repositories
├── service/      AppointmentService (Facade)
├── billing/      BillingStrategy (Strategy pattern)
├── config/       TreatmentPricingConfig, DataSeeder
├── web/          Thymeleaf controllers + session auth
└── api/          REST controllers
docs/             UML design and test plan documentation
```
