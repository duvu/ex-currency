package com.jack.currency.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CurrenciesResponse {
    private Map<String, String> currencies;
}