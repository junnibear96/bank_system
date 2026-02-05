package com.finance.app.dto;

import lombok.*;
import java.math.BigDecimal;

public class StockDto {

    @Getter
    @AllArgsConstructor
    public static class AccountResponse {
        private String accountNumber;
        private BigDecimal balanceKRW;
        private BigDecimal balanceUSD;
        private BigDecimal bankBalance; // Added Bank Balance
    }

    @Getter
    @Setter
    public static class TransferRequest {
        private BigDecimal amount;
    }

    @Getter
    @Setter
    public static class ExchangeRequest {
        private BigDecimal amount;
        private String sourceType; // "BANK" or "STOCK"
        private String direction; // "KRW_TO_USD" or "USD_TO_KRW"
    }

    @Getter
    @Setter
    public static class TradeRequest {
        private String ticker;
        private int quantity;
    }

    @Getter
    @AllArgsConstructor
    public static class PortfolioResponse {
        private String ticker;
        private int quantity;
        private BigDecimal avgPrice;
        private BigDecimal currentPrice;
    }

    @Getter
    @AllArgsConstructor
    public static class PriceResponse {
        private String ticker;
        private BigDecimal price;
    }

    @Getter
    @AllArgsConstructor
    public static class TransactionResponse {
        private Long id;
        private String type;
        private BigDecimal amount;
        private String currency;
        private java.time.LocalDateTime date;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class HistoryResponse {
        private String date; // YYYY-MM-DD
        private BigDecimal price;
    }
}
