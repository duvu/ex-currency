package com.jack.currency.service;

import com.jack.currency.client.OpenExchangeRatesClient;
import com.jack.currency.component.ExchangeRateCache;
import com.jack.currency.model.Currency;
import com.jack.currency.model.ExchangeRate;
import com.jack.currency.repository.CurrencyRepository;
import com.jack.currency.repository.ExchangeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private OpenExchangeRatesClient openExchangeRatesClient;

    @Mock
    private ExchangeRateCache exchangeRateCache;

    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;

    private ExchangeRate usdRate;
    private ExchangeRate eurRate;
    private Currency usdCurrency;
    private Currency eurCurrency;

    @BeforeEach
    void setUp() {
        // Create test currency data
        usdCurrency = new Currency();
        usdCurrency.setId(1L);
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");

        eurCurrency = new Currency();
        eurCurrency.setId(2L);
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");

        // Create test exchange rate data
        LocalDateTime now = LocalDateTime.now();

        usdRate = new ExchangeRate();
        usdRate.setId(1L);
        usdRate.setCurrencyCode("USD");
        usdRate.setRate(BigDecimal.ONE);
        usdRate.setTimestamp(now);

        eurRate = new ExchangeRate();
        eurRate.setId(2L);
        eurRate.setCurrencyCode("EUR");
        eurRate.setRate(BigDecimal.valueOf(0.85));
        eurRate.setTimestamp(now);
    }

    @Test
    void getLatestRate_ShouldReturnFromCache_WhenCacheHit() {
        // Given
        when(exchangeRateCache.getRate("USD")).thenReturn(Optional.of(usdRate));

        // When
        Optional<ExchangeRate> result = exchangeRateService.getLatestRate("USD");

        // Then
        assertEquals(BigDecimal.ONE, result.get().getRate());
        verify(exchangeRateCache, times(1)).getRate("USD");
        verify(exchangeRateRepository, never()).findLatestRateByCurrencyCode(anyString());
    }

    @Test
    void getLatestRate_ShouldReturnFromRepository_WhenCacheMiss() {
        // Given
        when(exchangeRateCache.getRate("EUR")).thenReturn(Optional.empty());
        when(exchangeRateRepository.findLatestRateByCurrencyCode("EUR")).thenReturn(Optional.of(eurRate));

        // When
        Optional<ExchangeRate> result = exchangeRateService.getLatestRate("EUR");

        // Then
        assertEquals(BigDecimal.valueOf(0.85), result.get().getRate());
        verify(exchangeRateCache, times(1)).getRate("EUR");
        verify(exchangeRateRepository, times(1)).findLatestRateByCurrencyCode("EUR");
        verify(exchangeRateCache, times(1)).updateRate(any(ExchangeRate.class));
    }
}