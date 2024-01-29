package com.example.account.controller;

import com.example.account.dto.CancelTransaction;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseTransaction;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.example.account.type.TransactionType.USE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  @MockBean
  private TransactionService transactionService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void whenSucceedToUseTransaction() throws Exception {
    //given
    given(transactionService.useTransaction(anyLong(), anyString(), anyLong()))
        .willReturn(TransactionDto.builder()
            .accountNumber("1000000000")
            .transactedAt(LocalDateTime.now())
            .amount(12345L)
            .transactionId("transactionId")
            .transactionResultType(TransactionResultType.SUCCEED)
            .build());

    //when
    //then
    mockMvc.perform(post("/transaction/use")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UseTransaction.Request(1L, "2000000000", 3000L)
            ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber").value("1000000000"))
        .andExpect(jsonPath("$.transactionResultType").value("SUCCEED"))
        .andExpect(jsonPath("$.transactionId").value("transactionId"))
        .andExpect(jsonPath("$.amount").value(12345))
        .andDo(print());
  }

  @Test
  void whenFailedToUseTransaction_AccountException() throws Exception {
    //given
    given(transactionService.useTransaction(anyLong(), anyString(), anyLong()))
        .willThrow(new AccountException(ErrorCode.USER_NOT_FOUND));

    //when
    //then
    mockMvc.perform(post("/transaction/use")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UseTransaction.Request(1L, "2000000000", 3000L)
            ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
        .andExpect(jsonPath("$.errorMessage").value("사용자를 찾을 수 없습니다."))
        .andDo(print());
  }

  @Test
  void whenFailedToUseTransaction_DataIntegrityViolationException() throws Exception {
    //given
    given(transactionService.useTransaction(anyLong(), anyString(), anyLong()))
        .willThrow(new DataIntegrityViolationException(""));

    //when
    //then
    mockMvc.perform(post("/transaction/use")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UseTransaction.Request(1L, "2000000000", 3000L)
            ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("잘못된 요청입니다."))
        .andDo(print());
  }

  @Test
  void whenSucceedToCancelTransaction() throws Exception {
    //given
    given(transactionService.cancelTransaction(anyString(), anyString(), anyLong()))
        .willReturn(TransactionDto.builder()
            .accountNumber("1000000000")
            .transactedAt(LocalDateTime.now())
            .amount(54321L)
            .transactionId("transactionIdForCancel")
            .transactionResultType(TransactionResultType.SUCCEED)
            .build());

    //when
    //then
    mockMvc.perform(post("/transaction/cancel")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CancelTransaction.Request("transactionId",
                    "2000000000", 3000L)
            ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber").value("1000000000"))
        .andExpect(jsonPath("$.transactionResultType").value("SUCCEED"))
        .andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
        .andExpect(jsonPath("$.amount").value(54321))
        .andDo(print());
  }

  @Test
  void whenFailedToCancelTransaction_AccountException() throws Exception {
    //given
    given(transactionService.cancelTransaction(anyString(), anyString(), anyLong()))
        .willThrow(new AccountException(ErrorCode.AMOUNT_MISMATCHED));

    //when
    //then
    mockMvc.perform(post("/transaction/cancel")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CancelTransaction.Request("transactionId",
                    "2000000000", 3000L)
            ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("AMOUNT_MISMATCHED"))
        .andExpect(jsonPath("$.errorMessage").value("거래 금액이 일치하지 않습니다."))
        .andDo(print());
  }

  @Test
  void whenFailedToCancelTransaction_DataIntegrityViolationException() throws Exception {
    //given
    given(transactionService.cancelTransaction(anyString(), anyString(), anyLong()))
        .willThrow(new DataIntegrityViolationException(""));

    //when
    //then
    mockMvc.perform(post("/transaction/cancel")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CancelTransaction.Request("transactionId",
                    "2000000000", 3000L)
            ))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("잘못된 요청입니다."))
        .andDo(print());
  }

  @Test
  void whenSucceedToInquiryTransaction() throws Exception {
    //given
    given(transactionService.inquiryTransaction(anyString()))
        .willReturn(TransactionDto.builder()
            .accountNumber("1000000000")
            .transactionType(USE)
            .transactedAt(LocalDateTime.now())
            .amount(54321L)
            .transactionId("transactionIdForCancel")
            .transactionResultType(TransactionResultType.SUCCEED)
            .build());

    //when
    //then
    mockMvc.perform(get("/transaction/12345"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accountNumber").value("1000000000"))
        .andExpect(jsonPath("$.transactionType").value("USE"))
        .andExpect(jsonPath("$.transactionResultType").value("SUCCEED"))
        .andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
        .andExpect(jsonPath("$.amount").value(54321))
        .andDo(print());
  }

  @Test
  void whenFailedToInquiryTransaction_AccountException() throws Exception {
    //given
    given(transactionService.inquiryTransaction(anyString()))
        .willThrow(new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));

    //when
    //then
    mockMvc.perform(get("/transaction/12345"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("TRANSACTION_NOT_FOUND"))
        .andExpect(jsonPath("$.errorMessage").value("거래를 찾을 수 없습니다."))
        .andDo(print());
  }

  @Test
  void whenFailedToInquiryTransaction_DataIntegrityViolationException() throws Exception {
    //given
    given(transactionService.inquiryTransaction(anyString()))
        .willThrow(new DataIntegrityViolationException(""));

    //when
    //then
    mockMvc.perform(get("/transaction/12345"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("잘못된 요청입니다."))
        .andDo(print());
  }
}