package com.jack.currency.service;

import com.jack.currency.client.OpenExchangeRatesClient;
import com.jack.currency.component.ExchangeRateCache;
import com.jack.currency.model.ExchangeRate;
import com.jack.currency.repository.CurrencyRepository;
import com.jack.currency.repository.ExchangeRateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final OpenExchangeRatesClient openExchangeRatesClient;
    private final ExchangeRateCache exchangeRateCache;

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
    public ExchangeRate saveExchangeRate(ExchangeRate exchangeRate) {
        ExchangeRate savedRate = exchangeRateRepository.save(exchangeRate);
        // Update cache
        exchangeRateCache.updateRate(savedRate);
        return savedRate;
    }
}