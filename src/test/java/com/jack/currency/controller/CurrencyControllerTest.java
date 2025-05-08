package com.jack.currency.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jack.currency.model.Currency;
import com.jack.currency.model.ExchangeRate;
import com.jack.currency.service.CurrencyService;
import com.jack.currency.service.ExchangeRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrencyService currencyService;
    
    @MockBean
    private ExchangeRateService exchangeRateService;

    private Currency usdCurrency;
    private Currency eurCurrency;
    private ExchangeRate usdRate;
    private ExchangeRate eurRate;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        usdCurrency = new Currency();
        usdCurrency.setId(1L);
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        usdCurrency.setCreatedAt(now);

        eurCurrency = new Currency();
        eurCurrency.setId(2L);
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");
        eurCurrency.setCreatedAt(now);
        
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
    void getAllCurrencies_ShouldReturnListOfCurrencies() throws Exception {
        // Given
        List<Currency> currencies = List.of(usdCurrency, eurCurrency);
        when(currencyService.getAllCurrencies()).thenReturn(currencies);

        // When/Then
        mockMvc.perform(get("/api/currencies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code", is("USD")))
                .andExpect(jsonPath("$[1].code", is("EUR")));

        verify(currencyService, times(1)).getAllCurrencies();
    }

    @Test
    void getCurrencyByCode_ShouldReturnCurrency_WhenExists() throws Exception {
        // Given
        when(currencyService.getCurrencyByCode("USD")).thenReturn(Optional.of(usdCurrency));

        // When/Then
        mockMvc.perform(get("/api/currencies/USD")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("USD")))
                .andExpect(jsonPath("$.name", is("US Dollar")));

        verify(currencyService, times(1)).getCurrencyByCode("USD");
    }

    @Test
    void getCurrencyByCode_ShouldReturn404_WhenNotExists() throws Exception {
        // Given
        when(currencyService.getCurrencyByCode("XYZ")).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/currencies/XYZ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(currencyService, times(1)).getCurrencyByCode("XYZ");
    }

    @Test
    void refreshCurrencies_ShouldCallService() throws Exception {
        // Given
        doNothing().when(currencyService).refreshCurrencies();

        // When/Then
        mockMvc.perform(post("/api/currencies/refresh")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(currencyService, times(1)).refreshCurrencies();
    }

    @Test
    void createCurrency_ShouldReturnCreatedCurrency_WhenValid() throws Exception {
        // Given
        Currency jpyCurrency = new Currency();
        jpyCurrency.setCode("JPY");
        jpyCurrency.setName("Japanese Yen");
        jpyCurrency.setCreatedAt(LocalDateTime.now());
        jpyCurrency.setId(3L);
        
        when(currencyService.createCurrency(any(Currency.class))).thenReturn(jpyCurrency);
        
        // When/Then
        mockMvc.perform(post("/api/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jpyCurrency)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("JPY")))
                .andExpect(jsonPath("$.name", is("Japanese Yen")));
        
        verify(currencyService, times(1)).createCurrency(any(Currency.class));
    }
    
    @Test
    void createCurrency_ShouldReturnBadRequest_WhenCurrencyExists() throws Exception {
        // Given
        Currency usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        
        when(currencyService.createCurrency(any(Currency.class)))
                .thenThrow(new IllegalArgumentException("Currency with code USD already exists"));
        
        // When/Then
        mockMvc.perform(post("/api/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usdCurrency)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Currency with code USD already exists"));
        
        verify(currencyService, times(1)).createCurrency(any(Currency.class));
    }
    
    @Test
    void getAllExchangeRates_ShouldReturnListOfRates() throws Exception {
        // Given
        List<ExchangeRate> rates = List.of(usdRate, eurRate);
        when(exchangeRateService.getAllRates()).thenReturn(rates);

        // When/Then
        mockMvc.perform(get("/api/currencies/exchange-rates")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].currencyCode", is("USD")))
                .andExpect(jsonPath("$[1].currencyCode", is("EUR")));

        verify(exchangeRateService, times(1)).getAllRates();
    }

    @Test
    void getLatestRate_ShouldReturnRate_WhenExists() throws Exception {
        // Given
        when(exchangeRateService.getLatestRate("EUR")).thenReturn(Optional.of(eurRate));

        // When/Then
        mockMvc.perform(get("/api/currencies/exchange-rates/EUR/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.currencyCode", is("EUR")))
                .andExpect(jsonPath("$.rate", is(0.85)));

        verify(exchangeRateService, times(1)).getLatestRate("EUR");
    }

    @Test
    void getLatestRate_ShouldReturn404_WhenNotExists() throws Exception {
        // Given
        when(exchangeRateService.getLatestRate("XYZ")).thenReturn(Optional.empty());

        // When/Then
        mockMvc.perform(get("/api/currencies/exchange-rates/XYZ/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(exchangeRateService, times(1)).getLatestRate("XYZ");
    }
}