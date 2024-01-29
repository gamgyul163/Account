package com.example.account.dto;

import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CloseAccount {

  /**
   * {
   *   "userId": ,
   *   "accountNumber":
   * }
   */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class Request {

    @NotNull
    @Min(1)
    private Long userId;

    @NotBlank
    @Size(min = 10, max = 10)
    private String accountNumber;
  }

  /**
   * {
   *   "userId": ,
   *   "accountNumber": ,
   *   "closedAt":
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
    private LocalDateTime closedAt;

    public static Response from(AccountDto accountDto) {
      return Response.builder()
          .userId(accountDto.getUserId())
          .accountNumber(accountDto.getAccountNumber())
          .closedAt(accountDto.getClosedAt())
          .build();
    }
  }
}
