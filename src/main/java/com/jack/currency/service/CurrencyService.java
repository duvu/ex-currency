package com.jack.currency.service;

import com.jack.currency.model.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyService {
    List<Currency> getAllCurrencies();
    Optional<Currency> getCurrencyByCode(String code);
    Currency saveCurrency(Currency currency);
    void refreshCurrencies();
    
    /**
     * Creates a new currency if it doesn't exist
     * @param currency The currency to create
     * @return The created currency
     * @throws IllegalArgumentException if currency with the same code already exists
     */
    Currency createCurrency(Currency currency);
}