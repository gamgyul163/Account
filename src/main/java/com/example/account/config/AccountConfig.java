package com.example.account.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AccountConfig {
  @Value("${spring.account.limit}")
  private int accountLimitPerUser; // 한 사람당 계좌 보유 한도
}
