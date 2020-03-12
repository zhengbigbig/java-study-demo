package com.zbb.basicserver.auth.oauth2;

import lombok.extern.java.Log;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Created by zhengzhiheng on 2020/3/12 10:47 上午
 * Description:
 */

@Log
public class QQOauth2Template extends OAuth2Template {

    public QQOauth2Template(String clientId, String clientSecret, String authorizeUrl, String accessTokenUrl) {
        super(clientId, clientSecret, authorizeUrl, accessTokenUrl);
        // 设置带上 client_id、client_secret
        setUseParametersForClientAuthentication(true);
    }

    /**
     * 解析QQ返回的令牌
     * 返回格式：access_token=FE04********CCE2&expires_in=7776000&refresh_token=88E4***********BE14
     *
     * @param accessTokenUrl 获取AccessToken的URL
     * @param parameters     Map参数，标准参数，但QQ返回是字符串形式，因此需要改造
     * @return AccessGrant 同意访问，accessToken，scope，refreshToken，expireTime
     */
    @Override
    protected AccessGrant postForAccessGrant(String accessTokenUrl, MultiValueMap<String, String> parameters) {
        String responseStr = getRestTemplate().postForObject(accessTokenUrl, parameters, String.class);

        log.info("获取accessToken: " + responseStr);

        String[] strings = StringUtils.splitByWholeSeparatorPreserveAllTokens(responseStr, "&");

        String accessToken = StringUtils.substringAfterLast(strings[0], "=");
        Long expiresIn = new Long(StringUtils.substringAfterLast(strings[1], "="));
        String refreshToken = StringUtils.substringAfterLast(strings[2], "=");

        return new AccessGrant(accessToken, null, refreshToken, expiresIn);

    }

    /**
     * QQ 响应 ContentType=text/html;因此需要加入 text/html; 的处理器
     * @return
     */

    @Override
    protected RestTemplate createRestTemplate() {
        RestTemplate restTemplate = super.createRestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charsets.UTF_8));
        return restTemplate;
    }
}
