package com.zbb.basicserver.auth.oauth2;

import com.zbb.basicserver.service.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.social.security.SocialUserDetails;
import org.springframework.social.security.SocialUserDetailsService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MyUserDetailsService implements SocialUserDetailsService {

    @Resource
    private CustomUserDetailsService customUserDetailsService;


    @Override
    public SocialUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {
        MyUserDetails user = (MyUserDetails) customUserDetailsService.loadUserByUsername(userId);

        return user;
    }

}