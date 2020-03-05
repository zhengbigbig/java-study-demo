package com.icoding.interceptor;

import com.icoding.utils.HttpUtil;
import com.icoding.utils.SSOClientUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

// 自己编写的 CAS 客户端！
public class SsoClientInterceptor implements HandlerInterceptor {
    // false 拦截    true 通行

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //1. 判断是否存在会话 isLogin = true
        HttpSession session =  request.getSession();
        Boolean isLogin = (Boolean) session.getAttribute("isLogin");

        if (isLogin!=null && isLogin){ //存在会话
            return true;
        }

        // 2. 判断token
        String token = request.getParameter("token");
        if (!StringUtils.isEmpty(token)){ //判断token 不为空
            System.out.println("检测到token信息，需要拿到服务器去校验token"+token);
            // 发起一个请求，携带一个参数！获得一个结果
            String httpUrl = SSOClientUtil.SERVER_URL_PREFIX + "/verify";
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("token",token);
            params.put("clientUrl",SSOClientUtil.getClientLogOutUrl());
            params.put("jsessionid",session.getId());
            try {
                String isVerify = HttpUtil.sendHttpRequest(httpUrl, params);
                if ("true".equals(isVerify)){
                    System.out.println("服务器端校验token信息通过");
                    session.setAttribute("isLogin",true);
                    return true;
                }
            }catch (Exception e){
                System.out.println("校验HTTP通信异常");
                e.printStackTrace();
            }

        }


        //没有登录信息，就需要跳转到登录的服务器！ www.sso.com:8080
        // http://www.sso.com:8080 /checkLogin?redirectUrl=http://www.tb.com:8081
        SSOClientUtil.redirectToSSOURL(request,response);
        return false;
    }

}
