# Bank Account Backend with Multi-Currency Support

[![Build Status](https://github.com/DimitryIvaniuta/bank-account-backend/actions/workflows/maven.yml/badge.svg)](https://github.com/DimitryIvaniuta/bank-account-backend/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A **Spring Boot 3.5** backend implementing the classic **Bank Account Kata**, now enhanced with full **multi-currency** support:

* Create, read, update & delete bank accounts
* **Deposit & withdrawal** operations in *any* ISO-4217 currency
* **Automatic FX conversion** via JSR‑354 (Moneta) + ECB exchange rates
* **Account statement** history (date, type, amount, resulting balance + currency)
* Java 21 · Spring Boot · PostgreSQL · Flyway · Lombok · Maven · JUnit 5 · Mockito · Testcontainers

---

## Table of Contents

* [Features](#features)
* [Prerequisites](#prerequisites)
* [Getting Started](#getting-started)

  * [Clone & Build](#clone--build)
  * [Database](#database)
  * [Configuration](#configuration)
  * [Run the Application](#run-the-application)
* [API Reference](#api-reference)
* [Domain & Persistence](#domain--persistence)
* [Testing](#testing)
* [Contributing](#contributing)
* [License](#license)

---

## Features

* **Account CRUD**: create accounts (with optional initial deposit), retrieve, update balance exactly, delete
* **Multi-Currency**: deposit/withdraw in any supported currency; backend stores all balances in account’s native currency
* **JSR‑354 Standard**: uses `MonetaryAmount` and `CurrencyUnit` via Moneta
* **FX Conversion**: real‑time ECB exchange rates with Moneta‑Convert
* **Embeddable MoneyValue**: persisting `amount` + `currency` in a single JPA embeddable
* **Operations History**: each deposit/withdrawal is recorded with amount, currency, timestamp, and post‑balance
* **Flyway Migrations**: database evolution scripts, including `VARCHAR(3)` currency column
* **Testing**: unit tests (Mockito), integration tests (Testcontainers)

---

## Prerequisites

* **Java 21** (JDK)
* **Maven 3.8+** (or use `./mvnw` wrapper)
* **Docker & Docker Compose** (for PostgreSQL in development / CI)

---

## Getting Started

### Clone & Build

```bash
git clone https://github.com/DimitryIvaniuta/bank-account-backend.git
cd bank-account-backend
./mvnw clean verify
```

### Database

Start PostgreSQL via Docker Compose (development profile):

```bash
docker-compose -f docker-compose-db.yml up -d
```

* **Image**: `postgres:15-alpine`
* **DB**: `bank` · **User**: `bank_user` · **Pass**: `bank_pass`
* **Ports**: host `5444` → container `5432`

Tear down:

```bash
docker-compose -f docker-compose-db.yml down
```

### Configuration

**`src/main/resources/application.yml`** (production/dev):

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

**`src/main/resources/application-test.yml`** (integration tests):

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

The API will be available at `http://localhost:8080/api/accounts`.

---

## API Reference

All endpoints accept/produce JSON.

| Operation              | HTTP                               | Body example                                | Response                               |
| ---------------------- | ---------------------------------- | ------------------------------------------- | -------------------------------------- |
| Create account         | POST `/api/accounts`               | `{"initialAmount":100.00,"currency":"USD"}` | `201 Created` + `AccountResponse`      |
| List all accounts      | GET `/api/accounts`                | —                                           | `200 OK` + list                        |
| Get one account        | GET `/api/accounts/{id}`           | —                                           | `200 OK` + `AccountResponse`           |
| Update balance exactly | PUT `/api/accounts/{id}`           | `{"targetAmount":150.00,"currency":"EUR"}`  | `200 OK` + `AccountResponse`           |
| Delete account         | DELETE `/api/accounts/{id}`        | —                                           | `204 No Content`                       |
| Deposit                | POST `/api/accounts/{id}/deposit`  | `{"amount":50.00,"currency":"GBP"}`         | `200 OK`                               |
| Withdraw               | POST `/api/accounts/{id}/withdraw` | `{"amount":20.00,"currency":"JPY"}`         | `200 OK`                               |
| Statement              | GET `/api/accounts/{id}/statement` | —                                           | `200 OK` + list of `StatementResponse` |

**Example Deposit**:

```bash
curl -X POST http://localhost:8080/api/accounts/1/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount":50.00,"currency":"GBP"}'
```

---

## Domain & Persistence

* **`Account`** entity embeds `MoneyValue` (fields: `amount DECIMAL(19,4)`, `currency VARCHAR(3)`).
* **`Operation`** entity embeds two `MoneyValue` (for transaction amount and post‑balance) and is linked to `Account`.
* **`MoneyValue`** is an `@Embeddable` wrapping `BigDecimal amount` + `CurrencyUnit currency` with a JPA converter.
* **FX Conversion**: `AccountService` uses `MonetaryConversions.getExchangeRateProvider("ECB")` to convert incoming amounts into the account’s native currency.

---

## Testing

* **Unit Tests**: JUnit 5 + Mockito cover `AccountService` logic, using `MoneyValue` and `MonetaryAmount` assertions.
* **Integration Tests**: Spring Boot + Testcontainers start an ephemeral PostgreSQL, run Flyway migrations, and exercise the full service via `AccountService` directly.

Run tests locally:

```bash
./mvnw test       # unit only
./mvnw verify     # includes integration tests
```

---

## Contributing

1. Fork repository
2. Create feature branch: `git checkout -b feature/my-feature`
3. Commit: \`git commit -am "feat: add..."
4. Push: `git push origin feature/my-feature`
5. Open Pull Request

Please adhere to existing code style, add tests for new logic, and document any breaking changes in the README.

---

## License

This project is licensed under the [MIT License](LICENSE).

---

## License

This project is licensed under the [MIT License](LICENSE).


## Contact

**Dimitry Ivaniuta** — [dzmitry.ivaniuta.services@gmail.com](mailto:dzmitry.ivaniuta.services@gmail.com) — [GitHub](https://github.com/DimitryIvaniuta)
