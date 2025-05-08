package com.jack.currency.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "exchange_rate")
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "currency_code", nullable = false)
    private String currencyCode;
    
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal rate;
    
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @ManyToOne
    @JoinColumn(name = "currency_code", referencedColumnName = "code", insertable = false, updatable = false)
    private Currency currency;
}