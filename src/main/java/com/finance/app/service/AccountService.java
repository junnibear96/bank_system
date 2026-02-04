package com.finance.app.service;

import com.finance.app.domain.Account;
import com.finance.app.domain.Transaction; // Added
import com.finance.app.domain.TransactionType; // Added
import com.finance.app.domain.User;
import com.finance.app.dto.TransactionDto; // Added
import com.finance.app.repository.AccountRepository;
import com.finance.app.repository.TransactionRepository; // Added
import com.finance.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime; // Added
import java.time.format.DateTimeFormatter; // Added
import java.util.List; // Added
import java.util.stream.Collectors; // Added

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    // Create Account for the logged-in user
    @Transactional
    public Account createAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (accountRepository.findByUser_Username(username).isPresent()) {
            throw new RuntimeException("Account already exists");
        }

        // Generate realistic account number (e.g., 015401-04-212749)
        String randomNum = String.format("%06d-%02d-%06d",
                (int) (Math.random() * 1000000),
                (int) (Math.random() * 100),
                (int) (Math.random() * 1000000));

        Account account = Account.builder()
                .user(user)
                .accountNumber(randomNum)
                .balance(BigDecimal.ZERO)
                .build();

        return accountRepository.save(account);
    }

    public Account getMyAccount(String username) {
        return accountRepository.findByUser_Username(username)
                .orElse(null); // Return null if no account yet
    }

    // DEPOSIT MONEY
    @Transactional
    public Account deposit(String username, BigDecimal amount) {
        // 0원 이하 입금 방지
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Wait, deposit amount must be greater than 0.");
        }

        Account account = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Account not found."));

        // 잔액 증가
        account.setBalance(account.getBalance().add(amount));
        Account savedAccount = accountRepository.save(account);

        // Save Transaction Record
        Transaction tx = Transaction.builder()
                .receiver(savedAccount)
                .amount(amount)
                .type(TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        return savedAccount;
    }

    // TRANSFER MONEY
    @Transactional
    public void transfer(String senderName, String toAccountNum, BigDecimal amount) {
        // 1. Check Amount > 0
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Wait, transfer amount must be greater than 0.");
        }

        Account sender = accountRepository.findByUser_Username(senderName)
                .orElseThrow(() -> new RuntimeException("You don't have an account"));

        Account receiver = accountRepository.findByAccountNumber(toAccountNum)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Logic
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        // Save Transaction Record
        Transaction tx = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(amount)
                .type(TransactionType.TRANSFER)
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);
    }

    // GET HISTORY
    public List<TransactionDto> getTransactionHistory(String username) {
        Account myAccount = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<Transaction> transactions = transactionRepository.findBySenderOrReceiverOrderByTimestampDesc(myAccount,
                myAccount);

        return transactions.stream().map(tx -> {
            String typeStr = "";
            String counterpartName = "";
            BigDecimal displayAmount = tx.getAmount();

            // Created by ME (Sender)
            if (tx.getSender() != null && tx.getSender().equals(myAccount)) {
                typeStr = "출금"; // Or "송금"
                displayAmount = displayAmount.negate(); // Show as negative
                counterpartName = (tx.getReceiver() != null) ? tx.getReceiver().getUser().getName() : "Unknown";
            }
            // Received by ME (Receiver)
            else {
                typeStr = "입금";
                counterpartName = (tx.getSender() != null) ? tx.getSender().getUser().getName() : "System";
            }

            return TransactionDto.builder()
                    .type(typeStr)
                    .counterpart(counterpartName)
                    .amount(displayAmount)
                    .date(tx.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .build();
        }).collect(Collectors.toList());
    }
}
