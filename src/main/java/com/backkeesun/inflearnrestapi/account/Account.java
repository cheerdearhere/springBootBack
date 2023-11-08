package com.backkeesun.inflearnrestapi.account;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Getter @Setter @EqualsAndHashCode(of="id")
@AllArgsConstructor @NoArgsConstructor @Builder
@Entity
public class Account {
    @Id @GeneratedValue @Column(name = "account_id")
    private Integer id;
    private String email;
    private String password;
    @ElementCollection(fetch=FetchType.EAGER) // Set, Map 등을 써서 여러 데이터를 쓰는 경우
    @Enumerated(EnumType.STRING)
    private Set<AccountRole> roles;
}
