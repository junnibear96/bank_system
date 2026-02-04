package com.finance.app.controller;

import com.finance.app.dto.StockDto;
import com.finance.app.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // Get My Stock Account Number
    @GetMapping("/account")
    public StockDto.AccountResponse getStockAccount(@AuthenticationPrincipal UserDetails user) {
        String accNum = stockService.getMyStockAccount(user.getUsername());
        return new StockDto.AccountResponse(accNum);
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
}
