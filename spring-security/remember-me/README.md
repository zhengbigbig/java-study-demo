# Spring Security RememberMe

说明： 紧接上文代码，后面不再赘述，包括数据库的初始化迁移都是一样

## 1. 基本配置

### 1.1 ```HttpSecurity```中配置

```java
    http.rememberMe();
```
若未配置全局的```UserDetailsServer```userDetailsService，需要指定配置或者在重写```configure```里指定

```java
    http.rememberMe()
        .userDetailsService(customUserDetailsService);
```

### 1.2 前端页面配置

```html
<label><input type="checkbox" name="remember-me" id="remember-me"/>记住密码</label>
```
重点是:```name="remember-me"```

配置完成后，基本功能就可以使用了，登录时勾选```记住我```，
检查浏览器，会发现Cookies相比一般登录，除了有```JSESSIONID```，
还有一个```remember-me```的```Token```

### 1.3 ```RememberMeToken```的组成部分

将刚刚的```token```进行Base64解密会得到
```admin:1584874217599:d1fff90ec4a0377578bc64b4163e61ac```

- RememberMeToken = username,expirationTime,signatureValue的Base64加密
- signatureValue = username,expirationTime & password和一个预定义的key，并将它们进行MD5签名

具体工具类```TokenBasedRememberMeServices```，部分源码
```java
	protected String makeTokenSignature(long tokenExpiryTime, String username,
			String password) {
		String data = username + ":" + tokenExpiryTime + ":" + password + ":" + getKey();
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("No MD5 algorithm available!");
		}

		return new String(Hex.encode(digest.digest(data.getBytes())));
	}
```

第一次使用了md5不可逆加密，然后调用其父类的时候，存入Cookie时，又进行了Base64加密

### 1.4 实现原理

![rememberMe](/image/rememberme.png)

- 勾选了remember-me，登录成功之后服务端会生成一个Cookie返回给浏览器，这个Cookie的名字默认是remember-me，当然也可以自定义，只是一个token令牌。
- 当我们在有效期内再次访问应用时，经过RememberMeAuthenticationFilter，读取Cookie中的token
- 生成```RememberMeAuthenticationToken```进行验证，验证的provider为```RememberMeAuthenticationProvider```
- 验正通过后可以直接访问应用资源

### 1.5 其他自定义配置项

```java
        http
                .rememberMe()
                .userDetailsService(customUserDetailsService)
                .rememberMeParameter("remember-me") // 接受客户端的参数
                .rememberMeCookieName("remember-me-cookie") // 返回给浏览器的cookie名称
                .tokenValiditySeconds(2 * 24 * 60 * 60) // remember-me token有效时间
```

## 2 token数据库存储方式

### 2.1 上文功能存在的问题

- 上文简单地实现了"记住我-自动登录"的功能，但这种方式存在一个缺点
- token与用户的对应关系是保存在内存中，若重启应用，所有的token将丢失，用户就必须重新登录了
- `Spring Security提供了将token存储到数据库的默认实现

### 2.2 将token保存到数据库

#### 2.2.1 新建数据库表
```sql
CREATE TABLE `persistent_logins`
(
    `username`  varchar(64)  NOT NULL,
    `series`    varchar(64) NOT NULL,
    `token`     varchar(64) NOT NULL,
    `last_used` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`series`)
);
```

#### 2.2.2 配置```SecurityConfiguration```

- 配置```HttpSecurity``
```java
        http
                .rememberMe()
                .userDetailsService(customUserDetailsService)
                .tokenRepository(persistentTokenRepository())
```

- 配置响应的Bean

```java
    @Resource
    private DataSource dataSource; // 对应yml中的数据库配置

    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        // jdbc操作token仓库的默认实现
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }
```

#### 2.2.3 测试

- 当勾选记住我登录后
- 数据库将存有token信息
- 实现token重启容器依然有效


