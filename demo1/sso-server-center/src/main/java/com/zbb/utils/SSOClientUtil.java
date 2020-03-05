package com.icoding.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

public class SSOClientUtil {

    private static Properties ssoProperties = new Properties();
    public static String SERVER_URL_PREFIX; //统一认证中心地址
    public static String CLIENT_HOST_URL; //当前客户端地址

    static {
        try {
            ssoProperties.load(SSOClientUtil.class.getClassLoader().getResourceAsStream("sso.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        SERVER_URL_PREFIX = ssoProperties.getProperty("server-url-prefix");
        CLIENT_HOST_URL = ssoProperties.getProperty("client-host-url");
    }

    /**
     * 当客户端请求被拦截,跳往统一认证中心,需要带redirectUrl的参数,统一认证中心登录后回调的地址
     */
    public static String getRedirectUrl(HttpServletRequest request){
        //获取请求URL
        return CLIENT_HOST_URL+request.getServletPath();
    }
    /**
     * 根据request获取跳转到统一认证中心的地址,通过Response跳转到指定的地址
     */
    public static void redirectToSSOURL(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String redirectUrl = getRedirectUrl(request);
        StringBuilder url = new StringBuilder(50)
                .append(SERVER_URL_PREFIX)
                .append("/checkLogin?redirectUrl=")
                .append(redirectUrl);
        response.sendRedirect(url.toString());
    }


    /**
     * 获取客户端的完整登出地址
     */
    public static String getClientLogOutUrl(){
        return CLIENT_HOST_URL+"/logOut";
    }
    /**
     * 获取认证中心的登出地址
     */
    public static String getServerLogOutUrl(){
        return SERVER_URL_PREFIX+"/logOut";
    }
}
