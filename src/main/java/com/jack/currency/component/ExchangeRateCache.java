package com.jack.currency.component;

import com.jack.currency.model.ExchangeRate;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ExchangeRateCache {
    
    private final Map<String, ExchangeRate> latestRates = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("Initializing exchange rate cache");
    }
    
    public void updateRate(ExchangeRate exchangeRate) {
        latestRates.put(exchangeRate.getCurrencyCode(), exchangeRate);
    }
    
    public Optional<ExchangeRate> getRate(String currencyCode) {
        return Optional.ofNullable(latestRates.get(currencyCode));
    }
    
    public Map<String, ExchangeRate> getAllRates() {
        return latestRates;
    }
    
    public void clear() {
        latestRates.clear();
    }
}