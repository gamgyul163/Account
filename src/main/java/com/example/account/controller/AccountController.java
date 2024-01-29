package com.example.account.controller;

import com.example.account.dto.AccountInfo;
import com.example.account.dto.CloseAccount;
import com.example.account.dto.OpenAccount;
import com.example.account.service.AccountService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/account")
  public OpenAccount.Response openAccount(
      @RequestBody @Valid OpenAccount.Request request) {
    return OpenAccount.Response.from(
        accountService.openAccount(request.getUserId(), request.getInitialBalance())
    );
  }

  @DeleteMapping("/account")
  public CloseAccount.Response getAccountsByUserId(
      @RequestBody @Valid CloseAccount.Request request) {
    return CloseAccount.Response.from(
        accountService.closeAccount(request.getUserId(), request.getAccountNumber())
    );
  }

  @GetMapping("/account")
  public List<AccountInfo> getAccountsByUserId(
      @RequestParam("user_id") Long userId
  ) {
    return accountService.getAccountsByUserId(userId)
        .stream().map(AccountInfo::fromDto).collect(Collectors.toList());
  }
}