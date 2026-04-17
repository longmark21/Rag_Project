# Rag-backend

A RAG knowledge base backend service based on Spring Boot 3.2.0 + Spring AI.

## Technology Stack

- **Spring Boot 3.2.0** (Java 17)

- **Spring AI** (Kimi Moonshot large model integration)

- **Spring Data JPA** (MySQL/PostgreSQL)

- **Spring Data Redis** (caching)

- **PostgreSQL** + **pgvector** (vector database)

- **Apache PDFBox** (PDF parsing)

- **Apache POI** (Word document parsing)

- **SpringDoc OpenAPI** (API documentation)

## Quick Start

### 1. Environment Requirements

- JDK 17+

- Maven 3.8+

- MySQL 8.0+ / PostgreSQL 14+

- Redis

### 2. Configuration

Copy `.env.example` to `.env` and configure the relevant parameters.

### 3. Build

```bash
mvn clean package

```

### 4. Run

```bash
java -jar target/rag-knowledge-base-1.0.0.jar

```
The service will start at `http://localhost:8080`.

## Project Structure

```
Rag-backend/

├── src/

│ ├── main/

│ │ ├── java/com/example/rag/

│ │ │ ├── controller/ # REST controller

│ │ │ ├── service/ # Business logic

│ │ │ ├── repository/ # Data access

│ │ │ ├── entity/ # Entity class

│ │ │ ├── dto/ # Data transfer object

│ │ │ ├── config/ # Configuration class

│ │ │ ├── util/ # Utility class

│ │ │ └── RagApplication.java

│ │ └── resources/

│ │ └── application.yml # Application configuration

│ └── test/ # Test

├── pom.xml # Maven Configuration

├── Dockerfile # Docker configuration

├── docker-compose.yml # Docker Compose configuration

└── README.md

```

## API Documentation

After starting the service, access: `http://localhost:8080/swagger-ui.html`
