package com.zbb.basicserver.auth.kaptcha;

import com.zbb.basicserver.auth.CustomAuthenticationFailureHandler;
import com.zbb.basicserver.entity.KaptchaImageVO;
import com.zbb.basicserver.utils.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
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
 * Created by zhengzhiheng on 2020/3/9 9:47 下午
 * Description:
 */
@Service
public class KaptchaFilter extends OncePerRequestFilter {

    @Resource
    CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 只对登录的请求进行校验
        if (StringUtils.equals("/login", request.getRequestURI())
                && StringUtils.equalsIgnoreCase(request.getMethod(), "post")) {

            try {
                // 校验验证码是否通过
                validateCode(new ServletWebRequest(request));

            } catch (AuthenticationException e) {
                // 校验失败由鉴权失败的handler处理
                customAuthenticationFailureHandler.onAuthenticationFailure(request, response, e);
                return;
            }
        }

        // 继续执行过滤链
        filterChain.doFilter(request, response);
    }

    private void validateCode(ServletWebRequest request) throws ServletRequestBindingException {
        HttpSession session = request.getRequest().getSession();

        // 1. 获取请求的验证码
        String codeInRequest = ServletRequestUtils.getStringParameter(request.getRequest(), Constants.KAPTCHA_REQUEST_KEY);

        // 2. 检验空值情况
        if (StringUtils.isBlank(codeInRequest)) {
            throw new SessionAuthenticationException("验证码不能为空");
        }

        // 3. 获取服务器session池中的验证码
        KaptchaImageVO kaptchaInSession = (KaptchaImageVO) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (Objects.isNull(kaptchaInSession)) {
            throw new SessionAuthenticationException("验证码不存在");
        }

        // 4. 校验验证码是否过期
        if (kaptchaInSession.isExpired()) {
            session.removeAttribute(Constants.KAPTCHA_SESSION_KEY);
            throw new SessionAuthenticationException("验证码已过期");
        }

        // 5. 校验验证码是否匹配
        if (!StringUtils.equals(codeInRequest, kaptchaInSession.getCode())) {
            throw new SessionAuthenticationException("验证码不匹配");
        }

        // 6. 移除已完成校验的验证码
        session.removeAttribute(Constants.KAPTCHA_SESSION_KEY);
    }


}
