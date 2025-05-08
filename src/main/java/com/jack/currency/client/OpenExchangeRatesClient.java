package com.jack.currency.client;

import com.jack.currency.dto.CurrenciesResponse;
import com.jack.currency.dto.ExchangeRatesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "open-exchange-rates", url = "${openexchangerates.api.url}")
public interface OpenExchangeRatesClient {

    @GetMapping("/currencies.json")
    CurrenciesResponse getCurrencies();

    @GetMapping("/latest.json")
    ExchangeRatesResponse getLatestExchangeRates(
        @RequestParam("app_id") String appId,
        @RequestParam(value = "base", required = false) String base,
        @RequestParam(value = "symbols", required = false) String symbols,
        @RequestParam(value = "prettyprint", required = false, defaultValue = "false") Boolean prettyprint,
        @RequestParam(value = "show_alternative", required = false, defaultValue = "false") Boolean showAlternative);
}