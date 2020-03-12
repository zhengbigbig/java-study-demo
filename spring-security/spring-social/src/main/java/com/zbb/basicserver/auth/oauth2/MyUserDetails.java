package com.zbb.basicserver.auth.oauth2;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.social.security.SocialUserDetails;

import java.util.Collection;

@Data
@AllArgsConstructor
public class MyUserDetails implements SocialUserDetails {
    
    String password;  //密码
    String username;  //用户名
    boolean accountNonExpired;   //是否没过期
    boolean accountNonLocked;   //是否没被锁定
    boolean credentialsNonExpired;  //是否没过期
    boolean enabled;  //账号是否可用
    Collection<? extends GrantedAuthority> authorities;  //用户的权限集合

    @Override
    public String getUserId() {
        return username;
    }
}