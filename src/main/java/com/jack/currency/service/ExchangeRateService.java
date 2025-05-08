package com.jack.currency.service;

import com.jack.currency.model.ExchangeRate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateService {
    List<ExchangeRate> getAllRates();
    Optional<ExchangeRate> getLatestRate(String currencyCode);
    ExchangeRate saveExchangeRate(ExchangeRate exchangeRate);
}