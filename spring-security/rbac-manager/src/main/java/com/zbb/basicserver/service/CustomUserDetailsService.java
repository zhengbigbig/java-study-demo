package com.zbb.basicserver.service;

import com.zbb.basicserver.dao.UserMapper;
import com.zbb.basicserver.entity.Permission;
import com.zbb.basicserver.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by zhengzhiheng on 2020/3/7 5:41 下午
 * Description:
 */
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + "不存在！");
        }
        List<Permission> permissions = Optional.ofNullable(userMapper.getPermissionsByUsername(username)).orElse(new ArrayList<>());
        // 将权限信息添加到SimpleGrantedAuthority中，之后进行全权限验证会使用该SimpleGrantedAuthority
        List<String> authorities = permissions.stream()
                .map(permission -> "ROLE" + permission.getName().toUpperCase())
                .collect(Collectors.toList());

        // 将,分割的生成List<GrantedAuthority>
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                String.join(",", authorities)
        );
        user.setAuthorities(grantedAuthorities); //用于登录时 @AuthenticationPrincipal 标签取值
        return user;
    }
}
