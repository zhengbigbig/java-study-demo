package com.zbb.basicserver.auth.sms;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 仿造UsernamePasswordAuthenticationFilter开发
 */
public class SmsCodeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public static final String SPRING_SECURITY_FORM_MOBILE_KEY = "phone";
    private String mobileParameter = SPRING_SECURITY_FORM_MOBILE_KEY;    //请求中携带手机号的参数名称
    private boolean postOnly = true;    //指定当前过滤器是否只处理POST请求

    public SmsCodeAuthenticationFilter() {
        //指定当前过滤器处理的请求
        super(new AntPathRequestMatcher("/smslogin", "POST"));
    }

    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response)
            throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String mobile = this.obtainMobile(request);
            if (mobile == null) {
                mobile = "";
            }
            mobile = mobile.trim();
            SmsCodeAuthenticationToken authRequest = new SmsCodeAuthenticationToken(mobile);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }


    protected String obtainMobile(HttpServletRequest request) {
        return request.getParameter(this.mobileParameter);
    }


    protected void setDetails(HttpServletRequest request, SmsCodeAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }

    public void setMobileParameter(String mobileParameter) {
        Assert.hasText(mobileParameter, "Username parameter must not be empty or null");
        this.mobileParameter = mobileParameter;
    }


    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

    public final String getMobileParameter() {
        return this.mobileParameter;
    }

}