package com.finance.app.controller;

import com.finance.app.dto.StockDto;
import com.finance.app.service.StockService;
import com.finance.app.repository.StockAccountRepository;
import com.finance.app.domain.StockAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockAccountRepository stockAccountRepository;
    private final com.finance.app.repository.AccountRepository accountRepository; // Inject Bank Account Repo

    // Get Stock Account with Balances
    @GetMapping("/account")
    public StockDto.AccountResponse getStockAccount(@AuthenticationPrincipal UserDetails user) {
        StockAccount acc = stockAccountRepository.findByUser_Username(user.getUsername()).orElse(null);
        BigDecimal bankBalance = BigDecimal.ZERO;

        // Fetch Bank Balance from Account Repository
        var bankAcc = accountRepository.findByUser_Username(user.getUsername());
        if (bankAcc.isPresent()) {
            bankBalance = bankAcc.get().getBalance();
        }

        if (acc == null) {
            return new StockDto.AccountResponse(null, BigDecimal.ZERO, BigDecimal.ZERO, bankBalance);
        }
        return new StockDto.AccountResponse(acc.getAccountNumber(), acc.getBalanceKRW(), acc.getBalanceUSD(),
                bankBalance);
    }

    @PostMapping("/transfer/deposit")
    public String transferToStock(@AuthenticationPrincipal UserDetails user,
            @RequestBody StockDto.TransferRequest req) {
        stockService.transferToStockAccount(user.getUsername(), req.getAmount());
        return "Transfer successful";
    }

    @PostMapping("/exchange")
    public String exchange(@AuthenticationPrincipal UserDetails user, @RequestBody StockDto.ExchangeRequest req) {
        stockService.exchangeCurrency(user.getUsername(), req.getAmount(), req.getSourceType(), req.getDirection());
        return "Exchange successful";
    }

    // Create Stock Account
    @PostMapping("/account")
    public String createStockAccount(@AuthenticationPrincipal UserDetails user) {
        return stockService.createStockAccount(user.getUsername());
    }

    // --- Trading Endpoints ---

    @GetMapping("/price/{ticker}")
    public StockDto.PriceResponse getPrice(@PathVariable String ticker) {
        return new StockDto.PriceResponse(ticker, stockService.getStockPrice(ticker));
    }

    @PostMapping("/buy")
    public String buy(@AuthenticationPrincipal UserDetails user, @RequestBody StockDto.TradeRequest req) {
        stockService.buyStock(user.getUsername(), req.getTicker(), req.getQuantity());
        return "매수 완료";
    }

    @PostMapping("/sell")
    public String sell(@AuthenticationPrincipal UserDetails user, @RequestBody StockDto.TradeRequest req) {
        stockService.sellStock(user.getUsername(), req.getTicker(), req.getQuantity());
        return "매도 완료";
    }

    @GetMapping("/portfolio")
    public List<StockDto.PortfolioResponse> getPortfolio(@AuthenticationPrincipal UserDetails user) {
        return stockService.getPortfolio(user.getUsername());
    }

    @GetMapping("/transactions")
    public List<StockDto.TransactionResponse> getTransactions(@AuthenticationPrincipal UserDetails user) {
        return stockService.getTransactions(user.getUsername());
    }
}
