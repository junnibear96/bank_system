package com.finance.app.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users") // 'user' is a reserved keyword in some DBs
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // ID

    @Column(nullable = false)
    private String password; // Encrypted

    @Column(nullable = false)
    private String name;
}
