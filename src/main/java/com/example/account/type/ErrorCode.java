package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
  USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
  REACHED_ACCOUNT_PER_USER_LIMIT("계좌 보유 수 한도에 도달하였습니다."),
  ACCOUNT_NOT_FOUND("계좌를 찾을 수 없습니다."),
  ACCOUNTUSER_MISMATCHED("사용자 정보가 계좌주 정보와 일치하지 않습니다."),
  ACCOUNT_CLOSED("해지된 계좌입니다."),
  BALANCE_NOT_ZERO("잔액이 0이 아닙니다."),
  BALANCE_NOT_ENOUGH("잔액이 부족합니다."),
  AMOUNT_NOT_VALID("거래 금액이 유효하지 않습니다."),
  TRANSACTION_NOT_FOUND("거래를 찾을 수 없습니다."),
  AMOUNT_MISMATCHED("거래 금액이 일치하지 않습니다."),
  ACCOUNT_MISMATCHED("계좌 정보가 일치하지 않습니다."),
  INVALID_REQUEST("잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR("내부 서버 오류가 발생했습니다."),
  ACCOUNT_TRANSACTION_LOCKED("현재 요청 처리중입니다."),
  ;
  private final String description;
}