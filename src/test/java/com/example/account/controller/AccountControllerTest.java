package com.example.account.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.account.dto.AccountDto;
import com.example.account.dto.CloseAccount;
import com.example.account.dto.OpenAccount;
import com.example.account.exception.AccountException;
import com.example.account.service.AccountService;
import com.example.account.type.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

  @MockBean
  private AccountService accountService;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void whenSucceedToOpenAccount() throws Exception {
    //given
    given(accountService.openAccount(anyLong(), anyLong()))
        .willReturn(AccountDto.builder()
            .userId(1L)
            .accountNumber("1234567890")
            .openedAt(LocalDateTime.now())
            .closedAt(LocalDateTime.now())
            .build());
    //when
    //then
    mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new OpenAccount.Request(3333L, 1111L)
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.accountNumber").value("1234567890"))
        .andDo(print());
  }

  @Test
  void whenFailedToOpenAccount_AccountException() throws Exception {
    //given
    given(accountService.openAccount(anyLong(), anyLong()))
        .willThrow(new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));
    //when
    //then
    mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new OpenAccount.Request(3333L, 1111L)
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"))
        .andExpect(jsonPath("$.errorMessage").value("계좌를 찾을 수 없습니다."))
        .andDo(print());
  }

  @Test
  void whenFailedToOpenAccount_DataIntegrityViolationException() throws Exception {
    //given
    given(accountService.openAccount(anyLong(), anyLong()))
        .willThrow(new DataIntegrityViolationException(""));
    //when
    //then
    mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new OpenAccount.Request(3333L, 1111L)
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("잘못된 요청입니다."))
        .andDo(print());
  }

  @Test
  void whenSucceedToCloseAccount() throws Exception {
    //given
    given(accountService.closeAccount(anyLong(), anyString()))
        .willReturn(AccountDto.builder()
            .userId(1L)
            .accountNumber("1234567890")
            .openedAt(LocalDateTime.now())
            .closedAt(LocalDateTime.now())
            .build());
    //when
    //then
    mockMvc.perform(delete("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CloseAccount.Request(3333L, "1111111111")
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.accountNumber").value("1234567890"))
        .andDo(print());
  }

  @Test
  void whenFailedToCloseAccount_AccountException() throws Exception {
    //given
    given(accountService.closeAccount(anyLong(), anyString()))
        .willThrow(new AccountException(ErrorCode.ACCOUNT_CLOSED));
    //when
    //then
    mockMvc.perform(delete("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CloseAccount.Request(3333L, "1111111111")
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("ACCOUNT_CLOSED"))
        .andExpect(jsonPath("$.errorMessage").value("해지된 계좌입니다."))
        .andDo(print());
  }

  @Test
  void whenFailedToCloseAccount_DataIntegrityViolationException() throws Exception {
    //given
    given(accountService.closeAccount(anyLong(), anyString()))
        .willThrow(new DataIntegrityViolationException(""));
    //when
    //then
    mockMvc.perform(delete("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CloseAccount.Request(3333L, "1111111111")
            )))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("잘못된 요청입니다."))
        .andDo(print());
  }

  @Test
  void whenSucceedTogetAccounts() throws Exception {
    //given
    List<AccountDto> accountDtos =
        Arrays.asList(
            AccountDto.builder()
                .accountNumber("1234567890")
                .balance(1000L).build(),
            AccountDto.builder()
                .accountNumber("1111111111")
                .balance(2000L).build(),
            AccountDto.builder()
                .accountNumber("2222222222")
                .balance(3000L).build()
        );
    given(accountService.getAccountsByUserId(anyLong()))
        .willReturn(accountDtos);

    //when
    //then
    mockMvc.perform(get("/account?user_id=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
        .andExpect(jsonPath("$[0].balance").value(1000))
        .andExpect(jsonPath("$[1].accountNumber").value("1111111111"))
        .andExpect(jsonPath("$[1].balance").value(2000))
        .andExpect(jsonPath("$[2].accountNumber").value("2222222222"))
        .andExpect(jsonPath("$[2].balance").value(3000))
        .andDo(print());
  }

  @Test
  void whenFailedTogetAccounts_AccountException() throws Exception {
    //given
    given(accountService.getAccountsByUserId(anyLong()))
        .willThrow(new AccountException(ErrorCode.USER_NOT_FOUND));
    //when
    //then
    mockMvc.perform(get("/account?user_id=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"))
        .andExpect(jsonPath("$.errorMessage").value("사용자를 찾을 수 없습니다."))
        .andDo(print());
  }

  @Test
  void whenFailedTogetAccounts_DataIntegrityViolationException() throws Exception {
    //given
    given(accountService.getAccountsByUserId(anyLong()))
        .willThrow(new DataIntegrityViolationException(""));
    //when
    //then
    mockMvc.perform(get("/account?user_id=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
        .andExpect(jsonPath("$.errorMessage").value("잘못된 요청입니다."))
        .andDo(print());
  }

}