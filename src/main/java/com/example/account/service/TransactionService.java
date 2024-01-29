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
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final AccountUserRepository accountUserRepository;
  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final TransactionConfig transactionConfig;
  @Transactional
  public TransactionDto useTransaction(Long userId, String accountNumber, Long amount) {
    AccountUser accountUser = accountUserRepository.findById(userId)
        .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
    Account account = getAccount(accountNumber);

    validateUseTransaction(accountUser, account, amount);

    account.subBalance(amount);

    return TransactionDto.fromEntity(
        saveTransaction(amount, account, TransactionType.USE, TransactionResultType.SUCCEED));
  }

  private void validateUseTransaction(AccountUser accountUser, Account account, Long amount) {
    if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
      throw new AccountException(ErrorCode.ACCOUNTUSER_MISMATCHED);
    }
    if (account.getAccountStatus() == AccountStatus.CLOSED) {
      throw new AccountException(ErrorCode.ACCOUNT_CLOSED);
    }
    if (account.getBalance() < amount) {
      throw new AccountException(ErrorCode.BALANCE_NOT_ENOUGH);
    }
    if (amount < transactionConfig.getAmountMinValue() || amount > transactionConfig.getAmountMaxValue()) {
      throw new AccountException(ErrorCode.AMOUNT_NOT_VALID);
    }
  }

  @Transactional
  public TransactionDto saveFailedUseTransaction(String accountNumber, Long amount) {
    Account account = getAccount(accountNumber);

    return TransactionDto.fromEntity(
        saveTransaction(amount, account, TransactionType.USE, TransactionResultType.FAILED));
  }

  @Transactional
  private Transaction saveTransaction(Long amount, Account account, TransactionType transactionType,
      TransactionResultType transactionResultType) {
    return transactionRepository.save(Transaction.builder()
        .transactionType(transactionType)
        .transactionResultType(transactionResultType)
        .account(account)
        .amount(amount)
        .transactionId(UUID.randomUUID().toString().replace("-", ""))
        .transactedAt(LocalDateTime.now())
        .build()
    );
  }

  private Account getAccount(String accountNumber) {
    return accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
  }

  @Transactional
  public TransactionDto cancelTransaction(String transactionId, String accountNumber, Long amount) {
    Transaction transaction = getTransaction(transactionId);
    Account account = getAccount(accountNumber);

    validateCancelTransaction(transaction, account, amount);

    account.addBalance(amount);

    return TransactionDto.fromEntity(
        saveTransaction(amount, account, TransactionType.CANCEL, TransactionResultType.SUCCEED));
  }

  private Transaction getTransaction(String transactionId) {
    return transactionRepository.findByTransactionId(transactionId)
        .orElseThrow(() -> new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));
  }

  public TransactionDto inquiryTransaction(String transactionId) {
    return TransactionDto.fromEntity(getTransaction(transactionId));
  }

  private void validateCancelTransaction(Transaction transaction, Account account, Long amount) {
    if (!Objects.equals(transaction.getAmount(), amount)) {
      throw new AccountException(ErrorCode.AMOUNT_MISMATCHED);
    }
    if (!Objects.equals(transaction.getAccount().getId(), account.getId())) {
      throw new AccountException(ErrorCode.ACCOUNT_MISMATCHED);
    }
  }

  @Transactional
  public TransactionDto saveFailedCancelTransaction(String accountNumber, Long amount) {
    Account account = getAccount(accountNumber);

    return TransactionDto.fromEntity(
        saveTransaction(amount, account, TransactionType.CANCEL, TransactionResultType.FAILED));
  }
}
