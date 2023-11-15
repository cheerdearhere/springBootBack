package com.backkeesun.inflearnrestapi.mapper;

import com.backkeesun.inflearnrestapi.account.Account;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountMapper {
    public List<Account> findUserList() throws Exception;
}
