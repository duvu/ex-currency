package com.jack.currency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Currency creation that doesn't require users to enter id and createdAt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDto {
    
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency code must be 3 uppercase letters")
    private String code;
    
    @NotBlank(message = "Currency name is required")
    @Size(max = 50, message = "Currency name cannot exceed 50 characters")
    private String name;
    
    private String base = "USD";
}