package com.jack.currency.service;

import com.jack.currency.client.OpenExchangeRatesClient;
import com.jack.currency.dto.CurrenciesResponse;
import com.jack.currency.model.Currency;
import com.jack.currency.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private OpenExchangeRatesClient exchangeRatesClient;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    private Currency usdCurrency;
    private Currency eurCurrency;

    @BeforeEach
    void setUp() {
        usdCurrency = new Currency();
        usdCurrency.setId(1L);
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        usdCurrency.setCreatedAt(LocalDateTime.now().minusDays(10));

        eurCurrency = new Currency();
        eurCurrency.setId(2L);
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");
        eurCurrency.setCreatedAt(LocalDateTime.now().minusDays(5));
    }

    @Test
    void getAllCurrencies_ShouldReturnListOfCurrencies() {
        // Given
        List<Currency> currencies = List.of(usdCurrency, eurCurrency);
        when(currencyRepository.findAll()).thenReturn(currencies);

        // When
        List<Currency> result = currencyService.getAllCurrencies();

        // Then
        assertEquals(2, result.size());
        assertEquals("USD", result.get(0).getCode());
        assertEquals("EUR", result.get(1).getCode());
        verify(currencyRepository, times(1)).findAll();
    }

    @Test
    void getCurrencyByCode_ShouldReturnCurrency_WhenCodeExists() {
        // Given
        when(currencyRepository.findByCode("USD")).thenReturn(Optional.of(usdCurrency));

        // When
        Optional<Currency> result = currencyService.getCurrencyByCode("USD");

        // Then
        assertTrue(result.isPresent());
        assertEquals("US Dollar", result.get().getName());
        verify(currencyRepository, times(1)).findByCode("USD");
    }

    @Test
    void refreshCurrencies_ShouldAddNewCurrencies() {
        // Given
        Map<String, String> currencyMap = new HashMap<>();
        currencyMap.put("JPY", "Japanese Yen");
        currencyMap.put("GBP", "British Pound");

        CurrenciesResponse response = new CurrenciesResponse();
        response.setCurrencies(currencyMap);
        
        when(exchangeRatesClient.getCurrencies()).thenReturn(response);
        when(currencyRepository.existsByCode("JPY")).thenReturn(false);
        when(currencyRepository.existsByCode("GBP")).thenReturn(true);

        // When
        currencyService.refreshCurrencies();

        // Then
        verify(currencyRepository, times(1)).existsByCode("JPY");
        verify(currencyRepository, times(1)).existsByCode("GBP");
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }

    @Test
    void createCurrency_ShouldSaveCurrency_WhenValidAndNotExists() {
        // Given
        Currency jpyCurrency = new Currency();
        jpyCurrency.setCode("JPY");
        jpyCurrency.setName("Japanese Yen");
        
        when(currencyRepository.existsByCode("JPY")).thenReturn(false);
        when(currencyRepository.save(any(Currency.class))).thenReturn(jpyCurrency);
        
        // When
        Currency result = currencyService.createCurrency(jpyCurrency);
        
        // Then
        assertEquals("JPY", result.getCode());
        assertEquals("Japanese Yen", result.getName());
        verify(currencyRepository, times(1)).existsByCode("JPY");
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }
    
    @Test
    void createCurrency_ShouldThrowException_WhenCurrencyExists() {
        // Given
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        
        when(currencyRepository.existsByCode("USD")).thenReturn(true);
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.createCurrency(usdCurrency);
        });
        
        verify(currencyRepository, times(1)).existsByCode("USD");
        verify(currencyRepository, never()).save(any(Currency.class));
    }
    
    @Test
    void createCurrency_ShouldThrowException_WhenInvalidCode() {
        // Given
        Currency invalidCurrency = new Currency();
        invalidCurrency.setCode("invalid");
        invalidCurrency.setName("Invalid Currency");
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            currencyService.createCurrency(invalidCurrency);
        });
        
        verify(currencyRepository, never()).existsByCode(anyString());
        verify(currencyRepository, never()).save(any(Currency.class));
    }

    @Test
    void updateCurrency_ShouldUpdateCurrency_WhenCurrencyExists() {
        // Given
        Currency updatedEurCurrency = new Currency();
        updatedEurCurrency.setCode("EUR");
        updatedEurCurrency.setName("European Euro");
        updatedEurCurrency.setBase("GBP");
        
        // Existing currency from DB
        Currency existingEurCurrency = new Currency();
        existingEurCurrency.setId(2L);
        existingEurCurrency.setCode("EUR");
        existingEurCurrency.setName("Euro");
        existingEurCurrency.setBase("USD");
        LocalDateTime originalCreationDate = LocalDateTime.now().minusDays(30);
        existingEurCurrency.setCreatedAt(originalCreationDate);
        
        when(currencyRepository.existsByCode("EUR")).thenReturn(true);
        when(currencyRepository.findByCode("EUR")).thenReturn(Optional.of(existingEurCurrency));
        when(currencyRepository.save(any(Currency.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        Currency result = currencyService.updateCurrency(updatedEurCurrency);
        
        // Then
        assertEquals("EUR", result.getCode());
        assertEquals("European Euro", result.getName());
        assertEquals("GBP", result.getBase());
        assertEquals(2L, result.getId()); // ID should be preserved
        assertEquals(originalCreationDate, result.getCreatedAt()); // Creation date should be preserved
        
        verify(currencyRepository, times(1)).existsByCode("EUR");
        verify(currencyRepository, times(1)).findByCode("EUR");
        verify(currencyRepository, times(1)).save(any(Currency.class));
    }
    
    @Test
    void updateCurrency_ShouldThrowException_WhenCurrencyDoesNotExist() {
        // Given
        Currency nonExistentCurrency = new Currency();
        nonExistentCurrency.setCode("GBP");
        nonExistentCurrency.setName("British Pound");
        nonExistentCurrency.setBase("USD");
        
        when(currencyRepository.existsByCode("GBP")).thenReturn(false);
        
        // When/Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            currencyService.updateCurrency(nonExistentCurrency);
        });
        
        assertEquals("Currency with code GBP does not exist", exception.getMessage());
        
        verify(currencyRepository, times(1)).existsByCode("GBP");
        verify(currencyRepository, never()).findByCode(anyString());
        verify(currencyRepository, never()).save(any(Currency.class));
    }
}