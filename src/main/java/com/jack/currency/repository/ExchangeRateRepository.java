package com.jack.currency.repository;

import com.jack.currency.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    Optional<ExchangeRate> findFirstByCurrencyCodeOrderByTimestampDesc(String currencyCode);
    
    List<ExchangeRate> findByCurrencyCodeAndTimestampBetweenOrderByTimestampDesc(
            String currencyCode, LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT er FROM ExchangeRate er WHERE er.currencyCode = ?1 AND er.timestamp = " +
            "(SELECT MAX(er2.timestamp) FROM ExchangeRate er2 WHERE er2.currencyCode = ?1)")
    Optional<ExchangeRate> findLatestRateByCurrencyCode(String currencyCode);
}