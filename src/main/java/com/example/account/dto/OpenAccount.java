package com.example.account.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class OpenAccount {

  /**
   * {
   *   "userId": ,
   *   "initialBalance":
   * }
   */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class Request {

    @NotNull
    @Min(1)
    private Long userId;

    @NotNull
    @Min(0)
    private Long initialBalance;
  }

  /**
   * {
   *   "userId": ,
   *   "accountNumber": ,
   *   "openedAt":
   * }
   */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {

    private Long userId;
    private String accountNumber;
    private LocalDateTime openedAt;

    public static Response from(AccountDto accountDto) {
      return Response.builder()
          .userId(accountDto.getUserId())
          .accountNumber(accountDto.getAccountNumber())
          .openedAt(accountDto.getOpenedAt())
          .build();
    }
  }
}
