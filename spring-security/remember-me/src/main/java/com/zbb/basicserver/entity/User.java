package com.zbb.basicserver.entity;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Created by zhengzhiheng on 2020/3/7 4:06 下午
 * Description:
 */

@Data
public class User implements UserDetails {
    private Long id;
    private String username;
    private String encryptedPassword;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    String password;
    boolean accountNonExpired; // 是否没过期
    boolean accountNonLocked; // 是否没被锁定
    boolean credentialsNonExpired; // 是否没过期
    Collection<? extends GrantedAuthority> authorities; // 用户权限集合

    public User(String username, String encryptedPassword) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return encryptedPassword;
    }

    public String getPrePassword(){
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
