package com.finance.app.dto;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class TransactionDto {
    private String type; // "Deposit", "Transfer", "Withdraw"
    private String counterpart; // Name of person sent to/received from
    private BigDecimal amount; // Amount
    private String date; // yyyy-MM-dd HH:mm
}
