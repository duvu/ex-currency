package com.jack.currency.service;

import com.jack.currency.client.OpenExchangeRatesClient;
import com.jack.currency.component.ExchangeRateCache;
import com.jack.currency.model.ExchangeRate;
import com.jack.currency.repository.CurrencyRepository;
import com.jack.currency.repository.ExchangeRateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final OpenExchangeRatesClient openExchangeRatesClient;
    private final ExchangeRateCache exchangeRateCache;
    
    @Value("${openexchangerates.api.app-id}")
    private String appId;

    @PostConstruct
    public void init() {
        // Initialize cache with latest rates from DB on startup
        log.info("Loading initial exchange rates into cache");
        exchangeRateRepository.findAll().forEach(rate -> 
            exchangeRateCache.updateRate(rate));
    }

    @Override
    public List<ExchangeRate> getAllRates() {
        return exchangeRateRepository.findAll();
    }

    @Override
    public Optional<ExchangeRate> getLatestRate(String currencyCode) {
        // First check in cache
        Optional<ExchangeRate> cachedRate = exchangeRateCache.getRate(currencyCode);
        if (cachedRate.isPresent()) {
            log.debug("Cache hit for currency: {}", currencyCode);
            return cachedRate;
        }
        
        // If not in cache, get from DB
        log.debug("Cache miss for currency: {}, fetching from DB", currencyCode);
        Optional<ExchangeRate> dbRate = exchangeRateRepository.findLatestRateByCurrencyCode(currencyCode);
        
        // Update cache if found in DB
        dbRate.ifPresent(exchangeRateCache::updateRate);
        
        return dbRate;
    }

    @Override
    public List<ExchangeRate> getRatesForPeriod(String currencyCode, LocalDateTime startTime, LocalDateTime endTime) {
        return exchangeRateRepository.findByCurrencyCodeAndTimestampBetweenOrderByTimestampDesc(
                currencyCode, startTime, endTime);
    }

    @Override
    public ExchangeRate saveExchangeRate(ExchangeRate exchangeRate) {
        ExchangeRate savedRate = exchangeRateRepository.save(exchangeRate);
        // Update cache
        exchangeRateCache.updateRate(savedRate);
        return savedRate;
    }

    @Override
    public BigDecimal convertCurrency(String fromCurrency, String toCurrency, BigDecimal amount) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }
        
        // Get latest rates for both currencies (against USD base)
        Optional<ExchangeRate> fromRateOpt = getLatestRate(fromCurrency);
        Optional<ExchangeRate> toRateOpt = getLatestRate(toCurrency);
        
        if (fromRateOpt.isEmpty() || toRateOpt.isEmpty()) {
            throw new RuntimeException("Exchange rate not available for one or both currencies");
        }
        
        BigDecimal fromRate = fromRateOpt.get().getRate();
        BigDecimal toRate = toRateOpt.get().getRate();
        
        // Convert: amount in fromCurrency -> USD -> toCurrency
        // First, convert to USD (divide by fromRate)
        BigDecimal amountInUsd = amount.divide(fromRate, 6, RoundingMode.HALF_UP);
        // Then, convert from USD to target currency (multiply by toRate)
        return amountInUsd.multiply(toRate).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Scheduled(cron = "0 0 */1 * * ?") // Run every hour
    public void refreshRates() {
        log.info("Starting exchange rates refresh at {}", LocalDateTime.now());
        try {
            var response = openExchangeRatesClient.getLatestExchangeRates(appId);
            Map<String, Double> rates = response.getCurrencyRates();
            LocalDateTime timestamp = Instant.ofEpochSecond(response.getUnixTimestamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            int count = 0;
            for (Map.Entry<String, Double> entry : rates.entrySet()) {
                String currencyCode = entry.getKey();
                
                // Skip if currency doesn't exist in our database
                if (!currencyRepository.existsByCode(currencyCode)) {
                    continue;
                }
                
                ExchangeRate exchangeRate = new ExchangeRate();
                exchangeRate.setCurrencyCode(currencyCode);
                exchangeRate.setRate(BigDecimal.valueOf(entry.getValue()));
                exchangeRate.setTimestamp(timestamp);
                
                // Save to DB
                ExchangeRate savedRate = exchangeRateRepository.save(exchangeRate);
                
                // Update cache
                exchangeRateCache.updateRate(savedRate);
                
                count++;
            }
            
            log.info("Exchange rates refresh completed. Updated {} rates.", count);
        } catch (Exception e) {
            log.error("Error during exchange rates refresh: ", e);
        }
    }
}