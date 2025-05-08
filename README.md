# Currency Exchange REST API

A Spring Boot application that provides currency exchange rates via REST API.

## Features

- Currency management (list, add, search)
- Exchange rate tracking with historical data
- Currency conversion
- In-memory caching for performance
- Scheduled updates from OpenExchangeRates API
- RESTful API with Swagger documentation

## Tech Stack

- Java 17
- Spring Boot 3.4.4
- Spring Data JPA
- Spring Cloud OpenFeign
- PostgreSQL
- Flyway for database migrations
- Docker for PostgreSQL
- Swagger UI for API documentation

## Getting Started

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- OpenExchangeRates API key (https://openexchangerates.org/signup/free)

### Running the Application

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd currency-project
   ```

2. Configure your OpenExchangeRates API Key:
   
   Update the `openexchangerates.api.app-id` property in `src/main/resources/application.yml`:
   
   ```yaml
   openexchangerates:
     api:
       url: https://openexchangerates.org/api
       app-id: your-api-key-here  # Replace with your API key
   ```

3. Start PostgreSQL database using Docker Compose:
   ```bash
   docker-compose up -d
   ```

4. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

5. The application will be available at: http://localhost:8080
   
   Swagger UI documentation is available at: http://localhost:8080/swagger-ui.html

## API Endpoints

### Currencies API

- `GET /api/currencies` - Get all currencies
- `GET /api/currencies/{code}` - Get currency by code
- `POST /api/currencies/refresh` - Refresh currencies from OpenExchangeRates

### Exchange Rates API

- `GET /api/exchange-rates` - Get all exchange rates
- `GET /api/exchange-rates/{currencyCode}/latest` - Get latest exchange rate for a currency
- `GET /api/exchange-rates/{currencyCode}/history` - Get exchange rate history for a currency
- `GET /api/exchange-rates/convert` - Convert amount from one currency to another
- `POST /api/exchange-rates/refresh` - Refresh exchange rates from OpenExchangeRates

## Example Usage

### Currency Conversion

To convert 100 EUR to USD:

```
GET /api/exchange-rates/convert?from=EUR&to=USD&amount=100
```

Response:
```json
{
  "from": "EUR",
  "to": "USD",
  "amount": 100,
  "convertedAmount": 108.25,
  "timestamp": "2025-05-08T12:34:56"
}
```

## Database Schema

The application uses two main tables:

1. `currency` - Stores currency information
   - `id` - Primary key
   - `code` - Currency code (e.g., USD, EUR)
   - `name` - Currency name
   - `created_at` - Creation timestamp

2. `exchange_rate` - Stores exchange rate history
   - `id` - Primary key
   - `currency_code` - Foreign key to currency.code
   - `rate` - Exchange rate value
   - `timestamp` - Rate timestamp