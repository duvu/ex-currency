package com.jack.currency.service;

import com.jack.currency.model.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    List<ExchangeRate> getAllRates();
    Optional<ExchangeRate> getLatestRate(String currencyCode);
    List<ExchangeRate> getRatesForPeriod(String currencyCode, LocalDateTime startTime, LocalDateTime endTime);
    ExchangeRate saveExchangeRate(ExchangeRate exchangeRate);
    BigDecimal convertCurrency(String fromCurrency, String toCurrency, BigDecimal amount);
    void refreshRates();
}