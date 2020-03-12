package com.zbb.basicserver.auth.oauth2;

import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;
import org.springframework.social.oauth2.OAuth2Operations;

/**
 * Created by zhengzhiheng on 2020/3/12 10:21 下午
 * Description:
 */

public class QQServiceProvider extends AbstractOAuth2ServiceProvider<QQApi> {

    //OAuth2获取授权码的请求地址
    private static final String URL_AUTHORIZE = "https://graph.qq.com/oauth2.0/authorize";

    //OAuth2获取AccessToken的请求地址
    private static final String URL_GET_ACCESS_TOKEN = "https://graph.qq.com/oauth2.0/token";

    private String appId;

    public QQServiceProvider(String appId, String appSecret) {
        super(new QQOauth2Template(appId, appSecret, URL_AUTHORIZE, URL_GET_ACCESS_TOKEN));
        this.appId = appId;
    }

    @Override
    public QQApi getApi(String accessToken) {
        return new QQApiImpl(accessToken, appId);
    }
}
