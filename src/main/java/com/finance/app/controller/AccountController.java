package com.finance.app.controller;

import com.finance.app.domain.Account;
import com.finance.app.dto.TransactionDto; // Added
import com.finance.app.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List; // Added
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // Get My Account Info
    @GetMapping("/my")
    public Account getMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        return accountService.getMyAccount(userDetails.getUsername());
    }

    // Create Account
    @PostMapping("/create")
    public Account createAccount(@AuthenticationPrincipal UserDetails userDetails) {
        return accountService.createAccount(userDetails.getUsername());
    }

    // Deposit Money
    @PostMapping("/deposit")
    public Account deposit(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        return accountService.deposit(userDetails.getUsername(), amount);
    }

    // Transfer Money
    @PostMapping("/transfer")
    public String transfer(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, Object> request) {
        String toAccount = (String) request.get("toAccount");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        accountService.transfer(userDetails.getUsername(), toAccount, amount);
        return "Transfer Successful";
    }

    // Get Transaction History
    @GetMapping("/history")
    public java.util.List<com.finance.app.dto.TransactionDto> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return accountService.getTransactionHistory(userDetails.getUsername());
    }
}
