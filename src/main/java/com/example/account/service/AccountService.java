package com.example.account.service;

import com.example.account.config.AccountConfig;
import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
  private final AccountRepository accountRepository;
  private final AccountUserRepository accountUserRepository;
  private final AccountConfig accountConfig;

  @Transactional
  public AccountDto openAccount(Long userId, Long initialBalance) {
    AccountUser accountUser = getAccountUser(userId);

    validateOpenAccount(accountUser);

    String newAccountNumber = "";
    Random random = new Random();
    do {
      // 정해진 계좌 번호 생성 정책이 있으면 random 대신 정책을 여기에 넣는다.
      newAccountNumber = Long.toString((long)(Math.random()*9_000_000_000L+1_000_000_000));
    } while (accountRepository.countByAccountNumber(newAccountNumber) > 0);

    return AccountDto.fromEntity(accountRepository.save(Account.builder()
        .accountUser(accountUser)
        .accountStatus(AccountStatus.IN_USE)
        .accountNumber(newAccountNumber)
        .balance(initialBalance)
        .openedAt(LocalDateTime.now())
        .build()
    ));
  }

  private void validateOpenAccount(AccountUser accountUser) {
    // AccountStatus.IN_USE 상태인 계좌의 수 체크
    if (accountRepository.countByAccountUserAndAccountStatus(accountUser, AccountStatus.IN_USE) >= accountConfig.getAccountLimitPerUser()) {
      throw new AccountException(ErrorCode.REACHED_ACCOUNT_PER_USER_LIMIT);
    }
  }

  @Transactional
  public AccountDto closeAccount(Long userId, String accountNumber) {
    AccountUser accountUser = getAccountUser(userId);
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

    validateCloseAccount(accountUser, account);

    account.setAccountStatus(AccountStatus.CLOSED);
    account.setClosedAt(LocalDateTime.now());

    return AccountDto.fromEntity(accountRepository.save(account));
  }

  private void validateCloseAccount(AccountUser accountUser, Account account) {
    if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
      throw new AccountException(ErrorCode.ACCOUNTUSER_MISMATCHED);
    }
    if (account.getAccountStatus() == AccountStatus.CLOSED) {
      throw new AccountException(ErrorCode.ACCOUNT_CLOSED);
    }
    if (account.getBalance() != 0) {
      throw new AccountException(ErrorCode.BALANCE_NOT_ZERO);
    }
  }

  public List<AccountDto> getAccountsByUserId(Long userId) {
    AccountUser accountUser = getAccountUser(userId);

    return accountRepository.findByAccountUser(accountUser)
        .stream().map(AccountDto::fromEntity).collect(Collectors.toList());
  }

  private AccountUser getAccountUser(Long userId) {
    return accountUserRepository.findById(userId)
        .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
  }
}
