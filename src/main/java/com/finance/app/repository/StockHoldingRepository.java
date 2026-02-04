package com.finance.app.repository;

import com.finance.app.domain.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    Optional<StockHolding> findByUser_UsernameAndTicker(String username, String ticker);

    List<StockHolding> findByUser_Username(String username);
}
