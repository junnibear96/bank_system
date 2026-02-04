package com.finance.app.repository;

import com.finance.app.domain.StockOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockOrderRepository extends JpaRepository<StockOrder, Long> {
    List<StockOrder> findByUser_UsernameOrderByTradeDateDesc(String username);
}
