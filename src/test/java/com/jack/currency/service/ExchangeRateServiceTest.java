package com.jack.currency.service;

import com.jack.currency.client.OpenExchangeRatesClient;
import com.jack.currency.component.ExchangeRateCache;
import com.jack.currency.dto.ExchangeRatesResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        // Set app id via reflection
        ReflectionTestUtils.setField(exchangeRateService, "appId", "test-api-key");

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

    @Test
    void convertCurrency_ShouldCalculateCorrectly() {
        // Given - USD to EUR conversion where 1 USD = 0.85 EUR
        when(exchangeRateCache.getRate("USD")).thenReturn(Optional.of(usdRate));
        when(exchangeRateCache.getRate("EUR")).thenReturn(Optional.of(eurRate));

        // When - Convert 100 USD to EUR
        BigDecimal result = exchangeRateService.convertCurrency("USD", "EUR", BigDecimal.valueOf(100));

        // Then - 100 USD should be 85 EUR (100 / 1 * 0.85)
        assertEquals(BigDecimal.valueOf(85).setScale(2), result);
    }

    @Test
    void refreshRates_ShouldUpdateRatesAndCache() {
        // Given
        ExchangeRatesResponse response = new ExchangeRatesResponse();
        response.setBase("USD");
        response.setUnixTimestamp(System.currentTimeMillis() / 1000);
        
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("EUR", 0.85);
        response.setCurrencyRates(rates);
        
        when(openExchangeRatesClient.getLatestExchangeRates("test-api-key")).thenReturn(response);
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenAnswer(i -> i.getArgument(0));

        // When
        exchangeRateService.refreshRates();

        // Then
        verify(openExchangeRatesClient, times(1)).getLatestExchangeRates("test-api-key");
        verify(currencyRepository, times(1)).existsByCode("USD");
        verify(currencyRepository, times(1)).existsByCode("EUR");
        verify(exchangeRateRepository, times(2)).save(any(ExchangeRate.class));
        verify(exchangeRateCache, times(2)).updateRate(any(ExchangeRate.class));
    }
}