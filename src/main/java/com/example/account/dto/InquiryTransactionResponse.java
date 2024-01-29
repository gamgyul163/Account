package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryTransactionResponse {
  private String accountNumber;
  private TransactionType transactionType;
  private TransactionResultType transactionResultType;
  private String transactionId;
  private Long amount;
  private LocalDateTime transactedAt;

  public static InquiryTransactionResponse from(TransactionDto transactionDto) {
    return InquiryTransactionResponse.builder()
        .accountNumber(transactionDto.getAccountNumber())
        .transactionType(transactionDto.getTransactionType())
        .transactionResultType(transactionDto.getTransactionResultType())
        .transactionId(transactionDto.getTransactionId())
        .amount(transactionDto.getAmount())
        .transactedAt(transactionDto.getTransactedAt())
        .build();
  }
}