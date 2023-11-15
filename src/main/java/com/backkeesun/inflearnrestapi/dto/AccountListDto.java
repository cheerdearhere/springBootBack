package com.backkeesun.inflearnrestapi.dto;

import com.backkeesun.inflearnrestapi.account.AccountRole;
import lombok.*;

import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = "accountId")
public class AccountListDto {
    private int accountId;
    private String email;
    private Set<AccountRole> roles;
}
