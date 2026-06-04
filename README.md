# TradePositionTracker

An FX trade position tracking application built with Spring Boot. Processes foreign exchange trade executions between two parties and maintains real-time currency positions with settlement lifecycle management.

## Tech Stack

- Java 21, Spring Boot 3.2
- Spring Data JPA with H2 in-memory database
- Flyway for database migrations
- Lombok for reducing boilerplate
- JPA Auditing for audit trail (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`)

## How to Run

```bash
mvn spring-boot:run
```

The application starts on port 8080. H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tradepositiontracker`, username: `sa`, no password).

## API Endpoints

### Trade Management

**Book a trade**
```
POST /api/trades
{
  "tradeReference": "TRD-001",
  "tradingParty": "Goldman Sachs",
  "counterParty": "Morgan Stanley",
  "primaryCurrency": "USD",
  "primaryAmount": 1000000.0000,
  "secondaryCurrency": "JPY",
  "secondaryAmount": 150000000.0000,
  "direction": "BUY",
  "valueDate": "2026-06-06"
}
```

**List trades (paginated)**
```
GET /api/trades?page=0&size=20&sort=createdAt,desc
GET /api/trades?status=PENDING&page=0&size=10
```

**Get a specific trade**
```
GET /api/trades/{tradeReference}
```

**Amend a trade**
```
PUT /api/trades/{tradeReference}/amend
{ ...updated trade fields... }
```

### Settlement Lifecycle

```
PUT /api/trades/{tradeReference}/match    — counterparty confirms
PUT /api/trades/{tradeReference}/settle   — mark as settled
PUT /api/trades/{tradeReference}/cancel   — cancel the trade
```

### Position Queries

```
GET /api/positions/{party}                                   — all positions
GET /api/positions/{party}?bucket=T0                         — settling today
GET /api/positions/{party}?bucket=T1                         — settling tomorrow
GET /api/positions/{party}?bucket=T2                         — settling T+2
GET /api/positions/{party}?bucket=FORWARD                    — beyond T+2
GET /api/positions/{party}?from=2026-06-04&to=2026-06-10     — date range
GET /api/positions/{party}/{currency}                        — positions in a specific currency
```

### Position History (Audit Trail)

```
GET /api/positions/{party}/history                           — full audit trail
GET /api/positions/{party}/{currency}/history                — audit trail for a currency
```

### Exchange Rates

```
POST /api/exchange-rates
{
  "currency": "JPY",
  "rateToUsd": 0.0067
}

GET /api/exchange-rates/{currency}
```

## Application Responsibilities

### 1. Trade Ingestion

- Accept FX trade executions via REST API
- Each trade involves two parties: a **trading party** and a **counter party**
- Each trade specifies a **primary currency** with its amount, and a **secondary currency** with its amount
- The **direction** (BUY or SELL) is from the trading party's perspective on the primary currency
- Validate all fields: both parties must be present and different, both currencies must be present and different, both amounts must be positive, direction must be BUY or SELL, value date is required
- A trade reference must be unique across the system
- Persist every trade to the database with status PENDING

### 2. Position Management

- Maintain a position per **party, per currency, per value date**
- Each trade creates **four position entries** — both parties in both currencies
- When the trading party **BUYs** the primary currency:
  - Trading party: **exposure** (expects to receive) in primary currency, **obligation** (must pay) in secondary currency
  - Counter party: **exposure** in secondary currency, **obligation** in primary currency
- When the trading party **SELLs** the primary currency, the exposure and obligation assignments are **reversed** compared to BUY:
  - Trading party: **obligation** in primary currency, **exposure** in secondary currency
  - Counter party: **obligation** in secondary currency, **exposure** in primary currency
- All amounts use BigDecimal with 4 decimal places to preserve precision

### 3. Settlement Lifecycle

Trades follow a defined lifecycle:
```
PENDING → MATCHED → SETTLED
                  → CANCELLED
         → CANCELLED
```

- **PENDING**: trade is booked, positions reflect exposure and obligation
- **MATCHED**: counterparty has confirmed the trade (positions unchanged)
- **SETTLED**: amounts are exchanged — exposure and obligation are reduced, net position is updated
  - The receiving party's exposure decreases and net position increases
  - The paying party's obligation decreases and net position decreases (can go negative — means they paid out more than received)
