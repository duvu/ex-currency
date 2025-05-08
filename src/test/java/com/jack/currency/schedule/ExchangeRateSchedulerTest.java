package com.jack.currency.schedule;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateSchedulerTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private OpenExchangeRatesClient openExchangeRatesClient;

    @Mock
    private ExchangeRateCache exchangeRateCache;

    @InjectMocks
    private ExchangeRateScheduler exchangeRateScheduler;

    @BeforeEach
    void setUp() {
        // Set app id via reflection
        ReflectionTestUtils.setField(exchangeRateScheduler, "appId", "test-api-key");
        // Removed baseCurrency field as it's no longer used in ExchangeRateScheduler
    }

    @Test
    void refreshRates_ShouldUpdateRatesForAllCurrencies() {
        // Given
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        usdCurrency.setBase("USD");

        Currency eurCurrency = new Currency();
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");
        eurCurrency.setBase("USD");
        
        Currency jpyCurrency = new Currency();
        jpyCurrency.setCode("JPY");
        jpyCurrency.setName("Japanese Yen");
        jpyCurrency.setBase("USD");

        // All currencies have the same base "USD"
        List<Currency> currencies = Arrays.asList(usdCurrency, eurCurrency, jpyCurrency);
        when(currencyRepository.findAll()).thenReturn(currencies);

        // Create API response
        ExchangeRatesResponse response = new ExchangeRatesResponse();
        response.setBase("USD");
        response.setUnixTimestamp(System.currentTimeMillis() / 1000);
        
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("EUR", 0.85);
        rates.put("JPY", 110.15);
        response.setCurrencyRates(rates);
        
        // Mock API response
        when(openExchangeRatesClient.getLatestExchangeRates(
                eq("test-api-key"), 
                eq("USD"), 
                eq("USD,EUR,JPY"),
                eq(false),
                eq(false)))
            .thenReturn(response);
                
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenAnswer(i -> i.getArgument(0));

        // When
        exchangeRateScheduler.refreshRates();

        // Then
        verify(currencyRepository, times(1)).findAll();
        
        // Verify API call with all currency codes
        verify(openExchangeRatesClient, times(1))
                .getLatestExchangeRates("test-api-key", "USD", "USD,EUR,JPY", false, false);
                
        // Verify 3 rates were saved (USD, EUR, JPY)
        verify(exchangeRateRepository, times(3)).save(any(ExchangeRate.class));
        verify(exchangeRateCache, times(3)).updateRate(any(ExchangeRate.class));
    }
    
    @Test
    void refreshRates_ShouldHandleMultipleBaseCurrencies() {
        // Given
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        usdCurrency.setBase("USD");

        Currency eurCurrency = new Currency();
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");
        eurCurrency.setBase("EUR"); // Using EUR as base
        
        Currency jpyCurrency = new Currency();
        jpyCurrency.setCode("JPY");
        jpyCurrency.setName("Japanese Yen");
        jpyCurrency.setBase("USD");

        // Currencies have different bases
        List<Currency> currencies = Arrays.asList(usdCurrency, eurCurrency, jpyCurrency);
        when(currencyRepository.findAll()).thenReturn(currencies);

        // Create API response for USD base
        ExchangeRatesResponse usdResponse = new ExchangeRatesResponse();
        usdResponse.setBase("USD");
        usdResponse.setUnixTimestamp(System.currentTimeMillis() / 1000);
        
        Map<String, Double> usdRates = new HashMap<>();
        usdRates.put("USD", 1.0);
        usdRates.put("JPY", 110.15);
        usdResponse.setCurrencyRates(usdRates);
        
        // Create API response for EUR base
        ExchangeRatesResponse eurResponse = new ExchangeRatesResponse();
        eurResponse.setBase("EUR");
        eurResponse.setUnixTimestamp(System.currentTimeMillis() / 1000);
        
        Map<String, Double> eurRates = new HashMap<>();
        eurRates.put("EUR", 1.0);
        eurResponse.setCurrencyRates(eurRates);
        
        // Mock API responses
        when(openExchangeRatesClient.getLatestExchangeRates(
                eq("test-api-key"), 
                eq("USD"), 
                eq("USD,JPY"),
                eq(false),
                eq(false)))
            .thenReturn(usdResponse);
            
        when(openExchangeRatesClient.getLatestExchangeRates(
                eq("test-api-key"), 
                eq("EUR"), 
                eq("EUR"),
                eq(false),
                eq(false)))
            .thenReturn(eurResponse);
                
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenAnswer(i -> i.getArgument(0));

        // When
        exchangeRateScheduler.refreshRates();

        // Then
        verify(currencyRepository, times(1)).findAll();
        
        // Verify API calls for each base currency
        verify(openExchangeRatesClient, times(1))
                .getLatestExchangeRates("test-api-key", "USD", "USD,JPY", false, false);
        verify(openExchangeRatesClient, times(1))
                .getLatestExchangeRates("test-api-key", "EUR", "EUR", false, false);
                
        // Verify rates were saved (2 for USD base, 1 for EUR base)
        verify(exchangeRateRepository, times(3)).save(any(ExchangeRate.class));
        verify(exchangeRateCache, times(3)).updateRate(any(ExchangeRate.class));
    }
    
    @Test
    void refreshRates_ShouldHandleEmptyCurrencyList() {
        // Given
        when(currencyRepository.findAll()).thenReturn(List.of());
        
        // When
        exchangeRateScheduler.refreshRates();
        
        // Then
        verify(currencyRepository, times(1)).findAll();
        verifyNoInteractions(openExchangeRatesClient);
        verifyNoInteractions(exchangeRateRepository);
        verifyNoInteractions(exchangeRateCache);
    }
}