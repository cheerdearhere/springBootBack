package com.backkeesun.inflearnrestapi.account;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountAdepter extends User {
    private Account account;
    public AccountAdepter(Account account){
        super(account.getEmail(),account.getPassword(),authorities(account));
        this.account = account;
    }
    public Account getAccount(){
        return account;
    }
    private static Collection<? extends GrantedAuthority> authorities(Account account) {
        return account.getRoles()
                .stream()
                .map(role->new SimpleGrantedAuthority("ROLE_"+role.name()))
                .collect(Collectors.toSet());
    }
}
