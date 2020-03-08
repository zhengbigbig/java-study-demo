package com.zbb.basicserver.service;

import com.zbb.basicserver.dao.UserMapper;
import com.zbb.basicserver.entity.Permission;
import com.zbb.basicserver.entity.Role;
import com.zbb.basicserver.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhengzhiheng on 2020/3/7 5:41 下午
 * Description:
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 加载用户基本信息
        User user = userMapper.findUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + "不存在！");
        }
        // 加载用户角色信息
        List<Role> roleList = userMapper.getRolesByUsername(username);

        // 通过角色信息加载用户资源权限列表
        List<Permission> permissions = userMapper.getPermissionsByRoles(
                roleList.stream().map(Role::getId).collect(Collectors.toList())
        );
        List<String> resources = permissions.stream().map(Permission::getName).collect(Collectors.toList());

        // 将用户角色加前缀
        List<String> authorities = roleList.stream()
                .map(permission -> "ROLE_" + permission.getRole().toUpperCase())
                .collect(Collectors.toList());

        authorities.addAll(resources);
        // 将,分割的生成List<GrantedAuthority>
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(
                String.join(",", authorities)
        );
        user.setAuthorities(grantedAuthorities); //用于登录时 @AuthenticationPrincipal 标签取值
        return user;
    }
}
