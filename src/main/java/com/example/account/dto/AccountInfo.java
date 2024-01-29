package com.example.account.dto;

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
public class AccountInfo {
  private String accountNumber;
  private Long balance;

  public static AccountInfo fromDto(AccountDto accountDto) {
    return AccountInfo.builder()
        .accountNumber(accountDto.getAccountNumber())
        .balance(accountDto.getBalance())
        .build();
  }
}
