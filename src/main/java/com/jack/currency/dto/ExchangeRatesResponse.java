package com.jack.currency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ExchangeRatesResponse {
    private String disclaimer;
    private String license;
    
    @JsonProperty("timestamp")
    private long unixTimestamp;
    
    private String base;
    
    @JsonProperty("rates")
    private Map<String, Double> currencyRates;
}