package com.homes.backend.global.security;

import com.homes.backend.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;

    // 💡 생성자: 우리 User 엔티티를 받아서 시큐리티 규격 상자로 변환합니다.
    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
    }

    public Long getId() {
        return id;
    }

    // 아래 메서드들은 스프링 시큐리티가 내부적으로 검증할 때 쓰는 필수 규칙들
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 현재 일반 유저 권한만 있으므로 빈 리스트나 기본 권한 줌
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; //이메일로 로그인하니까 username 자리에 이메일
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}