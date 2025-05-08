package com.jack.currency.schedule;

import com.jack.currency.client.OpenExchangeRatesClient;
import com.jack.currency.component.ExchangeRateCache;
import com.jack.currency.model.Currency;
import com.jack.currency.model.ExchangeRate;
import com.jack.currency.dto.ExchangeRatesResponse;
import com.jack.currency.repository.CurrencyRepository;
import com.jack.currency.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Scheduler component responsible for periodically refreshing exchange rates from the external API
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateScheduler {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final OpenExchangeRatesClient openExchangeRatesClient;
    private final ExchangeRateCache exchangeRateCache;
    
    @Value("${openexchangerates.api.app-id}")
    private String appId;
    
    /**
     * Refreshes exchange rates every hour by fetching the latest rates from the API
     * and storing them in the database and cache
     */
    @Scheduled(cron = "${openexchangerates.api.cron:0 0 */1 * * ?}") // Default: run every hour
    public void refreshRates() {
        log.info("Starting exchange rates refresh at {}", LocalDateTime.now());
        
        try {
            // Load all currencies from database
            List<Currency> currencies = currencyRepository.findAll();
            if (currencies.isEmpty()) {
                log.warn("No currencies found in database. Skipping refresh.");
                return;
            }
            
            log.info("Found {} currencies in database", currencies.size());
            
            // Group currencies by base currency to make efficient API calls
            Map<String, List<Currency>> currenciesByBase = currencies.stream()
                .collect(Collectors.groupingBy(Currency::getBase));
                
            int totalUpdated = 0;
            
            // Process each base currency group separately
            for (Map.Entry<String, List<Currency>> entry : currenciesByBase.entrySet()) {
                String baseCurrency = entry.getKey();
                List<Currency> currenciesForBase = entry.getValue();
                
                // Create a comma-separated list of currency codes for this base
                String symbols = currenciesForBase.stream()
                    .map(Currency::getCode)
                    .collect(Collectors.joining(","));
                
                log.info("Fetching exchange rates for base {} with symbols: {}", baseCurrency, symbols);
                    
                // Call API with the symbols parameter for this base currency
                ExchangeRatesResponse response = openExchangeRatesClient.getLatestExchangeRates(
                    appId,
                    baseCurrency,
                    symbols,
                    false, // prettyprint
                    false  // show_alternative
                );
                
                Map<String, Double> rates = response.getCurrencyRates();
                if (rates == null || rates.isEmpty()) {
                    log.warn("No rates returned from API for base currency: {}", baseCurrency);
                    continue;
                }
                
                String responseBase = response.getBase();
                LocalDateTime timestamp = Instant.ofEpochSecond(response.getUnixTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
                
                // Save each exchange rate for this base currency
                for (Map.Entry<String, Double> rateEntry : rates.entrySet()) {
                    String currencyCode = rateEntry.getKey();
                    Double rate = rateEntry.getValue();
                    
                    ExchangeRate exchangeRate = new ExchangeRate();
                    exchangeRate.setCurrencyCode(currencyCode);
                    exchangeRate.setBase(responseBase); // Use the base from the response
                    exchangeRate.setRate(BigDecimal.valueOf(rate));
                    exchangeRate.setTimestamp(timestamp);
                    
                    // Save to DB
                    ExchangeRate savedRate = exchangeRateRepository.save(exchangeRate);
                    
                    // Update cache
                    exchangeRateCache.updateRate(savedRate);
                    
                    totalUpdated++;
                }
            }
            
            log.info("Exchange rates refresh completed. Updated {} rates.", totalUpdated);
        } catch (Exception e) {
            log.error("Error during exchange rates refresh: ", e);
        }
    }
}