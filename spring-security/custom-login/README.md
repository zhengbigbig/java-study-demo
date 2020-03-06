# 自定义登录认证

## 1. 自定义处理
原因：前后端分离项目，需要自定义返回结果
### 1.1 自定义认证成功的处理
#### 1.1.1 上一个demo说到```UsernamePasswordAuthenticationFilter```，这次先说下```FilterChainProxy```对其父类```doFilter```的调用细节
先看其关键源码，分析其主要相关联作用：
```java
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
        //......
        /** 其子类通过调用构造器传入的login_url和login_method 也就是默认的 /login POST
            未匹配到就执行下一个过滤器
        **/
		if (!requiresAuthentication(request, response)) { chain.doFilter(request, response); return; }
        // TODO something
		Authentication authResult;
		try {
            // TODO 鉴权，返回的期待是Authentication
			authResult = attemptAuthentication(request, response);
			if (authResult == null) {
				// return immediately as subclass has indicated that it hasn't completed
				// authentication
				return;
			}
            // TODO 保存结果到session
			sessionStrategy.onAuthentication(authResult, request, response);
		}
	    // 若捕获异常，即鉴权失败，调用unsuccessfulAuthentication，并调用处理成功的handler
		catch (InternalAuthenticationServiceException failed) {
			logger.error(
					"An internal error occurred while trying to authenticate the user.",
					failed);
			unsuccessfulAuthentication(request, response, failed);

			return;
		}
		catch (AuthenticationException failed) {
			// Authentication failed
			unsuccessfulAuthentication(request, response, failed);

			return;
		}

		// Authentication success
		if (continueChainBeforeSuccessfulAuthentication) {
			chain.doFilter(request, response);
		}
        // TODO 鉴权成功，则调用successfulAuthentication，保存结果到上下文，并调用实现了AuthenticationSuccessHandler接口的类来处理
        //  默认SavedRequestAwareAuthenticationSuccessHandler，对跳转到成功后页面啊之类的进行了处理
		successfulAuthentication(request, response, chain, authResult);
	}
```
因此，这里我们需要实现相关的handler，一般成功，我们会使用实现了```AuthenticationSuccessHandler```接口的
```SavedRequestAwareAuthenticationSuccessHandler```类

#### 1.1.2 SavedRequestAwareAuthenticationSuccessHandler的 ```onAuthenticationSuccess```
可看源码，这里不贴了
主要作用就是：
- 若设置了```defaultSuccessUrl```并始终为默认跳转，则通过判断跳转至url
- 若未设置上面url，则拿出上次缓存的请求的url，进行跳转
- 若没有上次缓存，则调用父类```SimpleUrlAuthenticationSuccessHandler```的```onAuthenticationSuccess```的```defaultTargetUrl```，也就是```/```

#### 1.1.3 自定义AuthenticationSuccessHandler只需要重写```onAuthenticationSuccess```
需要注意的是：
```java
    http.csrf().disable()
            .formLogin()
//                .defaultSuccessUrl("/index")
            .successHandler(customAuthenticationSuccessHandler)
```
```defaultSuccessUrl```和```successHandler```只能取其一。

### 1.2 自定义认证失败的处理

#### 1.2.1 源码就不说了，失败处理需要实现```AuthenticationFailureHandler```接口
- 这里继承```SimpleUrlAuthenticationFailureHandler```
- 注意，并不是一定要继承哪一个，你也可以选择别的，或者干脆实现```AuthenticationFailureHandler```接口
AuthenticationException
同样将
```java
//  .failureUrl("/login.html") 俩者只能选其一
    .failureHandler(customAuthenticationFailureHandler)
```
添加至```HttpSecurity```

### ```SavedRequestAwareAuthenticationSuccessHandler```和```SimpleUrlAuthenticationFailureHandler```的区别
- 前者```onAuthenticationSuccess```三个参数分别是```HttpServletRequest request, HttpServletResponse response, Authentication authentication```
- 后者```onAuthenticationFailure```三个参数分别是 ```HttpServletRequest request, HttpServletResponse response, AuthenticationException exception```
- 第三个参数不一样，当然你也可以自定义类继承```AuthenticationException```，并抛出自定义内容
