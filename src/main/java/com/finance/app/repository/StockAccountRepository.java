package com.finance.app.repository;

import com.finance.app.domain.StockAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StockAccountRepository extends JpaRepository<StockAccount, Long> {
    Optional<StockAccount> findByUser_Username(String username);

    boolean existsByUser_Username(String username);
}
