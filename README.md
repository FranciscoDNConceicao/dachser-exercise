# Dachser Exercise – Calculate Profit

Calculates the profit or loss of each shipment: **Profit = Income − (Cost + Additional Cost)**.

Each calculation is independent, as in the UI prototype: it stores the income and costs entered
with it and its result, and appears as one row of the shipment's history. Deleting a calculation
also deletes its income and costs.

- **Backend:** Java 17, Spring Boot 3.1.5, Maven 3.9.3, H2 (in-memory)
- **Frontend:** Angular 17.1.0, Node 21.0.0, Angular Material 17.3.1, Bootstrap 5.3.3

## Project structure

```
backend/      Spring Boot API (entities, repositories, DTOs, mappers, services, controller)
frontend/     Angular application
database/     schema.sql (tables, relations, indexes), data.sql (sample data) and the model diagram
collection/   Postman collection
```

## How to run

**1. Backend** (port 8000)

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Requires JDK 17. The database is created and filled automatically from `database/` on startup.

**2. Frontend** (port 4200)

```bash
cd frontend
npm install
npm start
```

`npm start` runs the Angular dev server with Node 21.0.0 (through `npx`).

Open **http://localhost:4200**.

| What | URL |
|---|---|
| Application | http://localhost:4200 |
| API | http://localhost:8000/api/shipments |
| H2 console | http://localhost:8000/h2-console (JDBC URL `jdbc:h2:mem:profitdb`, user `sa`, no password) |

## API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/shipments` | List shipments |
| GET | `/api/shipments/code/{code}` | Find a shipment by code |
| POST | `/api/shipments/{id}/profit-calculations` | Store the income and costs as a new calculation and return its result |
| GET | `/api/shipments/{id}/profit-calculations` | Calculation history (most recent first) |
| DELETE | `/api/shipments/{id}/profit-calculations/{calculationId}` | Delete a calculation with its income and costs |

Example request body (`income` and `cost` are required, `additionalCost` is optional):

```json
{ "income": 230.5, "cost": 200, "additionalCost": 0 }
```

Result: income 230.50, total costs 200.00, profit 30.50.

Errors are returned as `ProblemDetail` (404 shipment or calculation not found, 400 invalid data).
The Postman collection is in `collection/`.

## Tests

```bash
cd backend
./mvnw test
```
