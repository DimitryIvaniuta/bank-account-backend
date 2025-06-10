# Bank Account Backend

[![Build Status](https://github.com/DimitryIvaniuta/bank-account-backend/actions/workflows/maven.yml/badge.svg)](https://github.com/DimitryIvaniuta/bank-account-backend/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A **Spring Boot** backend implementing **Bank Account Operations**:

* Create, read, update & delete bank accounts
* Deposit & withdrawal operations
* Account statement history
* Java 21 · Spring Boot 3.2 · PostgreSQL · Flyway · Lombok · Maven · JUnit 5 & Testcontainers

---

## Table of Contents

* [Features](#features)
* [Prerequisites](#prerequisites)
* [Getting Started](#getting-started)

  * [Clone & Build](#clone--build)
  * [Database with Docker Compose](#database-with-docker-compose)
  * [Configuration](#configuration)
  * [Run the Application](#run-the-application)
* [API Reference](#api-reference)
* [Testing](#testing)
* [Contributing](#contributing)
* [License](#license)

---

## Features

* **Account CRUD**: create accounts (with optional initial deposit), retrieve single/all accounts, update balance exactly, delete accounts
* **Transactions**: deposit & withdrawal endpoints, each recorded in `operation` history
* **Statement**: fetch chronological list of operations (date, type, amount, resulting balance)
* **Persistence**: PostgreSQL + Flyway migrations
* **Quality**: unit tests (Mockito), integration tests (Testcontainers), code generation via Lombok
* **Build**: Maven wrapper for reproducible builds

---

## Prerequisites

* **Java 21 JDK**
* **Maven 3.8+** (or use bundled Maven wrapper: `./mvnw`)
* **Docker & Docker Compose** (for local PostgreSQL)

---

## Getting Started

### Clone & Build

```bash
git clone https://github.com/DimitryIvaniuta/bank-account-backend.git
cd bank-account-backend
./mvnw clean verify
```

### Database with Docker Compose

```bash
docker-compose up -d
```

This starts a `postgres:15-alpine` container exposing port **5444**:

* **DB**: `bank`
* **User**: `bank_user`
* **Password**: `bank_pass`

Tear down when done:

```bash
docker-compose down
```

### Configuration

**`src/main/resources/application.yml`**:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5444/bank
    username: bank_user
    password: bank_pass
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
server:
  port: 8080
```

**`src/main/resources/application-test.yml`**:

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:15-alpine:///bank_test
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: validate
```

### Run the Application

```bash
./mvnw spring-boot:run
```

The API base URL is `http://localhost:8080/api/accounts`.

---

## API Reference


| Operation              | HTTP                               | Body                           | Response             |
| ---------------------- | ---------------------------------- | ------------------------------ | -------------------- |
| Create account         | `POST /api/accounts`               | `{ "initialBalance": 100.00 }` | `201 Created` + body |
| List all accounts      | `GET /api/accounts`                | —                             | `200 OK` + array     |
| Get one account        | `GET /api/accounts/{id}`           | —                             | `200 OK` + object    |
| Update balance exactly | `PUT /api/accounts/{id}`           | `{ "newBalance": 150.00 }`     | `200 OK` + object    |
| Delete account         | `DELETE /api/accounts/{id}`        | —                             | `204 No Content`     |
| Deposit                | `POST /api/accounts/{id}/deposit`  | `{ "amount": 50.00 }`          | `200 OK`             |
| Withdraw               | `POST /api/accounts/{id}/withdraw` | `{ "amount": 20.00 }`          | `200 OK`             |
| Get account statement  | `GET /api/accounts/{id}/statement` | —                             | `200 OK` + array     |

*Example deposit*:

```bash
curl -X POST http://localhost:8080/api/accounts/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":50.00}'
```

---

## Testing

* **Unit tests**: run `./mvnw test`
* **Integration tests**: run `./mvnw verify` (starts Testcontainers)

---

## Contributing

1. Fork the repo
2. Create a branch: `git checkout -b feature/xyz`
3. Commit your changes
4. Push: `git push origin feature/xyz`
5. Open a Pull Request

Please follow code conventions and include tests.

---

## License

This project is licensed under the [MIT License](LICENSE).


## Contact

**Dzmitry Ivaniuta** — [diafter@gmail.com](mailto:diafter@gmail.com) — [GitHub](https://github.com/DimitryIvaniuta)
