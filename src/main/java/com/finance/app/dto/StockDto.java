package com.finance.app.dto;

import lombok.*;
import java.math.BigDecimal;

public class StockDto {

    @Getter
    @AllArgsConstructor
    public static class AccountResponse {
        private String accountNumber;
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
}
