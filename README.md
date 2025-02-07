# SwiftCodes App

SwiftCodes App is a Spring Boot application written in Java that processes and exposes SWIFT (Bank Identifier Code) data. The application performs the following functions:

- **CSV Parsing:**  
  Reads SWIFT data from a CSV file, distinguishes headquarters (codes ending with "XXX") from branches (associated with headquarters if the branch’s first 8 characters match the headquarters code), and converts country codes and names to uppercase. Redundant columns in the CSV file are ignored.

- **Data Storage:**  
  Persists the parsed data into a PostgreSQL database optimized for fast, low-latency queries by SWIFT code and ISO-2 country code.

- **REST API:**  
  Provides endpoints to:
  - Retrieve details for a specific SWIFT code.
  - Retrieve all SWIFT codes for a given country.
  - Add new SWIFT code entries.
  - Delete SWIFT code entries.
  - Upload a CSV file containing SWIFT data.

- **Containerization:**  
  The application and its PostgreSQL database are containerized using Docker and docker-compose. When the containers are running, all endpoints are accessible at [http://localhost:8080](http://localhost:8080).

- **Testing:**  
  The project is thoroughly tested with unit and integration tests (JUnit 5, Mockito, Testcontainers) to ensure its reliability and correctness.

---

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Installation and Running Instructions](#Installation-and-Running-Instructions)

  

---

## Features

- **CSV Parsing:**
  - Identifies headquarters by detecting SWIFT codes ending with "XXX".
  - Associates branch codes to headquarters if the branch's first 8 characters match the headquarters' code.
  - Converts country codes and names to uppercase.
  - Ignores redundant columns in the CSV file.

- **Data Storage:**
  - Uses PostgreSQL to persist data.
  - Supports fast, efficient querying by SWIFT code and country ISO-2 code.

- **REST API:**
  - Retrieve details for individual SWIFT codes.
  - Retrieve all SWIFT codes for a specified country.
  - Add new SWIFT code entries.
  - Delete SWIFT code entries.
  - Upload a CSV file containing SWIFT data.

- **Containerization:**
  - Both the application and PostgreSQL run in Docker containers.
  - The endpoints are exposed on port 8080.

- **Testing:**
  - Comprehensive unit and integration tests ensure project quality and reliability.

---

## Technologies

- **Java 21**
- **Spring Boot**
- **Spring Data JPA**
- **MapStruct**
- **PostgreSQL**
- **Docker** and **docker-compose**
- **JUnit 5, Mockito, Testcontainers** (for testing)

---

## Installation and Running Instructions

Make sure you have Docker and Docker Compose installed before proceeding.

Clone the repository

Build and run the containers using: docker-compose up --build -d

Once the containers are up, the API will be available at: http://localhost:8080

Verify that the API is running using: curl http://localhost:8080/v1/swift-codes/{swiftCode}

Api endpoints:
- **/v1/swift-codes/{swiftCode}	Get details of a specific SWIFT code**
- **/v1/swift-codes/country/{countryISO2}	Get SWIFT codes for a country (ISO2 format)**
- **/v1/swift-codes	Add a new SWIFT code**
- **/v1/swift-codes/{swiftCode}	Delete a SWIFT code**
- **/v1/swift-codes/upload-csv	Upload SWIFT codes from a CSV fileL**

To stop the running containers use: docker-compose down



