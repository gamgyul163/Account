package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.type.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  Integer countByAccountUserAndAccountStatus(AccountUser accountUser, AccountStatus accountStatus);
  Integer countByAccountNumber(String accountNumber);
  Optional<Account> findByAccountNumber(String accountNumber);
  List<Account> findByAccountUser(AccountUser accountUser);

}
