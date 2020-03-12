package com.zbb.basicserver.auth.oauth2;

import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;

/**
 * Created by zhengzhiheng on 2020/3/12 10:43 下午
 * Description:
 */
public class QQApiAdapter implements ApiAdapter<QQApi> {

    //测试Api连接是否可用
    @Override
    public boolean test(QQApi api) {
        return true;
    }

    //QQApi 与 Connection 做适配（核心）
    @Override
    public void setConnectionValues(QQApi api, ConnectionValues values) {
        QQUser user = api.getUserInfo();

        values.setDisplayName(user.getNickname());
        values.setImageUrl(user.getFigureurl());
        values.setProviderUserId(user.getOpenId());
    }


    @Override
    public UserProfile fetchUserProfile(QQApi api) {
        return null;
    }

    @Override
    public void updateStatus(QQApi api, String message) {

    }
}
