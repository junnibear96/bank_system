package com.finance.app.repository;

import com.finance.app.domain.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByUser_UsernameOrderByCreatedAtDesc(String username);
}
