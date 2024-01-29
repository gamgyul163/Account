package com.example.account.service;

import com.example.account.config.TransactionConfig;
import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  public static final long USE_AMOUNT = 200L;
  public static final long CANCEL_AMOUNT = 200L;

  public static final AccountUser accountUser = AccountUser.builder()
      .id(12L)
      .name("Pobi").build();

  public static final Account account = Account.builder()
      .accountUser(accountUser)
      .accountStatus(AccountStatus.IN_USE)
      .balance(10000L)
      .accountNumber("1000000012").build();
  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private AccountUserRepository accountUserRepository;

  @Mock
  private TransactionConfig transactionConfig;

  @InjectMocks
  private TransactionService transactionService;

  @Test
  void successToUseTransactionWhenValidRequest() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(transactionConfig.getAmountMinValue())
        .willReturn(1);
    given(transactionConfig.getAmountMaxValue())
        .willReturn(1_000_000_000);
    given(transactionRepository.save(any()))
        .willReturn(Transaction.builder()
            .account(account)
            .build());
    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    //when
    TransactionDto transactionDto = transactionService.useTransaction(1L, "1000000000", USE_AMOUNT);

    //then
    verify(transactionRepository, times(1)).save(captor.capture());
    assertEquals(account.getAccountNumber(), transactionDto.getAccountNumber());
    assertEquals(TransactionResultType.SUCCEED, captor.getValue().getTransactionResultType());
    assertTrue(captor.getValue().getTransactionId().length() > 0);
    assertEquals(USE_AMOUNT, captor.getValue().getAmount());
    assertTrue(captor.getValue().getTransactedAt() != null);
    assertEquals(TransactionType.USE, captor.getValue().getTransactionType());
  }

  @Test
  @DisplayName("잔액 사용 실패 - 사용자 조회 실패")
  void throwAccountExceptionWhenUseTransaction_USER_NOT_FOUND() {
    //given
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useTransaction(1L, "1000000000", 1000L));

    //then
    assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  @DisplayName("잔액 사용 실패 - 계좌 소유주 불일치")
  void throwAccountExceptionWhenUseTransaction_ACCOUNTUSER_MISMATCHED() {
    //given
    AccountUser harry = AccountUser.builder()
        .name("Harry").build();
    harry.setId(13L);
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(Account.builder()
            .accountUser(harry)
            .balance(0L)
            .accountNumber("1000000012").build()));

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useTransaction(1L, "1234567890", 1000L));

    //then
    assertEquals(ErrorCode.ACCOUNTUSER_MISMATCHED, exception.getErrorCode());
  }

  @Test
  @DisplayName("잔액 사용 실패 - 해지된 계좌")
  void throwAccountExceptionWhenUseTransaction_ACCOUNT_CLOSED() {
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
        () -> transactionService.useTransaction(1L, "1234567890", 1000L));

    //then
    assertEquals(ErrorCode.ACCOUNT_CLOSED, exception.getErrorCode());
  }

  @Test
  @DisplayName("잔액 사용 실패 - 잔액 부족")
  void throwAccountExceptionWhenUseTransaction_BALANCE_NOT_ENOUGH() {
    //given
    Account account = Account.builder()
        .accountUser(accountUser)
        .accountStatus(AccountStatus.IN_USE)
        .balance(100L)
        .accountNumber("1000000012").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));

    //when
    //then
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.useTransaction(1L, "1234567890", 1000L));

    assertEquals(ErrorCode.BALANCE_NOT_ENOUGH, exception.getErrorCode());
    verify(transactionRepository, times(0)).save(any());
  }

  @Test
  @DisplayName("잔액 사용 실패 - 유효하지 않은 거래 금액")
  void throwAccountExceptionWhenUseTransaction_AMOUNT_NOT_VALID() {
    //given
    Account account = Account.builder()
        .accountUser(accountUser)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000012").build();
    given(accountUserRepository.findById(anyLong()))
        .willReturn(Optional.of(accountUser));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(transactionConfig.getAmountMinValue())
        .willReturn(100);
    given(transactionConfig.getAmountMaxValue())
        .willReturn(1000);
    //when
    //then
    AccountException exception1 = assertThrows(AccountException.class,
        () -> transactionService.useTransaction(1L, "1234567890", 99L));
    AccountException exception2 = assertThrows(AccountException.class,
        () -> transactionService.useTransaction(1L, "1234567890", 1001L));

    assertEquals(ErrorCode.AMOUNT_NOT_VALID, exception1.getErrorCode());
    assertEquals(ErrorCode.AMOUNT_NOT_VALID, exception2.getErrorCode());
    verify(transactionRepository, times(0)).save(any());
  }

  @Test
  void successToSaveFailedUseTransaction() {
    //given
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(transactionRepository.save(any()))
        .willReturn(Transaction.builder()
            .account(account)
            .build());
    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    //when
    TransactionDto transactionDto = transactionService.saveFailedUseTransaction("1000000000",
        USE_AMOUNT);

    //then
    verify(transactionRepository, times(1)).save(captor.capture());
    assertEquals(account.getAccountNumber(), transactionDto.getAccountNumber());
    assertEquals(TransactionResultType.FAILED, captor.getValue().getTransactionResultType());
    assertTrue(captor.getValue().getTransactionId().length() > 0);
    assertEquals(USE_AMOUNT, captor.getValue().getAmount());
    assertTrue(captor.getValue().getTransactedAt() != null);
    assertEquals(TransactionType.USE, captor.getValue().getTransactionType());
  }

  @Test
  void successToCancelTransaction() {
    //given
    Transaction transaction = Transaction.builder()
        .account(account)
        .amount(CANCEL_AMOUNT)
        .build();
    given(transactionRepository.findByTransactionId(anyString()))
        .willReturn(Optional.of(transaction));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));
    given(transactionRepository.save(any()))
        .willReturn(Transaction.builder()
            .account(account)
            .build());
    ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

    //when
    TransactionDto transactionDto = transactionService.cancelTransaction("transactionId",
        "1000000000", CANCEL_AMOUNT);

    //then
    verify(transactionRepository, times(1)).save(captor.capture());
    assertEquals(account.getAccountNumber(), transactionDto.getAccountNumber());
    assertEquals(TransactionResultType.SUCCEED, captor.getValue().getTransactionResultType());
    assertTrue(captor.getValue().getTransactionId().length() > 0);
    assertEquals(CANCEL_AMOUNT, captor.getValue().getAmount());
    assertTrue(captor.getValue().getTransactedAt() != null);
    assertEquals(TransactionType.CANCEL, captor.getValue().getTransactionType());
  }

  @Test
  @DisplayName("잔액 사용 취소 실패 - 거래 금액 불일치")
  void throwAccountExceptionWhenCancelTransaction_AMOUNT_MISMATCHED() {
    //given
    Transaction transaction = Transaction.builder()
        .account(account)
        .amount(CANCEL_AMOUNT + 1000L)
        .build();
    given(transactionRepository.findByTransactionId(anyString()))
        .willReturn(Optional.of(transaction));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(account));

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService
            .cancelTransaction(
                "transactionId",
                "1000000000",
                CANCEL_AMOUNT
            )
    );
    //then
    assertEquals(ErrorCode.AMOUNT_MISMATCHED, exception.getErrorCode());
  }

  @Test
  @DisplayName("잔액 사용 취소 실패 - 계좌 정보 불일치")
  void throwAccountExceptionWhenCancelTransaction_ACCOUNT_MISMATCHED() {
    //given
    Account accountNotUse = Account.builder()
        .id(2L)
        .accountUser(accountUser)
        .accountStatus(AccountStatus.IN_USE)
        .balance(10000L)
        .accountNumber("1000000013").build();
    Transaction transaction = Transaction.builder()
        .account(account)
        .amount(CANCEL_AMOUNT)
        .build();
    given(transactionRepository.findByTransactionId(anyString()))
        .willReturn(Optional.of(transaction));
    given(accountRepository.findByAccountNumber(anyString()))
        .willReturn(Optional.of(accountNotUse));

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService
            .cancelTransaction(
                "transactionId",
                "1000000000",
                CANCEL_AMOUNT
            )
    );

    //then
    assertEquals(ErrorCode.ACCOUNT_MISMATCHED, exception.getErrorCode());
  }

  @Test
  void successToInquiryTransaction() {
    //given
    Transaction transaction = Transaction.builder()
        .account(account)
        .transactionType(TransactionType.USE)
        .transactionResultType(TransactionResultType.SUCCEED)
        .transactionId("transactionId")
        .transactedAt(LocalDateTime.now())
        .amount(CANCEL_AMOUNT)
        .build();
    given(transactionRepository.findByTransactionId(anyString()))
        .willReturn(Optional.of(transaction));

    //when
    TransactionDto transactionDto = transactionService.inquiryTransaction("trxId");

    //then
    assertEquals(transaction.getAccount().getAccountNumber(), transactionDto.getAccountNumber());
    assertEquals(transaction.getTransactionResultType(), transactionDto.getTransactionResultType());
    assertEquals(transaction.getTransactionId(), transactionDto.getTransactionId());
    assertEquals(transaction.getAmount(), transactionDto.getAmount());
    assertEquals(transaction.getTransactedAt(), transactionDto.getTransactedAt());
    assertEquals(transaction.getTransactionType(), transactionDto.getTransactionType());
  }

  @Test
  @DisplayName("거래 조회 실패")
  void throwAccountExceptionWhenInquiryTransaction_TRANSACTION_NOT_FOUND() {
    //given
    given(transactionRepository.findByTransactionId(anyString()))
        .willReturn(Optional.empty());

    //when
    AccountException exception = assertThrows(AccountException.class,
        () -> transactionService.inquiryTransaction("transactionId"));

    //then
    assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
  }

}