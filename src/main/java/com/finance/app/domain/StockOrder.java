package com.finance.app.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String ticker;

    @Enumerated(EnumType.STRING)
    private OrderType type; // BUY, SELL

    private BigDecimal price; // Executed Price
    private Integer quantity; // Quantity
    private LocalDateTime tradeDate;
}
