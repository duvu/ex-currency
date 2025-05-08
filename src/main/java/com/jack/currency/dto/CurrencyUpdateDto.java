package com.jack.currency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating existing currencies without modifying id or createdAt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyUpdateDto {
    
    @NotBlank(message = "Currency name is required")
    @Size(max = 50, message = "Currency name cannot exceed 50 characters")
    private String name;
    
    private String base = "USD";
}