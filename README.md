# BITP3123 Group Project: SmartCampus Connect

This project is a distributed microservices backend platform designed to deliver and manage core university campus services. Built using Spring Boot and Java 26, the system relies on independently deployable services that manage their own isolated databases, communicating via RESTful APIs, SOAP, and asynchronous messaging workflows. 

### Functions and Modules Available
*   **Student Profile Service (Port 8081):** Manages student demographic data and academic records via CRUD operations over REST.
*   **Course Enrolment Service (Port 8082):** Handles semester course enrollments, drop/add requests, and capacity checks. Depends on the Student Profile Service for verification.
*   **Notification Service (Port 8083):** Asynchronously consumes and logs events triggered by other services (e.g., successful enrollment alerts).
*   **Library / Booking Service (Ports 8084 & 9091):** Manages physical discussion room reservations (REST) and exposes book catalog operations via a legacy SOAP/WSDL interface.
*   **Reporting / Analytics Service (Port 8085):** Produces aggregated dashboard views and metrics by reading data across the distributed domains.

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

## 4. API Testing & Demonstration (Postman)
Open Postman.

Click the "Import" button located in the top-left corner of the workspace.

Select and upload the "DAD PROJECT postman.json" file located in the project repository.

Once imported, you will see the "DAD PROJECT" collection in your sidebar, categorized by microservice.

Environment variables (e.g., {{STUDENT_SERVICE_URL}}) are securely pre-configured inside the collection. Simply open any folder and click "Send" to test the live services running on your localhost ports.

