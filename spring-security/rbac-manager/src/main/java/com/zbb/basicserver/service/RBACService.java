package com.zbb.basicserver.service;

import com.zbb.basicserver.dao.UserMapper;
import com.zbb.basicserver.entity.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by zhengzhiheng on 2020/3/8 4:23 下午
 * Description:
 */
@Service("rbacService")
public class RBACService {
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Resource
    private UserMapper userMapper;

    /**
     * 判断某用户是否具有该request资源的访问权限
     */
    public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            // 从数据库动态加载，为了提高性能，实际可更改为redis spring cache
            List<String> urls = userMapper.getPermissionsByUsername(username);
            return urls.stream().anyMatch(
                    url -> antPathMatcher.match(url, request.getRequestURI())
            );
        }
        return false;
    }
}
