package com.zbb.basicserver.auth;

import com.zbb.basicserver.dao.UserMapper;
import com.zbb.basicserver.entity.SmsCode;
import com.zbb.basicserver.entity.User;
import com.zbb.basicserver.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;

/**
 * Created by zhengzhiheng on 2020/3/10 1:25 下午
 * Description:
 */

@Service
public class SmsValidateFilter extends OncePerRequestFilter {

    @Resource
    UserMapper userMapper;

    @Resource
    CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI().equals("/smslogin")
                && request.getMethod().equalsIgnoreCase("post")) {
            try {
                validate(new ServletWebRequest(request));

            } catch (AuthenticationException e) {
                customAuthenticationFailureHandler.onAuthenticationFailure(
                        request, response, e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void validate(ServletWebRequest request) throws ServletRequestBindingException {
        HttpSession session = request.getRequest().getSession();
        SmsCode codeInSession = (SmsCode) session.getAttribute(Constants.SMS_SESSION_KEY);
        String codeInRequest = request.getParameter(Constants.SMS_REQUEST_KEY);
        String mobileInRequest = request.getParameter(Constants.SMS_REQUEST_PHONE_NAME);


        if (StringUtils.isBlank(mobileInRequest)) {
            throw new SessionAuthenticationException("手机号码不能为空！");
        }
        if (StringUtils.isBlank(codeInRequest)) {
            throw new SessionAuthenticationException("短信验证码不能为空！");
        }
        if (Objects.isNull(codeInSession)) {
            throw new SessionAuthenticationException("短信验证码不存在！");
        }
        if (codeInSession.isExpired()) {
            session.removeAttribute(Constants.SMS_SESSION_KEY);
            throw new SessionAuthenticationException("短信验证码已过期！");
        }
        if (!codeInSession.getCode().equals(codeInRequest)) {
            throw new SessionAuthenticationException("短信验证码不正确！");
        }

        if (!codeInSession.getPhone().equals(mobileInRequest)) {
            throw new SessionAuthenticationException("短信发送目标与该手机号不一致！");
        }

        User user = userMapper.findUserByUsername(mobileInRequest);
        if (Objects.isNull(user)) {
            throw new SessionAuthenticationException("您输入的手机号不是系统的注册用户");
        }

        session.removeAttribute(Constants.SMS_SESSION_KEY);
    }
}