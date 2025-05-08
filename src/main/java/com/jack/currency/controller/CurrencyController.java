package com.jack.currency.controller;

import com.jack.currency.dto.CurrencyDto;
import com.jack.currency.dto.CurrencyUpdateDto;
import com.jack.currency.model.Currency;
import com.jack.currency.model.ExchangeRate;
import com.jack.currency.service.CurrencyService;
import com.jack.currency.service.ExchangeRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;
    
    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }
    
    @GetMapping("/{code}")
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createCurrency(@Valid @RequestBody CurrencyDto currencyDto) {
        try {
            Currency currency = new Currency();
            currency.setCode(currencyDto.getCode());
            currency.setName(currencyDto.getName());
            currency.setBase(currencyDto.getBase());
            currency.setCreatedAt(LocalDateTime.now());
            
            Currency createdCurrency = currencyService.createCurrency(currency);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCurrency);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{code}")
    public ResponseEntity<?> updateCurrency(@PathVariable String code, @Valid @RequestBody CurrencyUpdateDto updateDto) {
        try {
            Optional<Currency> existingCurrency = currencyService.getCurrencyByCode(code);
            if (existingCurrency.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Currency currency = existingCurrency.get();
            currency.setName(updateDto.getName());
            currency.setBase(updateDto.getBase());
            
            Currency updatedCurrency = currencyService.updateCurrency(currency);
            return ResponseEntity.ok(updatedCurrency);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshCurrencies() {
        currencyService.refreshCurrencies();
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/exchange-rates")
    public ResponseEntity<List<ExchangeRate>> getAllExchangeRates() {
        return ResponseEntity.ok(exchangeRateService.getAllRates());
    }
    
    @GetMapping("/exchange-rates/{currencyCode}/latest")
    public ResponseEntity<ExchangeRate> getLatestRate(@PathVariable String currencyCode) {
        return exchangeRateService.getLatestRate(currencyCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}