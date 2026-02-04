package com.finance.app.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

public class AccountDto {

    @Getter
    @Setter
    public static class DepositRequest {
        private BigDecimal amount;
    }

    @Getter
    @Setter
    public static class TransferRequest {
        private String toAccount; // Recipient Account Number
        private BigDecimal amount;
    }

    // Response DTO (To avoid exposing Account Entity directly)
    @Getter
    @Setter
    public static class Response {
        private String accountNumber;
        private BigDecimal balance;

        public Response(String accountNumber, BigDecimal balance) {
            this.accountNumber = accountNumber;
            this.balance = balance;
        }
    }
}
