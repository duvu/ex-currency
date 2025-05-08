# Currency Exchange API

A Spring Boot application that provides real-time currency exchange rates and conversion functionality via a RESTful API.

## Features

- Currency management (list, add, search)
- In-memory caching for optimal performance
- Automated hourly updates from OpenExchangeRates API
- Swagger UI documentation

## Requirements

- Java 17 or higher
- Docker and Docker Compose (for PostgreSQL)
- Maven (or use the included Maven wrapper)
- OpenExchangeRates API key (free tier available at https://openexchangerates.org/signup/free)

## Running the Application

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/currency-project.git
cd currency-project
```

### Step 2: Start PostgreSQL Database

The application uses PostgreSQL for data storage. Use Docker Compose to start a pre-configured PostgreSQL instance:

```bash
docker-compose up -d
```

This command starts PostgreSQL on port 5433 with the following configuration:
- Database: currency_db
- Username: currency_user
- Password: currency_password

### Step 3: Configure API Key

Add your OpenExchangeRates API key to the application configuration:

```bash
# Edit application.yml
nano src/main/resources/application.yml

# Update the following property:
# openexchangerates.api.app-id: your-api-key-here
```

### Step 4: Run the Application

Using Maven:

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/currency-project-0.0.1-SNAPSHOT.jar
```

The application will start on port 9090.

### Step 5: Access the API

- API Base URL: http://localhost:9090
- Swagger UI Documentation: http://localhost:9090/swagger-ui.html

## API Usage Examples

### Get All Currencies

```bash
curl http://localhost:9090/api/currencies
```

### Get Latest Exchange Rate

```bash
curl http://localhost:9090/api/currencies/exchange-rates/EUR/latest
```

### Add a New Currency

```bash
curl -X POST http://localhost:9090/api/currencies \
  -H "Content-Type: application/json" \
  -d '{"code": "JPY", "name": "Japanese Yen"}'
```

### Refresh Exchange Rates

```bash
curl -X POST http://localhost:9090/api/currencies/refresh
```
## Running Tests

Execute the test suite using:

```bash
./mvnw test
```

Generate a test coverage report with JaCoCo:

```bash
./mvnw verify
```

The coverage report will be available at: `target/site/jacoco/index.html`
