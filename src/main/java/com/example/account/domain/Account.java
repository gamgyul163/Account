package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Account extends BaseEntity {

  @ManyToOne
  private AccountUser accountUser;

  private String accountNumber;
  private Long balance;

  @Enumerated(EnumType.STRING)
  private AccountStatus accountStatus;

  private LocalDateTime openedAt;
  private LocalDateTime closedAt;

  public void subBalance(Long amount) {
    if (amount > balance) {
      throw new AccountException(ErrorCode.BALANCE_NOT_ENOUGH);
    }
    balance -= amount;
  }

  public void addBalance(Long amount) {
    if (amount < 0) {
      throw new AccountException(ErrorCode.BALANCE_NOT_ENOUGH);
    }
    balance += amount;
  }
}
