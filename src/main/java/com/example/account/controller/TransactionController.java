package com.example.account.controller;

import com.example.account.aop.AccountLock;
import com.example.account.dto.CancelTransaction;
import com.example.account.dto.InquiryTransactionResponse;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseTransaction;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping("/transaction/use")
  @AccountLock
  public UseTransaction.Response useTransaction(
      @RequestBody @Valid UseTransaction.Request request) throws InterruptedException {
    try {

      Thread.sleep(3000L);
      TransactionDto transactionDto = transactionService.useTransaction(
          request.getUserId(), request.getAccountNumber(), request.getAmount()
      );
      return UseTransaction.Response.from(transactionDto);
    } catch (AccountException e) {
      log.error("잔액 사용 실패");
      transactionService.saveFailedUseTransaction(request.getAccountNumber(), request.getAmount());
      throw e;
    }
  }

  @PostMapping("/transaction/cancel")
  @AccountLock
  public CancelTransaction.Response cancelTransaction(
      @RequestBody @Valid CancelTransaction.Request request) {
    try {
      TransactionDto transactionDto = transactionService.cancelTransaction(
          request.getTransactionId(), request.getAccountNumber(), request.getAmount()
      );
      return CancelTransaction.Response.from(transactionDto);
    } catch (AccountException e) {
      log.error("거래 취소 실패");
      transactionService.saveFailedCancelTransaction(request.getAccountNumber(),
          request.getAmount());
      throw e;
    }
  }

  @GetMapping("/transaction/{transactionId}")
  public InquiryTransactionResponse getTransactionByTransactionId(
      @PathVariable String transactionId
  ) {
    return InquiryTransactionResponse.from(transactionService.inquiryTransaction(transactionId));
  }
}
