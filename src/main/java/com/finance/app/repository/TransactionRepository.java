package com.finance.app.repository;

import com.finance.app.domain.Account;
import com.finance.app.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Find all transactions where I am sender OR receiver, ordered by time
    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(Account sender, Account receiver);
}
