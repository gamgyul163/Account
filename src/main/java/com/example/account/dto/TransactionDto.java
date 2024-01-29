package com.example.account.dto;

import com.example.account.domain.Transaction;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
  private String accountNumber;
  private TransactionResultType transactionResultType;
  private String transactionId;
  private Long amount;
  private LocalDateTime transactedAt;
  private TransactionType transactionType;

  public static TransactionDto fromEntity(Transaction transaction) {
    return TransactionDto.builder()
        .accountNumber(transaction.getAccount().getAccountNumber())
        .transactionResultType(transaction.getTransactionResultType())
        .transactionId(transaction.getTransactionId())
        .amount(transaction.getAmount())
        .transactedAt(transaction.getTransactedAt())
        .transactionType(transaction.getTransactionType())
        .build();
  }
}
