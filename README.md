# Mini-Statement Analyzer

A Spring Boot–based REST service that ingests raw bank statement transactions, processes them **asynchronously**, sanitizes messy data, and computes key financial risk metrics.

This project was built as part of a **technical assignment** to demonstrate backend design, concurrency handling, financial precision, and clean architecture.

---

## Problem Overview

Financial systems often receive **unstructured and inconsistent transaction data** from third-party aggregators.
This service:

* Accepts raw transaction data
* Processes it **off the main thread**
* Handles invalid / duplicate / future-dated transactions
* Computes risk metrics safely using `BigDecimal`
* Exposes results via a polling API

---

## Architecture & Design

The project follows **clear separation of concerns**:

```
src/main/java/com/assignment
│
├── config
│   └── AsyncConfig.java
│
├── controller
│   └── StatementAnalysisController.java
│
├── entity
│   ├── AnalysisJob.java
│   ├── AnalysisResult.java
│   ├── FlaggedTransaction.java
│   ├── RiskAnalysis.java
│   ├── StatementRequest.java
│   ├── Transaction.java
│   ├── enums (TransactionType, JobStatus)
│
├── exception
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
│
├── repository
│   └── InMemoryJobRepository.java
│
└── service
    ├── AnalysisOrchestratorService.java
    └── StatementAnalysisService.java
```

### Key Design Decisions

* **Async Processing**: `@Async` + `CompletableFuture` style background execution
* **Thread-safe storage**: `ConcurrentHashMap` for in-memory job tracking
* **Financial safety**: Strict use of `BigDecimal` (no `double`/`float`)
* **Fail-safe input handling**: Defensive checks for nulls, duplicates, and invalid dates
* **Stateless API layer**: All business logic isolated in service classes

---

## API Endpoints

### Ingest Statement (Async)

**POST** `/api/v1/analyze`

* Accepts raw transaction data
* Immediately returns `202 Accepted`
* Processing happens in the background

#### Sample Request

```json
{
  "customerName": "John Doe",
  "statementDate": "2024-02-01",
  "transactions": [
    {
      "transactionId": "TXN_001",
      "date": "2024-01-15",
      "amount": 50000.00,
      "type": "CREDIT",
      "description": "Salary"
    },
    {
      "transactionId": "TXN_002",
      "date": "2024-01-18",
      "amount": 12000.00,
      "type": "DEBIT",
      "description": "Rent Payment"
    }
  ]
}
```

#### Response

```json
{
  "jobId": "c9b6e9b4-7c4e-4c8d-bc9a-6a4c2b0e3c91"
}
```

---

### Fetch Analysis Result

**GET** `/api/v1/analyze/{jobId}`

#### While Processing

```json
{
  "jobId": "c9b6e9b4-7c4e-4c8d-bc9a-6a4c2b0e3c91",
  "status": "IN_PROGRESS"
}
```

#### When Completed

```json
{
  "jobId": "c9b6e9b4-7c4e-4c8d-bc9a-6a4c2b0e3c91",
  "status": "COMPLETED",
  "result": {
    "totalCredit": 50000.00,
    "totalDebit": 12000.00,
    "balance": 38000.00,
    "riskAnalysis": {
      "hasSuspiciousActivity": true,
      "flaggedTransactions": [
        {
          "transactionId": "TXN_002",
          "reason": "Debit amount 12000.00 exceeds 20% of total income."
        }
      ]
    }
  }
}
```

---

## Business Logic (Math Engine)

Handled inside `StatementAnalysisService`

### Rules Implemented

* **Total Credit**: Sum of all valid `CREDIT` transactions
* **Total Debit**: Sum of all valid `DEBIT` transactions
* **Balance**: `totalCredit - totalDebit`
* **Risk Rule**:

    * Any single debit > **20% of total credit** is flagged as suspicious

### Data Sanitization Rules

* Null amounts → skipped
* Future-dated transactions → skipped
* Duplicate transaction IDs → first occurrence only
* Invalid dates → safely handled via global exception handler

---

## Testing Strategy

```
src/test/java/com/assignment
│
├── loadTester
│   └── StatementLoadTester.java
│
└── scenarios
    └── StatementAnalysisServiceTest.java
```

### Included Tests

* High-value debit triggers risk flag
* Duplicate transactions are ignored
* Null amount transactions are skipped
* Future-dated transactions are excluded

> Tests focus on **business correctness**, not framework wiring.

---

## How to Run Locally

### Prerequisites

* Java 17+
* Maven 3.8+

### Steps

```bash
git clone https://github.com/Devansh-ds/StatementAnalyzer_Assignment.git
cd StatementAnalyzer_Assignment/StatementAnalyzer
mvn clean install
mvn spring-boot:run
```

Application will start on:

```
http://localhost:8080
```

---

## 🛠️ Tech Stack

* Java 17
* Spring Boot
* Spring Async (`@Async`)
* JUnit 5
* Maven
* ConcurrentHashMap (in-memory storage)

---