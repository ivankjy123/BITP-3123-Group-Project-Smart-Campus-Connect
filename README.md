# BITP3123 Group Project: SmartCampus Connect

This project is a distributed microservices backend platform designed to deliver and manage core university campus services. Built using Spring Boot and Java 26, the system relies on independently deployable services that manage their own isolated databases, communicating via RESTful APIs, SOAP, and asynchronous messaging workflows.

### Functions and Modules Available
*   **Student Profile Service (Port 8081):** Manages student demographic data and academic records via CRUD operations over REST.
*   **Course Enrolment Service (Port 8082):** Handles semester course enrollments, drop/add requests, capacity checks, and constraint check (if academic data exists, delete is prohibited). Depends on the Student Profile Service for verification.
*   **Notification Service (Port 8083):** Asynchronously consumes and logs events triggered by other services (e.g., successful enrollment alerts).
*   **Library / Booking Service (Ports 8084 & 9091):** Manages physical discussion room reservations (REST) and exposes book catalog operations via a legacy SOAP/WSDL interface.
*   **Reporting / Analytics Service (Port 8085):** Produces aggregated dashboard views and metrics by reading data across the distributed domains.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Services](#services)
  - [1. Student Service](#1-student-service-port-8081)
  - [2. Enrolment Service](#2-enrolment-service-port-8082)
  - [3. Notification Service](#3-notification-service-port-8083--tcp-9090)
  - [4. Library Service](#4-library-service-port-8084--soap-9091)
  - [5. Reporting Service](#5-reporting-service-port-8085)
- [Inter-Service Communication](#inter-service-communication)
- [Tech Stack](#tech-stack)
- [1. Tools & Prerequisites](#1-tools--prerequisites)
- [2. Dependencies (Database Setup)](#2-dependencies-database-setup)
- [3. Installation & Running the Services](#3-installation--running-the-services)
- [4. API Testing & Demonstration (Postman)](#4-api-testing--demonstration-postman)
- [5. Architectural Specifications](#5-architectural-specifications)
- [6. Architectural Pattern Selection](#6-architectural-pattern-selection)
- [Project Structure](#project-structure)
- [Authors](#authors)

---

## Architecture Overview

Each service is a self-contained Spring Boot application with its own schema, following a database-per-service pattern typical of microservices architectures. Services do not share a database; they instead call each other's REST/SOAP APIs directly, and publish asynchronous events to a central Notification Service over raw TCP sockets.

| Service              | Port | Protocol(s)      | Database               |
|----------------------|------|------------------|-------------------------|
| Student Service      | 8081 | REST             | `db_student_profile`    |
| Enrolment Service    | 8082 | REST             | `db_course_enrolment`   |
| Notification Service | 8083 | REST + TCP (9090)| `db_notification`       |
| Library Service      | 8084 | REST + SOAP (9091)| `db_library_booking`   |
| Reporting Service    | 8085 | REST             | `db_reporting`          |

---

## Services

### 1. Student Service (Port 8081)

Owns student demographic data and academic transcript records.

**`/api/students`**
| Method | Endpoint            | Description              |
|--------|---------------------|--------------------------|
| GET    | `/api/students`     | List all students        |
| GET    | `/api/students/{matricNo}` | Get a student by matriculation number |
| POST   | `/api/students`     | Create a new student      |
| PUT    | `/api/students/{matricNo}` | Update a student        |
| DELETE | `/api/students/{matricNo}` | Delete a student        |

**`/api/academic-records`**
| Method | Endpoint                                             | Description                          |
|--------|-------------------------------------------------------|---------------------------------------|
| GET    | `/api/academic-records/students/{matricNo}`           | Get a student's academic records      |
| POST   | `/api/academic-records`                                | Create an academic record              |
| PUT    | `/api/academic-records/students/{matricNo}/course/{courseCode}` | Update a record for a course |
| DELETE | `/api/academic-records/students/{matricNo}/course/{courseCode}` | Delete a record for a course |

### 2. Enrolment Service (Port 8082)

Handles semester course enrolment, add/drop requests, course capacity checks, and a constraint check that blocks deletion when an active academic record already exists for that course. Depends on the Student Service to validate students before creating an enrolment.

**`/api/enrolments`**
| Method | Endpoint                                              | Description                              |
|--------|--------------------------------------------------------|--------------------------------------------|
| GET    | `/api/enrolments`                                       | List all enrolments                        |
| GET    | `/api/enrolments/students/{matricNo}`                   | Get enrolments for a student                |
| POST   | `/api/enrolments`                                       | Create a new enrolment (checks student existence & course capacity) |
| DELETE | `/api/enrolments/students/{matricNo}/courses/{courseCode}` | Drop a course (blocked if an academic record already exists) |

**`/api/enrolments/courses`**
| Method | Endpoint                                | Description                 |
|--------|-------------------------------------------|------------------------------|
| GET    | `/api/enrolments/courses`                 | List all courses             |
| GET    | `/api/enrolments/courses/{courseCode}`    | Get a course by code          |
| POST   | `/api/enrolments/courses`                 | Create a new course           |
| PUT    | `/api/enrolments/courses/{courseCode}`    | Update a course                |
| DELETE | `/api/enrolments/courses/{courseCode}`    | Delete a course (blocked if students are enrolled) |

The service also derives the **active academic semester** automatically from the current date and enforces enrolment locking (via `ReentrantLock`) to avoid race conditions when multiple students enrol into a course near capacity.

### 3. Notification Service (Port 8083 / TCP 9090)

A lightweight event log and notification hub. It runs a raw TCP server (default port `9090`) that other services connect to via a shared `NotificationTcpClient`/`NotificationMessage` DTO to push events (e.g. `ENROLMENT_SUCCESS`, loan/reservation confirmations). Received events are persisted and exposed via REST for auditing.

**`/api/notifications`**
| Method | Endpoint                                   | Description                        |
|--------|-----------------------------------------------|--------------------------------------|
| GET    | `/api/notifications`                          | List all logged notifications        |
| GET    | `/api/notifications/type/{eventType}`         | Filter notifications by event type    |
| GET    | `/api/notifications/email/{email}`            | Filter notifications by recipient email |

**`/api/stats`**
| Method | Endpoint            | Description                                              |
|--------|----------------------|------------------------------------------------------------|
| GET    | `/api/stats/counts`  | Get a running count of received messages, grouped by event type |

### 4. Library Service (Port 8084 / SOAP 9091)

Manages discussion room bookings via REST, and exposes a legacy-style book catalog/loan interface via **SOAP/WSDL** (published separately on port `9091` to avoid clashing with the embedded Tomcat server on `8084`).

**`/api/rooms`** (REST)
| Method | Endpoint                                       | Description                    |
|--------|---------------------------------------------------|----------------------------------|
| GET    | `/api/rooms`                                       | List all discussion rooms         |
| GET    | `/api/rooms/{roomId}`                              | Get a room by ID                  |
| POST   | `/api/rooms`                                       | Create a room                     |
| PUT    | `/api/rooms/{roomId}`                              | Update a room                     |
| DELETE | `/api/rooms/{roomId}`                              | Delete a room                     |
| GET    | `/api/rooms/reservations`                          | List all reservations             |
| GET    | `/api/rooms/reservations/{reservationId}`          | Get a reservation by ID            |
| GET    | `/api/rooms/reservations/room/{roomId}`            | Get reservations for a room        |
| GET    | `/api/rooms/reservations/student/{matricNo}`       | Get reservations for a student      |
| POST   | `/api/rooms/reservations`                          | Create a room reservation           |
| DELETE | `/api/rooms/reservations/{reservationId}`          | Cancel a reservation                |

**`/api/loans`** (REST — Book Loans)
| Method | Endpoint                          | Description                    |
|--------|-------------------------------------|----------------------------------|
| GET    | `/api/loans`                        | List all book loans               |
| GET    | `/api/loans/student/{matricNo}`     | Get loans for a student            |
| POST   | `/api/loans`                        | Create a new book loan             |
| PUT    | `/api/loans/{loanId}/return`        | Mark a loan as returned             |

**SOAP — Book Catalog** (`LibraryService`)
- WSDL published at: `http://localhost:9091/ws/library?wsdl`
- Exposes book catalog operations (e.g. lookup, add, availability checks), with custom SOAP faults for scenarios such as a book not found or a book with active loans.

### 5. Reporting Service (Port 8085)

A read-only analytics layer that aggregates data across the Student, Enrolment, and Library services via REST clients (`StudentClient`, `EnrolmentClient`, `LibraryClient`), producing dashboard-ready summaries. A scheduled job (`ReportingScheduler`) periodically refreshes and persists aggregated metrics.

**`/api/reports`**
| Method | Endpoint                    | Description                                     |
|--------|-------------------------------|---------------------------------------------------|
| GET    | `/api/reports/dashboard`      | Get an overall campus dashboard summary            |
| GET    | `/api/reports/programmes`     | Get a report broken down by academic programme      |
| GET    | `/api/reports/metrics`        | Get raw aggregated metrics                          |
| POST   | `/api/reports/refresh`        | Manually trigger a metrics refresh/aggregation       |

---

## Inter-Service Communication

- **REST (synchronous):** The Enrolment, Reporting, and Library services call other services' REST endpoints directly over HTTP (e.g. the Enrolment Service calls the Student Service to validate a `matricNo` before creating an enrolment).
- **SOAP (synchronous, legacy-style):** The Library Service exposes its book catalog operations over SOAP/WSDL on a dedicated port (`9091`), separate from its REST endpoints on `8084`.
- **TCP messaging (asynchronous):** The Student, Enrolment, and Library services each hold a `NotificationTcpClient` that pushes `NotificationMessage` events over a raw TCP socket to the Notification Service's TCP server (port `9090`), which logs them and exposes them for querying via REST.

---

## Tech Stack

- **Language:** Java (per `pom.xml`, `java.version` is set to 21; project description above references Java 26 for the runtime/toolchain used to develop it)
- **Framework:** Spring Boot 4.1 (Spring Web MVC, Spring Data JPA)
- **Database:** MySQL (one schema per service)
- **Web Services:** REST (Spring MVC), SOAP (Jakarta XML Web Services)
- **Messaging:** Custom TCP client/server for async event notifications
- **Build Tool:** Maven (via Maven Wrapper — `mvnw` / `mvnw.cmd`)
- **API Testing:** Postman collection included (`DAD PROJECT postman.json`)

---

## 1. Tools & Prerequisites

To develop, run, and test this project, ensure you have the following tools installed:
*   [Java Development Kit (JDK) 26](https://adoptium.net/)
*   **XAMPP** (or a standalone MySQL Server) for database hosting
*   **IntelliJ IDEA** or **VS Code** (IDE for Java/Spring Boot development)
*   **Postman** (For API testing and executing the demonstration collection)

## 2. Dependencies (Database Setup)

Please ensure the MySQL service is running and the database schemas are created **before** starting the Spring Boot applications. The services will fail to boot if they cannot connect to their respective databases.

1. Start your local MySQL server via **XAMPP Control Panel** (ensure port `3306` is active).
2. Open your preferred SQL client (phpMyAdmin, MySQL Workbench, DataGrip, etc.).
3. Execute the provided `SQL-script for demonstration.sql` file located in the root directory.
4. Verify that the following five isolated databases have been created and populated:
   * `db_student_profile`
   * `db_course_enrolment`
   * `db_notification`
   * `db_library_booking`
   * `db_reporting`

## 3. Installation & Running the Services

This project uses the Maven Wrapper. To run the entire microservices stack concurrently, you will need to open your IDE (VS Code or IntelliJ), launch five separate terminal panels, and run the respective startup commands in each folder.

Execute the following commands in your separate terminal windows:

```powershell
# Terminal 1
cd student-service && .\mvnw clean spring-boot:run

# Terminal 2
cd enrolment-service && .\mvnw clean spring-boot:run

# Terminal 3
cd library-service && .\mvnw clean spring-boot:run

# Terminal 4
cd notification-service && .\mvnw clean spring-boot:run

# Terminal 5
cd reporting-service && .\mvnw clean spring-boot:run
```
 
> On macOS/Linux (or other Unix-like systems), use `./mvnw` instead of `.\mvnw`.


## 4. API Testing & Demonstration (Postman)
Open Postman.

Click the "Import" button located in the top-left corner of the workspace.

Select and upload the "DAD PROJECT postman.json" file located in the project repository.

Once imported, you will see the "DAD PROJECT" collection in your sidebar, categorized by microservice.

Environment variables (e.g., {{STUDENT_SERVICE_URL}}) are securely pre-configured inside the collection. Simply open any folder and click "Send" to test the live services running on your localhost ports.

## 5. Architectural Specifications
The SmartCampus Connect platform addresses the distributed system goals as below.
* **Location Transparency**: Users interact with logical URL endpoints via Postman. The client remain oblivious to where the actual data stores.
* **Access Transparency**: The application handles the underlying communication protocols uniformly on frontend (Postman).
* **Concurrency Transparency**: Multiple services can update records concurrently. For example, users changing their student profile does not interfere with enrolment service.
* **Failure Handling**: The system uses custom error message to handle every failures, such as services offline or exception while interacting with REST or SOAP.

## 6. Architectural Pattern Selection
The ecosystem implements a hybrid **Multi-Tier Client-Server Architecture** with Distributed Components, combined with a **Master-Slave / Shared-Read pattern** inside the Reporting engine.
 * **Zero Coupling**: Each service has its own database schema.
 * **Scaling**: Each service is scalable horizontally.
 * **Fault Tolerance**: The faults are isolated via the Network Boumdaries.
 * **Loose Coupling**: State transitions occur over explicit HTTP boundaries; direct schema manipulation across services is blocked.
 * **Interoperability**: JSON endpoints interact directly with legacy JAX-WS XML setups.
 * **Strict Data Ownership**: Each service communicates exclusively with its declared database schema (db_student_profile, db_course_enrolment, etc.). Sharing tables across domains is forbidden.
 * **Integration of various techniques**: Each service uses different type of technique.

    | Service | Technique/Architecture Used|
    | --- | --- |
    | Student Profile Service | REST, Spring Boot |
    | Course Enrolment Service | REST, Spring Boot, Load test with SQL script simulation |
    | Notification Service | Raw TCP Socket / Producer-Consumer, REST, Spring Boot |
    | Library Service | Legacy SOAP and WSDL, REST, Spring Boot |
    | Reporting Service | Spring Boot, REST, Data Aggregation |

---

## Project Structure

```
SmartCampusConnect67/
├── student-service/         # Student profiles & academic records (8081)
├── enrolment-service/       # Course enrolment & course management (8082)
├── notification-service/    # Event log + TCP server (8083 / TCP 9090)
├── library-service/         # Room bookings (REST) + book catalog (SOAP) (8084 / 9091)
├── reporting-service/       # Cross-service dashboards & analytics (8085)
├── DAD PROJECT postman.json # Postman collection for API testing
├── SQL-script for demonstration.sql
├── SQL-script for reporting.sql
└── README.md
```

Each service follows a consistent internal layout:

```
src/main/java/my/smartcampusconnect67/<service_name>/
├── client/       # Outbound clients (REST/TCP) to other services
├── controller/   # REST (and SOAP config, where applicable) endpoints
├── dto/          # Data transfer objects (e.g. NotificationMessage)
├── error/        # Standardized error response models
├── model/        # JPA entities
├── repository/   # Spring Data JPA repositories
├── service/      # Business logic (where present)
└── <Service>Application.java
```

---

## Authors

- [@ivankjy123](https://github.com/ivankjy123)
- [@Hew04](https://github.com/Hew04)
- [@kkoklianglaw-hub](https://github.com/kkoklianglaw-hub)
- [@mpp0719](https://github.com/mpp0719)
