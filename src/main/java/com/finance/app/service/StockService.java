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
    private final StockTransactionRepository stockTransactionRepository; // New Repo
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

    // 3. Get Stock Price (Manual HTTP Request because library is blocked)
    public StockDto.PriceResponse getStockPrice(String ticker) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new RuntimeException("Ticker symbol cannot be empty.");
        }
        System.out.println("Fetching price (Manual) for: [" + ticker + "]");

        try {
            // Manual Request to bypass User-Agent blocking
            java.net.URL url = new java.net.URL(
                    "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker.trim() + "?interval=1d&range=1d");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

            if (conn.getResponseCode() != 200) {
                System.out.println("Yahoo Finance Error: " + conn.getResponseCode());
                throw new RuntimeException("Yahoo Finance blocked the request (HTTP " + conn.getResponseCode() + ")");
            }

            java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            String json = content.toString();

            // 1. Parse Price: "regularMarketPrice":123.45
            java.util.regex.Pattern pricePattern = java.util.regex.Pattern.compile("\"regularMarketPrice\":([0-9.]+)");
            java.util.regex.Matcher priceMatcher = pricePattern.matcher(json);

            BigDecimal price;
            if (priceMatcher.find()) {
                price = new BigDecimal(priceMatcher.group(1));
            } else {
                // Try "chartPreviousClose" as fallback if market is closed/pre-market sometimes
                // varies
                java.util.regex.Pattern fallbackPattern = java.util.regex.Pattern
                        .compile("\"chartPreviousClose\":([0-9.]+)");
                java.util.regex.Matcher fallbackMatcher = fallbackPattern.matcher(json);
                if (fallbackMatcher.find()) {
                    price = new BigDecimal(fallbackMatcher.group(1));
                } else {
                    throw new RuntimeException("Could not parse price from Yahoo data.");
                }
            }

            // 2. Parse Company Name: "shortName":"Samsung Electronics Co Ltd"
            // Note: shortName might contain escaped quotes or unicode, but for now simple
            // regex
            String companyName = ticker; // Default to ticker
            // Look for "shortName":"..."
            // We use a non-greedy match for the content inside quotes
            java.util.regex.Pattern namePattern = java.util.regex.Pattern.compile("\"shortName\":\"(.*?)\"");
            java.util.regex.Matcher nameMatcher = namePattern.matcher(json);

            if (nameMatcher.find()) {
                companyName = nameMatcher.group(1);
                // Simple unicode unescape if needed, but usually browsers handle it or Java
                // string might need it.
                // For now raw string from regex.
            }

            return new StockDto.PriceResponse(ticker, price, companyName);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Returning mock price due to error.");
            return new StockDto.PriceResponse(ticker, BigDecimal.valueOf(150.00), "Unknown Company");
        }
    }

    // 4. Get Stock History (Manual)
    public List<StockDto.HistoryResponse> getStockHistory(String ticker) {
        if (ticker == null || ticker.trim().isEmpty())
            return java.util.Collections.emptyList();

        List<StockDto.HistoryResponse> history = new java.util.ArrayList<>();
        try {
            // Yahoo Chart API (1 Month, Daily)
            // https://query1.finance.yahoo.com/v8/finance/chart/AAPL?interval=1d&range=1mo
            String urlStr = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker.trim()
                    + "?interval=1d&range=1mo";
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() != 200) {
                return history; // Return empty on error
            }

            java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                content.append(inputLine);
            in.close();

            String json = content.toString();

            // Very Basic Parsing (Regex) to extract timestamps and closes
            // Note: Use a proper JSON library (Jackson/Gson) in production!

            // 1. Extract Timestamps: "timestamp":[167...,167...]
            java.util.regex.Pattern pTime = java.util.regex.Pattern.compile("\"timestamp\":\\[(.*?)\\]");
            java.util.regex.Matcher mTime = pTime.matcher(json);

            // 2. Extract Closes: "close":[150.1,152.3...] (inside events or indicators) -
            // tricky with regex
            // Simplification: Look for "adjclose":[{"adjclose":[...]}] or "close":[...]
            // using a wider search
            // Let's rely on "close":[....] pattern which usually appears in "indicators"
            java.util.regex.Pattern pClose = java.util.regex.Pattern.compile("\"close\":\\[(.*?)\\]");
            java.util.regex.Matcher mClose = pClose.matcher(json);

            if (mTime.find() && mClose.find()) {
                String[] times = mTime.group(1).split(",");
                String[] prices = mClose.group(1).split(",");

                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

                for (int i = 0; i < times.length && i < prices.length; i++) {
                    try {
                        // Skip nulls
                        if (prices[i].equals("null"))
                            continue;

                        long ts = Long.parseLong(times[i]);
                        BigDecimal price = new BigDecimal(prices[i]);

                        LocalDateTime date = LocalDateTime.ofEpochSecond(ts, 0, java.time.ZoneOffset.UTC);
                        history.add(new StockDto.HistoryResponse(date.format(dtf), price));
                    } catch (Exception ignored) {
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return history;
    }

    // 1. Transfer Bank -> Stock Account
    @Transactional
    public void transferToStockAccount(String username, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Invalid amount");

        // Withdraw from Bank Account
        Account bankAccount = accountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Bank account not found."));
        if (bankAccount.getBalance().compareTo(amount) < 0)
            throw new RuntimeException("은행 계좌에 자금이 부족합니다");
        bankAccount.setBalance(bankAccount.getBalance().subtract(amount));

        // Deposit to Stock Account (KRW)
        StockAccount stockAccount = stockAccountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Stock account required."));

        // Null Safety
        if (stockAccount.getBalanceKRW() == null)
            stockAccount.setBalanceKRW(BigDecimal.ZERO);
        if (stockAccount.getBalanceUSD() == null)
            stockAccount.setBalanceUSD(BigDecimal.ZERO);

        stockAccount.setBalanceKRW(stockAccount.getBalanceKRW().add(amount));

        // Record Transaction
        stockTransactionRepository.save(StockTransaction.builder()
                .user(stockAccount.getUser())
                .type("DEPOSIT")
                .amount(amount)
                .currency("KRW")
                .createdAt(LocalDateTime.now())
                .build());
    }

    // 2. Exchange Currency (KRW <-> USD)
    @Transactional
    public void exchangeCurrency(String username, BigDecimal amount, String sourceType, String direction) {
        StockAccount account = stockAccountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Stock account required."));

        // Null Safety
        if (account.getBalanceKRW() == null)
            account.setBalanceKRW(BigDecimal.ZERO);
        if (account.getBalanceUSD() == null)
            account.setBalanceUSD(BigDecimal.ZERO);

        // Fetch Real-time Rate (KRW=X: USD to KRW rate, e.g., 1300)
        BigDecimal rate;
        try {
            rate = getStockPrice("KRW=X").getPrice(); // Returns ~1300
        } catch (Exception e) {
            rate = new BigDecimal("1300"); // Fallback
        }

        if ("KRW_TO_USD".equals(direction)) {
            // Case A: Buy USD with KRW
            if ("BANK".equals(sourceType)) {
                // Deduct from Bank
                Account bankAccount = accountRepository.findByUser_Username(username)
                        .orElseThrow(() -> new RuntimeException("Bank account not found."));
                if (bankAccount.getBalance().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient funds in bank account");
                }
                bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
            } else {
                // Deduct from Stock Data
                if (account.getBalanceKRW().compareTo(amount) < 0) {
                    throw new RuntimeException("Insufficient KRW balance in stock account");
                }
                account.setBalanceKRW(account.getBalanceKRW().subtract(amount));
            }

            // Calculate USD Amount (Amount / Rate)
            BigDecimal amountUSD = amount.divide(rate, 2, java.math.RoundingMode.HALF_DOWN);
            account.setBalanceUSD(account.getBalanceUSD().add(amountUSD));

        } else if ("USD_TO_KRW".equals(direction)) {
            // Case B: Buy KRW with USD
            // Source must be STOCK for USD (Bank doesn't hold USD)
            if (account.getBalanceUSD().compareTo(amount) < 0) {
                throw new RuntimeException("불충분한 USD 잔액");
            }
            account.setBalanceUSD(account.getBalanceUSD().subtract(amount));

            // Calculate KRW Amount (Amount * Rate)
            BigDecimal amountKRW = amount.multiply(rate).setScale(0, java.math.RoundingMode.HALF_DOWN);
            account.setBalanceKRW(account.getBalanceKRW().add(amountKRW));
        }

        // Record Transaction
        stockTransactionRepository.save(StockTransaction.builder()
                .user(account.getUser())
                .type("EXCHANGE")
                .amount(amount)
                .currency(direction) // Record direction
                .createdAt(LocalDateTime.now())
                .build());
    }

    // 4. Buy Stock (Updated for Currency)
    @Transactional
    public void buyStock(String username, String ticker, int quantity) {
        StockAccount account = stockAccountRepository.findByUser_Username(username)
                .orElseThrow(() -> new RuntimeException("Stock account required."));

        BigDecimal currentPrice = getStockPrice(ticker).getPrice();
        BigDecimal totalCost = currentPrice.multiply(BigDecimal.valueOf(quantity));

        // Check if Korean Stock (.KS or .KQ)
        boolean isKoreanStock = ticker.endsWith(".KS") || ticker.endsWith(".KQ");

        if (isKoreanStock) {
            // Pay with KRW
            if (account.getBalanceKRW().compareTo(totalCost) < 0)
                throw new RuntimeException("Insufficient KRW balance.");
            account.setBalanceKRW(account.getBalanceKRW().subtract(totalCost));
        } else {
            // Pay with USD
            if (account.getBalanceUSD().compareTo(totalCost) < 0)
                throw new RuntimeException("불충분한 USD 잔액.");
            account.setBalanceUSD(account.getBalanceUSD().subtract(totalCost));
        }

        // Update Stock Holding
        StockHolding holding = holdingRepository.findByUser_UsernameAndTicker(username, ticker)
                .orElse(StockHolding.builder()
                        .user(account.getUser())
                        .ticker(ticker)
                        .quantity(0)
                        .averagePrice(BigDecimal.ZERO)
                        .build());

        // Avg Price Calculation matches previous logic
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

        BigDecimal currentPrice = getStockPrice(ticker).getPrice();
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
                currentPrice = getStockPrice(h.getTicker()).getPrice();
            } catch (Exception e) {
            }
            return new StockDto.PortfolioResponse(h.getTicker(), h.getQuantity(), h.getAveragePrice(), currentPrice);
        }).collect(Collectors.toList());
    }

    // 7. Get Transaction History
    public List<StockDto.TransactionResponse> getTransactions(String username) {
        List<StockTransaction> transactions = stockTransactionRepository
                .findByUser_UsernameOrderByCreatedAtDesc(username);
        return transactions.stream().map(t -> new StockDto.TransactionResponse(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getCurrency(),
                t.getCreatedAt())).collect(Collectors.toList());
    }
}
