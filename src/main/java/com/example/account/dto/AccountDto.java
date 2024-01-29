package com.example.account.dto;

import com.example.account.domain.Account;
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
public class AccountDto {

  private Long userId;
  private String accountNumber;
  private Long balance;
  private LocalDateTime openedAt;
  private LocalDateTime closedAt;

  public static AccountDto fromEntity(Account account) {
    return AccountDto.builder()
        .userId(account.getAccountUser().getId())
        .balance(account.getBalance())
        .accountNumber(account.getAccountNumber())
        .openedAt(account.getOpenedAt())
        .closedAt(account.getClosedAt())
        .build();
  }

}
