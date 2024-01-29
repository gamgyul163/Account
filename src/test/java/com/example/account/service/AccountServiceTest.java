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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

  public static final AccountUser accountUser = AccountUser.builder()
      .id(12L)
      .name("Pobi").build();

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AccountUserRepository accountUserRepository;

  @Mock
  private AccountConfig accountConfig;

  @InjectMocks
  private AccountService accountService;

  @Test
  void successToOpenAccountWhenValidRequest() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.countByAccountUserAndAccountStatus(any(), any()))
        .willReturn(0);
    given(accountConfig.getAccountLimitPerUser())
        .willReturn(10);
    given(accountRepository.save(any()))
        .willReturn(Account.builder()
            .accountUser(accountUser)
            .build());
    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

    //when
    AccountDto accountDto = accountService.openAccount(1L, 1000L);

    //then
    verify(accountRepository, times(1)).save(captor.capture());
    assertEquals(12L, accountDto.getUserId());
    assertEquals(1000L, captor.getValue().getBalance());
    assertTrue(captor.getValue().getAccountNumber().matches("\\d{10}"));
    assertTrue(captor.getValue().getOpenedAt() != null);
    assertTrue(captor.getValue().getClosedAt() == null);

  }

  @Test
  @DisplayName("계좌 생성 실패 - 사용자 조회 실패")
  void throwAccountExceptionWhenOpenAccount_USER_NOT_FOUND() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.countByAccountUserAndAccountStatus(any(), any()))
        .willReturn(10);
    given(accountConfig.getAccountLimitPerUser())
        .willReturn(10);

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.openAccount(1L, 1000L));

    //then
    assertEquals(ErrorCode.REACHED_ACCOUNT_PER_USER_LIMIT, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌 생성 실패 - 계좌 보유 수 한도 도달")
  void throwAccountExceptionWhenOpenAccount_REACHED_ACCOUNT_PER_USER_LIMIT() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.openAccount(1L, 1000L));

    //then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void successToCloseAccountWhenValidRequest() {
    //given
    Account account = Account.builder()
        .accountUser(accountUser)
        .accountStatus(AccountStatus.IN_USE)
        .balance(0L)
        .accountNumber("1000000012")
        .openedAt(LocalDateTime.now())
        .build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(accountRepository.save(account))
        .willReturn(Account.builder()
            .accountUser(account.getAccountUser())
            .balance(account.getBalance())
            .accountNumber(account.getAccountNumber())
            .openedAt(account.getOpenedAt())
            .build());
    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

    //when
    AccountDto accountDto = accountService.closeAccount(1L, "1234567890");

    //then
    verify(accountRepository, times(1)).save(captor.capture());
    assertEquals(account.getAccountUser().getId(), accountDto.getUserId());
    assertEquals(account.getBalance(), accountDto.getBalance());
    assertEquals(account.getAccountNumber(),accountDto.getAccountNumber());
    assertEquals(account.getOpenedAt(),accountDto.getOpenedAt());
    assertTrue(accountDto.getClosedAt() == null);
  }

  @Test
  @DisplayName("계좌 해지 실패 - 사용자 조회 실패")
  void throwAccountExceptionWhenCloseAccount_USER_NOT_FOUND() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.closeAccount(1L, "1234567890"));

    //then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌 해지 실패 - 계좌 소유주 불일치")
  void throwAccountExceptionWhenCloseAccount_ACCOUNTUSER_MISMATCHED() {
    //given
    AccountUser harry = AccountUser.builder()
        .id(13L)
        .name("Harry").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(Account.builder()
            .accountUser(harry)
            .balance(0L)
            .accountNumber("1000000012").build()));

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.closeAccount(1L, "1234567890"));

    //then
    assertEquals(ErrorCode.ACCOUNTUSER_MISMATCHED, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌 해지 실패 - 해지된 계좌")
  void throwAccountExceptionWhenCloseAccount_ACCOUNT_CLOSED() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(Account.builder()
            .accountUser(accountUser)
            .accountStatus(AccountStatus.CLOSED)
            .balance(0L)
            .accountNumber("1000000012").build()));

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.closeAccount(1L, "1234567890"));

    //then
    assertEquals(ErrorCode.ACCOUNT_CLOSED, exception.getErrorCode());
  }

  @Test
  @DisplayName("계좌 해지 실패 - 계좌 잔액 존재")
  void throwAccountExceptionWhenCloseAccount_BALANCE_NOT_ZERO() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(Account.builder()
            .accountUser(accountUser)
            .balance(100L)
            .accountNumber("1000000012").build()));

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.closeAccount(1L, "1234567890"));

    //then
    assertEquals(ErrorCode.BALANCE_NOT_ZERO, exception.getErrorCode());
  }

  @Test
  void successToGetAccountsWhenValidRequest() {
    //given
    List<Account> accounts = Arrays.asList(
        Account.builder()
            .accountUser(accountUser)
            .accountNumber("1111111111")
            .balance(1000L)
            .build(),
        Account.builder()
            .accountNumber("2222222222")
            .accountUser(accountUser)
            .balance(2000L)
            .build(),
        Account.builder()
            .accountNumber("3333333333")
            .accountUser(accountUser)
            .balance(3000L)
            .build()
    );
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountUser(any()))
        .willReturn(accounts);

    //when
    List<AccountDto> accountDtos = accountService.getAccountsByUserId(1L);

    //then
    assertEquals(3, accountDtos.size());
    assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
    assertEquals(1000, accountDtos.get(0).getBalance());
    assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
    assertEquals(2000, accountDtos.get(1).getBalance());
    assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
    assertEquals(3000, accountDtos.get(2).getBalance());
  }

  @Test
  @DisplayName("계좌 확인 실패 - 사용자 조회 실패")
  void throwAccountExceptionWhenGetAccounts_USER_NOT_FOUND() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> accountService.getAccountsByUserId(1L));

    //then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }
}