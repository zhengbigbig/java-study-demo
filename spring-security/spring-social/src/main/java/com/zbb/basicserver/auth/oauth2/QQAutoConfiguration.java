package com.zbb.basicserver.auth.oauth2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SpringSocialConfigurer;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
@EnableSocial
public class QQAutoConfiguration extends SocialConfigurerAdapter {

    @Resource
    private DataSource dataSource;

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        // UsersConnectionRepository是用于操作数据库UserConnection表的持久层封装
        JdbcUsersConnectionRepository usersConnectionRepository =
                new JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, Encryptors.noOpText());
        // 设置表前缀
        usersConnectionRepository.setTablePrefix("sys_");
        return usersConnectionRepository;
    }

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer,
                                       Environment environment) {
        // 向Spring Social添加一个ConnectionFactory
        connectionFactoryConfigurer.addConnectionFactory(
                new QQConnectionFactory("qq",  //这里配置什么取决于你的回调地址
                        "你申请的APP ID","你申请的APP KEY")); //这里可以优化为application配置
    }


    @Override
    public UserIdSource getUserIdSource() {
        return new AuthenticationNameUserIdSource();
    }

    @Bean
    public SpringSocialConfigurer qqFilterConfig() {
        // filterProcessesUrl是用于拦截用户QQ登录请求和认证服务器回调请求的路径
        QQFilterConfigurer configurer = new QQFilterConfigurer("/login");
        configurer.signupUrl("/bind.html");
        configurer.postLoginUrl("/index");
        return configurer;
    }

}