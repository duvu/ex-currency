package com.jack.currency.controller;

import com.jack.currency.model.Currency;
import com.jack.currency.model.ExchangeRate;
import com.jack.currency.service.CurrencyService;
import com.jack.currency.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> createCurrency(@RequestBody Currency currency) {
        try {
            Currency createdCurrency = currencyService.createCurrency(currency);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCurrency);
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