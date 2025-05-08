package com.jack.currency.service;

import com.jack.currency.client.OpenExchangeRatesClient;
import com.jack.currency.model.Currency;
import com.jack.currency.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final OpenExchangeRatesClient exchangeRatesClient;

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Override
    public Optional<Currency> getCurrencyByCode(String code) {
        return currencyRepository.findByCode(code);
    }

    @Override
    public Currency saveCurrency(Currency currency) {
        return currencyRepository.save(currency);
    }
    
    @Override
    public Currency createCurrency(Currency currency) {
        // Validate the currency code (typically 3 uppercase letters)
        if (currency.getCode() == null || !currency.getCode().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency code must be 3 uppercase letters");
        }
        
        // Check if currency with the same code already exists
        if (currencyRepository.existsByCode(currency.getCode())) {
            throw new IllegalArgumentException("Currency with code " + currency.getCode() + " already exists");
        }
        
        // Set creation timestamp if not set
        if (currency.getCreatedAt() == null) {
            currency.setCreatedAt(LocalDateTime.now());
        }
        
        log.info("Creating new currency: {}", currency.getCode());
        return currencyRepository.save(currency);
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?") // Run at 1 AM every day
    public void refreshCurrencies() {
        log.info("Starting currency refresh at {}", LocalDateTime.now());
        try {
            Map<String, String> currencies = exchangeRatesClient.getCurrencies().getCurrencies();
            int count = 0;
            
            for (Map.Entry<String, String> entry : currencies.entrySet()) {
                if (!currencyRepository.existsByCode(entry.getKey())) {
                    Currency currency = new Currency();
                    currency.setCode(entry.getKey());
                    currency.setName(entry.getValue());
                    currency.setCreatedAt(LocalDateTime.now());
                    currencyRepository.save(currency);
                    count++;
                }
            }
            
            log.info("Currency refresh completed. Added {} new currencies.", count);
        } catch (Exception e) {
            log.error("Error during currency refresh: ", e);
        }
    }
}