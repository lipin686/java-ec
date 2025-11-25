package com.example.demo.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定義 UserDetails 實現
 * 擴展標準的 UserDetails，增加 userId 欄位
 */
@Getter
public class CustomUserDetails extends User {

    private final Long userId;
    private final String email;

    public CustomUserDetails(
            Long userId,
            String email,
            String password,
            Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.userId = userId;
        this.email = email;
    }

    public CustomUserDetails(
            Long userId,
            String email,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities) {
        super(email, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
        this.email = email;
    }
}

