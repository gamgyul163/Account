package com.example.account.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TransactionConfig {
  @Value("${spring.transaction.amountMaxValue}") // 최대 거래액
  private Integer amountMaxValue;

  @Value("${spring.transaction.amountMinValue}") // 최소 거래액
  private Integer amountMinValue;
}
