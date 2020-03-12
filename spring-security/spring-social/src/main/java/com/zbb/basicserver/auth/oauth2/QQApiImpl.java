package com.zbb.basicserver.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.oauth2.TokenStrategy;

/**
 * Created by zhengzhiheng on 2020/3/12 1:45 下午
 * Description:
 */

/**
 * AbstractOAuth2ApiBinding封装了accessToken以及RestTemplate，
 * 帮助我们实现HTTP请求的参数携带，以及请求结果到对象的反序列化工作等
 * RestTemplate用于帮助我们实现HTTP请求与响应的处理操作
 */

@Log
public class QQApiImpl extends AbstractOAuth2ApiBinding implements QQApi {

    private static final String URL_GET_OPENID = "https://graph.qq.com/oauth2.0/me?access_token=%s";

    private static final String URL_GET_USERINFO = "https://graph.qq.com/user/get_user_info?oauth_consumer_key=%s&openid=%s";

    private String appId;

    private String openId;

    private ObjectMapper objectMapper = new ObjectMapper();

    public QQApiImpl(String accessToken, String appId) {
        // AbstractOAuth2ApiBinding默认是使用header传递accessToken，而QQ比较特殊是用parameter传递token
        super(accessToken, TokenStrategy.ACCESS_TOKEN_PARAMETER);
        this.appId = appId;
        this.openId = getOpen(accessToken);
        log.info("QQ互联平台openId: " + this.openId);
    }

    // 通过接口通过access_token获取返回值并提取openId
    private String getOpen(String accessToken) {
        String url = String.format(URL_GET_OPENID, accessToken);
        String result = getRestTemplate().getForObject(url, String.class);
        return StringUtils.substringBetween(result, "\"openid\":\"", "\"}");
    }

    // 通过接口获取用户信息
    @Override
    public QQUser getUserInfo() {
        try {
            String url = String.format(URL_GET_USERINFO, appId, openId);
            String result = getRestTemplate().getForObject(url, String.class);
            // ObjectMapper 是jackson的类，此处用于将JSON字符串转换为QQUser对象
            QQUser userInfo = objectMapper.readValue(result, QQUser.class);
            userInfo.setOpenId(openId);
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("获取用户信息失败", e);
        }
    }
}
