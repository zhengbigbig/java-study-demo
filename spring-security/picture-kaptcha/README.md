# 多种图片验证码实现方案

## 1. 图片验证码验证

### 1.1 流程

- 浏览器发起请求，获取图片验证码，服务端生成验证码返回前端，前端展示图片验证码，后端正确结果保存在session
- 前端通过用户输入，向服务端发起校验。
- 验证用户输入是否与session内验证码数据一致

### 1.2 验证码在session的储存

- session存储验证码到服务端，，不适用于集群应用
- 应该使用共享session来做集群，可以使用基于对称算法的验证码
- 共享session可以通过redis或数据库等来进行集群

### 1.3 图片验证码开发步骤

- 生成验证码文字或其他用于校验的数据形式
- 生成验证码前端显示图片或拼图等
- 用于校验用户输入与实际结果的校验方法

## 2. 验证码工具配置

### 2.1 引入kaptcha依赖
```xml
        <!-- https://mvnrepository.com/artifact/com.github.penggle/kaptcha -->
        <dependency>
            <groupId>com.github.penggle</groupId>
            <artifactId>kaptcha</artifactId>
            <version>2.3.2</version>
        </dependency>

```

### 2.2 配置properties，非spring环境变量，需手动配置加载
```properties
kaptcha.border=no
kaptcha.border.color=105,179,90
kaptcha.image.width=100
kaptcha.image.height=45
kaptcha.session.key=code
kaptcha.textproducer.font.color=blue
kaptcha.textproducer.font.size=35
kaptcha.textproducer.char.length=4
kaptcha.textproducer.font.names=宋体,楷体,微软雅黑
```

具体配置在```CaptchaConfig```

## 3. 验证码加载