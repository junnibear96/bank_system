package com.finance.app.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
// Unique constraint: A user can only have one holding entry per ticker
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "ticker" }))
public class StockHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String ticker; // e.g., AAPL, TSLA

    private Integer quantity; // Owned Quantity

    private BigDecimal averagePrice; // Average Buy Price
}
