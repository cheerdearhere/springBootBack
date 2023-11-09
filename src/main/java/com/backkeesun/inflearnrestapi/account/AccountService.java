package com.backkeesun.inflearnrestapi.account;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException(username));
        return new User(account.getEmail(),account.getPassword(),setAuthorities(account.getRoles()));
    }

    private Collection<? extends GrantedAuthority> setAuthorities(Set<AccountRole> roles) {
        return roles.stream()
                .map(r->new SimpleGrantedAuthority("ROLE_"+r.name()))
                .collect(Collectors.toSet());
    }
}
