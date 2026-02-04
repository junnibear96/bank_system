package com.finance.app.controller;

import com.finance.app.domain.Account;
import com.finance.app.dto.AccountDto; // Added
import com.finance.app.dto.TransactionDto;
import com.finance.app.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    // Get My Account (Use DTO)
    @GetMapping("/my")
    public AccountDto.Response getMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        Account account = accountService.getMyAccount(userDetails.getUsername());
        if (account == null)
            return null;
        return new AccountDto.Response(account.getAccountNumber(), account.getBalance());
    }

    // Create Account
    @PostMapping("/create")
    public AccountDto.Response createAccount(@AuthenticationPrincipal UserDetails userDetails) {
        Account account = accountService.createAccount(userDetails.getUsername());
        return new AccountDto.Response(account.getAccountNumber(), account.getBalance());
    }

    // Deposit (Use DTO)
    @PostMapping("/deposit")
    public AccountDto.Response deposit(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AccountDto.DepositRequest request) {
        Account account = accountService.deposit(userDetails.getUsername(), request.getAmount());
        return new AccountDto.Response(account.getAccountNumber(), account.getBalance());
    }

    // Transfer (Use DTO)
    @PostMapping("/transfer")
    public String transfer(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AccountDto.TransferRequest request) {
        accountService.transfer(userDetails.getUsername(), request.getToAccount(), request.getAmount());
        return "송금이 완료되었습니다.";
    }

    // Get Transaction History
    @GetMapping("/history")
    public java.util.List<com.finance.app.dto.TransactionDto> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return accountService.getTransactionHistory(userDetails.getUsername());
    }
}