- **CANCELLED**: trade is reversed — all position entries created by the trade are undone
- Only PENDING or MATCHED trades can be amended, settled, or cancelled

### 4. Trade Amendment

- A PENDING or MATCHED trade can be amended with new amounts, currencies, parties, or value date
- Amendment process: reverse the original position entries, then apply new ones
- The trade reference remains the same; only the trade details change

### 5. Value Date Bucketing

Positions are stored by actual value date but can be queried by settlement bucket:
- **T0**: trades settling today — requires immediate funding
- **T1**: trades settling tomorrow
- **T2**: trades settling in two days (standard FX spot)
- **FORWARD**: all trades settling beyond T+2

### 6. USD Equivalent

- Each position entry carries a USD equivalent of its net position
- Computed using exchange rates stored via the Exchange Rate API
- USD positions have a 1:1 equivalent
- Updated whenever a position changes and an exchange rate is available

### 7. Position Audit Trail

- Every position change is recorded in the position history table
- Each record captures: party, currency, value date, trade reference, action type, and before/after values for exposure, obligation, and net position
- Actions: TRADE_BOOKED, TRADE_REVERSED, TRADE_SETTLED

## Example Walkthrough

**Trade**: Goldman Sachs BUYs 1,000,000 USD / 150,000,000 JPY from Morgan Stanley, value date 2026-06-06

**On booking (PENDING):**

| Party | Currency | Value Date | Exposure | Obligation | Net Position |
|---|---|---|---|---|---|
| Goldman Sachs | USD | 2026-06-06 | 1,000,000.0000 | 0.0000 | 0.0000 |
| Goldman Sachs | JPY | 2026-06-06 | 0.0000 | 150,000,000.0000 | 0.0000 |
| Morgan Stanley | USD | 2026-06-06 | 0.0000 | 1,000,000.0000 | 0.0000 |
| Morgan Stanley | JPY | 2026-06-06 | 150,000,000.0000 | 0.0000 | 0.0000 |

**On settlement:**

| Party | Currency | Value Date | Exposure | Obligation | Net Position |
|---|---|---|---|---|---|
| Goldman Sachs | USD | 2026-06-06 | 0.0000 | 0.0000 | +1,000,000.0000 |
| Goldman Sachs | JPY | 2026-06-06 | 0.0000 | 0.0000 | -150,000,000.0000 |
| Morgan Stanley | USD | 2026-06-06 | 0.0000 | 0.0000 | -1,000,000.0000 |
| Morgan Stanley | JPY | 2026-06-06 | 0.0000 | 0.0000 | +150,000,000.0000 |

Goldman Sachs received USD (net positive) and paid JPY (net negative). Morgan Stanley received JPY and paid USD.

## Project Structure

```
src/main/java/com/tradepositiontracker/
├── TradePositionTrackerApplication.java
├── config/
│   └── JpaAuditingConfig.java
├── dto/
│   ├── TradeRequest.java / TradeResponse.java
│   ├── PositionResponse.java
│   ├── PositionHistoryResponse.java
│   └── ExchangeRateRequest.java / ExchangeRateResponse.java
├── enums/
│   ├── Direction.java
│   ├── TradeStatus.java
│   └── PositionAction.java
├── model/
│   ├── Auditable.java              ← MappedSuperclass with JPA audit fields
│   ├── Trade.java
│   ├── Position.java
│   ├── PositionHistory.java
│   └── ExchangeRate.java
├── repository/
│   ├── TradeRepository.java
│   ├── PositionRepository.java
│   ├── PositionHistoryRepository.java
│   └── ExchangeRateRepository.java
├── service/
│   ├── TradeValidationService.java
│   ├── TradeService.java
│   ├── PositionService.java
│   ├── PositionHistoryService.java
│   ├── SettlementService.java
│   └── ExchangeRateService.java
└── controller/
    ├── TradeController.java
    ├── SettlementController.java
    ├── PositionController.java
    ├── PositionHistoryController.java
    ├── ExchangeRateController.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.properties
└── db/migration/
    ├── V1__create_trades_table.sql
    ├── V2__create_positions_table.sql
    ├── V3__create_position_history_table.sql
    └── V4__create_exchange_rates_table.sql
```
