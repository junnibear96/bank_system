package com.finance.app.service;

import com.finance.app.domain.*;
import com.finance.app.dto.StockDto;
import com.finance.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yahoofinance.YahooFinance;
import yahoofinance.Stock;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockAccountRepository stockAccountRepository;
    private final AccountRepository accountRepository; // Bank Account Repo
    private final StockHoldingRepository holdingRepository;
    private final StockOrderRepository orderRepository;
    private final UserRepository userRepository;

    // 1. Create Stock Account
    @Transactional
    public String createStockAccount(String username) {
        if (stockAccountRepository.existsByUser_Username(username)) {
            throw new RuntimeException("Stock account already exists.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate Stock Account Number (Format: XXX-XXXXXXXX)
        // Example: 012-34567891
        int prefix = (int) (Math.random() * 900) + 100; // 100-999
        int suffix = (int) (Math.random() * 90000000) + 10000000; // 10000000-99999999
        String accNum = String.format("%03d-%08d", prefix, suffix);

        StockAccount account = StockAccount.builder()
                .user(user)
                .accountNumber(accNum)
                .createdAt(LocalDateTime.now())
                .build();

        stockAccountRepository.save(account);
        return accNum;
    }

    // 2. Get Stock Account (for checking existence)
    public String getMyStockAccount(String username) {
        return stockAccountRepository.findByUser_Username(username)
                .map(StockAccount::getAccountNumber)
                .orElse(null); // Return null if not found
    }

    // --- Trading Logic ---

    // 3. Get Stock Price (Yahoo Finance)
    public BigDecimal getStockPrice(String ticker) {
        try {
            Stock stock = YahooFinance.get(ticker);
            if (stock == null || stock.getQuote() == null) {
                throw new RuntimeException("Stock info not found: " + ticker);
            }
            return stock.getQuote().getPrice();
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch stock price.");
        }
    }

    // 4. Buy Stock
    @Transactional
    public void buyStock(String username, String ticker, int quantity) {
        // Validation: Must have a stock account
        if (!stockAccountRepository.existsByUser_Username(username)) {
            throw new RuntimeException("You need a stock account to trade.");
        }

        BigDecimal currentPrice = getStockPrice(ticker);
        BigDecimal totalCost = currentPrice.multiply(BigDecimal.valueOf(quantity));

        // Deduct from Main Bank Account
        Account account = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Bank account not found."));

        if (account.getBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient funds in bank account.");
        }
        account.setBalance(account.getBalance().subtract(totalCost));

        // Update Stock Holding (Average Price Logic)
        StockHolding holding = holdingRepository.findByUser_UsernameAndTicker(username, ticker)
                .orElse(StockHolding.builder()
                        .user(account.getUser())
                        .ticker(ticker)
                        .quantity(0)
                        .averagePrice(BigDecimal.ZERO)
                        .build());

        // New Avg Price = ((OldQty * OldAvg) + (NewQty * NewPrice)) / TotalQty
        BigDecimal oldTotal = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal newTotal = oldTotal.add(totalCost);
        int totalQty = holding.getQuantity() + quantity;

        holding.setQuantity(totalQty);
        holding.setAveragePrice(newTotal.divide(BigDecimal.valueOf(totalQty), 2, java.math.RoundingMode.HALF_UP));

        holdingRepository.save(holding);

        // Record Order
        saveOrder(account.getUser(), ticker, OrderType.BUY, currentPrice, quantity);
    }

    // 5. Sell Stock
    @Transactional
    public void sellStock(String username, String ticker, int quantity) {
        if (!stockAccountRepository.existsByUser_Username(username)) {
            throw new RuntimeException("Stock account required.");
        }

        BigDecimal currentPrice = getStockPrice(ticker);
        BigDecimal totalEarn = currentPrice.multiply(BigDecimal.valueOf(quantity));

        StockHolding holding = holdingRepository.findByUser_UsernameAndTicker(username, ticker)
                .orElseThrow(() -> new RuntimeException("You don't own this stock."));

        if (holding.getQuantity() < quantity) {
            throw new RuntimeException("Not enough shares.");
        }

        // Decrease Quantity (Avg Price doesn't change on sell)
        holding.setQuantity(holding.getQuantity() - quantity);
        if (holding.getQuantity() == 0) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        // Add to Main Bank Account
        Account account = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
        account.setBalance(account.getBalance().add(totalEarn));

        // Record Order
        saveOrder(account.getUser(), ticker, OrderType.SELL, currentPrice, quantity);
    }

    private void saveOrder(User user, String ticker, OrderType type, BigDecimal price, int qty) {
        orderRepository.save(StockOrder.builder()
                .user(user)
                .ticker(ticker)
                .type(type)
                .price(price)
                .quantity(qty)
                .tradeDate(LocalDateTime.now())
                .build());
    }

    // 6. Get Portfolio
    public List<StockDto.PortfolioResponse> getPortfolio(String username) {
        List<StockHolding> holdings = holdingRepository.findByUser_Username(username);
        return holdings.stream().map(h -> {
            BigDecimal currentPrice = BigDecimal.ZERO;
            try {
                currentPrice = getStockPrice(h.getTicker());
            } catch (Exception e) {
            }
            return new StockDto.PortfolioResponse(h.getTicker(), h.getQuantity(), h.getAveragePrice(), currentPrice);
        }).collect(Collectors.toList());
    }
}
