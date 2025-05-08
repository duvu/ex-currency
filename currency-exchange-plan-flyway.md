
# 🛠️ Development Plan: Currency Exchange REST API (Spring Boot, Java 17)

## ✅ 1. Project Setup
- [ ] Initialize Spring Boot project (Java 17, Maven)
  - Dependencies:
    - Spring Web
    - Spring Data JPA
    - PostgreSQL Driver
    - Flyway Core
    - Spring Boot DevTools
    - Spring Boot Test
    - Spring Cloud OpenFeign
- [ ] Enable OpenFeign in main application class
  ```java
  @EnableFeignClients
  @SpringBootApplication
  public class CurrencyApp { ... }
  ```
- [ ] Create Git repository and push project
- [ ] Set up PostgreSQL using `docker-compose.yml`

---

## ✅ 2. Database Setup with Flyway
- [ ] Add Flyway config in `application.yml`
- [ ] Create SQL migration scripts:
  - `V1__create_currency_table.sql`
  - `V2__create_exchange_rate_table.sql`
- [ ] Run app to verify Flyway applies DB schema correctly

---

## ✅ 3. Domain Model
- [ ] Create entities:
  - `Currency` (id, code)
  - `ExchangeRate` (id, currency_code, rate, timestamp)
- [ ] JPA repositories:
  - `CurrencyRepository`
  - `ExchangeRateRepository`

---

## ✅ 4. In-Memory Caching
- [ ] Use `ConcurrentHashMap<String, ExchangeRate>` to store latest rates
- [ ] Expose service to access/update rates in memory

---

## ✅ 5. REST API
- [ ] `GET /currencies` — List all currencies
- [ ] `POST /currencies` — Add a new currency
- [ ] `GET /rates/{currency}` — Get exchange rate from in-memory map

---

## ✅ 6. External API Integration via OpenFeign
- [ ] Define `ExchangeRateClient` interface using Feign:
  ```java
  @FeignClient(name = "exchangeRates", url = "${openexchangerates.api.url}")
  public interface ExchangeRateClient {
      @GetMapping("/latest.json")
      ExchangeRateResponse getLatestRates(@RequestParam("app_id") String appId);
  }
  ```
- [ ] Map the response to a POJO: `ExchangeRateResponse`
- [ ] Inject `ExchangeRateClient` in service to fetch latest rates

---

## ✅ 7. Scheduled Tasks
- [ ] Use `@Scheduled(fixedRate = 3600000)` to update exchange rates every hour
- [ ] For each added currency:
  - Call `ExchangeRateClient`
  - Persist result to DB
  - Update in-memory cache

---

## ✅ 8. API Documentation
- [ ] Add Swagger UI using `springdoc-openapi-ui`
- [ ] Ensure endpoints appear in Swagger

---

## ✅ 9. Testing
- [ ] Unit tests (JUnit 5):
  - Services, Feign client (mocked), Controllers
- [ ] Integration tests for:
  - End-to-end API flow
  - DB interactions

---

## ✅ 10. README & Delivery
- [ ] Write a complete `README.md`:
  - How to start PostgreSQL (via Docker)
  - How to run the app
  - Example API usage
- [ ] Push entire project and changelogs to GitHub

---

## 🔖 Notes
- Only fetch rates for currencies added by user
- Use Feign instead of `RestTemplate` or `WebClient`
- No security or detailed error handling is needed
