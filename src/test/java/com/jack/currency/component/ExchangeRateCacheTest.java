package com.jack.currency.component;

import com.jack.currency.model.ExchangeRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateCacheTest {

    private ExchangeRateCache exchangeRateCache;
    private ExchangeRate eurExchangeRate;
    private ExchangeRate jpyExchangeRate;

    @BeforeEach
    void setUp() {
        exchangeRateCache = new ExchangeRateCache();
        exchangeRateCache.init(); // Call PostConstruct method manually
        
        // Create sample exchange rates for testing
        eurExchangeRate = new ExchangeRate();
        eurExchangeRate.setCurrencyCode("EUR");
        eurExchangeRate.setRate(new BigDecimal("0.91"));
        eurExchangeRate.setBase("USD");
        eurExchangeRate.setTimestamp(LocalDateTime.now());

        jpyExchangeRate = new ExchangeRate();
        jpyExchangeRate.setCurrencyCode("JPY");
        jpyExchangeRate.setRate(new BigDecimal("150.45"));
        jpyExchangeRate.setBase("USD");
        jpyExchangeRate.setTimestamp(LocalDateTime.now());
    }

    @Test
    void updateRate_shouldStoreRateInCache() {
        // When
        exchangeRateCache.updateRate(eurExchangeRate);
        
        // Then
        Optional<ExchangeRate> cachedRate = exchangeRateCache.getRate("EUR");
        assertTrue(cachedRate.isPresent());
        assertEquals(eurExchangeRate, cachedRate.get());
    }

    @Test
    void updateRate_shouldOverwriteExistingRate() {
        // Given
        exchangeRateCache.updateRate(eurExchangeRate);
        
        // When
        ExchangeRate updatedEurRate = new ExchangeRate();
        updatedEurRate.setCurrencyCode("EUR");
        updatedEurRate.setRate(new BigDecimal("0.92"));
        updatedEurRate.setBase("USD");
        updatedEurRate.setTimestamp(LocalDateTime.now());
        exchangeRateCache.updateRate(updatedEurRate);
        
        // Then
        Optional<ExchangeRate> cachedRate = exchangeRateCache.getRate("EUR");
        assertTrue(cachedRate.isPresent());
        assertEquals(new BigDecimal("0.92"), cachedRate.get().getRate());
    }
    
    @Test
    void getRate_shouldReturnEmptyOptionalIfRateNotFound() {
        // When
        Optional<ExchangeRate> cachedRate = exchangeRateCache.getRate("GBP");
        
        // Then
        assertTrue(cachedRate.isEmpty());
    }
    
    @Test
    void getRate_shouldReturnCorrectRate() {
        // Given
        exchangeRateCache.updateRate(eurExchangeRate);
        exchangeRateCache.updateRate(jpyExchangeRate);
        
        // When
        Optional<ExchangeRate> cachedEurRate = exchangeRateCache.getRate("EUR");
        Optional<ExchangeRate> cachedJpyRate = exchangeRateCache.getRate("JPY");
        
        // Then
        assertTrue(cachedEurRate.isPresent());
        assertTrue(cachedJpyRate.isPresent());
        assertEquals(eurExchangeRate, cachedEurRate.get());
        assertEquals(jpyExchangeRate, cachedJpyRate.get());
    }
    
    @Test
    void getAllRates_shouldReturnAllRatesInCache() {
        // Given
        exchangeRateCache.updateRate(eurExchangeRate);
        exchangeRateCache.updateRate(jpyExchangeRate);
        
        // When
        Map<String, ExchangeRate> allRates = exchangeRateCache.getAllRates();
        
        // Then
        assertEquals(2, allRates.size());
        assertTrue(allRates.containsKey("EUR"));
        assertTrue(allRates.containsKey("JPY"));
        assertEquals(eurExchangeRate, allRates.get("EUR"));
        assertEquals(jpyExchangeRate, allRates.get("JPY"));
    }
    
    @Test
    void getAllRates_shouldReturnEmptyMapWhenCacheIsEmpty() {
        // When
        Map<String, ExchangeRate> allRates = exchangeRateCache.getAllRates();
        
        // Then
        assertTrue(allRates.isEmpty());
    }
    
    @Test
    void clear_shouldRemoveAllRatesFromCache() {
        // Given
        exchangeRateCache.updateRate(eurExchangeRate);
        exchangeRateCache.updateRate(jpyExchangeRate);
        assertEquals(2, exchangeRateCache.getAllRates().size());
        
        // When
        exchangeRateCache.clear();
        
        // Then
        assertTrue(exchangeRateCache.getAllRates().isEmpty());
        assertTrue(exchangeRateCache.getRate("EUR").isEmpty());
        assertTrue(exchangeRateCache.getRate("JPY").isEmpty());
    }
}