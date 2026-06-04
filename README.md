# TradePositionTracker

A Spring Boot application that processes trade executions and maintains real-time net positions per trading party per instrument, with profit and loss calculation.

## How to Run

```bash
mvn spring-boot:run
```

The application starts on port 8080. H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:tradepositiontracker`, username: `sa`, no password).

## API Endpoints

### Submit a Trade
```
POST /api/trades
{
  "tradingParty": "Alice",
  "instrument": "AAPL",
  "side": "BUY",
  "quantity": 100,
  "price": 150.50
}
```

### Query Positions
```
GET /api/positions                           — all positions
GET /api/positions/{tradingParty}            — positions for a trading party
GET /api/positions/{tradingParty}/{instrument} — specific position with unrealized P&L
```

### Update Market Price
```
POST /api/market-prices
{
  "instrument": "AAPL",
  "price": 155.00
}

GET /api/market-prices/{instrument}
```

## Application Responsibilities

### 1. Trade Ingestion
- Accept trade executions via REST API
- Each trade has: trading party, instrument, side (BUY or SELL), quantity, and price
- Validate that all fields are present, side is BUY or SELL, and quantity and price are positive
- Persist every trade to the database

### 2. Position Management
- Maintain a net position per trading party per instrument
- Position quantity is positive for long positions, negative for short positions
- A BUY trade increases the position quantity; a SELL trade decreases it
- When adding to an existing position (same direction), update the average cost price using a weighted average:
  ```
  new average cost = (existing quantity * existing average cost + new quantity * new price) / (existing quantity + new quantity)
  ```
  This calculation must preserve decimal precision.

### 3. Realized Profit and Loss
- Realized profit and loss is calculated when a position is **reduced or closed** (trade in the opposite direction)
- For a long position being sold: `realized P&L = closing quantity * (sell price - average cost price)`
- For a short position being bought: `realized P&L = closing quantity * (average cost price - buy price)`
- **Position flip**: when a single trade flips the position from long to short (or vice versa), it must be treated as two parts:
  1. **Close** the existing position — the closing quantity is the size of the existing position, not the full trade quantity
  2. **Open** a new position in the opposite direction with the remaining quantity at the trade price
- Realized P&L only applies to the closing portion of a flip trade

### 4. Unrealized Profit and Loss
- Calculated against the current market price for the instrument
- For a long position: `unrealized P&L = position quantity * (market price - average cost price)`
- For a short position: `unrealized P&L = |position quantity| * (average cost price - market price)`
- Requires a market price to be set for the instrument

### 5. Market Price Management
- Accept and store current market prices per instrument
- Used by the unrealized profit and loss calculation

## Example Walkthrough

| # | Trade | Position After | Avg Cost | Realized P&L |
|---|-------|---------------|----------|-------------|
| 1 | BUY 100 AAPL @ 150.50 | +100 | 150.50 | 0 |
| 2 | BUY 50 AAPL @ 153.00 | +150 | 151.33 | 0 |
| 3 | SELL 80 AAPL @ 155.00 | +70 | 151.33 | 293.60 |
| 4 | SELL 100 AAPL @ 148.00 | -30 | 148.00 | 293.60 + 70*(148.00-151.33) = 60.50 |

In trade #4, the position flips from +70 to -30. This is split into:
- Close +70 at 148.00: realized P&L = 70 * (148.00 - 151.33) = -233.10
- Open -30 (short) at 148.00: new position with average cost 148.00

Cumulative realized P&L after trade #4 = 293.60 + (-233.10) = 60.50
